package com.ecommerce.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ProductImage {

    private String altText;

    private String imageData;
}
