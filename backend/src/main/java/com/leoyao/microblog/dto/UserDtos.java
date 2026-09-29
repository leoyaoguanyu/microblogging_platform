package com.leoyao.microblog.dto;

public final class UserDtos {
    private UserDtos() {}

    public record UserResponse(
            Long id,
            String username,
            String displayName,
            String bio,
            long followerCount,
            long followingCount,
            long postCount,
            boolean followedByMe) {}
}
