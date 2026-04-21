package com.example.twitterlike.dto;

import java.util.List;

public class StreamResponse {

    private long total;
    private List<PostResponse> posts;

    public StreamResponse() {
    }

    public StreamResponse(long total, List<PostResponse> posts) {
        this.total = total;
        this.posts = posts;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<PostResponse> getPosts() {
        return posts;
    }

    public void setPosts(List<PostResponse> posts) {
        this.posts = posts;
    }
}
