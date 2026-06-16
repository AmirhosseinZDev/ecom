package com.ecommerce.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ProductImage {

    @Column(name = "image_alt_text", length = 255)
    private String altText;

    @Column(name = "image_media_id")
    private Long mediaId;
}
