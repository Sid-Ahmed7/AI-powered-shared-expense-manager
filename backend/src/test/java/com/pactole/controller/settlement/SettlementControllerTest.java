package com.pactole.controller.settlement;

import com.pactole.TestcontainersConfiguration;
import com.pactole.services.balance.BalanceService;
import com.pactole.services.expensesGroup.GroupAccess;
import com.pactole.services.settlement.SettlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class SettlementControllerTest {

    @Autowired WebApplicationContext wac;

    @MockitoBean GroupAccess groupAccess;
    @MockitoBean BalanceService balanceService;
    @MockitoBean SettlementService settlementService;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
    }

    private static final String BASE = "/api/groups/{groupId}";

    @Test
    void balances_withoutJwt_returns401() throws Exception {
        mockMvc.perform(get(BASE + "/balances", UUID.randomUUID())).andExpect(status().isUnauthorized());
    }

    @Test
    void listSettlements_withoutJwt_returns401() throws Exception {
        mockMvc.perform(get(BASE + "/settlements", UUID.randomUUID())).andExpect(status().isUnauthorized());
    }

    @Test
    void createSettlement_withoutJwt_returns401() throws Exception {
        mockMvc.perform(post(BASE + "/settlements", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteSettlement_withoutJwt_returns401() throws Exception {
        mockMvc.perform(delete(BASE + "/settlements/{settlementId}", UUID.randomUUID(), UUID.randomUUID())).andExpect(status().isUnauthorized());
    }
}
