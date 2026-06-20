package com.ecommerce.application.service.cart;

import com.ecommerce.application.api.dto.cart.CartResponseDto;
import com.ecommerce.persistence.entity.Cart;
import com.ecommerce.persistence.entity.Product;
import com.ecommerce.persistence.entity.enumeration.VariantType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CartService_getCartUTest extends BaseCartServiceUTest {

    @Test
    void get_returns_existing_cart_with_totals() {
        Product product = product(PRODUCT_ID, 10);
        Cart cart = cart(1L, USER_ID);
        addItemToCart(cart, item(50L, PRODUCT_ID, VariantType.COLOR, 2, BigDecimal.valueOf(100), null));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        stubProductsForDto(product);

        CartResponseDto response = cartService.getCart(USER_ID);

        assertEquals(1, response.getItems().size());
        assertEquals(2, response.getTotalQuantity());
        assertEquals("Product " + PRODUCT_ID, response.getItems().getFirst().getProductName());
    }

    @Test
    void get_creates_empty_cart_when_none_exists() {
        stubLazyCreatedCart();

        CartResponseDto response = cartService.getCart(USER_ID);

        assertTrue(response.getItems().isEmpty());
        assertEquals(0, response.getTotalQuantity());
        assertEquals(USER_ID, response.getUserId());
        verify(cartFactory).createNew(USER_ID);
    }
}
