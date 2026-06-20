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

class CartService_removeItemAndClearUTest extends BaseCartServiceUTest {

    @Test
    void remove_drops_the_targeted_line_only() {
        Product product = product(PRODUCT_ID, 10);
        Cart cart = cart(1L, USER_ID);
        addItemToCart(cart, item(50L, PRODUCT_ID, VariantType.COLOR, 1, BigDecimal.valueOf(100), null));
        addItemToCart(cart, item(51L, PRODUCT_ID, VariantType.SIZE, 1, BigDecimal.valueOf(100), null));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        stubProductsForDto(product);

        CartResponseDto response = cartService.removeItem(USER_ID, 50L);

        assertEquals(1, response.getItems().size());
        assertEquals(51L, response.getItems().getFirst().getId());
    }

    @Test
    void remove_unknown_item_throws_cart_item_not_found() {
        Cart cart = cart(1L, USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.removeItem(USER_ID, 50L));

        assertEquals(ECOMErrorType.CART_ITEM_NOT_FOUND, exception.getEcomErrorType());
    }

    @Test
    void clear_empties_all_lines() {
        Cart cart = cart(1L, USER_ID);
        addItemToCart(cart, item(50L, PRODUCT_ID, VariantType.COLOR, 1, BigDecimal.valueOf(100), null));
        addItemToCart(cart, item(51L, PRODUCT_ID, VariantType.SIZE, 2, BigDecimal.valueOf(100), null));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

        CartResponseDto response = cartService.clearCart(USER_ID);

        assertTrue(response.getItems().isEmpty());
        assertEquals(0, response.getTotalQuantity());
    }

    @Test
    void clear_without_cart_is_a_no_op_returning_empty_cart() {
        stubLazyCreatedCart();

        CartResponseDto response = cartService.clearCart(USER_ID);

        assertTrue(response.getItems().isEmpty());
        assertEquals(0, response.getTotalQuantity());
    }
}
