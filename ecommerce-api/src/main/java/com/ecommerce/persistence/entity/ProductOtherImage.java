package com.ecommerce.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_other_image")
@Getter
@Setter
public class ProductOtherImage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_other_image_seq")
    @SequenceGenerator(name = "product_other_image_seq", sequenceName = "product_other_image_seq", allocationSize = 50)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(name = "image_data", columnDefinition = "TEXT", nullable = false)
    private String imageData;
}
