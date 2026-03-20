package com.github.vylegzhaninn.wallet.controllertest;

import com.github.vylegzhaninn.wallet.security.SecurityConfig;
import com.github.vylegzhaninn.wallet.security.UserDetailsServiceImpl;
import com.github.vylegzhaninn.wallet.transfer.TransferController;
import com.github.vylegzhaninn.wallet.transfer.TransferDto;
import com.github.vylegzhaninn.wallet.transfer.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
@Import(SecurityConfig.class)
public class TransferControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private TransferService transferService;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void executeTransfer_Unauthenticated_returns401() throws Exception {
        mvc.perform(post("/transfer")
                        .contentType("application/json")
                        .content("""
                            {
                                "from": 1,
                                "to": 2,
                                "userIdFrom": 10,
                                "userIdTo": 20,
                                "amount": 10
                            }
                            """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(transferService);
    }

    @Test
    void executeTransfer_withHttpBasic_returnsOk() throws Exception {
        TransferDto request = new TransferDto(1L, 2L, 10L, 20L, BigDecimal.TEN);

        when(userDetailsService.loadUserByUsername("alice")).thenReturn(
                User.withUsername("alice")
                        .password(passwordEncoder.encode("secret"))
                        .authorities(List.of())
                        .build()
        );

        mvc.perform(post("/transfer")
                        .with(httpBasic("alice", "secret"))
                        .contentType("application/json")
                        .content("""
                            {
                                "from": 1,
                                "to": 2,
                                "userIdFrom": 10,
                                "userIdTo": 20,
                                "amount": 10
                            }
                            """))
                .andExpect(status().isOk());

        verify(transferService).transfer(request);
    }

    @Test
    @WithMockUser
    void executeTransfer() throws Exception {
        TransferDto request = new TransferDto(1L, 2L, 10L, 20L, BigDecimal.TEN);

        mvc.perform(post("/transfer")
                .contentType("application/json")
                .content("""
                    {
                        "from": 1,
                        "to": 2,
                        "userIdFrom": 10,
                        "userIdTo": 20,
                        "amount": 10
                    }
                    """))
            .andExpect(status().isOk());

        verify(transferService).transfer(request);
    }

    @Test
    @WithMockUser
    void getTransferHistory() throws Exception {
        Long accountId = 1L;
        TransferDto transfer = new TransferDto(1L, 2L, 10L, 20L, BigDecimal.TEN);
        List<TransferDto> history = List.of(transfer);

        when(transferService.getHistory(accountId)).thenReturn(history);

        mvc.perform(get("/transfer/{accountId}", accountId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].from").value(1L))
            .andExpect(jsonPath("$[0].amount").value(10));

        verify(transferService).getHistory(accountId);
    }
}
