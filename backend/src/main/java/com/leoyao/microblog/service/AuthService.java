package com.leoyao.microblog.service;

import com.leoyao.microblog.dto.AuthDtos.AuthResponse;
import com.leoyao.microblog.dto.AuthDtos.LoginRequest;
import com.leoyao.microblog.dto.AuthDtos.RegisterRequest;
import com.leoyao.microblog.exception.ConflictException;
import com.leoyao.microblog.model.User;
import com.leoyao.microblog.repository.UserRepository;
import com.leoyao.microblog.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService,
                       UserService userService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (users.existsByUsername(req.username())) {
            throw new ConflictException("Username already taken");
        }
        User user = users.save(new User(req.username(), passwordEncoder.encode(req.password()), req.displayName()));
        return new AuthResponse(jwtService.generateToken(user.getUsername()), userService.toResponse(user, user.getId()));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        // Throws BadCredentialsException (-> 401) if the username/password pair is wrong.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        User user = userService.findByUsername(req.username());
        return new AuthResponse(jwtService.generateToken(user.getUsername()), userService.toResponse(user, user.getId()));
    }
}
