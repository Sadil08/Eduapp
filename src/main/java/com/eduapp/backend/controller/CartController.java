package com.eduapp.backend.controller;

import com.eduapp.backend.model.Cart;
import com.eduapp.backend.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<List<Cart>> getAll() {
        logger.info("Fetching all carts");
        List<Cart> carts = cartService.findAll();
        return ResponseEntity.ok(carts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cart> getById(@PathVariable Long id) {
        logger.info("Fetching cart with ID: {}", id);
        Optional<Cart> cart = cartService.findById(id);
        return cart.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Cart> create(@RequestBody Cart cart) {
        logger.info("Creating cart");
        Cart saved = cartService.save(cart);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cart> update(@PathVariable Long id, @RequestBody Cart cart) {
        logger.info("Updating cart with ID: {}", id);
        if (!cartService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        cart.setId(id);
        Cart updated = cartService.save(cart);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deleting cart with ID: {}", id);
        if (!cartService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        cartService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}