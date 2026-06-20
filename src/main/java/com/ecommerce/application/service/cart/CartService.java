package com.ecommerce.application.service.cart;

import com.ecommerce.application.api.dto.cart.AddCartItemRequestDto;
import com.ecommerce.application.api.dto.cart.CartResponseDto;
import com.ecommerce.application.api.exception.ECOMErrorType;
import com.ecommerce.application.api.exception.EcommerceException;
import com.ecommerce.persistence.entity.Cart;
import com.ecommerce.persistence.entity.CartItem;
import com.ecommerce.persistence.entity.Price;
import com.ecommerce.persistence.entity.Product;
import com.ecommerce.persistence.entity.enumeration.ProductStatus;
import com.ecommerce.persistence.entity.enumeration.VariantType;
import com.ecommerce.persistence.repository.CartRepository;
import com.ecommerce.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;
    private final CartFactory cartFactory;

    @Transactional
    public CartResponseDto getCart(Long userId) {
        return toDto(getOrCreateCart(userId));
    }

    @Transactional
    public CartResponseDto addItem(Long userId, AddCartItemRequestDto requestDto) {
        Product product = findProductOrThrow(requestDto.getProductId());
        requirePurchasable(product);
        Price price = findVariantPriceOrThrow(product, requestDto.getVariantType());

        Cart cart = getOrCreateCart(userId);
        CartItem item = findItem(cart, requestDto.getProductId(), requestDto.getVariantType());

        int currentQuantity = item != null ? item.getQuantity() : 0;
        int newQuantity = currentQuantity + requestDto.getQuantity();
        requireSufficientStock(product, newQuantity);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProductId(product.getId());
            item.setVariantType(requestDto.getVariantType());
            item.setUnitPrice(price.getPrice());
            item.setDiscountPrice(price.getDiscountPrice());
            item.setQuantity(newQuantity);
            cart.getItems().add(item);
        } else {
            item.setQuantity(newQuantity);
        }

        return toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartResponseDto updateItemQuantity(Long userId, Long itemId, int quantity) {
        Cart cart = getCartOrThrow(userId);
        CartItem item = findItemOrThrow(cart, itemId);
        Product product = findProductOrThrow(item.getProductId());
        requireSufficientStock(product, quantity);
        item.setQuantity(quantity);
        return toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartResponseDto incrementItem(Long userId, Long itemId) {
        Cart cart = getCartOrThrow(userId);
        CartItem item = findItemOrThrow(cart, itemId);
        Product product = findProductOrThrow(item.getProductId());
        int newQuantity = item.getQuantity() + 1;
        requireSufficientStock(product, newQuantity);
        item.setQuantity(newQuantity);
        return toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartResponseDto decrementItem(Long userId, Long itemId) {
        Cart cart = getCartOrThrow(userId);
        CartItem item = findItemOrThrow(cart, itemId);
        if (item.getQuantity() <= 1) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(item.getQuantity() - 1);
        }
        return toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartResponseDto removeItem(Long userId, Long itemId) {
        Cart cart = getCartOrThrow(userId);
        CartItem item = findItemOrThrow(cart, itemId);
        cart.getItems().remove(item);
        return toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartResponseDto clearCart(Long userId) {
        // Clearing is idempotent: a missing cart is treated as already-empty rather than 404.
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        return toDto(cartRepository.save(cart));
    }

    private Cart getOrCreateCart(Long userId) {
        Optional<Cart> existing = cartRepository.findByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        try {
            return cartFactory.createNew(userId);
        } catch (DataIntegrityViolationException concurrentCreation) {
            // A concurrent request created the cart first (uk_cart_user); re-read it.
            return cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new EcommerceException(ECOMErrorType.GENERAL_ERROR));
        }
    }

    private Cart getCartOrThrow(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EcommerceException(ECOMErrorType.CART_ITEM_NOT_FOUND));
    }

    private CartItem findItem(Cart cart, Long productId, VariantType variantType) {
        return cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId) && i.getVariantType() == variantType)
                .findFirst()
                .orElse(null);
    }

    private CartItem findItemOrThrow(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EcommerceException(ECOMErrorType.CART_ITEM_NOT_FOUND));
    }

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new EcommerceException(ECOMErrorType.PRODUCT_NOT_FOUND));
    }

    private void requirePurchasable(Product product) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new EcommerceException(ECOMErrorType.PRODUCT_NOT_AVAILABLE);
        }
    }

    private Price findVariantPriceOrThrow(Product product, VariantType variantType) {
        return product.getPrices().stream()
                .filter(p -> p.getVariantType() == variantType)
                .findFirst()
                .orElseThrow(() -> new EcommerceException(ECOMErrorType.PRODUCT_VARIANT_NOT_FOUND));
    }

    private void requireSufficientStock(Product product, int requestedQuantity) {
        if (product.getInventoryCount() == null || requestedQuantity > product.getInventoryCount()) {
            throw new EcommerceException(ECOMErrorType.INSUFFICIENT_STOCK);
        }
    }

    private CartResponseDto toDto(Cart cart) {
        List<Long> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .distinct()
                .toList();
        Map<Long, Product> products = productIds.isEmpty()
                ? Map.of()
                : productRepository.findAllById(productIds).stream()
                        .collect(Collectors.toMap(Product::getId, Function.identity()));
        return cartMapper.toResponseDto(cart, products);
    }
}
