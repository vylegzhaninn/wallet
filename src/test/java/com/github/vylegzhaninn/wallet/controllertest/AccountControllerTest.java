package com.github.vylegzhaninn.wallet.controllertest;

import com.github.vylegzhaninn.wallet.account.Account;
import com.github.vylegzhaninn.wallet.account.AccountController;
import com.github.vylegzhaninn.wallet.account.AccountDto;
import com.github.vylegzhaninn.wallet.account.AccountService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AccountService accountService;

    @Test
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
    void getAllAccounts() throws Exception {
        Account account = new Account(1L, 2L, BigDecimal.ZERO, LocalDateTime.now());
        List<Account> list = List.of(account, account, account);

        when(accountService.getAll()).thenReturn(list);

        mvc.perform(get("/account"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].id").value(1L));

        verify(accountService).getAll();
    }

    @Test
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
    void deleteAccount() throws Exception {
        Long id = 1L;

        mvc.perform(delete("/account/{id}", id))
            .andExpect(status().isNoContent());

        verify(accountService).delete(id);
    }
}