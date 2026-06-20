package com.ecommerce.application.integration.product;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductDeleteITest extends AbstractProductITest {

    @Test
    void delete_product_removes_it_from_the_catalog() throws Exception {
        Long id = createProductAndGetId("to-delete");

        mockMvc.perform(delete("/products/{id}", id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void delete_product_not_found_returns_404() throws Exception {
        mockMvc.perform(delete("/products/{id}", 99999L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void delete_product_without_auth_returns_401() throws Exception {
        Long id = createProductAndGetId("del-no-auth");

        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_product_with_user_role_returns_403() throws Exception {
        Long id = createProductAndGetId("del-user-forbidden");

        mockMvc.perform(delete("/products/{id}", id)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
