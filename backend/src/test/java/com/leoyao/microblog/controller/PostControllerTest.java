package com.leoyao.microblog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end HTTP tests for the posts API, including the JWT security chain.
 * Runs against an in-memory H2 database (the "h2" profile); demo users
 * alice/bob/carol/dave (password "password123") are seeded on startup.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class PostControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** Logs in through the real /api/auth/login endpoint and returns a JWT. */
    private String tokenFor(String username) throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("username", username, "password", "password123"));
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    private String bearer(String username) throws Exception {
        return "Bearer " + tokenFor(username);
    }

    /** Creates a post as the given user and returns its id. */
    private long createPostAs(String username, String content) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("content", content));
        MvcResult result = mvc.perform(post("/api/posts")
                        .header("Authorization", bearer(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Test
    void unauthenticatedCreatePost_returns401() throws Exception {
        mvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithWrongPassword_returns401() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("username", "alice", "password", "wrong-password"));
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUser_canCreatePost() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("content", "hello from a test"));
        mvc.perform(post("/api/posts")
                        .header("Authorization", bearer("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("hello from a test"))
                .andExpect(jsonPath("$.authorUsername").value("alice"));
    }

    @Test
    void createPost_over280Chars_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("content", "a".repeat(281)));
        mvc.perform(post("/api/posts")
                        .header("Authorization", bearer("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonAuthor_cannotEditPost_returns403() throws Exception {
        long postId = createPostAs("alice", "alice's post");
        String body = objectMapper.writeValueAsString(Map.of("content", "bob tries to edit"));
        mvc.perform(put("/api/posts/" + postId)
                        .header("Authorization", bearer("bob"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void author_canEditOwnPost() throws Exception {
        long postId = createPostAs("alice", "original");
        String body = objectMapper.writeValueAsString(Map.of("content", "edited"));
        mvc.perform(put("/api/posts/" + postId)
                        .header("Authorization", bearer("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("edited"));
    }

    @Test
    void publicTimeline_readableWithoutAuth() throws Exception {
        mvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void feed_requiresAuth() throws Exception {
        mvc.perform(get("/api/feed"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void like_requiresAuth() throws Exception {
        mvc.perform(post("/api/posts/1/like"))
                .andExpect(status().isUnauthorized());
    }
}
