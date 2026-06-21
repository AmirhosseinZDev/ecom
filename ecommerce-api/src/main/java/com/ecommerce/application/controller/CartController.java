package com.ecommerce.application.controller;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
        return cartService.getCart(userId(authentication));
    }

    @PostMapping(value = "/items", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponseDto addItem(@RequestBody AddCartItemRequestDto requestDto, Authentication authentication) {
        return cartService.addItem(userId(authentication), requestDto);
    }

    @PatchMapping(value = "/items/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponseDto updateItemQuantity(@PathVariable Long itemId,
            @RequestBody UpdateCartItemQuantityRequestDto requestDto, Authentication authentication) {
        return cartService.updateItemQuantity(userId(authentication), itemId, requestDto.getQuantity());
    }

    @PostMapping(value = "/items/{itemId}/increment", produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponseDto incrementItem(@PathVariable Long itemId, Authentication authentication) {
        return cartService.incrementItem(userId(authentication), itemId);
    }

    @PostMapping(value = "/items/{itemId}/decrement", produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponseDto decrementItem(@PathVariable Long itemId, Authentication authentication) {
        return cartService.decrementItem(userId(authentication), itemId);
    }

    @DeleteMapping(value = "/items/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponseDto removeItem(@PathVariable Long itemId, Authentication authentication) {
        return cartService.removeItem(userId(authentication), itemId);
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CartResponseDto clearCart(Authentication authentication) {
        return cartService.clearCart(userId(authentication));
    }

    private Long userId(Authentication authentication) {
        return ((UserDetailsDto) authentication.getPrincipal()).getId();
    }
}
