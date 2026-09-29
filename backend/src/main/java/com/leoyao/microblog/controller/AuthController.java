package com.leoyao.microblog.controller;

import com.leoyao.microblog.dto.AuthDtos.AuthResponse;
import com.leoyao.microblog.dto.AuthDtos.LoginRequest;
import com.leoyao.microblog.dto.AuthDtos.RegisterRequest;
import com.leoyao.microblog.dto.UserDtos.UserResponse;
import com.leoyao.microblog.security.AppUserPrincipal;
import com.leoyao.microblog.service.AuthService;
import com.leoyao.microblog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    /** Current user (requires a valid JWT). */
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AppUserPrincipal me) {
        return userService.getById(me.getId());
    }
}
