package com.ecommerce.application.api.dto.cart;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CartResponseDto {

    private Long id;

    private List<CartItemResponseDto> items = new ArrayList<>();

    private Integer totalQuantity = 0;

    private BigDecimal totalPrice = BigDecimal.ZERO;
}
