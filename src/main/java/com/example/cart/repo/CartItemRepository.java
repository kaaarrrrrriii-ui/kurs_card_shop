package com.example.cart.repo;

import com.example.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
  boolean existsByCart_CartId(Long cartId);
}
