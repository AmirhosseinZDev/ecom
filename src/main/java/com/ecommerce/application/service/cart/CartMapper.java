package com.ecommerce.application.service.cart;

import com.ecommerce.application.api.dto.cart.CartItemResponseDto;
import com.ecommerce.application.api.dto.cart.CartResponseDto;
import com.ecommerce.persistence.entity.Cart;
import com.ecommerce.persistence.entity.CartItem;
import com.ecommerce.persistence.entity.Price;
import com.ecommerce.persistence.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartResponseDto toResponseDto(Cart cart, Map<Long, Product> productsById) {
        CartResponseDto responseDto = new CartResponseDto();
        responseDto.setId(cart.getId());

        List<CartItemResponseDto> items = cart.getItems().stream()
                .sorted(Comparator.comparing(CartItem::getProductId))
                .map(item -> toItemResponseDto(item, productsById.get(item.getProductId())))
                .toList();
        responseDto.setItems(items);

        responseDto.setTotalQuantity(items.stream().mapToInt(CartItemResponseDto::getQuantity).sum());
        responseDto.setTotalPrice(items.stream()
                .map(CartItemResponseDto::getLineTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return responseDto;
    }

    private CartItemResponseDto toItemResponseDto(CartItem item, Product product) {
        CartItemResponseDto dto = new CartItemResponseDto();
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        if (product != null) {
            dto.setCode(product.getCode());
            dto.setName(product.getName());
            dto.setLocalName(product.getLocalName());
            BigDecimal unitPrice = effectivePrice(product);
            dto.setUnitPrice(unitPrice);
            if (unitPrice != null) {
                dto.setLineTotal(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        return dto;
    }

    private BigDecimal effectivePrice(Product product) {
        if (product.getPrices() == null || product.getPrices().isEmpty()) {
            return null;
        }
        Price price = product.getPrices().getFirst();
        return price.getDiscountPrice() != null ? price.getDiscountPrice() : price.getPrice();
    }

    Map<Long, Product> indexById(List<Product> products) {
        return products.stream().collect(Collectors.toMap(Product::getId, Function.identity()));
    }
}
