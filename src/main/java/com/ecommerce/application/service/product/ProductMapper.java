package com.ecommerce.application.service.product;

import com.ecommerce.application.api.dto.product.ProductRequestDto;
import com.ecommerce.application.api.dto.product.ProductResponseDto;
import com.ecommerce.persistence.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void apply(ProductRequestDto requestDto, @MappingTarget Product product);

    ProductResponseDto toResponseDto(Product product);
}
