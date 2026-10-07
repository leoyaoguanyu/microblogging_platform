package com.leoyao.microblog.security;

import com.leoyao.microblog.model.Comment;
import com.leoyao.microblog.model.Post;
import com.leoyao.microblog.model.User;
import com.leoyao.microblog.repository.CommentRepository;
import com.leoyao.microblog.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/** Unit tests for the resource-level authorization rules in {@link PostSecurity}. */
@ExtendWith(MockitoExtension.class)
class PostSecurityTest {

    @Mock
    private PostRepository posts;

    @Mock
    private CommentRepository comments;

    @InjectMocks
    private PostSecurity postSecurity;

    private static User userWithId(String username, long id) {
        User user = new User(username, "hash", username);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static Post postWithId(User author, long id) {
        Post post = new Post(author, "content");
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private static AppUserPrincipal principal(long userId, String username) {
        return new AppUserPrincipal(userWithId(username, userId));
    }

    @Test
    void isAuthor_returnsTrueForAuthor() {
        User alice = userWithId("alice", 1L);
        when(posts.findWithAuthorById(7L)).thenReturn(Optional.of(postWithId(alice, 7L)));

        assertTrue(postSecurity.isAuthor(7L, principal(1L, "alice")));
    }

    @Test
    void isAuthor_returnsFalseForStranger() {
        User alice = userWithId("alice", 1L);
        when(posts.findWithAuthorById(7L)).thenReturn(Optional.of(postWithId(alice, 7L)));

        assertFalse(postSecurity.isAuthor(7L, principal(2L, "bob")));
    }

    @Test
    void canDeleteComment_returnsTrueForCommentAuthor() {
        User alice = userWithId("alice", 1L);
        User bob = userWithId("bob", 2L);
        Post post = postWithId(alice, 7L);
        Comment comment = new Comment(post, bob, "nice post");
        ReflectionTestUtils.setField(comment, "id", 3L);
        when(comments.findWithRelationsById(3L)).thenReturn(Optional.of(comment));

        assertTrue(postSecurity.canDeleteComment(7L, 3L, principal(2L, "bob")));
    }

    @Test
    void canDeleteComment_returnsTrueForPostAuthor() {
        User alice = userWithId("alice", 1L);
        User bob = userWithId("bob", 2L);
        Post post = postWithId(alice, 7L);
        Comment comment = new Comment(post, bob, "nice post");
        ReflectionTestUtils.setField(comment, "id", 3L);
        when(comments.findWithRelationsById(3L)).thenReturn(Optional.of(comment));

        assertTrue(postSecurity.canDeleteComment(7L, 3L, principal(1L, "alice")));
    }

    @Test
    void canDeleteComment_returnsFalseForStranger() {
        User alice = userWithId("alice", 1L);
        User bob = userWithId("bob", 2L);
        Post post = postWithId(alice, 7L);
        Comment comment = new Comment(post, bob, "nice post");
        ReflectionTestUtils.setField(comment, "id", 3L);
        when(comments.findWithRelationsById(3L)).thenReturn(Optional.of(comment));

        assertFalse(postSecurity.canDeleteComment(7L, 3L, principal(9L, "carol")));
    }
}
