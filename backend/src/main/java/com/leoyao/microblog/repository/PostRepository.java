package com.leoyao.microblog.repository;

import com.leoyao.microblog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = "author")
    Optional<Post> findWithAuthorById(Long id);

    @EntityGraph(attributePaths = "author")
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Page<Post> findByAuthorUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    long countByAuthorId(Long authorId);

    /**
     * Paginated home feed: the user's own posts plus posts from everyone they follow.
     */
    @EntityGraph(attributePaths = "author")
    @Query(value = """
            SELECT p FROM Post p
            WHERE p.author.id = :userId
               OR p.author.id IN (SELECT f.followee.id FROM Follow f WHERE f.follower.id = :userId)
            ORDER BY p.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(p) FROM Post p
            WHERE p.author.id = :userId
               OR p.author.id IN (SELECT f.followee.id FROM Follow f WHERE f.follower.id = :userId)
            """)
    Page<Post> findFeed(@Param("userId") Long userId, Pageable pageable);
}
