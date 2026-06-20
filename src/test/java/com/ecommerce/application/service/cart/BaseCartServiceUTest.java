package com.ecommerce.application.service.cart;

import com.ecommerce.application.api.dto.cart.AddCartItemRequestDto;
import com.ecommerce.persistence.entity.Cart;
import com.ecommerce.persistence.entity.CartItem;
import com.ecommerce.persistence.entity.Price;
import com.ecommerce.persistence.entity.Product;
import com.ecommerce.persistence.entity.enumeration.ProductStatus;
import com.ecommerce.persistence.entity.enumeration.VariantType;
import com.ecommerce.persistence.repository.CartRepository;
import com.ecommerce.persistence.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
abstract class BaseCartServiceUTest {

    protected static final Long USER_ID = 7L;
    protected static final Long PRODUCT_ID = 100L;

    @Mock
    protected CartRepository cartRepository;
    @Mock
    protected ProductRepository productRepository;
    @Mock
    protected CartFactory cartFactory;

    protected CartService cartService;

    @BeforeEach
    void baseSetUp() {
        cartService = new CartService(cartRepository, productRepository, new CartMapperImpl(), cartFactory);
        // save returns the persisted instance; harmless if a given test never saves.
        lenient().when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /**
     * Stubs the lazy-creation path: no cart exists yet, so the service delegates to
     * {@link CartFactory#createNew(Long)}.
     */
    protected Cart stubLazyCreatedCart() {
        Cart created = cart(1L, USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(cartFactory.createNew(USER_ID)).thenReturn(created);
        return created;
    }

    protected void stubProductsForDto(Product... products) {
        lenient().when(productRepository.findAllById(anyIterable())).thenReturn(List.of(products));
    }

    protected Product product(Long id, int inventory, ProductStatus status, VariantType variant,
                              BigDecimal price, BigDecimal discountPrice) {
        Product product = new Product();
        product.setId(id);
        product.setName("Product " + id);
        product.setCode(id + "-1");
        product.setStatus(status);
        product.setInventoryCount(inventory);
        Price variantPrice = new Price();
        variantPrice.setVariantType(variant);
        variantPrice.setPrice(price);
        variantPrice.setDiscountPrice(discountPrice);
        product.setPrices(new ArrayList<>(List.of(variantPrice)));
        return product;
    }

    protected Product product(Long id, int inventory) {
        return product(id, inventory, ProductStatus.ACTIVE, VariantType.COLOR, BigDecimal.valueOf(100), null);
    }

    protected Cart cart(Long id, Long userId) {
        Cart cart = new Cart();
        cart.setId(id);
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        return cart;
    }

    protected CartItem item(Long id, Long productId, VariantType variant, int quantity,
                            BigDecimal unitPrice, BigDecimal discountPrice) {
        CartItem item = new CartItem();
        item.setId(id);
        item.setProductId(productId);
        item.setVariantType(variant);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setDiscountPrice(discountPrice);
        return item;
    }

    protected void addItemToCart(Cart cart, CartItem item) {
        item.setCart(cart);
        cart.getItems().add(item);
    }

    protected AddCartItemRequestDto addRequest(Long productId, VariantType variant, int quantity) {
        AddCartItemRequestDto requestDto = new AddCartItemRequestDto();
        requestDto.setProductId(productId);
        requestDto.setVariantType(variant);
        requestDto.setQuantity(quantity);
        return requestDto;
    }
}
