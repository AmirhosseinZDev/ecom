package com.ecommerce.application.api.dto.product;

import com.ecommerce.persistence.entity.enumeration.ProductStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchRequestDto {

    private String localName;

    private Long categoryId;

    private Long subCategoryId;

    private Long brandId;

    private ProductStatus status;

    private Boolean isAvailable;
}
