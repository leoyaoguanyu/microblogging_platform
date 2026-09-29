package com.leoyao.microblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class PostDtos {
    private PostDtos() {}

    public record PostRequest(@NotBlank @Size(max = 280) String content) {}

    public record PostResponse(
            Long id,
            String content,
            Long authorId,
            String authorUsername,
            String authorDisplayName,
            Instant createdAt,
            Instant updatedAt,
            long likeCount,
            long commentCount,
            boolean likedByMe) {}

    public record CommentRequest(@NotBlank @Size(max = 280) String content) {}

    public record CommentResponse(
            Long id,
            String content,
            Long authorId,
            String authorUsername,
            String authorDisplayName,
            Instant createdAt) {}
}
