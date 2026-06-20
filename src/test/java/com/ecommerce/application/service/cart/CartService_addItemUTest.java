package com.ecommerce.application.service.cart;

import com.ecommerce.application.api.dto.cart.CartResponseDto;
import com.ecommerce.application.api.exception.ECOMErrorType;
import com.ecommerce.application.api.exception.EcommerceException;
import com.ecommerce.persistence.entity.Cart;
import com.ecommerce.persistence.entity.Product;
import com.ecommerce.persistence.entity.enumeration.ProductStatus;
import com.ecommerce.persistence.entity.enumeration.VariantType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CartService_addItemUTest extends BaseCartServiceUTest {

    @Test
    void adding_to_empty_cart_creates_line_with_price_snapshot_and_totals() {
        Product product = product(PRODUCT_ID, 10, ProductStatus.ACTIVE, VariantType.COLOR,
                BigDecimal.valueOf(100), BigDecimal.valueOf(80));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        stubLazyCreatedCart();
        stubProductsForDto(product);

        CartResponseDto response = cartService.addItem(USER_ID, addRequest(PRODUCT_ID, VariantType.COLOR, 2));

        assertEquals(1, response.getItems().size());
        var line = response.getItems().getFirst();
        assertEquals(2, line.getQuantity());
        assertEquals(BigDecimal.valueOf(100), line.getUnitPrice());
        assertEquals(BigDecimal.valueOf(80), line.getDiscountPrice());
        assertEquals(BigDecimal.valueOf(80), line.getEffectivePrice());
        assertEquals(0, line.getLineTotal().compareTo(BigDecimal.valueOf(160)));
        assertEquals(2, response.getTotalQuantity());
        assertEquals(0, response.getTotalPrice().compareTo(BigDecimal.valueOf(160)));
    }

    @Test
    void adding_same_product_and_variant_again_merges_into_one_line() {
        Product product = product(PRODUCT_ID, 10);
        Cart cart = cart(1L, USER_ID);
        addItemToCart(cart, item(50L, PRODUCT_ID, VariantType.COLOR, 1,
                BigDecimal.valueOf(100), null));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        stubProductsForDto(product);

        CartResponseDto response = cartService.addItem(USER_ID, addRequest(PRODUCT_ID, VariantType.COLOR, 3));

        assertEquals(1, response.getItems().size());
        assertEquals(4, response.getItems().getFirst().getQuantity());
    }

    @Test
    void adding_different_variant_of_same_product_creates_separate_line() {
        Product product = product(PRODUCT_ID, 10, ProductStatus.ACTIVE, VariantType.COLOR,
                BigDecimal.valueOf(100), null);
        product.getPrices().add(price(VariantType.SIZE, BigDecimal.valueOf(120)));
        Cart cart = cart(1L, USER_ID);
        addItemToCart(cart, item(50L, PRODUCT_ID, VariantType.COLOR, 1, BigDecimal.valueOf(100), null));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        stubProductsForDto(product);

        CartResponseDto response = cartService.addItem(USER_ID, addRequest(PRODUCT_ID, VariantType.SIZE, 2));

        assertEquals(2, response.getItems().size());
        assertEquals(3, response.getTotalQuantity());
    }

    @Test
    void unknown_product_throws_product_not_found() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.addItem(USER_ID, addRequest(PRODUCT_ID, VariantType.COLOR, 1)));

        assertEquals(ECOMErrorType.PRODUCT_NOT_FOUND, exception.getEcomErrorType());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void inactive_product_throws_product_not_available() {
        Product product = product(PRODUCT_ID, 10, ProductStatus.INACTIVE, VariantType.COLOR,
                BigDecimal.valueOf(100), null);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.addItem(USER_ID, addRequest(PRODUCT_ID, VariantType.COLOR, 1)));

        assertEquals(ECOMErrorType.PRODUCT_NOT_AVAILABLE, exception.getEcomErrorType());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void variant_not_offered_by_product_throws_product_variant_not_found() {
        Product product = product(PRODUCT_ID, 10, ProductStatus.ACTIVE, VariantType.COLOR,
                BigDecimal.valueOf(100), null);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.addItem(USER_ID, addRequest(PRODUCT_ID, VariantType.SIZE, 1)));

        assertEquals(ECOMErrorType.PRODUCT_VARIANT_NOT_FOUND, exception.getEcomErrorType());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void requesting_more_than_inventory_throws_insufficient_stock() {
        Product product = product(PRODUCT_ID, 3);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        stubLazyCreatedCart();

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.addItem(USER_ID, addRequest(PRODUCT_ID, VariantType.COLOR, 4)));

        assertEquals(ECOMErrorType.INSUFFICIENT_STOCK, exception.getEcomErrorType());
    }

    @Test
    void merged_quantity_exceeding_inventory_throws_insufficient_stock() {
        Product product = product(PRODUCT_ID, 5);
        Cart cart = cart(1L, USER_ID);
        addItemToCart(cart, item(50L, PRODUCT_ID, VariantType.COLOR, 4, BigDecimal.valueOf(100), null));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.addItem(USER_ID, addRequest(PRODUCT_ID, VariantType.COLOR, 2)));

        assertEquals(ECOMErrorType.INSUFFICIENT_STOCK, exception.getEcomErrorType());
    }

    private com.ecommerce.persistence.entity.Price price(VariantType variant, BigDecimal value) {
        com.ecommerce.persistence.entity.Price price = new com.ecommerce.persistence.entity.Price();
        price.setVariantType(variant);
        price.setPrice(value);
        return price;
    }
}
