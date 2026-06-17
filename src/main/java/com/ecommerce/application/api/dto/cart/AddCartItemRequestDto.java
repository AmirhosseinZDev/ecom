package com.ecommerce.application.api.dto.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCartItemRequestDto {

    @NotNull
    private Long productId;

    @Positive
    private Integer quantity;
}
