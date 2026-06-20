package com.ecommerce.application.config.security;

public class PublicEndPoint {

    static final String[] POST_PUBLIC_ENDPOINTS = {
            "/user/signup-ticket",
            "/user/signup-ticket/validation",
            "/user/signup",
            "/user/check-registration",
            "/user/login",
            "/user/login-ticket",
            "/user/login-ticket/validation"
    };

    static final String[] GET_PUBLIC_ENDPOINTS = {
            "/v3/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/products",
            "/products/**"
    };
}
