package com.leoyao.microblog.repository;

import com.leoyao.microblog.model.Follow;
import com.leoyao.microblog.model.Post;
import com.leoyao.microblog.model.PostLike;
import com.leoyao.microblog.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests against an in-memory H2 database (auto-configured by @DataJpaTest).
 * Verifies the uniqueness rules and the paginated feed query.
 */
@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private UserRepository users;

    @Autowired
    private PostRepository posts;

    @Autowired
    private PostLikeRepository likes;

    @Autowired
    private FollowRepository follows;

    private static User userWithId(String username, long id) {
        User user = new User(username, "hash", username);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private User saveUser(String username) {
        return users.save(new User(username, "hash", username));
    }

    private Post savePostAt(User author, String content, Instant createdAt) {
        Post post = posts.save(new Post(author, content));
        ReflectionTestUtils.setField(post, "createdAt", createdAt);
        return posts.save(post);
    }

    @Test
    void duplicateLike_violatesUniqueConstraint() {
        User alice = saveUser("alice");
        User bob = saveUser("bob");
        Post post = posts.save(new Post(alice, "hello"));

        likes.saveAndFlush(new PostLike(post, bob));

        assertThrows(DataIntegrityViolationException.class,
                () -> likes.saveAndFlush(new PostLike(post, bob)));
    }

    @Test
    void duplicateFollow_violatesUniqueConstraint() {
        User alice = saveUser("alice");
        User bob = saveUser("bob");

        follows.saveAndFlush(new Follow(alice, bob));

        assertThrows(DataIntegrityViolationException.class,
                () -> follows.saveAndFlush(new Follow(alice, bob)));
    }

    @Test
    void feed_containsOwnAndFollowedPosts_butNotStrangers() {
        User alice = saveUser("alice");
        User bob = saveUser("bob");
        User carol = saveUser("carol");
        Instant now = Instant.now();
        Post alicePost = savePostAt(alice, "alice post", now.minusSeconds(30));
        Post bobPost = savePostAt(bob, "bob post", now.minusSeconds(20));
        savePostAt(carol, "carol post", now.minusSeconds(10)); // not followed: excluded
        follows.save(new Follow(alice, bob));

        Page<Post> feed = posts.findFeed(alice.getId(), PageRequest.of(0, 10));

        List<Long> ids = feed.getContent().stream().map(Post::getId).toList();
        assertTrue(ids.contains(alicePost.getId()), "feed should contain own posts");
        assertTrue(ids.contains(bobPost.getId()), "feed should contain followed users' posts");
        assertEquals(2, feed.getTotalElements(), "feed should exclude strangers' posts");
    }

    @Test
    void feed_isPaginated_newestFirst() {
        User alice = saveUser("alice");
        Instant now = Instant.now();
        savePostAt(alice, "oldest", now.minusSeconds(30));
        savePostAt(alice, "middle", now.minusSeconds(20));
        Post newest = savePostAt(alice, "newest", now.minusSeconds(10));

        Page<Post> firstPage = posts.findFeed(alice.getId(), PageRequest.of(0, 2));

        assertEquals(2, firstPage.getContent().size());
        assertEquals(3, firstPage.getTotalElements());
        assertEquals(newest.getId(), firstPage.getContent().get(0).getId(),
                "feed should be newest first");
        assertTrue(firstPage.hasNext() || firstPage.getTotalPages() == 2);
    }
}
