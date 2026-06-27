package com.ecommerce.application.service.order;

import com.ecommerce.application.api.dto.order.CheckoutRequestDto;
import com.ecommerce.application.api.dto.order.OrderResponseDto;
import com.ecommerce.application.api.exception.ECOMErrorType;
import com.ecommerce.application.api.exception.EcommerceException;
import com.ecommerce.application.service.shipping.ShippingResult;
import com.ecommerce.persistence.entity.Product;
import com.ecommerce.persistence.entity.enumeration.OrderStatus;
import com.ecommerce.persistence.entity.enumeration.ProductStatus;
import com.ecommerce.persistence.entity.enumeration.Province;
import com.ecommerce.persistence.entity.enumeration.ShippingZone;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckoutService_checkoutUTest extends BaseCheckoutServiceUTest {

    private static final BigDecimal SHIPPING_COST = BigDecimal.valueOf(183000);

    private CheckoutRequestDto request() {
        CheckoutRequestDto dto = new CheckoutRequestDto();
        dto.setAddressId(ADDRESS_ID);
        return dto;
    }

    @Test
    void checkout_builds_order_snapshot_with_totals_and_clears_cart() {
        Product product = product(10, 500, ProductStatus.ACTIVE);
        when(userAddressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address()));
        when(cartItemRepository.findByUserId(USER_ID))
                .thenReturn(List.of(cartItem(2, BigDecimal.valueOf(100), BigDecimal.valueOf(80))));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(shippingCalculator.calculate(eq(Province.TEHRAN), anyInt()))
                .thenReturn(new ShippingResult(ShippingZone.INTRA_PROVINCE, SHIPPING_COST));

        OrderResponseDto response = checkoutService.checkout(USER_ID, request());

        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(1, response.getItems().size());
        // discount price wins: 80 * 2 = 160 subtotal
        assertEquals(0, response.getSubtotal().compareTo(BigDecimal.valueOf(160)));
        assertEquals(0, response.getShippingCost().compareTo(SHIPPING_COST));
        assertEquals(0, response.getTotalAmount().compareTo(SHIPPING_COST.add(BigDecimal.valueOf(160))));
        assertEquals(1000, response.getTotalWeightGram());
        assertEquals(ShippingZone.INTRA_PROVINCE, response.getShippingZone());

        // address snapshot
        assertEquals("Ali", response.getRecipientFirstName());
        assertEquals(Province.TEHRAN, response.getProvince());

        // order line snapshot
        var line = response.getItems().getFirst();
        assertEquals("Laptop", line.getProductName());
        assertEquals("1-1", line.getProductCode());
        assertEquals(0, line.getLineTotal().compareTo(BigDecimal.valueOf(160)));

        // stock is NOT touched at checkout (only at payment); cart is emptied
        assertEquals(10, product.getInventoryCount());
        verify(cartItemRepository).deleteByUserId(USER_ID);
    }

    @Test
    void unknown_address_throws_address_not_found() {
        when(userAddressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.empty());

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> checkoutService.checkout(USER_ID, request()));

        assertEquals(ECOMErrorType.ADDRESS_NOT_FOUND, exception.getEcomErrorType());
        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void empty_cart_throws_empty_cart() {
        when(userAddressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address()));
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(List.of());

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> checkoutService.checkout(USER_ID, request()));

        assertEquals(ECOMErrorType.EMPTY_CART, exception.getEcomErrorType());
        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void inactive_product_throws_product_not_available() {
        when(userAddressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address()));
        when(cartItemRepository.findByUserId(USER_ID))
                .thenReturn(List.of(cartItem(1, BigDecimal.valueOf(100), null)));
        when(productRepository.findById(PRODUCT_ID))
                .thenReturn(Optional.of(product(10, 500, ProductStatus.INACTIVE)));

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> checkoutService.checkout(USER_ID, request()));

        assertEquals(ECOMErrorType.PRODUCT_NOT_AVAILABLE, exception.getEcomErrorType());
        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void quantity_above_inventory_throws_insufficient_stock() {
        when(userAddressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address()));
        when(cartItemRepository.findByUserId(USER_ID))
                .thenReturn(List.of(cartItem(5, BigDecimal.valueOf(100), null)));
        when(productRepository.findById(PRODUCT_ID))
                .thenReturn(Optional.of(product(3, 500, ProductStatus.ACTIVE)));

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> checkoutService.checkout(USER_ID, request()));

        assertEquals(ECOMErrorType.INSUFFICIENT_STOCK, exception.getEcomErrorType());
        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
