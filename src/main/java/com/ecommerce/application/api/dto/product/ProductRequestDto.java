package com.ecommerce.application.api.dto.product;

import com.ecommerce.persistence.entity.enumeration.InventoryStatus;
import com.ecommerce.persistence.entity.enumeration.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProductRequestDto {

    @NotEmpty
    private String code;

    @NotNull
    private Long categoryId;

    private Long subCategoryId;

    @NotEmpty
    private String url;

    @NotEmpty
    @Valid
    private List<PriceDto> prices;

    private String shortDescription;

    private String fullDescription;

    private Map<String, String> specification;

    @Valid
    private ProductImageDto image;

    @NotEmpty
    private String name;

    private String localName;

    private Long brandId;

    @NotNull
    private InventoryStatus inventoryStatus;

    @NotNull
    private ProductStatus status;

    @NotNull
    @PositiveOrZero
    private Integer inventoryCount;
}
