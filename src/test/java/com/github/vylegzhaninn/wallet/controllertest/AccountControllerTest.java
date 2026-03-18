package com.github.vylegzhaninn.wallet.controllertest;

import com.github.vylegzhaninn.wallet.account.Account;
import com.github.vylegzhaninn.wallet.account.AccountController;
import com.github.vylegzhaninn.wallet.account.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AccountService accountService;

    @Test
    void createAccountTest() throws Exception {
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
}