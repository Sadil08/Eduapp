package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {}