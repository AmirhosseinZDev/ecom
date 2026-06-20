package com.ecommerce.application.service.cart;

import com.ecommerce.application.api.dto.cart.CartResponseDto;
import com.ecommerce.application.api.exception.ECOMErrorType;
import com.ecommerce.application.api.exception.EcommerceException;
import com.ecommerce.persistence.entity.Cart;
import com.ecommerce.persistence.entity.Product;
import com.ecommerce.persistence.entity.enumeration.VariantType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class CartService_decrementItemUTest extends BaseCartServiceUTest {

    @Test
    void decrement_reduces_quantity_by_one() {
        Product product = product(PRODUCT_ID, 10);
        Cart cart = cart(1L, USER_ID);
        addItemToCart(cart, item(50L, PRODUCT_ID, VariantType.COLOR, 3, BigDecimal.valueOf(100), null));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        stubProductsForDto(product);

        CartResponseDto response = cartService.decrementItem(USER_ID, 50L);

        assertEquals(2, response.getItems().getFirst().getQuantity());
    }

    @Test
    void decrement_to_zero_removes_the_line() {
        Cart cart = cart(1L, USER_ID);
        addItemToCart(cart, item(50L, PRODUCT_ID, VariantType.COLOR, 1, BigDecimal.valueOf(100), null));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

        CartResponseDto response = cartService.decrementItem(USER_ID, 50L);

        assertTrue(response.getItems().isEmpty());
        assertEquals(0, response.getTotalQuantity());
        assertEquals(0, response.getTotalPrice().compareTo(BigDecimal.ZERO));
    }

    @Test
    void decrement_unknown_item_throws_cart_item_not_found() {
        Cart cart = cart(1L, USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.decrementItem(USER_ID, 50L));

        assertEquals(ECOMErrorType.CART_ITEM_NOT_FOUND, exception.getEcomErrorType());
    }
}
