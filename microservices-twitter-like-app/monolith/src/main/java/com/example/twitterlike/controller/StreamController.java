package com.example.twitterlike.controller;

import com.example.twitterlike.dto.PostResponse;
import com.example.twitterlike.dto.StreamResponse;
import com.example.twitterlike.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stream")
@Tag(name = "Stream", description = "Global public stream")
public class StreamController {

    private final PostService postService;

    public StreamController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    @Operation(summary = "Get global stream")
    public StreamResponse getStream() {
        List<PostResponse> posts = postService.getPublicPosts();
        return new StreamResponse(posts.size(), posts);
    }
}
