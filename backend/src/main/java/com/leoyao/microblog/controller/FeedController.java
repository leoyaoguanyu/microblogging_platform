package com.leoyao.microblog.controller;

import com.leoyao.microblog.dto.PageResponse;
import com.leoyao.microblog.dto.PostDtos.PostResponse;
import com.leoyao.microblog.security.AppUserPrincipal;
import com.leoyao.microblog.service.PostService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final PostService postService;

    public FeedController(PostService postService) {
        this.postService = postService;
    }

    /** Paginated home feed of posts from followed users (and the user's own). Requires auth. */
    @GetMapping
    public PageResponse<PostResponse> feed(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @AuthenticationPrincipal AppUserPrincipal me) {
        return postService.feed(me.getId(), page, size);
    }
}
