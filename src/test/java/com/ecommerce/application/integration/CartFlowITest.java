package com.ecommerce.application.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end cart flow ({@code /cart}, JWT-protected). Seeds a small product catalog directly through
 * JDBC, then drives the cart through the real HTTP stack: add, accumulate, increment, decrement,
 * remove, clear, plus the inventory / purchasability guards and the security boundary.
 */
class CartFlowITest extends AbstractIntegrationITest {

    private static final long IN_STOCK_PRODUCT = 1000L;
    private static final long SECOND_PRODUCT = 1001L;
    private static final long INACTIVE_PRODUCT = 1002L;
    private static final long SCARCE_PRODUCT = 1003L;

    @BeforeEach
    void seedCatalog() {
        jdbcTemplate.execute("TRUNCATE TABLE cart, cart_item, product, category RESTART IDENTITY CASCADE");
        jdbcTemplate.update("INSERT INTO category (id, name) VALUES (1, 'Electronics')");
        insertProduct(IN_STOCK_PRODUCT, "P-IN-STOCK", "ACTIVE", "IN_STOCK", 50);
        insertProduct(SECOND_PRODUCT, "P-SECOND", "ACTIVE", "IN_STOCK", 50);
        insertProduct(INACTIVE_PRODUCT, "P-INACTIVE", "INACTIVE", "IN_STOCK", 50);
        insertProduct(SCARCE_PRODUCT, "P-SCARCE", "ACTIVE", "LOW_STOCK", 2);
        jdbcTemplate.update("INSERT INTO product_price (product_id, price, variant_type) VALUES (?, ?, ?)",
                IN_STOCK_PRODUCT, 100, "COLOR");
    }

    // 1 — adding a product makes it appear in the cart with its quantity and product details.
    @Test
    void added_product_appears_in_cart() throws Exception {
        String token = registerAndLogin(newMobile());

        addItem(token, IN_STOCK_PRODUCT, 2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(IN_STOCK_PRODUCT))
                .andExpect(jsonPath("$.items[0].name").value("name-" + IN_STOCK_PRODUCT))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.totalQuantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(200));

        getCart(token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value(IN_STOCK_PRODUCT))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    // 2 — adding the same product again accumulates the quantity rather than duplicating the line.
    @Test
    void adding_the_same_product_accumulates_quantity() throws Exception {
        String token = registerAndLogin(newMobile());

        addItem(token, IN_STOCK_PRODUCT, 2).andExpect(status().isOk());
        addItem(token, IN_STOCK_PRODUCT, 3)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(5));
    }

    // 3 — multiple distinct products coexist in the same cart.
    @Test
    void multiple_products_coexist() throws Exception {
        String token = registerAndLogin(newMobile());

        addItem(token, IN_STOCK_PRODUCT, 1).andExpect(status().isOk());
        addItem(token, SECOND_PRODUCT, 4)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalQuantity").value(5));
    }

    // 4 — increment / decrement adjust the quantity by one.
    @Test
    void increment_and_decrement_adjust_quantity() throws Exception {
        String token = registerAndLogin(newMobile());
        addItem(token, IN_STOCK_PRODUCT, 2).andExpect(status().isOk());

        increment(token, IN_STOCK_PRODUCT)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(3));

        decrement(token, IN_STOCK_PRODUCT)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    // 5 — decrementing the last unit removes the product from the cart.
    @Test
    void decrement_to_zero_removes_product() throws Exception {
        String token = registerAndLogin(newMobile());
        addItem(token, IN_STOCK_PRODUCT, 1).andExpect(status().isOk());

        decrement(token, IN_STOCK_PRODUCT)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    // 6 — explicitly setting a quantity replaces the current value.
    @Test
    void update_quantity_sets_absolute_value() throws Exception {
        String token = registerAndLogin(newMobile());
        addItem(token, IN_STOCK_PRODUCT, 2).andExpect(status().isOk());

        updateQuantity(token, IN_STOCK_PRODUCT, 9)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(9));
    }

    // 7 — removing a product drops its line from the cart.
    @Test
    void remove_drops_product() throws Exception {
        String token = registerAndLogin(newMobile());
        addItem(token, IN_STOCK_PRODUCT, 2).andExpect(status().isOk());
        addItem(token, SECOND_PRODUCT, 1).andExpect(status().isOk());

        mockMvc.perform(withAuth(delete("/cart/items/" + IN_STOCK_PRODUCT), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(SECOND_PRODUCT));
    }

    // 8 — clearing empties the whole cart.
    @Test
    void clear_empties_cart() throws Exception {
        String token = registerAndLogin(newMobile());
        addItem(token, IN_STOCK_PRODUCT, 2).andExpect(status().isOk());
        addItem(token, SECOND_PRODUCT, 1).andExpect(status().isOk());

        mockMvc.perform(withAuth(delete("/cart"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalQuantity").value(0));
    }

    // 9 — a fresh user simply sees an empty cart.
    @Test
    void new_user_has_empty_cart() throws Exception {
        String token = registerAndLogin(newMobile());

        getCart(token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    // 10 — requesting more than the available inventory is rejected.
    @Test
    void exceeding_inventory_is_rejected() throws Exception {
        String token = registerAndLogin(newMobile());

        addItem(token, SCARCE_PRODUCT, 5)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_INVENTORY"));
    }

    // 11 — an inactive product cannot be added.
    @Test
    void inactive_product_is_rejected() throws Exception {
        String token = registerAndLogin(newMobile());

        addItem(token, INACTIVE_PRODUCT, 1)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_PURCHASABLE"));
    }

    // 12 — adding an unknown product yields a not-found error.
    @Test
    void unknown_product_is_rejected() throws Exception {
        String token = registerAndLogin(newMobile());

        addItem(token, 99999L, 1)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));
    }

    // 13 — carts are per-user: one user's items never leak into another's cart.
    @Test
    void carts_are_isolated_per_user() throws Exception {
        String tokenA = registerAndLogin(newMobile());
        String tokenB = registerAndLogin(newMobile());

        addItem(tokenA, IN_STOCK_PRODUCT, 3).andExpect(status().isOk());

        MvcResult result = getCart(tokenB).andExpect(status().isOk()).andReturn();
        JsonNode body = json(result);
        org.junit.jupiter.api.Assertions.assertEquals(0, body.get("items").size());
    }

    // 14 — the cart endpoints are protected: no token is rejected.
    @Test
    void cart_requires_authentication() throws Exception {
        mockMvc.perform(get("/cart")).andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------------------------------

    private void insertProduct(long id, String code, String status, String inventoryStatus, int inventoryCount) {
        jdbcTemplate.update(
                "INSERT INTO product (id, code, category_id, url, name, inventory_status, status, inventory_count) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id, code, 1L, "url-" + id, "name-" + id, inventoryStatus, status, inventoryCount);
    }

    private ResultActions getCart(String token) throws Exception {
        return mockMvc.perform(withAuth(get("/cart"), token));
    }

    private ResultActions addItem(String token, long productId, int quantity) throws Exception {
        return postJson("/cart/items", Map.of("productId", productId, "quantity", quantity), token);
    }

    private ResultActions increment(String token, long productId) throws Exception {
        return mockMvc.perform(withAuth(post("/cart/items/" + productId + "/increment"), token));
    }

    private ResultActions decrement(String token, long productId) throws Exception {
        return mockMvc.perform(withAuth(post("/cart/items/" + productId + "/decrement"), token));
    }

    private ResultActions updateQuantity(String token, long productId, int quantity) throws Exception {
        return mockMvc.perform(withAuth(put("/cart/items/" + productId), token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("quantity", quantity))));
    }
}
