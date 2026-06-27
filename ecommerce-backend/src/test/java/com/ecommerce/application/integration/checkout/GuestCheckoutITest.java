package com.ecommerce.application.integration.checkout;

import com.ecommerce.application.api.dto.address.AddressRequestDto;
import com.ecommerce.application.api.dto.order.GuestCheckoutRequestDto;
import com.ecommerce.application.api.dto.order.GuestItemRequestDto;
import com.ecommerce.persistence.entity.enumeration.Province;
import com.ecommerce.persistence.entity.enumeration.VariantType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GuestCheckoutITest extends AbstractCheckoutITest {

    private GuestCheckoutRequestDto guestRequest(String mobile, Long productId, int quantity, Province province) {
        GuestCheckoutRequestDto dto = new GuestCheckoutRequestDto();
        dto.setFirstName("Sara");
        dto.setLastName("Ahmadi");
        dto.setMobile(mobile);
        dto.setEmail("sara@example.com");
        dto.setNationalId("0011223344");

        AddressRequestDto address = addressRequest(province);
        address.setRecipientFirstName("Sara");
        address.setRecipientLastName("Ahmadi");
        address.setRecipientMobile(mobile);
        dto.setAddress(address);

        GuestItemRequestDto item = new GuestItemRequestDto();
        item.setProductId(productId);
        item.setVariantType(VariantType.COLOR);
        item.setQuantity(quantity);
        dto.setItems(List.of(item));
        return dto;
    }

    @Test
    void guest_checkout_creates_unregistered_user_address_and_order() throws Exception {
        Long productId = createActiveProduct("guest-laptop", 10, 500);
        String mobile = newMobile();

        guestCheckout(guestRequest(mobile, productId, 2, Province.TEHRAN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.subtotal").value(200.0))
                .andExpect(jsonPath("$.shippingZone").value("INTRA_PROVINCE"))
                .andExpect(jsonPath("$.totalAmount").value(183200.0))
                .andExpect(jsonPath("$.recipientFirstName").value("Sara"));

        // a guest AppUser was persisted with isRegistered = false
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM app_user WHERE mobile = ?", Long.class, mobile);
        Boolean isRegistered = jdbcTemplate.queryForObject(
                "SELECT is_registered FROM app_user WHERE id = ?", Boolean.class, userId);
        assertEquals(false, isRegistered);

        // address + order persisted under the guest user
        Integer addressCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_address WHERE user_id = ?", Integer.class, userId);
        assertEquals(1, addressCount);
        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE user_id = ?", Integer.class, userId);
        assertEquals(1, orderCount);

        // an unplaced order doesn't change stock (no payment/deduction step exists)
        assertEquals(10, inventoryOf(productId));
    }

    @Test
    void guest_can_later_complete_signup_for_the_same_mobile() throws Exception {
        Long productId = createActiveProduct("guest-phone", 10, 300);
        String mobile = newMobile();

        guestCheckout(guestRequest(mobile, productId, 1, Province.TEHRAN))
                .andExpect(status().isOk());

        // The guest mobile is not yet registered.
        clearSignupTicketState(mobile);
        // Completing the signup flow promotes the same record to a registered account.
        register(mobile);

        Boolean isRegistered = jdbcTemplate.queryForObject(
                "SELECT is_registered FROM app_user WHERE mobile = ?", Boolean.class, mobile);
        assertEquals(true, isRegistered);
        // still a single row for that mobile (the guest record was upgraded, not duplicated)
        assertEquals(1, countUsers(mobile));
    }

    @Test
    void guest_checkout_with_already_registered_mobile_is_rejected() throws Exception {
        Long productId = createActiveProduct("guest-watch", 10, 300);
        String mobile = newMobile();
        register(mobile); // becomes a registered account

        guestCheckout(guestRequest(mobile, productId, 1, Province.TEHRAN))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("USER_ALREADY_EXISTS"));
    }

    @Test
    void guest_checkout_with_unknown_product_returns_404() throws Exception {
        guestCheckout(guestRequest(newMobile(), 999999L, 1, Province.TEHRAN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void guest_checkout_without_items_returns_400() throws Exception {
        GuestCheckoutRequestDto dto = guestRequest(newMobile(), 1L, 1, Province.TEHRAN);
        dto.setItems(List.of());

        guestCheckout(dto)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void guest_checkout_over_stock_returns_409() throws Exception {
        Long productId = createActiveProduct("guest-rare", 2, 300);

        guestCheckout(guestRequest(newMobile(), productId, 5, Province.TEHRAN))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_STOCK"));
    }
}
