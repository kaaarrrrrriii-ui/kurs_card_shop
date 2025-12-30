package com.example.cart.repo;

import com.example.cart.model.Cart;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
  List<Cart> findByActiveTrue();

  boolean existsByCustomer_CustomerId(Long customerId);
}
