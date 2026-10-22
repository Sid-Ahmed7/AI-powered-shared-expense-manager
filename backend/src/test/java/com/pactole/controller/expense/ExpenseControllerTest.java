package com.pactole.controller.expense;

import com.pactole.TestcontainersConfiguration;
import com.pactole.services.expense.ExpenseService;
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
class ExpenseControllerTest {

    @Autowired WebApplicationContext wac;

    @MockitoBean ExpenseService expenseService;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .apply(springSecurity())
            .build();
    }

    private static final String BASE = "/api/groups/{groupId}/expenses";

    @Test
    void list_withoutJwt_returns401() throws Exception {
        mockMvc.perform(get(BASE, UUID.randomUUID())).andExpect(status().isUnauthorized());
    }

    @Test
    void get_withoutJwt_returns401() throws Exception {
        mockMvc.perform(get(BASE + "/{id}", UUID.randomUUID(), UUID.randomUUID())).andExpect(status().isUnauthorized());
    }

    @Test
    void create_withoutJwt_returns401() throws Exception {
        mockMvc.perform(post(BASE, UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void update_withoutJwt_returns401() throws Exception {
        mockMvc.perform(put(BASE + "/{id}", UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_withoutJwt_returns401() throws Exception {
        mockMvc.perform(delete(BASE + "/{id}", UUID.randomUUID(), UUID.randomUUID())).andExpect(status().isUnauthorized());
    }
}
