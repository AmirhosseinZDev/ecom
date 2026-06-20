package com.ecommerce.application.integration.product;

import com.ecommerce.application.api.dto.product.CreateProductRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductCreateITest extends AbstractProductITest {

    @Test
    void create_product_without_image_succeeds_and_returns_expected_fields() throws Exception {
        MvcResult result = multipartCreate(validRequest("laptop-pro"), adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.url").value("laptop-pro"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.prices", hasSize(1)))
                .andExpect(jsonPath("$.otherImages", hasSize(0)))
                .andExpect(jsonPath("$.mainImage", nullValue()))
                .andReturn();

        String code = json(result).get("code").asText();
        assertTrue(code.startsWith(categoryId + "-"), "code must be prefixed with the product's categoryId");
    }

    @Test
    void create_product_with_main_image_stores_base64() throws Exception {
        mockMvc.perform(multipart("/products")
                        .part(jsonPart("data", validRequest("camera-x1")))
                        .part(imagePart("image"))
                        .param("altText", "front view")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mainImage.altText").value("front view"))
                .andExpect(jsonPath("$.mainImage.imageData").isNotEmpty());
    }

    @Test
    void create_product_missing_required_fields_returns_400() throws Exception {
        mockMvc.perform(multipart("/products")
                        .part(jsonPart("data", new CreateProductRequestDto()))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void create_product_with_unknown_category_returns_404() throws Exception {
        CreateProductRequestDto req = validRequest("ghost-product");
        req.setCategoryId(99999L);

        mockMvc.perform(multipart("/products")
                        .part(jsonPart("data", req))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    void create_product_with_duplicate_url_returns_conflict() throws Exception {
        multipartCreate(validRequest("shared-url"), adminToken).andExpect(status().isOk());

        mockMvc.perform(multipart("/products")
                        .part(jsonPart("data", validRequest("shared-url")))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_URL_ALREADY_EXISTS"));
    }

    @Test
    void create_product_without_auth_returns_401() throws Exception {
        mockMvc.perform(multipart("/products")
                        .part(jsonPart("data", validRequest("no-auth-product"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_product_with_user_role_returns_403() throws Exception {
        mockMvc.perform(multipart("/products")
                        .part(jsonPart("data", validRequest("user-forbidden")))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
