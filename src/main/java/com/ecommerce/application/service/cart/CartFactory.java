package com.ecommerce.application.service.cart;

import com.ecommerce.persistence.entity.Cart;
import com.ecommerce.persistence.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class CartFactory {

    private final CartRepository cartRepository;

    // Runs in its own transaction so that a unique-constraint violation raised by a
    // concurrent creation (uk_cart_user) rolls back only this insert. The caller can
    // then recover by re-reading the cart, instead of having its own transaction
    // poisoned (marked rollback-only) by the failed flush.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Cart createNew(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.saveAndFlush(cart);
    }
}
