package com.ecommerce.application.service.cart;

import com.ecommerce.application.api.dto.cart.AddCartItemRequestDto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartService_addItemUTest {

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
    void adding_a_product_to_an_empty_cart_creates_the_item() {
        Product product = product(10L, 5);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));

        CartResponseDto response = cartService.addItem(1L, addRequest(10L, 2));

        assertEquals(1, response.getItems().size());
        assertEquals(2, response.getItems().getFirst().getQuantity());
        assertEquals(2, response.getTotalQuantity());
    }

    @Test
    void adding_an_existing_product_accumulates_the_quantity() {
        Product product = product(10L, 10);
        Cart cart = cartWithItem(10L, 3);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));

        CartResponseDto response = cartService.addItem(1L, addRequest(10L, 4));

        assertEquals(7, response.getItems().getFirst().getQuantity());
    }

    @Test
    void missing_quantity_defaults_to_one() {
        Product product = product(10L, 5);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));

        CartResponseDto response = cartService.addItem(1L, addRequest(10L, null));

        assertEquals(1, response.getItems().getFirst().getQuantity());
    }

    @Test
    void unknown_product_throws_product_not_found() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.addItem(1L, addRequest(99L, 1)));

        assertEquals(ECOMErrorType.PRODUCT_NOT_FOUND, exception.getEcomErrorType());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void inactive_product_is_not_purchasable() {
        Product product = product(10L, 5);
        product.setStatus(ProductStatus.INACTIVE);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.addItem(1L, addRequest(10L, 1)));

        assertEquals(ECOMErrorType.PRODUCT_NOT_PURCHASABLE, exception.getEcomErrorType());
    }

    @Test
    void out_of_stock_product_is_not_purchasable() {
        Product product = product(10L, 5);
        product.setInventoryStatus(InventoryStatus.OUT_OF_STOCK);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.addItem(1L, addRequest(10L, 1)));

        assertEquals(ECOMErrorType.PRODUCT_NOT_PURCHASABLE, exception.getEcomErrorType());
    }

    @Test
    void requesting_more_than_inventory_throws_insufficient_inventory() {
        Product product = product(10L, 3);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> cartService.addItem(1L, addRequest(10L, 4)));

        assertEquals(ECOMErrorType.INSUFFICIENT_INVENTORY, exception.getEcomErrorType());
        verify(cartRepository, never()).save(any());
    }

    private AddCartItemRequestDto addRequest(Long productId, Integer quantity) {
        AddCartItemRequestDto requestDto = new AddCartItemRequestDto();
        requestDto.setProductId(productId);
        requestDto.setQuantity(quantity);
        return requestDto;
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
