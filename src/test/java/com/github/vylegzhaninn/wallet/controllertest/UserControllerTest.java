package com.github.vylegzhaninn.wallet.controllertest;

import com.github.vylegzhaninn.wallet.security.SecurityConfig;
import com.github.vylegzhaninn.wallet.security.UserDetailsServiceImpl;
import com.github.vylegzhaninn.wallet.user.User;
import com.github.vylegzhaninn.wallet.user.UserController;
import com.github.vylegzhaninn.wallet.user.UserDto;
import com.github.vylegzhaninn.wallet.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void getUserById_Unauthenticated_returns401() throws Exception {
        mvc.perform(get("/user/{id}", 1L))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    void getUserById_withHttpBasic_returnsUser() throws Exception {
        User apiUser = User.builder()
                .id(1L)
                .name("Max")
                .email("max@example.com")
                .password("encodedPass")
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getById(1L)).thenReturn(apiUser);
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(
                org.springframework.security.core.userdetails.User.builder()
                        .username("alice")
                        .password(passwordEncoder.encode("secret"))
                        .authorities(List.of())
                        .build()
        );

        mvc.perform(get("/user/{id}", 1L).with(httpBasic("alice", "secret")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.email").value("max@example.com"));

        verify(userService).getById(1L);
    }

    @Test
    void createUser() throws Exception {
        UserDto request = new UserDto("Max", "max@example.com", "password123");
        User user = User.builder()
                .id(1L)
                .name("Max")
                .email("max@example.com")
                .password("encodedPass")
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.create(request)).thenReturn(user);

        mvc.perform(post("/user")
                .contentType("application/json")
                .content("""
                    {
                        "name": "Max",
                        "email": "max@example.com",
                        "password": "password123"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Max"))
            .andExpect(jsonPath("$.email").value("max@example.com"));

        verify(userService).create(request);
    }

    @Test
    @WithMockUser
    void getUserById() throws Exception {
        User user = User.builder()
                .id(1L)
                .name("Max")
                .email("max@example.com")
                .password("encodedPass")
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getById(1L)).thenReturn(user);

        mvc.perform(get("/user/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Max"))
            .andExpect(jsonPath("$.email").value("max@example.com"));

        verify(userService).getById(1L);
    }

    @Test
    @WithMockUser
    void getAllUsers() throws Exception {
        User user = User.builder()
                .id(1L)
                .name("Max")
                .email("max@example.com")
                .password("encodedPass")
                .createdAt(LocalDateTime.now())
                .build();
        Page<User> page = new PageImpl<>(List.of(user));

        when(userService.getAll(any(Pageable.class))).thenReturn(page);

        mvc.perform(get("/user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(1L));

        verify(userService).getAll(any(Pageable.class));
    }

    @Test
    @WithMockUser
    void updateUser() throws Exception {
        Long id = 1L;
        UserDto request = new UserDto("Max Updated", "max.updated@example.com", "newPass");
        User user = User.builder()
                .id(id)
                .name("Max Updated")
                .email("max.updated@example.com")
                .password("encodedPass")
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.update(id, request)).thenReturn(user);

        mvc.perform(put("/user/{id}", id)
                .contentType("application/json")
                .content("""
                    {
                        "name": "Max Updated",
                        "email": "max.updated@example.com",
                        "password": "newPass"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value("Max Updated"));
    
        verify(userService).update(id, request);
    }

    @Test
    @WithMockUser
    void deleteUser() throws Exception {
        Long id = 1L;

        mvc.perform(delete("/user/{id}", id))
            .andExpect(status().isNoContent());

        verify(userService).delete(id);
    }
}
