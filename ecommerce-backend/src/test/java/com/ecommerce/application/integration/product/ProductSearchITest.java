package com.ecommerce.application.integration.product;

import com.ecommerce.application.api.dto.product.CreateProductRequestDto;
import com.ecommerce.persistence.entity.enumeration.ProductStatus;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductSearchITest extends AbstractProductITest {

    @Test
    void search_with_no_filters_returns_all_active_products() throws Exception {
        createProductAndGetId("phone-a");
        createProductAndGetId("phone-b");

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void search_filters_by_local_name() throws Exception {
        CreateProductRequestDto keyboard = validRequest("keyboard-1");
        keyboard.setLocalName("کیبورد");
        multipartCreate(keyboard, adminToken);

        CreateProductRequestDto monitor = validRequest("monitor-1");
        monitor.setLocalName("مانیتور");
        multipartCreate(monitor, adminToken);

        mockMvc.perform(get("/api/products").param("localName", "کیبورد"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].url").value("keyboard-1"));
    }

    @Test
    void search_filters_by_category_id() throws Exception {
        Long otherCategoryId = jdbcTemplate.queryForObject(
                "INSERT INTO category (name) VALUES ('Fashion') RETURNING id", Long.class);

        createProductAndGetId("tech-item");

        CreateProductRequestDto fashionReq = validRequest("fashion-item");
        fashionReq.setCategoryId(otherCategoryId);
        multipartCreate(fashionReq, adminToken);

        mockMvc.perform(get("/api/products").param("categoryId", categoryId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].url").value("tech-item"));
    }

    @Test
    void search_is_available_true_returns_only_products_with_positive_inventory() throws Exception {
        CreateProductRequestDto inStock = validRequest("in-stock-product");
        inStock.setInventoryCount(5);
        multipartCreate(inStock, adminToken);

        CreateProductRequestDto outOfStock = validRequest("out-of-stock-product");
        outOfStock.setInventoryCount(0);
        multipartCreate(outOfStock, adminToken);

        mockMvc.perform(get("/api/products").param("isAvailable", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].url").value("in-stock-product"));
    }

    @Test
    void search_is_available_false_returns_only_zero_inventory_products() throws Exception {
        CreateProductRequestDto inStock = validRequest("still-available");
        inStock.setInventoryCount(3);
        multipartCreate(inStock, adminToken);

        CreateProductRequestDto outOfStock = validRequest("empty-shelf");
        outOfStock.setInventoryCount(0);
        multipartCreate(outOfStock, adminToken);

        mockMvc.perform(get("/api/products").param("isAvailable", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].url").value("empty-shelf"));
    }

    @Test
    void search_admin_can_filter_by_status() throws Exception {
        createProductAndGetId("active-item");

        CreateProductRequestDto inactive = validRequest("inactive-item");
        inactive.setStatus(ProductStatus.INACTIVE);
        multipartCreate(inactive, adminToken);

        mockMvc.perform(get("/api/products").param("status", "INACTIVE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].url").value("inactive-item"));
    }

    @Test
    void search_unauthenticated_only_returns_active_products() throws Exception {
        createProductAndGetId("active-item");

        CreateProductRequestDto inactive = validRequest("inactive-item");
        inactive.setStatus(ProductStatus.INACTIVE);
        multipartCreate(inactive, adminToken);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].url").value("active-item"));
    }

    @Test
    void search_is_public_no_auth_required() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }
}
