package com.ecommerce.persistence.repository;

import com.ecommerce.persistence.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByIdDesc(Long userId);

    Optional<Order> findByIdAndUserId(Long id, Long userId);
}
