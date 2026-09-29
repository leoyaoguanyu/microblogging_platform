package com.leoyao.microblog.controller;

import com.leoyao.microblog.dto.PageResponse;
import com.leoyao.microblog.dto.PostDtos.CommentRequest;
import com.leoyao.microblog.dto.PostDtos.CommentResponse;
import com.leoyao.microblog.dto.PostDtos.PostRequest;
import com.leoyao.microblog.dto.PostDtos.PostResponse;
import com.leoyao.microblog.security.AppUserPrincipal;
import com.leoyao.microblog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.leoyao.microblog.controller.UserController.idOf;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /** All posts, newest first (public "explore" timeline). */
    @GetMapping
    public PageResponse<PostResponse> list(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @AuthenticationPrincipal AppUserPrincipal me) {
        return postService.listAll(page, size, idOf(me));
    }

    @GetMapping("/{id}")
    public PostResponse get(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal me) {
        return postService.get(id, idOf(me));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(@Valid @RequestBody PostRequest req, @AuthenticationPrincipal AppUserPrincipal me) {
        return postService.create(me.getId(), req);
    }

    /** Resource-level authorization: only the post's author may edit it. */
    @PutMapping("/{id}")
    @PreAuthorize("@postSecurity.isAuthor(#id, principal)")
    public PostResponse update(@PathVariable Long id, @Valid @RequestBody PostRequest req,
                               @AuthenticationPrincipal AppUserPrincipal me) {
        return postService.update(id, req, me.getId());
    }

    /** Resource-level authorization: only the post's author may delete it. */
    @DeleteMapping("/{id}")
    @PreAuthorize("@postSecurity.isAuthor(#id, principal)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        postService.delete(id);
    }

    // ----- likes -----

    @PostMapping("/{id}/like")
    public PostResponse like(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal me) {
        return postService.like(id, me.getId());
    }

    @DeleteMapping("/{id}/like")
    public PostResponse unlike(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal me) {
        return postService.unlike(id, me.getId());
    }

    // ----- comments -----

    @GetMapping("/{id}/comments")
    public List<CommentResponse> comments(@PathVariable Long id) {
        return postService.listComments(id);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest req,
                                      @AuthenticationPrincipal AppUserPrincipal me) {
        return postService.addComment(id, me.getId(), req);
    }

    /** Comment author or post author may delete a comment. */
    @DeleteMapping("/{id}/comments/{commentId}")
    @PreAuthorize("@postSecurity.canDeleteComment(#id, #commentId, principal)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long id, @PathVariable Long commentId) {
        postService.deleteComment(id, commentId);
    }
}
