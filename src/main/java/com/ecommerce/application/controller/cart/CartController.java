package com.ecommerce.application.controller.cart;

import com.ecommerce.application.api.dto.cart.AddCartItemRequestDto;
import com.ecommerce.application.api.dto.cart.CartResponseDto;
import com.ecommerce.application.api.dto.cart.UpdateCartItemQuantityRequestDto;
import com.ecommerce.application.config.security.UserDetailsDto;
import com.ecommerce.application.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponseDto getCart(Authentication authentication) {
        return cartService.getCart(currentUserId(authentication));
    }

    @PostMapping(value = "/items", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponseDto addItem(@RequestBody AddCartItemRequestDto requestDto, Authentication authentication) {
        return cartService.addItem(currentUserId(authentication), requestDto);
    }

    @PutMapping(value = "/items/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponseDto updateQuantity(@PathVariable Long productId,
            @RequestBody UpdateCartItemQuantityRequestDto requestDto, Authentication authentication) {
        return cartService.updateQuantity(currentUserId(authentication), productId, requestDto);
    }

    @PostMapping(value = "/items/{productId}/increment", produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponseDto incrementItem(@PathVariable Long productId, Authentication authentication) {
        return cartService.incrementItem(currentUserId(authentication), productId);
    }

    @PostMapping(value = "/items/{productId}/decrement", produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponseDto decrementItem(@PathVariable Long productId, Authentication authentication) {
        return cartService.decrementItem(currentUserId(authentication), productId);
    }

    @DeleteMapping(value = "/items/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponseDto removeItem(@PathVariable Long productId, Authentication authentication) {
        return cartService.removeItem(currentUserId(authentication), productId);
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponseDto clear(Authentication authentication) {
        return cartService.clear(currentUserId(authentication));
    }

    private Long currentUserId(Authentication authentication) {
        return ((UserDetailsDto) authentication.getPrincipal()).getId();
    }
}
