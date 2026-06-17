package com.ecommerce.application.api.dto.cart;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CartItemResponseDto {

    private Long productId;

    private String code;

    private String name;

    private String localName;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal lineTotal;
}
