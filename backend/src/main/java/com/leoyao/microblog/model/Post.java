package com.leoyao.microblog.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_author_created", columnList = "author_id, created_at"),
        @Index(name = "idx_posts_created", columnList = "created_at")
})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 280)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected Post() {}

    public Post(User author, String content) {
        this.author = author;
        this.content = content;
        this.updatedAt = this.createdAt;   // identical until the post is edited
    }

    public Long getId() { return id; }
    public User getAuthor() { return author; }
    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content;
        this.updatedAt = Instant.now();
    }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
