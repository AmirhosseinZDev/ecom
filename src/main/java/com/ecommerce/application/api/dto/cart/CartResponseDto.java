package com.ecommerce.application.api.dto.cart;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class CartResponseDto {

    private Long id;

    private Long userId;

    private List<CartItemResponseDto> items;

    private Integer totalQuantity;

    private BigDecimal totalPrice;

    private Date createdAt;

    private Date updatedAt;
}
