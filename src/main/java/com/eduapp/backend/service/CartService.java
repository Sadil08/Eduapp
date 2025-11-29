package com.eduapp.backend.service;

import com.eduapp.backend.model.Cart;
import com.eduapp.backend.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public List<Cart> findAll() {
        logger.info("Fetching all carts");
        return cartRepository.findAll();
    }

    public Optional<Cart> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        logger.info("Fetching cart with ID: {}", id);
        return cartRepository.findById(id);
    }

    public Cart save(Cart cart) {
        if (cart == null) {
            throw new IllegalArgumentException("Cart cannot be null");
        }
        logger.info("Saving cart");
        return cartRepository.save(cart);
    }

    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        logger.info("Deleting cart with ID: {}", id);
        cartRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return cartRepository.existsById(id);
    }
}