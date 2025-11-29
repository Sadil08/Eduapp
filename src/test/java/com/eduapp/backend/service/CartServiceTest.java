package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduapp.backend.model.Cart;
import com.eduapp.backend.repository.CartRepository;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void findAll_ReturnsList() {
        // Arrange
        Cart cart = new Cart();
        when(cartRepository.findAll()).thenReturn(List.of(cart));

        // Act
        List<Cart> result = cartService.findAll();

        // Assert
        assertThat(result).hasSize(1);
    }
}