package com.example.cart.service;

import com.example.cart.repo.CartItemRepository;
import com.example.cart.repo.CartRepository;
import com.example.cart.repo.CustomerRepository;
import com.example.cart.repo.ProductRepository;
import com.example.cart.repo.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
@RequiredArgsConstructor
public class ViewDataService {
  private final CustomerRepository customerRepository;
  private final ProductRepository productRepository;
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final PromoCodeRepository promoCodeRepository;

  public void populateLists(Model model) {
    model.addAttribute("customers", customerRepository.findAll());
    model.addAttribute("products", productRepository.findAllProducts());
    model.addAttribute("carts", cartRepository.findAll());
    model.addAttribute("cartItems", cartItemRepository.findAll());
    model.addAttribute("promoCodes", promoCodeRepository.findAll());
  }
}
