package com.leoyao.microblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 30)
            @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "letters, digits and underscore only")
            String username,
            @NotBlank @Size(min = 6, max = 72) String password,
            @NotBlank @Size(max = 50) String displayName) {}

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record AuthResponse(String token, UserDtos.UserResponse user) {}
}
