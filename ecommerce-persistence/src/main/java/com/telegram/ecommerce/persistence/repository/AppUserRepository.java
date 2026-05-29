package com.telegram.ecommerce.persistence.repository;

import com.telegram.ecommerce.persistence.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByMobile(String mobileNumber);
}
