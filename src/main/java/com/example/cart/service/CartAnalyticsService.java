package com.example.cart.service;

import com.example.cart.dto.CartFinalTotalDto;
import com.example.cart.dto.CartEfficiencyDto;
import com.example.cart.dto.CartTotalDto;
import com.example.cart.dto.CategoryCountDto;
import com.example.cart.dto.CustomerCartCountDto;
import com.example.cart.dto.ProductCountDto;
import com.example.cart.dto.PromoCodeDiscountDto;
import com.example.cart.model.Cart;
import com.example.cart.model.CartItem;
import com.example.cart.model.DiscountType;
import com.example.cart.model.Product;
import com.example.cart.model.PromoCode;
import com.example.cart.repo.CartItemRepository;
import com.example.cart.repo.CartRepository;
import com.example.cart.repo.PromoCodeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartAnalyticsService extends AnalyticsBase {
  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
  private static final BigDecimal WEIGHT_TOTAL = new BigDecimal("0.5");
  private static final BigDecimal WEIGHT_ITEMS = new BigDecimal("0.3");
  private static final BigDecimal WEIGHT_DISTINCT = new BigDecimal("0.2");

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final PromoCodeRepository promoCodeRepository;

  public List<CartTotalDto> calculateActiveCartTotals() {
    return cartRepository.findByActiveTrue().stream()
        .map(cart -> new CartTotalDto(cart.getCartId(), calculateCartTotal(cart)))
        .toList();
  }

  public List<ProductCountDto> top5ProductsByAdditions() {
    List<CartItem> items = cartItemRepository.findAll();
    Map<Long, String> names = new HashMap<>();
    Map<Long, Long> counts = items.stream()
        .filter(item -> item.getSourceProductId() != null)
        .peek(item -> names.putIfAbsent(
            item.getSourceProductId(),
            item.getName()))
        .collect(Collectors.groupingBy(
            CartItem::getSourceProductId,
            Collectors.summingLong(CartItem::getQuantity)));

    return counts.entrySet().stream()
        .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder()))
        .limit(5)
        .map(entry -> new ProductCountDto(
            entry.getKey(),
            names.get(entry.getKey()),
            entry.getValue()))
        .toList();
  }

  public List<CustomerCartCountDto> activeCartCountPerCustomer() {
    List<Cart> activeCarts = cartRepository.findByActiveTrue();
    Map<Long, Long> counts = activeCarts.stream()
        .filter(cart -> cart.getCustomer() != null)
        .collect(Collectors.groupingBy(
            cart -> cart.getCustomer().getCustomerId(),
            Collectors.counting()));

    Map<Long, String> emails = activeCarts.stream()
        .filter(cart -> cart.getCustomer() != null)
        .collect(Collectors.toMap(
            cart -> cart.getCustomer().getCustomerId(),
            cart -> cart.getCustomer().getEmail(),
            (a, b) -> a));

    return counts.entrySet().stream()
        .map(entry -> new CustomerCartCountDto(
            entry.getKey(),
            emails.get(entry.getKey()),
            entry.getValue()))
        .toList();
  }

  public List<CategoryCountDto> categoryItemCounts() {
    CategoryNormalizer normalizer = this;
    Map<String, Long> counts = cartItemRepository.findAll().stream()
        .filter(item -> item.getCategory() != null)
        .collect(Collectors.groupingBy(
            item -> normalizer.normalizeCategory(item.getCategory()),
            Collectors.summingLong(CartItem::getQuantity)));

    return counts.entrySet().stream()
        .map(entry -> new CategoryCountDto(entry.getKey(), entry.getValue()))
        .toList();
  }

  public List<CartFinalTotalDto> finalTotalsWithPromo() {
    return cartRepository.findAll().stream()
        .filter(cart -> cart.getPromoCode() != null && !cart.getPromoCode().isBlank())
        .map(cart -> {
          BigDecimal total = calculateCartTotal(cart);
          PromoCode promo = promoCodeRepository.findById(cart.getPromoCode()).orElse(null);
          BigDecimal discount = calculateDiscount(cart, promo, true);
          BigDecimal finalTotal = total.subtract(discount).max(BigDecimal.ZERO);
          return new CartFinalTotalDto(cart.getCartId(), cart.getPromoCode(), finalTotal);
        })
        .toList();
  }

  public List<PromoCode> activePromoCodesNeverUsed() {
    Set<String> usedCodes = cartRepository.findAll().stream()
        .map(Cart::getPromoCode)
        .filter(code -> code != null && !code.isBlank())
        .collect(Collectors.toSet());

    return promoCodeRepository.findByActiveTrue().stream()
        .filter(promo -> !usedCodes.contains(promo.getCode()))
        .toList();
  }

  public List<PromoCodeDiscountDto> totalDiscountIfAppliedToEligibleCarts() {
    List<Cart> carts = cartRepository.findAll();

    return promoCodeRepository.findAll().stream()
        .map(promo -> {
          BigDecimal totalDiscount = carts.stream()
              .map(cart -> calculateDiscount(cart, promo, false))
              .reduce(BigDecimal.ZERO, BigDecimal::add);
          return new PromoCodeDiscountDto(promo.getCode(), totalDiscount);
        })
        .toList();
  }

  public List<Product> productsInCartsOutOfStock() {
    Map<Long, Product> products = cartItemRepository.findAll().stream()
        .filter(item -> item.getSourceProductId() != null)
        .filter(item -> !item.isInStock())
        .collect(Collectors.toMap(CartItem::getSourceProductId, item -> item, (a, b) -> a));

    return products.values().stream().toList();
  }

  public List<CartEfficiencyDto> weightedCartEfficiencyScores() {
    List<Cart> carts = cartRepository.findAll();
    if (carts.isEmpty()) {
      return List.of();
    }

    Map<Long, BigDecimal> totals = new HashMap<>();
    Map<Long, Long> itemCounts = new HashMap<>();
    Map<Long, Long> distinctProducts = new HashMap<>();

    for (Cart cart : carts) {
      totals.put(cart.getCartId(), calculateCartTotal(cart));

      long items = cart.getItems().stream()
          .mapToLong(CartItem::getQuantity)
          .sum();
      itemCounts.put(cart.getCartId(), items);

      long distinct = cart.getItems().stream()
          .map(CartItem::getSourceProductId)
          .filter(Objects::nonNull)
          .distinct()
          .count();
      distinctProducts.put(cart.getCartId(), distinct);
    }

    BigDecimal maxTotal = totals.values().stream()
        .max(Comparator.naturalOrder())
        .orElse(BigDecimal.ZERO);
    long maxItems = itemCounts.values().stream().max(Long::compareTo).orElse(0L);
    long maxDistinct = distinctProducts.values().stream().max(Long::compareTo).orElse(0L);

    return carts.stream()
        .map(cart -> {
          BigDecimal total = totals.getOrDefault(cart.getCartId(), BigDecimal.ZERO);
          long items = itemCounts.getOrDefault(cart.getCartId(), 0L);
          long distinct = distinctProducts.getOrDefault(cart.getCartId(), 0L);

          BigDecimal totalScore = normalize(total, maxTotal);
          BigDecimal itemScore = normalize(BigDecimal.valueOf(items), BigDecimal.valueOf(maxItems));
          BigDecimal distinctScore = normalize(BigDecimal.valueOf(distinct), BigDecimal.valueOf(maxDistinct));

          BigDecimal weightedScore = totalScore.multiply(WEIGHT_TOTAL)
              .add(itemScore.multiply(WEIGHT_ITEMS))
              .add(distinctScore.multiply(WEIGHT_DISTINCT))
              .multiply(ONE_HUNDRED)
              .setScale(2, RoundingMode.HALF_UP);

          return new CartEfficiencyDto(cart.getCartId(), total, items, distinct, weightedScore);
        })
        .sorted(Comparator.comparing(CartEfficiencyDto::getScore).reversed())
        .toList();
  }

  private BigDecimal calculateCartTotal(Cart cart) {
    return cart.getItems().stream()
        .map(item -> item.getBasePrice()
            .multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal calculateDiscount(Cart cart, PromoCode promo, boolean requireActive) {
    if (promo == null) {
      return BigDecimal.ZERO;
    }
    if (requireActive && !promo.isActive()) {
      return BigDecimal.ZERO;
    }

    BigDecimal eligibleTotal = cart.getItems().stream()
        .filter(item -> isPromoApplicable(promo, item))
        .map(item -> item.getBasePrice()
            .multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (eligibleTotal.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    if (promo.getDiscountType() == DiscountType.PERCENT) {
      return eligibleTotal.multiply(promo.getDiscountValue())
          .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    return promo.getDiscountValue().min(eligibleTotal);
  }

  private boolean isPromoApplicable(PromoCode promo, Product product) {
    if (promo.getApplicableCategory() == null || promo.getApplicableCategory().isBlank()) {
      return true;
    }
    return promo.getApplicableCategory().equalsIgnoreCase(product.getCategory());
  }

  private BigDecimal normalize(BigDecimal value, BigDecimal max) {
    if (max.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return value.divide(max, 4, RoundingMode.HALF_UP);
  }
}
