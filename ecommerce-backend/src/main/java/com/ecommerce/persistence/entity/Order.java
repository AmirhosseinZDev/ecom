package com.ecommerce.persistence.entity;

import com.ecommerce.persistence.entity.enumeration.OrderStatus;
import com.ecommerce.persistence.entity.enumeration.Province;
import com.ecommerce.persistence.entity.enumeration.ShippingZone;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_seq")
    @SequenceGenerator(name = "orders_seq", sequenceName = "orders_seq", allocationSize = 50)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OrderStatus status;

    // -----------------------------------------------------------------------------------------
    // Shipping address snapshot: captured at checkout so the order is unaffected by later edits
    // or deletion of the source UserAddress.
    // -----------------------------------------------------------------------------------------
    @Column(name = "recipient_first_name", nullable = false, length = 255)
    private String recipientFirstName;

    @Column(name = "recipient_last_name", nullable = false, length = 255)
    private String recipientLastName;

    @Column(name = "recipient_mobile", nullable = false, length = 20)
    private String recipientMobile;

    @Column(name = "recipient_national_id", length = 20)
    private String recipientNationalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "province", nullable = false, length = 64)
    private Province province;

    @Column(name = "city", nullable = false, length = 255)
    private String city;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "address_line", nullable = false, columnDefinition = "TEXT")
    private String addressLine;

    @Column(name = "plaque", length = 32)
    private String plaque;

    @Column(name = "unit", length = 32)
    private String unit;

    // -----------------------------------------------------------------------------------------
    // Money / shipping
    // -----------------------------------------------------------------------------------------
    @Column(name = "subtotal", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "shipping_cost", nullable = false, precision = 14, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "total_weight_gram", nullable = false)
    private Integer totalWeightGram;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_zone", nullable = false, length = 32)
    private ShippingZone shippingZone;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 25)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Date updatedAt;

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }
}
