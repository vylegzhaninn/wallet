package com.github.vylegzhaninn.wallet.controllertest;

import com.github.vylegzhaninn.wallet.account.Account;
import com.github.vylegzhaninn.wallet.account.AccountController;
import com.github.vylegzhaninn.wallet.account.AccountDto;
import com.github.vylegzhaninn.wallet.account.AccountService;
import com.github.vylegzhaninn.wallet.security.SecurityConfig;
import com.github.vylegzhaninn.wallet.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
public class AccountControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void getAllAccounts_Unauthenticated_returns401() throws Exception {
        mvc.perform(get("/account"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(accountService);
    }

    @Test
    void getAllAccounts_withHttpBasic_returnsPage() throws Exception {
        Account account = new Account(1L, 2L, BigDecimal.ZERO, LocalDateTime.now());
        Page<Account> page = new PageImpl<>(List.of(account));

        when(userDetailsService.loadUserByUsername("alice")).thenReturn(
                User.withUsername("alice")
                        .password(passwordEncoder.encode("secret"))
                        .authorities(List.of())
                        .build()
        );
        when(accountService.getAll(any(Pageable.class))).thenReturn(page);

        mvc.perform(get("/account").with(httpBasic("alice", "secret")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()" ).value(1))
            .andExpect(jsonPath("$.content[0].id" ).value(1L));

        verify(accountService).getAll(any(Pageable.class));
    }

    @Test
    @WithMockUser
    void createAccount() throws Exception {
        Long userId = 1L;
        Account account = new Account(1L, userId, BigDecimal.ZERO, LocalDateTime.now());

        when(accountService.create(userId)).thenReturn(account);

        mvc.perform(post("/account/{userId}", userId))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(account.getId()))
            .andExpect(jsonPath("$.userId").value(account.getUserId()))
            .andExpect(jsonPath("$.balance").value(0));

        verify(accountService).create(userId);
    }

    @Test
    @WithMockUser
    void getAccountById() throws Exception {
        Account account = new Account(1L, 2L, BigDecimal.ZERO, LocalDateTime.now());

        when(accountService.getById(1L)).thenReturn(account);

        mvc.perform(get("/account/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.userId").value(2L))
            .andExpect(jsonPath("$.balance").value(0));

        verify(accountService).getById(1L);
    }

    @Test
    @WithMockUser
    void getAllAccounts() throws Exception {
        Account account = new Account(1L, 2L, BigDecimal.ZERO, LocalDateTime.now());
        Page<Account> page = new PageImpl<>(List.of(account, account, account));

        when(accountService.getAll(any(Pageable.class))).thenReturn(page);

        mvc.perform(get("/account"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.content[0].id").value(1L));

        verify(accountService).getAll(any(Pageable.class));
    }

    @Test
    @WithMockUser
    void deposit() throws Exception {
        Long userId = 2L;
        Long id = 1L;
        BigDecimal amount = BigDecimal.valueOf(100);
        AccountDto request = new AccountDto(userId, id, amount);
        Account account = new Account(id, userId, amount, LocalDateTime.now());

        when(accountService.deposit(request)).thenReturn(account);

        mvc.perform(patch("/account/deposit")
                .contentType("application/json")
                .content("""
                    {
                        "userId": 2,
                        "id": 1,
                        "amount": 100
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.balance").value(100));

        verify(accountService).deposit(request);
    }

    @Test
    @WithMockUser
    void deleteAccount() throws Exception {
        Long id = 1L;

        mvc.perform(delete("/account/{id}", id))
            .andExpect(status().isNoContent());

        verify(accountService).delete(id);
    }
}