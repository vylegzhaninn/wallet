package com.github.vylegzhaninn.wallet.servicetest;

import com.github.vylegzhaninn.wallet.exception.AlreadyExistsException;
import com.github.vylegzhaninn.wallet.exception.NotFoundException;
import com.github.vylegzhaninn.wallet.user.User;
import com.github.vylegzhaninn.wallet.user.UserDto;
import com.github.vylegzhaninn.wallet.user.UserRepository;
import com.github.vylegzhaninn.wallet.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createUser_Success() {
        UserDto dto = new UserDto("Max", "max@example.com", "password123");
        User user = User.builder().id(1L).name("Max").email("max@example.com").password("encodedPass").build();

        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userRepository.existsByName(dto.name())).thenReturn(false);
        when(passwordEncoder.encode(dto.password())).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User created = userService.create(dto);

        assertNotNull(created);
        assertEquals("Max", created.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UserAlreadyExistsByEmail() {
        UserDto dto = new UserDto("Max", "max@example.com", "password123");

        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> userService.create(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_UserAlreadyExistsByName() {
        UserDto dto = new UserDto("Max", "max@example.com", "password123");

        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userRepository.existsByName(dto.name())).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> userService.create(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getById_Success() {
        User user = User.builder().id(1L).name("Max").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User found = userService.getById(1L);
        assertEquals("Max", found.getName());
    }

    @Test
    void getById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getById(1L));
    }

    @Test
    void updateUser_Success() {
        User user = User.builder().id(1L).name("Max").email("max@example.com").password("oldPass").build();
        UserDto updateDto = new UserDto("NewMax", "new@example.com", "newPass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.update(1L, updateDto);

        assertEquals("NewMax", updated.getName());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals("encodedNewPass", updated.getPassword());
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.delete(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_NotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> userService.delete(1L));
    }
}
