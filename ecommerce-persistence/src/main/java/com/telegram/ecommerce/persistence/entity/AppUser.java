package com.telegram.ecommerce.persistence.entity;

import com.telegram.ecommerce.persistence.entity.enumeration.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

/**
 * @author AmirHossein ZamanZade
 * @since 12/25/25
 */
@Entity
@Table(name = "app_user", uniqueConstraints = {
        @UniqueConstraint(name = "uk_app_user_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_app_user_mobile", columnNames = "mobile")
})
@Getter
@Setter
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_user_seq")
    @SequenceGenerator(name = "app_user_seq", sequenceName = "app_user_seq", allocationSize = 50)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 512)
    private String name;

    @Column(name = "username", nullable = false, length = 255)
    private String username;

    @Column(name = "password", nullable = false, length = 512)
    private String password;

    @Column(name = "mobile", nullable = false, length = 20)
    private String mobile;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt;

    @Column(name = "role", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "IS_ENABLED", nullable = false, length = 1)
    private Boolean isEnabled;
}
