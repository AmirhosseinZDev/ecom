package com.ecommerce.application.integration;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Authenticated change-password flow ({@code /user/change-password}, JWT-protected, no OTP).
 * Verifies the happy path actually rotates the credential, the confirm-password guard, and that the
 * endpoint is genuinely protected.
 */
class ChangePasswordFlowITest extends AbstractIntegrationITest {

    private static final String NEW_PASSWORD = "N3wStr0ng!";

    // 1 — changing the password rotates the credential: the old one stops working, the new one works.
    @Test
    void change_password_rotates_credential() throws Exception {
        String mobile = newMobile();
        String token = registerAndLogin(mobile);

        postJson("/user/change-password",
                Map.of("newPassword", NEW_PASSWORD, "confirmPassword", NEW_PASSWORD), token)
                .andExpect(status().isOk());

        postJson("/user/login", Map.of("mobileNumber", mobile, "password", DEFAULT_PASSWORD))
                .andExpect(status().isUnauthorized());
        postJson("/user/login", Map.of("mobileNumber", mobile, "password", NEW_PASSWORD))
                .andExpect(status().isOk());
    }

    // 2 — new/confirm mismatch is rejected and leaves the original password intact.
    @Test
    void change_password_with_mismatched_confirmation_is_rejected() throws Exception {
        String mobile = newMobile();
        String token = registerAndLogin(mobile);

        postJson("/user/change-password",
                Map.of("newPassword", NEW_PASSWORD, "confirmPassword", "Different1!"), token)
                .andExpect(status().isBadRequest());

        // Original password still works.
        postJson("/user/login", Map.of("mobileNumber", mobile, "password", DEFAULT_PASSWORD))
                .andExpect(status().isOk());
    }

    // 3 — blank fields fail request validation.
    @Test
    void change_password_with_blank_fields_is_rejected() throws Exception {
        String token = registerAndLogin(newMobile());

        postJson("/user/change-password", Map.of("newPassword", "", "confirmPassword", ""), token)
                .andExpect(status().isBadRequest());
    }

    // 4 — the endpoint is protected: no token is rejected before reaching the controller.
    @Test
    void change_password_without_token_is_rejected() throws Exception {
        postJson("/user/change-password",
                Map.of("newPassword", NEW_PASSWORD, "confirmPassword", NEW_PASSWORD))
                .andExpect(status().is4xxClientError());
    }

    // 5 — a malformed JWT is rejected as unauthorized.
    @Test
    void change_password_with_invalid_token_is_unauthorized() throws Exception {
        postJson("/user/change-password",
                Map.of("newPassword", NEW_PASSWORD, "confirmPassword", NEW_PASSWORD), "not-a-real-jwt")
                .andExpect(status().isUnauthorized());
    }
}
