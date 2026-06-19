package com.ecommerce.application.service.product;

import com.ecommerce.application.api.dto.product.ProductOtherImageDto;
import com.ecommerce.application.api.dto.product.ProductRequestDto;
import com.ecommerce.application.api.dto.product.ProductResponseDto;
import com.ecommerce.persistence.entity.Product;
import com.ecommerce.persistence.entity.ProductOtherImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "mainImage", ignore = true)
    @Mapping(target = "otherImages", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void apply(ProductRequestDto requestDto, @MappingTarget Product product);

    ProductResponseDto toResponseDto(Product product);

    @Mapping(target = "id", source = "id")
    ProductOtherImageDto toOtherImageDto(ProductOtherImage entity);
}
