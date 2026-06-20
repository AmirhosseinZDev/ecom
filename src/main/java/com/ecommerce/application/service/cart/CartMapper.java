package com.ecommerce.application.service.cart;

import com.ecommerce.application.api.dto.cart.CartItemResponseDto;
import com.ecommerce.application.api.dto.cart.CartResponseDto;
import com.ecommerce.persistence.entity.Cart;
import com.ecommerce.persistence.entity.CartItem;
import com.ecommerce.persistence.entity.Product;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.Map;

@Mapper(componentModel = "spring")
interface CartMapper {

    @Mapping(target = "totalQuantity", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    CartResponseDto toResponseDto(Cart cart, @Context Map<Long, Product> products);

    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "productCode", ignore = true)
    @Mapping(target = "effectivePrice", ignore = true)
    @Mapping(target = "lineTotal", ignore = true)
    CartItemResponseDto toItemDto(CartItem item, @Context Map<Long, Product> products);

    @AfterMapping
    default void enrichItem(CartItem item, @Context Map<Long, Product> products,
                            @MappingTarget CartItemResponseDto dto) {
        Product product = products.get(item.getProductId());
        if (product != null) {
            dto.setProductName(product.getName());
            dto.setProductCode(product.getCode());
        }
        BigDecimal effectivePrice = item.getDiscountPrice() != null ? item.getDiscountPrice() : item.getUnitPrice();
        dto.setEffectivePrice(effectivePrice);
        dto.setLineTotal(effectivePrice.multiply(BigDecimal.valueOf(item.getQuantity())));
    }

    @AfterMapping
    default void enrichCart(@MappingTarget CartResponseDto dto) {
        if (dto.getItems() == null) {
            dto.setTotalQuantity(0);
            dto.setTotalPrice(BigDecimal.ZERO);
            return;
        }
        dto.setTotalQuantity(dto.getItems().stream()
                .mapToInt(CartItemResponseDto::getQuantity)
                .sum());
        dto.setTotalPrice(dto.getItems().stream()
                .map(CartItemResponseDto::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
