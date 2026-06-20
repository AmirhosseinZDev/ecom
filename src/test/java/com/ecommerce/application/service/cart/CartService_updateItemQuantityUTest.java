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
import static org.mockito.Mockito.when;

class CartService_updateItemQuantityUTest extends BaseCartServiceUTest {

    @Test
    void update_sets_absolute_quantity() {
        Product product = product(PRODUCT_ID, 10);
        Cart cart = cart(1L, USER_ID);
        addItemToCart(cart, item(50L, PRODUCT_ID, VariantType.COLOR, 2, BigDecimal.valueOf(100), null));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        stubProductsForDto(product);

        CartResponseDto response = cartService.updateItemQuantity(USER_ID, 50L, 7);

        assertEquals(7, response.getItems().getFirst().getQuantity());
    }

    @Test
    void update_above_inventory_throws_insufficient_stock() {
        Product product = product(PRODUCT_ID, 5);
        Cart cart = cart(1L, USER_ID);
        addItemToCart(cart, item(50L, PRODUCT_ID, VariantType.COLOR, 2, BigDecimal.valueOf(100), null));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.updateItemQuantity(USER_ID, 50L, 6));

        assertEquals(ECOMErrorType.INSUFFICIENT_STOCK, exception.getEcomErrorType());
    }

    @Test
    void update_unknown_item_throws_cart_item_not_found() {
        Cart cart = cart(1L, USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.updateItemQuantity(USER_ID, 50L, 3));

        assertEquals(ECOMErrorType.CART_ITEM_NOT_FOUND, exception.getEcomErrorType());
    }
}
