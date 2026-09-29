package com.leoyao.microblog.controller;

import com.leoyao.microblog.dto.PageResponse;
import com.leoyao.microblog.dto.PostDtos.PostResponse;
import com.leoyao.microblog.dto.UserDtos.UserResponse;
import com.leoyao.microblog.security.AppUserPrincipal;
import com.leoyao.microblog.service.PostService;
import com.leoyao.microblog.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PostService postService;

    public UserController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    @GetMapping
    public List<UserResponse> list(@AuthenticationPrincipal AppUserPrincipal me) {
        return userService.listUsers(idOf(me));
    }

    @GetMapping("/{username}")
    public UserResponse profile(@PathVariable String username, @AuthenticationPrincipal AppUserPrincipal me) {
        return userService.getProfile(username, idOf(me));
    }

    @GetMapping("/{username}/posts")
    public PageResponse<PostResponse> posts(@PathVariable String username,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @AuthenticationPrincipal AppUserPrincipal me) {
        return postService.listByUser(username, page, size, idOf(me));
    }

    @PostMapping("/{username}/follow")
    public UserResponse follow(@PathVariable String username, @AuthenticationPrincipal AppUserPrincipal me) {
        return userService.follow(username, me.getId());
    }

    @DeleteMapping("/{username}/follow")
    public UserResponse unfollow(@PathVariable String username, @AuthenticationPrincipal AppUserPrincipal me) {
        return userService.unfollow(username, me.getId());
    }

    static Long idOf(AppUserPrincipal me) {
        return me == null ? null : me.getId();
    }
}
