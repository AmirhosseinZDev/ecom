package com.ecommerce.application.service.cart;

import com.ecommerce.application.api.dto.cart.AddCartItemRequestDto;
import com.ecommerce.application.api.dto.cart.CartResponseDto;
import com.ecommerce.application.api.dto.cart.UpdateCartItemQuantityRequestDto;
import com.ecommerce.application.api.exception.ECOMErrorType;
import com.ecommerce.application.api.exception.EcommerceException;
import com.ecommerce.persistence.entity.Cart;
import com.ecommerce.persistence.entity.CartItem;
import com.ecommerce.persistence.entity.Product;
import com.ecommerce.persistence.entity.enumeration.InventoryStatus;
import com.ecommerce.persistence.entity.enumeration.ProductStatus;
import com.ecommerce.persistence.repository.CartRepository;
import com.ecommerce.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Transactional(readOnly = true)
    public CartResponseDto getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseGet(CartResponseDto::new);
    }

    public CartResponseDto addItem(Long userId, AddCartItemRequestDto requestDto) {
        int quantityToAdd = requestDto.getQuantity() == null ? 1 : requestDto.getQuantity();
        Product product = findPurchasableProductOrThrow(requestDto.getProductId());

        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        Optional<CartItem> existingItem = existingCart.flatMap(cart -> findItem(cart, product.getId()));
        int newQuantity = existingItem.map(CartItem::getQuantity).orElse(0) + quantityToAdd;
        validateInventory(product, newQuantity);

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(newQuantity);
            return toResponse(cartRepository.save(existingCart.get()));
        }

        Cart cart = existingCart.orElseGet(() -> newCart(userId));
        CartItem item = new CartItem();
        item.setProductId(product.getId());
        item.setQuantity(newQuantity);
        cart.addItem(item);
        return toResponse(cartRepository.save(cart));
    }

    public CartResponseDto updateQuantity(Long userId, Long productId, UpdateCartItemQuantityRequestDto requestDto) {
        Cart cart = findCartOrThrow(userId);
        CartItem item = findItemOrThrow(cart, productId);
        Product product = findPurchasableProductOrThrow(productId);
        validateInventory(product, requestDto.getQuantity());
        item.setQuantity(requestDto.getQuantity());
        return toResponse(cartRepository.save(cart));
    }

    public CartResponseDto incrementItem(Long userId, Long productId) {
        Cart cart = findCartOrThrow(userId);
        CartItem item = findItemOrThrow(cart, productId);
        Product product = findPurchasableProductOrThrow(productId);
        int newQuantity = item.getQuantity() + 1;
        validateInventory(product, newQuantity);
        item.setQuantity(newQuantity);
        return toResponse(cartRepository.save(cart));
    }

    public CartResponseDto decrementItem(Long userId, Long productId) {
        Cart cart = findCartOrThrow(userId);
        CartItem item = findItemOrThrow(cart, productId);
        if (item.getQuantity() <= 1) {
            cart.removeItem(item);
        } else {
            item.setQuantity(item.getQuantity() - 1);
        }
        return toResponse(cartRepository.save(cart));
    }

    public CartResponseDto removeItem(Long userId, Long productId) {
        Cart cart = findCartOrThrow(userId);
        CartItem item = findItemOrThrow(cart, productId);
        cart.removeItem(item);
        return toResponse(cartRepository.save(cart));
    }

    public CartResponseDto clear(Long userId) {
        Cart cart = findCartOrThrow(userId);
        cart.getItems().clear();
        return toResponse(cartRepository.save(cart));
    }

    private Cart newCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cart;
    }

    private Cart findCartOrThrow(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EcommerceException(ECOMErrorType.CART_ITEM_NOT_FOUND));
    }

    private Optional<CartItem> findItem(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }

    private CartItem findItemOrThrow(Cart cart, Long productId) {
        return findItem(cart, productId)
                .orElseThrow(() -> new EcommerceException(ECOMErrorType.CART_ITEM_NOT_FOUND));
    }

    private Product findPurchasableProductOrThrow(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EcommerceException(ECOMErrorType.PRODUCT_NOT_FOUND));
        if (product.getStatus() != ProductStatus.ACTIVE
                || product.getInventoryStatus() == InventoryStatus.OUT_OF_STOCK) {
            throw new EcommerceException(ECOMErrorType.PRODUCT_NOT_PURCHASABLE);
        }
        return product;
    }

    private void validateInventory(Product product, int requestedQuantity) {
        if (product.getInventoryCount() == null || requestedQuantity > product.getInventoryCount()) {
            throw new EcommerceException(ECOMErrorType.INSUFFICIENT_INVENTORY);
        }
    }

    private CartResponseDto toResponse(Cart cart) {
        List<Long> productIds = cart.getItems().stream().map(CartItem::getProductId).toList();
        List<Product> products = productIds.isEmpty() ? List.of() : productRepository.findAllById(productIds);
        return cartMapper.toResponseDto(cart, cartMapper.indexById(products));
    }
}
