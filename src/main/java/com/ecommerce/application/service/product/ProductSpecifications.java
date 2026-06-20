package com.ecommerce.application.service.product;

import com.ecommerce.application.api.dto.product.SearchProductRequestDto;
import com.ecommerce.persistence.entity.Product;
import com.ecommerce.persistence.entity.enumeration.ProductStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class ProductSpecifications {

    private ProductSpecifications() {
    }

    static Specification<Product> build(SearchProductRequestDto dto, boolean adminView) {
        var localName = dto.getLocalName();
        var categoryId = dto.getCategoryId();
        var subCategoryId = dto.getSubCategoryId();
        var brandId = dto.getBrandId();
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (localName != null && !localName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("localName")),
                        "%" + localName.toLowerCase() + "%"));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            if (subCategoryId != null) {
                predicates.add(cb.equal(root.get("subCategoryId"), subCategoryId));
            }
            if (brandId != null) {
                predicates.add(cb.equal(root.get("brandId"), brandId));
            }
            if (adminView) {
                if (dto.getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), dto.getStatus()));
                }
            } else {
                predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));
            }
            if (dto.getIsAvailable() != null) {
                if (dto.getIsAvailable()) {
                    predicates.add(cb.greaterThan(root.get("inventoryCount"), 0));
                } else {
                    predicates.add(cb.equal(root.get("inventoryCount"), 0));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
