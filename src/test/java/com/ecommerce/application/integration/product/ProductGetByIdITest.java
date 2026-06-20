package com.ecommerce.application.integration.product;

import com.ecommerce.application.api.dto.product.CreateProductRequestDto;
import com.ecommerce.persistence.entity.enumeration.ProductStatus;
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
    void get_by_id_active_product_is_public() throws Exception {
        Long id = createProductAndGetId("public-product");

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void get_by_id_inactive_product_returns_404_for_unauthenticated() throws Exception {
        CreateProductRequestDto req = validRequest("hidden-product");
        req.setStatus(ProductStatus.INACTIVE);
        Long id = json(multipartCreate(req, adminToken).andReturn()).get("id").asLong();

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void get_by_id_admin_can_view_inactive_product() throws Exception {
        CreateProductRequestDto req = validRequest("admin-only-product");
        req.setStatus(ProductStatus.INACTIVE);
        Long id = json(multipartCreate(req, adminToken).andReturn()).get("id").asLong();

        mockMvc.perform(get("/products/{id}", id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("admin-only-product"));
    }
}
