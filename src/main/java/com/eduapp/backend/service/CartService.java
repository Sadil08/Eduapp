package com.eduapp.backend.service;

import com.eduapp.backend.dto.CartDto;
import com.eduapp.backend.model.Cart;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final com.eduapp.backend.repository.UserRepository userRepository;

    public CartService(CartRepository cartRepository, com.eduapp.backend.repository.UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
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

    /**
     * Get or create a cart for the user. Returns the Cart entity for internal
     * service use.
     */
    public Cart getOrCreateCart(Long userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);
        if (carts.isEmpty()) {
            com.eduapp.backend.model.User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            return cartRepository.save(new Cart(user));
        }

        // Return first, delete others if duplicates exist
        if (carts.size() > 1) {
            logger.warn("Found {} duplicate carts for user {}. Cleaning up...", carts.size(), userId);
            Cart primary = carts.get(0);
            for (int i = 1; i < carts.size(); i++) {
                try {
                    cartRepository.delete(carts.get(i));
                } catch (Exception e) {
                    logger.error("Failed to delete duplicate cart: " + carts.get(i).getId(), e);
                }
            }
            return primary;
        }

        return carts.get(0);
    }

    /**
     * Convert Cart entity to CartDto to avoid Hibernate proxy serialization issues.
     */
    private CartDto toCartDto(Cart cart) {
        List<CartDto.CartBundleDto> bundleDtos = cart.getBundles().stream()
                .map(this::toBundleDto)
                .collect(Collectors.toList());

        return new CartDto(
                cart.getId(),
                cart.getUser().getId(),
                bundleDtos);
    }

    private CartDto.CartBundleDto toBundleDto(PaperBundle bundle) {
        String examTypeName = bundle.getExamType() != null ? bundle.getExamType().getName() : null;
        return new CartDto.CartBundleDto(
                bundle.getId(),
                bundle.getName(),
                bundle.getDescription(),
                bundle.getPrice(),
                bundle.getType() != null ? bundle.getType().name() : null,
                examTypeName,
                bundle.getIsPastPaper());
    }

    public CartDto getMyCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return toCartDto(cart);
    }

    @org.springframework.transaction.annotation.Transactional
    public CartDto addToCart(Long userId, Long bundleId,
            com.eduapp.backend.repository.PaperBundleRepository bundleRepo) {
        Cart cart = getOrCreateCart(userId);
        com.eduapp.backend.model.PaperBundle bundle = bundleRepo.findById(bundleId)
                .orElseThrow(() -> new RuntimeException("Bundle not found"));

        // Avoid duplicates
        if (cart.getBundles().stream().noneMatch(b -> b.getId().equals(bundleId))) {
            cart.getBundles().add(bundle);
            cart = cartRepository.save(cart);
        }
        return toCartDto(cart);
    }

    @org.springframework.transaction.annotation.Transactional
    public CartDto removeFromCart(Long userId, Long bundleId) {
        Cart cart = getOrCreateCart(userId);
        cart.getBundles().removeIf(b -> b.getId().equals(bundleId));
        cart = cartRepository.save(cart);
        return toCartDto(cart);
    }
}