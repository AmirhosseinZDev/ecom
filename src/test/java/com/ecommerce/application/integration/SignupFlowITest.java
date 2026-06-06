package com.ecommerce.application.integration;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Signup + password-login flow:
 * send ticket -> validate ticket (OTP captured from the mock SMS server) -> signup -> login.
 *
 * <p>Covers the happy path plus request validation, wrong-OTP handling, OTP-failure blocking,
 * resend cooldown, duplicate signup and signup-token / login failures.
 */
class SignupFlowITest extends AbstractIntegrationITest {

    // 1 — full registration then login, asserting the SMS call and the persisted (hashed) user.
    @Test
    void full_signup_then_login_succeeds_and_persists_hashed_user() throws Exception {
        String mobile = newMobile();

        sendSignupTicket(mobile)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketTTLInSecond").isNumber());
        assertEquals(1, smsRequestCount(), "exactly one OTP SMS should be sent");

        String otp = captureLastOtp();
        MvcResult validation = postJson("/user/signup-ticket/validation",
                Map.of("ticket", otp, "mobileNumber", mobile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signupToken").isNotEmpty())
                .andReturn();
        String signupToken = json(validation).get("signupToken").asText();

        signup(signupToken).andExpect(status().isOk());

        Map<String, Object> stored = jdbcTemplate.queryForMap(
                "SELECT username, mobile, password, role, is_enabled, is_registered "
                        + "FROM app_user WHERE mobile = ?", mobile);
        assertEquals(mobile, stored.get("username"), "username is the mobile number");
        assertEquals(mobile, stored.get("mobile"));
        assertEquals("ROLE_APP_USER", stored.get("role"));
        assertEquals(true, stored.get("is_enabled"));
        assertEquals(true, stored.get("is_registered"));
        assertNotEquals(DEFAULT_PASSWORD, stored.get("password"), "password must not be stored in plain text");
        assertTrue(((String) stored.get("password")).startsWith("$2"), "password must be BCrypt-hashed");

        MvcResult login = postJson("/user/login", Map.of("mobileNumber", mobile, "password", DEFAULT_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ROLE_APP_USER"))
                .andReturn();
        assertNotNull(json(login).get("token").asText());
    }

    // 2 — request-validation failures at ticket send produce 400 and never call the SMS provider.
    @Test
    void signup_ticket_with_malformed_mobile_is_rejected_without_sending_sms() throws Exception {
        postJson("/user/signup-ticket", Map.of("mobileNumber", "not-a-mobile"))
                .andExpect(status().isBadRequest());
        assertEquals(0, smsRequestCount());
    }

    @Test
    void signup_ticket_with_missing_mobile_is_rejected_without_sending_sms() throws Exception {
        postJson("/user/signup-ticket", Map.of())
                .andExpect(status().isBadRequest());
        assertEquals(0, smsRequestCount());
    }

    // 3 — wrong OTP: 400 and no signup token issued.
    @Test
    void validate_signup_ticket_with_wrong_otp_is_rejected() throws Exception {
        String mobile = newMobile();
        sendSignupTicket(mobile).andExpect(status().isOk());

        postJson("/user/signup-ticket/validation", Map.of("ticket", "000000", "mobileNumber", mobile))
                .andExpect(status().isBadRequest());
    }

    // 4 — repeated wrong OTPs block the mobile number; a further send is then rejected too.
    @Test
    void repeated_wrong_otp_blocks_mobile_number() throws Exception {
        String mobile = newMobile();
        sendSignupTicket(mobile).andExpect(status().isOk());

        for (int attempt = 0; attempt < 5; attempt++) {
            postJson("/user/signup-ticket/validation", Map.of("ticket", "111111", "mobileNumber", mobile))
                    .andExpect(status().is4xxClientError());
        }

        // The mobile is now blocked, so even a fresh ticket request is rejected.
        sendSignupTicket(mobile).andExpect(status().is4xxClientError());
    }

    // 5 — resend cooldown: a second send for the same mobile inside the TTL window is rejected and
    // sends no second SMS. The service raises SendTicketTimeLimitNotExceededException (an unmapped
    // RuntimeException), which the global advice surfaces as 500.
    @Test
    void resending_ticket_within_cooldown_is_rejected() throws Exception {
        String mobile = newMobile();
        sendSignupTicket(mobile).andExpect(status().isOk());

        sendSignupTicket(mobile).andExpect(status().is5xxServerError());
        assertEquals(1, smsRequestCount(), "no second OTP SMS should be sent during the cooldown window");
    }

    // 6 — when the SMS provider fails, the send is reported as an error and the OTP is rolled back so
    // a fresh send is immediately possible.
    @Test
    void sms_provider_failure_rolls_back_the_ticket() throws Exception {
        String mobile = newMobile();
        stubSmsFailure();

        sendSignupTicket(mobile).andExpect(status().is5xxServerError());
        assertEquals(1, smsRequestCount());

        // Ticket + cooldown were rolled back, so a retry (now succeeding) is allowed straight away.
        stubSmsSuccess();
        sendSignupTicket(mobile).andExpect(status().isOk());
        assertEquals(2, smsRequestCount());
    }

    // 7 — duplicate signup is rejected and leaves a single user row.
    @Test
    void duplicate_signup_is_rejected() throws Exception {
        String mobile = newMobile();
        register(mobile);

        // Obtain a fresh, valid signup token for the same (already-registered) mobile.
        clearSignupTicketState(mobile);
        sendSignupTicket(mobile).andExpect(status().isOk());
        String signupToken = validateSignupTicket(mobile, captureLastOtp());

        signup(signupToken).andExpect(status().isBadRequest());

        assertEquals(1, countUsers(mobile), "the duplicate signup must not create a second row");
    }

    // 8 — signup with an unknown / malformed token.
    @Test
    void signup_with_unknown_token_is_rejected() throws Exception {
        signup(UUID.randomUUID().toString()).andExpect(status().isBadRequest());
    }

    @Test
    void signup_with_malformed_token_fails_validation() throws Exception {
        postJson("/user/signup", Map.of("signupToken", "not-a-uuid", "password", DEFAULT_PASSWORD,
                "firstName", DEFAULT_FIRST_NAME, "lastName", DEFAULT_LAST_NAME))
                .andExpect(status().isBadRequest());
    }

    // 9 — login failures.
    @Test
    void login_with_wrong_password_is_unauthorized() throws Exception {
        String mobile = newMobile();
        register(mobile);

        postJson("/user/login", Map.of("mobileNumber", mobile, "password", "WrongPass!"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_for_unknown_user_is_unauthorized() throws Exception {
        postJson("/user/login", Map.of("mobileNumber", newMobile(), "password", DEFAULT_PASSWORD))
                .andExpect(status().isUnauthorized());
    }
}
