package com.example.twitterlike;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getPostsShouldBePublic() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk());
    }

    @Test
    void createPostShouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPostShouldRequireWriteScope() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_read:posts")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPostShouldWorkWithWriteScope() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .with(jwt().jwt(jwt -> jwt.subject("auth0|123").claim("name", "Juan"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_write:posts")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hola mundo\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void meShouldRequireReadProfileScope() throws Exception {
        mockMvc.perform(get("/api/me")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_write:posts"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void meShouldWorkWithReadProfileScope() throws Exception {
        mockMvc.perform(get("/api/me")
                        .with(jwt().jwt(jwt -> jwt.subject("auth0|123").claim("name", "Juan"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_read:profile"))))
                .andExpect(status().isOk());
    }
}
