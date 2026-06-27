package com.ecommerce.application.integration.checkout;

import com.ecommerce.persistence.entity.enumeration.Province;
import com.ecommerce.persistence.entity.enumeration.VariantType;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CheckoutITest extends AbstractCheckoutITest {

    @Test
    void checkout_creates_pending_order_and_clears_cart_without_touching_stock() throws Exception {
        Long productId = createActiveProduct("laptop", 10, 500);
        addToCart(userToken, productId, VariantType.COLOR, 2);
        long addressId = createAddressAndGetId(userToken, Province.TEHRAN);

        MvcResult result = checkout(userToken, addressId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId").value(productId))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].lineTotal").value(200.0))
                .andExpect(jsonPath("$.subtotal").value(200.0))
                .andExpect(jsonPath("$.totalWeightGram").value(1000))
                .andExpect(jsonPath("$.shippingZone").value("INTRA_PROVINCE"))
                .andExpect(jsonPath("$.shippingCost").value(183000.0))
                .andExpect(jsonPath("$.totalAmount").value(183200.0))
                .andExpect(jsonPath("$.province").value("TEHRAN"))
                .andExpect(jsonPath("$.recipientFirstName").value("Ali"))
                .andReturn();

        long orderId = json(result).get("id").asLong();

        // cart is emptied at checkout
        mockMvc.perform(withAuth(get("/api/cart"), userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));

        // stock is NOT decremented by an unpaid order
        assertEquals(10, inventoryOf(productId));

        // order is retrievable
        mockMvc.perform(withAuth(get("/api/orders/{id}", orderId), userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
        listOrders(userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void checkout_to_adjacent_province_uses_adjacent_tariff() throws Exception {
        Long productId = createActiveProduct("phone", 10, 300);
        addToCart(userToken, productId, VariantType.COLOR, 1);
        long addressId = createAddressAndGetId(userToken, Province.ALBORZ);

        checkout(userToken, addressId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shippingZone").value("ADJACENT_PROVINCE"))
                .andExpect(jsonPath("$.shippingCost").value(260000.0));
    }

    @Test
    void checkout_with_heavy_cart_uses_over_threshold_tariff() throws Exception {
        Long productId = createActiveProduct("anvil", 10, 600);
        addToCart(userToken, productId, VariantType.COLOR, 2); // 1200g > 1000g threshold
        long addressId = createAddressAndGetId(userToken, Province.TEHRAN);

        checkout(userToken, addressId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWeightGram").value(1200))
                .andExpect(jsonPath("$.shippingZone").value("INTRA_PROVINCE"))
                .andExpect(jsonPath("$.shippingCost").value(570000.0));
    }

    @Test
    void checkout_with_empty_cart_returns_409() throws Exception {
        long addressId = createAddressAndGetId(userToken, Province.TEHRAN);

        checkout(userToken, addressId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("EMPTY_CART"));
    }

    @Test
    void checkout_with_unknown_address_returns_404() throws Exception {
        Long productId = createActiveProduct("watch", 10, 200);
        addToCart(userToken, productId, VariantType.COLOR, 1);

        checkout(userToken, 999999L)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ADDRESS_NOT_FOUND"));
    }

    @Test
    void checkout_with_another_users_address_returns_404() throws Exception {
        Long productId = createActiveProduct("tablet", 10, 200);
        addToCart(userToken, productId, VariantType.COLOR, 1);

        String otherToken = registerAndLogin(newMobile());
        long otherAddressId = createAddressAndGetId(otherToken, Province.TEHRAN);

        checkout(userToken, otherAddressId)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ADDRESS_NOT_FOUND"));
    }

    @Test
    void checkout_without_auth_returns_401() throws Exception {
        mockMvc.perform(post("/api/checkout")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void user_cannot_read_another_users_order() throws Exception {
        Long productId = createActiveProduct("camera", 10, 200);
        addToCart(userToken, productId, VariantType.COLOR, 1);
        long addressId = createAddressAndGetId(userToken, Province.TEHRAN);
        MvcResult result = checkout(userToken, addressId).andExpect(status().isOk()).andReturn();
        long orderId = json(result).get("id").asLong();

        String otherToken = registerAndLogin(newMobile());
        mockMvc.perform(withAuth(get("/api/orders/{id}", orderId), otherToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ORDER_NOT_FOUND"));
    }
}
