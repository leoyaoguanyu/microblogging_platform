package com.leoyao.microblog.config;

import com.leoyao.microblog.model.*;
import com.leoyao.microblog.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Seeds a few demo users/posts on first start so the app has something to show. */
@Component
public class DataSeeder implements ApplicationRunner {

    public static final String DEMO_PASSWORD = "password123";

    private final UserRepository users;
    private final PostRepository posts;
    private final CommentRepository comments;
    private final PostLikeRepository likes;
    private final FollowRepository follows;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, PostRepository posts, CommentRepository comments,
                      PostLikeRepository likes, FollowRepository follows, PasswordEncoder encoder) {
        this.users = users;
        this.posts = posts;
        this.comments = comments;
        this.likes = likes;
        this.follows = follows;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.count() > 0) return;

        String hash = encoder.encode(DEMO_PASSWORD);
        User alice = users.save(withBio(new User("alice", hash, "Alice Chen"), "Backend engineer. Java & Spring."));
        User bob   = users.save(withBio(new User("bob",   hash, "Bob Martinez"), "React person. Coffee first."));
        User carol = users.save(withBio(new User("carol", hash, "Carol Okafor"), "Data + SQL. PostgreSQL fan."));
        User dave  = users.save(withBio(new User("dave",  hash, "Dave Kim"), "Lurker."));

        Post p1 = posts.save(new Post(alice, "Hello world! First post on the new microblog. 🚀"));
        Post p2 = posts.save(new Post(bob,   "Hot take: React + Spring Boot is a very productive combo."));
        Post p3 = posts.save(new Post(carol, "Reminder: add an index on (author_id, created_at) before your feed query gets slow."));
        Post p4 = posts.save(new Post(alice, "Spent the morning wiring up JWT auth with Spring Security. Stateless FTW."));
        Post p5 = posts.save(new Post(bob,   "Pagination tip: return hasNext instead of exposing Spring's Page object directly."));
        Post p6 = posts.save(new Post(carol, "Unique constraint on (follower_id, followee_id) = no duplicate follows. Let the DB do the work."));
        Post p7 = posts.save(new Post(dave,  "Just here to read. 👀"));

        // follow graph
        follows.save(new Follow(alice, bob));
        follows.save(new Follow(alice, carol));
        follows.save(new Follow(bob, alice));
        follows.save(new Follow(carol, alice));
        follows.save(new Follow(carol, bob));
        follows.save(new Follow(dave, alice));

        // likes
        likes.save(new PostLike(p1, bob));
        likes.save(new PostLike(p1, carol));
        likes.save(new PostLike(p2, alice));
        likes.save(new PostLike(p3, alice));
        likes.save(new PostLike(p3, bob));
        likes.save(new PostLike(p4, carol));

        // comments
        comments.save(new Comment(p1, bob,   "Welcome aboard!"));
        comments.save(new Comment(p1, carol, "🎉"));
        comments.save(new Comment(p3, alice, "Good call, adding it now."));
        comments.save(new Comment(p5, carol, "Agreed, keeps the API contract stable."));
    }

    private static User withBio(User u, String bio) {
        u.setBio(bio);
        return u;
    }
}
