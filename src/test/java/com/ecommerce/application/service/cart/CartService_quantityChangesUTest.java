package com.ecommerce.application.service.cart;

import com.ecommerce.application.api.dto.cart.CartResponseDto;
import com.ecommerce.application.api.exception.ECOMErrorType;
import com.ecommerce.application.api.exception.EcommerceException;
import com.ecommerce.persistence.entity.Cart;
import com.ecommerce.persistence.entity.CartItem;
import com.ecommerce.persistence.entity.Product;
import com.ecommerce.persistence.entity.enumeration.InventoryStatus;
import com.ecommerce.persistence.entity.enumeration.ProductStatus;
import com.ecommerce.persistence.repository.CartRepository;
import com.ecommerce.persistence.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartService_quantityChangesUTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, productRepository, new CartMapper());
    }

    @Test
    void increment_raises_quantity_by_one() {
        Product product = product(10L, 10);
        Cart cart = cartWithItem(10L, 2);
        stubCart(cart, product);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        CartResponseDto response = cartService.incrementItem(1L, 10L);

        assertEquals(3, response.getItems().getFirst().getQuantity());
    }

    @Test
    void increment_beyond_inventory_is_rejected() {
        Product product = product(10L, 2);
        Cart cart = cartWithItem(10L, 2);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.incrementItem(1L, 10L));

        assertEquals(ECOMErrorType.INSUFFICIENT_INVENTORY, exception.getEcomErrorType());
    }

    @Test
    void decrement_lowers_quantity_by_one() {
        Product product = product(10L, 10);
        Cart cart = cartWithItem(10L, 3);
        stubCart(cart, product);

        CartResponseDto response = cartService.decrementItem(1L, 10L);

        assertEquals(2, response.getItems().getFirst().getQuantity());
    }

    @Test
    void decrement_to_zero_removes_the_item() {
        Cart cart = cartWithItem(10L, 1);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartResponseDto response = cartService.decrementItem(1L, 10L);

        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void remove_deletes_the_item() {
        Cart cart = cartWithItem(10L, 4);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartResponseDto response = cartService.removeItem(1L, 10L);

        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void removing_a_product_not_in_cart_throws_cart_item_not_found() {
        Cart cart = cartWithItem(10L, 4);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.removeItem(1L, 99L));

        assertEquals(ECOMErrorType.CART_ITEM_NOT_FOUND, exception.getEcomErrorType());
    }

    @Test
    void operating_on_a_missing_cart_throws_cart_item_not_found() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.incrementItem(1L, 10L));

        assertEquals(ECOMErrorType.CART_ITEM_NOT_FOUND, exception.getEcomErrorType());
    }

    private void stubCart(Cart cart, Product product) {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);
        lenient().when(productRepository.findAllById(List.of(product.getId()))).thenReturn(List.of(product));
    }

    private Product product(Long id, int inventoryCount) {
        Product product = new Product();
        product.setId(id);
        product.setName("Product " + id);
        product.setCode("CODE-" + id);
        product.setStatus(ProductStatus.ACTIVE);
        product.setInventoryStatus(InventoryStatus.IN_STOCK);
        product.setInventoryCount(inventoryCount);
        return product;
    }

    private Cart cartWithItem(Long productId, int quantity) {
        Cart cart = new Cart();
        cart.setUserId(1L);
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        cart.addItem(item);
        return cart;
    }
}
