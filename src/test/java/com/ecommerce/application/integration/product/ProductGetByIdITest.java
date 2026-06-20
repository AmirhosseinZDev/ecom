package com.ecommerce.application.integration.product;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductGetByIdITest extends AbstractProductITest {

    @Test
    void get_by_id_returns_product() throws Exception {
        Long id = createProductAndGetId("headphones-v2");

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.url").value("headphones-v2"));
    }

    @Test
    void get_by_id_not_found_returns_404() throws Exception {
        mockMvc.perform(get("/products/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void get_by_id_is_public_no_auth_required() throws Exception {
        Long id = createProductAndGetId("public-product");

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk());
    }
}
