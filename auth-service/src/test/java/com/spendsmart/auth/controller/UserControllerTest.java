package com.spendsmart.auth.controller;

import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@org.springframework.context.annotation.Import({
    com.spendsmart.auth.config.SecurityConfig.class,
    com.spendsmart.auth.security.JwtAuthFilter.class,
    com.spendsmart.auth.security.OAuth2SuccessHandler.class,
    com.spendsmart.auth.exception.GlobalExceptionHandler.class
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private com.spendsmart.auth.security.JwtUtil jwtUtil;

    @MockBean
    private com.spendsmart.auth.service.AuthService authService;

    @Test
    void getProfile() throws Exception {
        User user = new User();
        user.setEmail("test@test.com");
        user.setName("Test");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/user/profile")
                        .header("X-User-Email", "test@test.com"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void getProfile_NotFound() throws Exception {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/user/profile")
                        .header("X-User-Email", "test@test.com"))
                .andExpect(status().isBadRequest()); // ExceptionHandler maps RuntimeException to 400
    }

    @Test
    void upgradeUser() throws Exception {
        mockMvc.perform(post("/api/user/internal/upgrade")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk());

        verify(authService).upgradeUser("test@test.com");
    }

    @Test
    void getUsersCount_AdminSuccess() throws Exception {
        when(userRepository.countByRole("USER")).thenReturn(10L);

        mockMvc.perform(get("/api/user/admin/users/count")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("10"));
    }

    @Test
    void getUsersCount_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/user/admin/users/count")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isUnauthorized()); // ExceptionHandler maps to 401
    }

    @Test
    void getPremiumUsersCount_AdminSuccess() throws Exception {
        when(userRepository.countByIsPremiumTrue()).thenReturn(5L);

        mockMvc.perform(get("/api/user/admin/users/premium/count")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("5"));
    }

    @Test
    void getPremiumUsersCount_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/user/admin/users/premium/count")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isUnauthorized());
    }

}
