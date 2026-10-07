package com.leoyao.microblog.service;

import com.leoyao.microblog.model.Follow;
import com.leoyao.microblog.model.User;
import com.leoyao.microblog.repository.FollowRepository;
import com.leoyao.microblog.repository.PostRepository;
import com.leoyao.microblog.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit tests for follow/unfollow edge cases in {@link UserService}. */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository users;

    @Mock
    private FollowRepository follows;

    @Mock
    private PostRepository posts;

    @InjectMocks
    private UserService userService;

    private static User userWithId(String username, long id) {
        User user = new User(username, "hash", username);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void followSelf_throwsIllegalArgument() {
        when(users.findByUsername("alice")).thenReturn(Optional.of(userWithId("alice", 1L)));

        assertThrows(IllegalArgumentException.class, () -> userService.follow("alice", 1L));
    }

    @Test
    void follow_isIdempotentWhenAlreadyFollowing() {
        when(users.findByUsername("bob")).thenReturn(Optional.of(userWithId("bob", 2L)));
        when(follows.existsByFollowerIdAndFolloweeId(1L, 2L)).thenReturn(true);

        userService.follow("bob", 1L);

        verify(follows, never()).save(any(Follow.class));
    }

    @Test
    void follow_savesWhenNotFollowing() {
        User bob = userWithId("bob", 2L);
        when(users.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(follows.existsByFollowerIdAndFolloweeId(1L, 2L)).thenReturn(false);

        userService.follow("bob", 1L);

        verify(follows).save(any(Follow.class));
    }

    @Test
    void unfollow_removesExistingFollow() {
        User bob = userWithId("bob", 2L);
        User alice = userWithId("alice", 1L);
        Follow follow = new Follow(alice, bob);
        when(users.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(follows.findByFollowerIdAndFolloweeId(1L, 2L)).thenReturn(Optional.of(follow));

        userService.unfollow("bob", 1L);

        verify(follows).delete(follow);
    }

    @Test
    void unfollow_isNoopWhenNotFollowing() {
        when(users.findByUsername("bob")).thenReturn(Optional.of(userWithId("bob", 2L)));
        when(follows.findByFollowerIdAndFolloweeId(1L, 2L)).thenReturn(Optional.empty());

        userService.unfollow("bob", 1L);

        verify(follows, never()).delete(any(Follow.class));
    }
}
