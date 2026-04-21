package com.example.twitterlike.controller;

import com.example.twitterlike.dto.CreatePostRequest;
import com.example.twitterlike.dto.PostResponse;
import com.example.twitterlike.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Posts", description = "Post creation and public listing")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    @Operation(summary = "Get public posts stream")
    public List<PostResponse> getPosts() {
        return postService.getPublicPosts();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_write:posts')")
    @Operation(summary = "Create post", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        PostResponse response = postService.createPost(request, jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
