package com.ecommerce.application.integration.product;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductUploadImageITest extends AbstractProductITest {

    @Test
    void upload_main_image_stores_base64_and_returns_updated_product() throws Exception {
        Long id = createProductAndGetId("cam-product");

        mockMvc.perform(multipart("/products/{id}/images", id)
                        .part(imagePart("image"))
                        .param("type", "MAIN")
                        .param("altText", "product shot")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mainImage.altText").value("product shot"))
                .andExpect(jsonPath("$.mainImage.imageData").isNotEmpty());
    }

    @Test
    void upload_main_image_twice_replaces_the_existing_image() throws Exception {
        Long id = createProductAndGetId("replace-img-product");

        mockMvc.perform(multipart("/products/{id}/images", id)
                        .part(imagePart("image"))
                        .param("type", "MAIN")
                        .param("altText", "first")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mainImage.altText").value("first"));

        mockMvc.perform(multipart("/products/{id}/images", id)
                        .part(imagePart("image"))
                        .param("type", "MAIN")
                        .param("altText", "second")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mainImage.altText").value("second"));
    }

    @Test
    void upload_other_image_appends_each_time() throws Exception {
        Long id = createProductAndGetId("gallery-product");

        mockMvc.perform(multipart("/products/{id}/images", id)
                        .part(imagePart("image"))
                        .param("type", "OTHER")
                        .param("altText", "angle 1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.otherImages", hasSize(1)));

        mockMvc.perform(multipart("/products/{id}/images", id)
                        .part(imagePart("image"))
                        .param("type", "OTHER")
                        .param("altText", "angle 2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.otherImages", hasSize(2)))
                .andExpect(jsonPath("$.otherImages[0].id").isNumber())
                .andExpect(jsonPath("$.otherImages[1].id").isNumber());
    }

    @Test
    void upload_image_for_unknown_product_returns_404() throws Exception {
        mockMvc.perform(multipart("/products/{id}/images", 99999L)
                        .part(imagePart("image"))
                        .param("type", "MAIN")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void upload_image_without_auth_returns_401() throws Exception {
        Long id = createProductAndGetId("img-no-auth");

        mockMvc.perform(multipart("/products/{id}/images", id)
                        .part(imagePart("image"))
                        .param("type", "MAIN"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void upload_image_with_user_role_returns_403() throws Exception {
        Long id = createProductAndGetId("img-user-forbidden");

        mockMvc.perform(multipart("/products/{id}/images", id)
                        .part(imagePart("image"))
                        .param("type", "MAIN")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
