package com.leoyao.microblog.security;

import com.leoyao.microblog.exception.NotFoundException;
import com.leoyao.microblog.model.Comment;
import com.leoyao.microblog.model.Post;
import com.leoyao.microblog.repository.CommentRepository;
import com.leoyao.microblog.repository.PostRepository;
import org.springframework.stereotype.Component;

/**
 * Resource-level authorization rules, referenced from @PreAuthorize expressions,
 * e.g. @PreAuthorize("@postSecurity.isAuthor(#id, principal)").
 */
@Component("postSecurity")
public class PostSecurity {

    private final PostRepository posts;
    private final CommentRepository comments;

    public PostSecurity(PostRepository posts, CommentRepository comments) {
        this.posts = posts;
        this.comments = comments;
    }

    /** Only the author of a post may edit or delete it. */
    public boolean isAuthor(Long postId, Object principal) {
        if (!(principal instanceof AppUserPrincipal me)) return false;
        Post post = posts.findWithAuthorById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
        return post.getAuthor().getId().equals(me.getId());
    }

    /** A comment may be deleted by its author or by the author of the post it belongs to. */
    public boolean canDeleteComment(Long postId, Long commentId, Object principal) {
        if (!(principal instanceof AppUserPrincipal me)) return false;
        Comment comment = comments.findWithRelationsById(commentId)
                .filter(c -> c.getPost().getId().equals(postId))
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        return comment.getAuthor().getId().equals(me.getId())
                || comment.getPost().getAuthor().getId().equals(me.getId());
    }
}
