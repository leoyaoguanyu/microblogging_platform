package com.leoyao.microblog.service;

import com.leoyao.microblog.dto.UserDtos.UserResponse;
import com.leoyao.microblog.exception.NotFoundException;
import com.leoyao.microblog.model.Follow;
import com.leoyao.microblog.model.User;
import com.leoyao.microblog.repository.FollowRepository;
import com.leoyao.microblog.repository.PostRepository;
import com.leoyao.microblog.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository users;
    private final FollowRepository follows;
    private final PostRepository posts;

    public UserService(UserRepository users, FollowRepository follows, PostRepository posts) {
        this.users = users;
        this.follows = follows;
        this.posts = posts;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(String username, Long viewerId) {
        return toResponse(findByUsername(username), viewerId);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(users.findById(id).orElseThrow(() -> new NotFoundException("User not found")), id);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers(Long viewerId) {
        return users.findAll(Sort.by("username")).stream().map(u -> toResponse(u, viewerId)).toList();
    }

    @Transactional
    public UserResponse follow(String username, Long meId) {
        User target = findByUsername(username);
        if (target.getId().equals(meId)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }
        if (!follows.existsByFollowerIdAndFolloweeId(meId, target.getId())) {
            follows.save(new Follow(users.getReferenceById(meId), target));
        }
        return toResponse(target, meId);
    }

    @Transactional
    public UserResponse unfollow(String username, Long meId) {
        User target = findByUsername(username);
        follows.findByFollowerIdAndFolloweeId(meId, target.getId()).ifPresent(follows::delete);
        return toResponse(target, meId);
    }

    public User findByUsername(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

    public UserResponse toResponse(User u, Long viewerId) {
        boolean followedByMe = viewerId != null
                && !viewerId.equals(u.getId())
                && follows.existsByFollowerIdAndFolloweeId(viewerId, u.getId());
        return new UserResponse(
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getBio(),
                follows.countByFolloweeId(u.getId()),
                follows.countByFollowerId(u.getId()),
                posts.countByAuthorId(u.getId()),
                followedByMe);
    }
}
