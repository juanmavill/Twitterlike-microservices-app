package com.example.twitterlike.service;

import com.example.twitterlike.dto.CreatePostRequest;
import com.example.twitterlike.dto.PostResponse;
import com.example.twitterlike.entity.Post;
import com.example.twitterlike.repository.PostRepository;
import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<PostResponse> getPublicPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PostResponse createPost(CreatePostRequest request, Jwt jwt) {
        String authorId = jwt.getSubject();
        if (authorId == null || authorId.isBlank()) {
            throw new IllegalArgumentException("JWT subject is required to create a post");
        }

        Post post = new Post();
        post.setContent(request.getContent());
        post.setAuthorId(authorId);
        post.setAuthorName(resolveAuthorName(jwt));

        Post savedPost = postRepository.save(post);
        return toResponse(savedPost);
    }

    private String resolveAuthorName(Jwt jwt) {
        String name = jwt.getClaimAsString("name");
        if (name != null && !name.isBlank()) {
            return name;
        }

        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }

        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return email;
        }

        return jwt.getSubject();
    }

    private PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getContent(),
                post.getAuthorId(),
                post.getAuthorName(),
                post.getCreatedAt()
        );
    }
}
