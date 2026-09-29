package com.leoyao.microblog.repository;

import com.leoyao.microblog.model.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = "author")
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    long countByPostId(Long postId);

    /** Loads a comment together with its author, its post and the post's author (for permission checks). */
    @EntityGraph(attributePaths = {"author", "post", "post.author"})
    Optional<Comment> findWithRelationsById(Long id);

    void deleteByPostId(Long postId);
}
