package com.example.cart.service;

import com.example.cart.model.Cart;
import com.example.cart.model.CartItem;
import com.example.cart.model.Customer;
import com.example.cart.model.DiscountType;
import com.example.cart.model.LoyaltyLevel;
import com.example.cart.model.Product;
import com.example.cart.model.PromoCode;
import com.example.cart.repo.CartItemRepository;
import com.example.cart.repo.CartRepository;
import com.example.cart.repo.CustomerRepository;
import com.example.cart.repo.ProductRepository;
import com.example.cart.repo.PromoCodeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataSearchService {
  private final CustomerRepository customerRepository;
  private final ProductRepository productRepository;
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final PromoCodeRepository promoCodeRepository;

  public List<Customer> searchCustomers(
      String email,
      String loyaltyLevel,
      String registeredFrom,
      String registeredTo) {
    LocalDate from = parseDate(registeredFrom);
    LocalDate to = parseDate(registeredTo);
    LoyaltyLevel level = parseLoyalty(loyaltyLevel);

    return customerRepository.findAll().stream()
        .filter(customer -> isBlank(email) || containsIgnoreCase(customer.getEmail(), email))
        .filter(customer -> level == null || customer.getLoyaltyLevel() == level)
        .filter(customer -> isWithin(customer.getRegistrationDate(), from, to))
        .sorted(Comparator.comparing(Customer::getCustomerId))
        .toList();
  }

  public List<Product> searchProducts(
      String name,
      String category,
      String inStock,
      String minPrice,
      String maxPrice) {
    Boolean stock = parseBoolean(inStock);
    BigDecimal min = parseDecimal(minPrice);
    BigDecimal max = parseDecimal(maxPrice);

    return productRepository.findAllProducts().stream()
        .filter(product -> isBlank(name) || containsIgnoreCase(product.getName(), name))
        .filter(product -> isBlank(category) || containsIgnoreCase(product.getCategory(), category))
        .filter(product -> stock == null || product.isInStock() == stock)
        .filter(product -> isBetween(product.getBasePrice(), min, max))
        .sorted(Comparator.comparing(Product::getProductId))
        .toList();
  }

  public List<Cart> searchCarts(
      String customerId,
      String active,
      String promoCode) {
    Long customer = parseLong(customerId);
    Boolean isActive = parseBoolean(active);

    return cartRepository.findAll().stream()
        .filter(cart -> customer == null
            || (cart.getCustomer() != null && customer.equals(cart.getCustomer().getCustomerId())))
        .filter(cart -> isActive == null || cart.isActive() == isActive)
        .filter(cart -> isBlank(promoCode) || containsIgnoreCase(cart.getPromoCode(), promoCode))
        .sorted(Comparator.comparing(Cart::getCartId))
        .toList();
  }

  public List<CartItem> searchCartItems(
      String cartId,
      String productId,
      String minQuantity) {
    Long cart = parseLong(cartId);
    Long product = parseLong(productId);
    Integer min = parseInt(minQuantity);

    return cartItemRepository.findAll().stream()
        .filter(item -> cart == null
            || (item.getCart() != null && cart.equals(item.getCart().getCartId())))
        .filter(item -> product == null
            || (item.getSourceProductId() != null && product.equals(item.getSourceProductId())))
        .filter(item -> min == null || item.getQuantity() >= min)
        .sorted(Comparator.comparing(CartItem::getCartItemId))
        .toList();
  }

  public List<PromoCode> searchPromos(
      String discountType,
      String active,
      String category) {
    DiscountType type = parseDiscountType(discountType);
    Boolean isActive = parseBoolean(active);

    return promoCodeRepository.findAll().stream()
        .filter(promo -> type == null || promo.getDiscountType() == type)
        .filter(promo -> isActive == null || promo.isActive() == isActive)
        .filter(promo -> isBlank(category) || containsIgnoreCase(promo.getApplicableCategory(), category))
        .sorted(Comparator.comparing(PromoCode::getCode))
        .toList();
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private boolean containsIgnoreCase(String value, String needle) {
    if (value == null || needle == null) {
      return false;
    }
    return value.toLowerCase().contains(needle.toLowerCase().trim());
  }

  private LocalDate parseDate(String value) {
    if (isBlank(value)) {
      return null;
    }
    try {
      return LocalDate.parse(value.trim());
    } catch (Exception ex) {
      return null;
    }
  }

  private LoyaltyLevel parseLoyalty(String value) {
    if (isBlank(value)) {
      return null;
    }
    try {
      return LoyaltyLevel.valueOf(value.trim().toUpperCase());
    } catch (Exception ex) {
      return null;
    }
  }

  private DiscountType parseDiscountType(String value) {
    if (isBlank(value)) {
      return null;
    }
    try {
      return DiscountType.valueOf(value.trim().toUpperCase());
    } catch (Exception ex) {
      return null;
    }
  }

  private Boolean parseBoolean(String value) {
    if (isBlank(value)) {
      return null;
    }
    return Boolean.parseBoolean(value.trim());
  }

  private Long parseLong(String value) {
    if (isBlank(value)) {
      return null;
    }
    try {
      return Long.parseLong(value.trim());
    } catch (Exception ex) {
      return null;
    }
  }

  private Integer parseInt(String value) {
    if (isBlank(value)) {
      return null;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (Exception ex) {
      return null;
    }
  }

  private BigDecimal parseDecimal(String value) {
    if (isBlank(value)) {
      return null;
    }
    try {
      return new BigDecimal(value.trim());
    } catch (Exception ex) {
      return null;
    }
  }

  private boolean isWithin(LocalDate value, LocalDate from, LocalDate to) {
    if (from == null && to == null) {
      return true;
    }
    if (value == null) {
      return false;
    }
    boolean afterFrom = from == null || !value.isBefore(from);
    boolean beforeTo = to == null || !value.isAfter(to);
    return afterFrom && beforeTo;
  }

  private boolean isBetween(BigDecimal value, BigDecimal min, BigDecimal max) {
    if (min == null && max == null) {
      return true;
    }
    if (value == null) {
      return false;
    }
    boolean above = min == null || value.compareTo(min) >= 0;
    boolean below = max == null || value.compareTo(max) <= 0;
    return above && below;
  }
}
