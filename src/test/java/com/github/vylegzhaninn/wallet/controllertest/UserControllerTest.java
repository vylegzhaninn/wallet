package com.github.vylegzhaninn.wallet.controllertest;

import com.github.vylegzhaninn.wallet.user.User;
import com.github.vylegzhaninn.wallet.user.UserController;
import com.github.vylegzhaninn.wallet.user.UserDto;
import com.github.vylegzhaninn.wallet.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private UserService userService;

    @Test
    void createUser() throws Exception {
        UserDto request = new UserDto("Max", "max@example.com");
        User user = new User(1L, "Max", "max@example.com", LocalDateTime.now());

        when(userService.create(request)).thenReturn(user);

        mvc.perform(post("/user")
                .contentType("application/json")
                .content("""
                    {
                        "name": "Max",
                        "email": "max@example.com"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Max"))
            .andExpect(jsonPath("$.email").value("max@example.com"));

        verify(userService).create(request);
    }

    @Test
    void getUserById() throws Exception {
        User user = new User(1L, "Max", "max@example.com", LocalDateTime.now());

        when(userService.getById(1L)).thenReturn(user);

        mvc.perform(get("/user/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Max"))
            .andExpect(jsonPath("$.email").value("max@example.com"));

        verify(userService).getById(1L);
    }

    @Test
    void getAllUsers() throws Exception {
        User user = new User(1L, "Max", "max@example.com", LocalDateTime.now());
        List<User> list = List.of(user);

        when(userService.getAll()).thenReturn(list);

        mvc.perform(get("/user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1L));

        verify(userService).getAll();
    }

    @Test
    void updateUser() throws Exception {
        Long id = 1L;
        UserDto request = new UserDto("Max Updated", "max.updated@example.com");
        User user = new User(id, "Max Updated", "max.updated@example.com", LocalDateTime.now());

        when(userService.update(id, request)).thenReturn(user);

        mvc.perform(put("/user/{id}", id)
                .contentType("application/json")
                .content("""
                    {
                        "name": "Max Updated",
                        "email": "max.updated@example.com"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value("Max Updated"));

        verify(userService).update(id, request);
    }

    @Test
    void deleteUser() throws Exception {
        Long id = 1L;

        mvc.perform(delete("/user/{id}", id))
            .andExpect(status().isNoContent());

        verify(userService).delete(id);
    }
}
