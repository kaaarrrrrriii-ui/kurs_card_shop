package com.example.cart.repo;

import com.example.cart.model.PromoCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromoCodeRepository extends JpaRepository<PromoCode, String> {
  List<PromoCode> findByActiveTrue();
}
