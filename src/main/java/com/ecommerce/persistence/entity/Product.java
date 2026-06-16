package com.ecommerce.persistence.entity;

import com.ecommerce.persistence.entity.enumeration.InventoryStatus;
import com.ecommerce.persistence.entity.enumeration.ProductStatus;
import com.ecommerce.persistence.entity.enumeration.SpecificationKey;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(
        name = "product",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_code", columnNames = "code"),
                @UniqueConstraint(name = "uk_product_url", columnNames = "url")
        }
)
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @SequenceGenerator(name = "product_seq", sequenceName = "product_seq", allocationSize = 50)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "local_name", length = 255)
    private String localName;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "sub_category_id")
    private Long subCategoryId;

    @Column(name = "url", nullable = false, length = 255)
    private String url;

    @ElementCollection
    @CollectionTable(name = "product_price", joinColumns = @JoinColumn(name = "product_id"))
    private List<Price> prices = new ArrayList<>();

    @Column(name = "short_description", length = 1024)
    private String shortDescription;

    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specification", columnDefinition = "jsonb")
    private Map<SpecificationKey, String> specification = new HashMap<>();

    @Embedded
    private ProductImage mainImage;

    @Embedded
    private ProductImage otherImages;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "inventory_status", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private InventoryStatus inventoryStatus;

    @Column(name = "status", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column(name = "inventory_count", nullable = false)
    private Integer inventoryCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Date updatedAt;
}
