package com.ecommerce.application.integration.product;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductUpdateITest extends AbstractProductITest {

    @Test
    void update_product_reflects_changes_in_response() throws Exception {
        Long id = createProductAndGetId("original-url");

        var updated = validRequest("updated-url");
        updated.setName("Updated Name");

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(updated))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("updated-url"))
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void update_product_keeping_its_own_url_is_allowed() throws Exception {
        Long id = createProductAndGetId("stable-url");

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(validRequest("stable-url")))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void update_with_another_products_url_returns_conflict() throws Exception {
        createProductAndGetId("taken-url");
        Long id = createProductAndGetId("my-url");

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(validRequest("taken-url")))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_URL_ALREADY_EXISTS"));
    }

    @Test
    void update_product_not_found_returns_404() throws Exception {
        mockMvc.perform(put("/api/products/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(validRequest("ghost-update")))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void update_without_auth_returns_401() throws Exception {
        Long id = createProductAndGetId("update-no-auth");

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(validRequest("update-no-auth"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_with_user_role_returns_403() throws Exception {
        Long id = createProductAndGetId("update-user-forbidden");

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(validRequest("update-user-forbidden")))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
