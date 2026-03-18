package com.github.vylegzhaninn.wallet.controllertest;

import com.github.vylegzhaninn.wallet.transfer.TransferController;
import com.github.vylegzhaninn.wallet.transfer.TransferDto;
import com.github.vylegzhaninn.wallet.transfer.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
public class TransferControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private TransferService transferService;

    @Test
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
