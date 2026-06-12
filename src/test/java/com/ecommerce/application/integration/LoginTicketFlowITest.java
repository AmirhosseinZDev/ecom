package com.ecommerce.application.integration;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OTP login flow ({@code /user/login-ticket} + {@code /user/login-ticket/validation}) and the public
 * {@code /user/check-registration} probe. The login OTP is only ever sent to registered users.
 */
class LoginTicketFlowITest extends AbstractIntegrationITest {

    // 1 — a registered user can request a login OTP and exchange it for a working JWT.
    @Test
    void registered_user_logs_in_with_otp_and_receives_a_working_jwt() throws Exception {
        String mobile = newMobile();
        register(mobile);

        postJson("/user/login-ticket", Map.of("mobileNumber", mobile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketTTLInSecond").isNumber());

        String otp = captureLastOtp();
        MvcResult validation = postJson("/user/login-ticket/validation",
                Map.of("mobileNumber", mobile, "ticket", otp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ROLE_APP_USER"))
                .andReturn();
        String token = json(validation).get("token").asText();
        assertNotNull(token);

        // The JWT really authorizes a protected call.
        postJson("/user/change-password",
                Map.of("newPassword", "An0therPass!", "confirmPassword", "An0therPass!"), token)
                .andExpect(status().isOk());
    }

    // 2 — login OTP is refused for a mobile that is not a registered user, and no SMS is sent.
    @Test
    void login_ticket_for_unregistered_mobile_is_rejected_without_sms() throws Exception {
        postJson("/user/login-ticket", Map.of("mobileNumber", newMobile()))
                .andExpect(status().isNotFound());
        assertEquals(0, smsRequestCount());
    }

    // 3 — a mobile that only requested a signup OTP (never completed signup) is still not registered.
    @Test
    void login_ticket_for_started_but_unfinished_signup_is_rejected() throws Exception {
        String mobile = newMobile();
        sendSignupTicket(mobile).andExpect(status().isOk());

        postJson("/user/login-ticket", Map.of("mobileNumber", mobile))
                .andExpect(status().isNotFound());
        assertEquals(1, smsRequestCount(), "only the signup OTP was sent; no login OTP");
    }

    // 4 — a wrong login OTP is rejected.
    @Test
    void validate_login_ticket_with_wrong_otp_is_rejected() throws Exception {
        String mobile = newMobile();
        register(mobile);
        postJson("/user/login-ticket", Map.of("mobileNumber", mobile)).andExpect(status().isOk());

        postJson("/user/login-ticket/validation", Map.of("mobileNumber", mobile, "ticket", "000000"))
                .andExpect(status().isBadRequest());
    }

    // 5 — malformed mobile on the login-ticket request fails validation without an SMS.
    @Test
    void login_ticket_with_malformed_mobile_is_rejected() throws Exception {
        postJson("/user/login-ticket", Map.of("mobileNumber", "0912"))
                .andExpect(status().isBadRequest());
        assertEquals(0, smsRequestCount());
    }

    // 6 — check-registration reflects the persisted state (public endpoint, no auth required).
    @Test
    void check_registration_reflects_user_state() throws Exception {
        String mobile = newMobile();

        MvcResult before = postJson("/user/check-registration", Map.of("mobileNumber", mobile))
                .andExpect(status().isOk())
                .andReturn();
        assertFalse(readRegistrationFlag(json(before)), "unknown mobile is not registered");

        register(mobile);

        MvcResult after = postJson("/user/check-registration", Map.of("mobileNumber", mobile))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(readRegistrationFlag(json(after)), "registered mobile reports true");
    }
}
