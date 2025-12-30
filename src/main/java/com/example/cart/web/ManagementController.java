package com.example.cart.web;

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
import com.example.cart.service.DataSearchService;
import com.example.cart.service.ViewDataService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ManagementController {
  private final CustomerRepository customerRepository;
  private final ProductRepository productRepository;
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final PromoCodeRepository promoCodeRepository;
  private final DataSearchService dataSearchService;
  private final ViewDataService viewDataService;

  @PostMapping("/manage/customer/create")
  public String createCustomer(
      @RequestParam String email,
      @RequestParam(required = false) String registrationDate,
      @RequestParam(defaultValue = "BRONZE") LoyaltyLevel loyaltyLevel,
      RedirectAttributes redirectAttributes) {
    LocalDate date = (registrationDate == null || registrationDate.isBlank())
        ? LocalDate.now()
        : LocalDate.parse(registrationDate);

    Customer customer = new Customer();
    customer.setEmail(email);
    customer.setRegistrationDate(date);
    customer.setLoyaltyLevel(loyaltyLevel);
    customerRepository.save(customer);

    return redirectWithMessage(redirectAttributes, "Покупатель добавлен", "customers");
  }

  @PostMapping("/manage/customer/delete")
  public String deleteCustomer(@RequestParam Long customerId, RedirectAttributes redirectAttributes) {
    if (cartRepository.existsByCustomer_CustomerId(customerId)) {
      return redirectWithMessage(
          redirectAttributes,
          "Нельзя удалить покупателя: есть связанные корзины",
          "customers");
    }
    if (customerRepository.existsById(customerId)) {
      customerRepository.deleteById(customerId);
      return redirectWithMessage(redirectAttributes, "Покупатель удален", "customers");
    }
    return redirectWithMessage(redirectAttributes, "Покупатель не найден", "customers");
  }

  @PostMapping("/manage/customer/update")
  public String updateCustomer(
      @RequestParam Long customerId,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String registrationDate,
      @RequestParam(required = false) String loyaltyLevel,
      RedirectAttributes redirectAttributes) {
    Optional<Customer> customerOptional = customerRepository.findById(customerId);
    if (customerOptional.isEmpty()) {
      return redirectWithMessage(redirectAttributes, "Покупатель не найден", "customers");
    }

    Customer customer = customerOptional.get();
    if (email != null && !email.isBlank()) {
      customer.setEmail(email.trim());
    }
    if (registrationDate != null && !registrationDate.isBlank()) {
      customer.setRegistrationDate(LocalDate.parse(registrationDate));
    }
    if (loyaltyLevel != null && !loyaltyLevel.isBlank()) {
      customer.setLoyaltyLevel(LoyaltyLevel.valueOf(loyaltyLevel.trim().toUpperCase()));
    }

    customerRepository.save(customer);
    return redirectWithMessage(redirectAttributes, "Покупатель обновлен", "customers");
  }

  @PostMapping("/manage/product/create")
  public String createProduct(
      @RequestParam String name,
      @RequestParam String category,
      @RequestParam BigDecimal basePrice,
      @RequestParam(defaultValue = "false") boolean inStock,
      RedirectAttributes redirectAttributes) {
    Product product = new Product();
    product.setName(name);
    product.setCategory(category);
    product.setBasePrice(basePrice);
    product.setInStock(inStock);
    productRepository.save(product);

    return redirectWithMessage(redirectAttributes, "Товар добавлен", "products");
  }

  @PostMapping("/manage/product/delete")
  public String deleteProduct(@RequestParam Long productId, RedirectAttributes redirectAttributes) {
    if (productRepository.existsProductById(productId)) {
      productRepository.deleteById(productId);
      return redirectWithMessage(redirectAttributes, "Товар удален", "products");
    }
    return redirectWithMessage(redirectAttributes, "Товар не найден", "products");
  }

  @PostMapping("/manage/product/update")
  public String updateProduct(
      @RequestParam Long productId,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String basePrice,
      @RequestParam(required = false) String inStock,
      RedirectAttributes redirectAttributes) {
    Optional<Product> productOptional = productRepository.findProductById(productId);
    if (productOptional.isEmpty()) {
      return redirectWithMessage(redirectAttributes, "Товар не найден", "products");
    }

    Product product = productOptional.get();
    if (name != null && !name.isBlank()) {
      product.setName(name.trim());
    }
    if (category != null && !category.isBlank()) {
      product.setCategory(category.trim());
    }
    if (basePrice != null && !basePrice.isBlank()) {
      product.setBasePrice(new BigDecimal(basePrice));
    }
    Boolean inStockValue = parseBoolean(inStock);
    if (inStockValue != null) {
      product.setInStock(inStockValue);
    }

    productRepository.save(product);
    return redirectWithMessage(redirectAttributes, "Товар обновлен", "products");
  }

  @PostMapping("/manage/cart/create")
  public String createCart(
      @RequestParam Long customerId,
      @RequestParam(required = false) String promoCode,
      @RequestParam(defaultValue = "false") boolean active,
      RedirectAttributes redirectAttributes) {
    Optional<Customer> customer = customerRepository.findById(customerId);
    if (customer.isEmpty()) {
      return redirectWithMessage(redirectAttributes, "Покупатель не найден", "carts");
    }

    Cart cart = new Cart();
    cart.updateCustomer(customer.get());
    cart.updateCreatedDate(LocalDateTime.now());
    cart.updateActive(active);
    cart.updatePromoCode(promoCode);
    cartRepository.save(cart);

    return redirectWithMessage(redirectAttributes, "Корзина создана", "carts");
  }

  @PostMapping("/manage/cart/delete")
  public String deleteCart(@RequestParam Long cartId, RedirectAttributes redirectAttributes) {
    if (cartItemRepository.existsByCart_CartId(cartId)) {
      return redirectWithMessage(
          redirectAttributes,
          "Нельзя удалить корзину: есть позиции",
          "carts");
    }
    if (cartRepository.existsById(cartId)) {
      cartRepository.deleteById(cartId);
      return redirectWithMessage(redirectAttributes, "Корзина удалена", "carts");
    }
    return redirectWithMessage(redirectAttributes, "Корзина не найдена", "carts");
  }

  @PostMapping("/manage/cart-item/create")
  public String createCartItem(
      @RequestParam Long cartId,
      @RequestParam Long productId,
      @RequestParam int quantity,
      RedirectAttributes redirectAttributes) {
    if (quantity <= 0) {
      return redirectWithMessage(redirectAttributes, "Количество должно быть больше нуля", "carts");
    }

    Optional<Cart> cart = cartRepository.findById(cartId);
    Optional<Product> product = productRepository.findProductById(productId);
    if (cart.isEmpty() || product.isEmpty()) {
      return redirectWithMessage(redirectAttributes, "Корзина или товар не найдены", "carts");
    }

    CartItem item = new CartItem();
    item.updateCart(cart.get());
    item.updateProductSnapshot(product.get());
    item.updateQuantity(quantity);
    item.updateAddedDate(LocalDateTime.now());
    cartItemRepository.save(item);

    return redirectWithMessage(redirectAttributes, "Позиция добавлена", "carts");
  }

  @PostMapping("/manage/cart-item/delete")
  public String deleteCartItem(@RequestParam Long cartItemId, RedirectAttributes redirectAttributes) {
    if (cartItemRepository.existsById(cartItemId)) {
      cartItemRepository.deleteById(cartItemId);
      return redirectWithMessage(redirectAttributes, "Позиция удалена", "carts");
    }
    return redirectWithMessage(redirectAttributes, "Позиция не найдена", "carts");
  }

  @PostMapping("/manage/cart/update")
  public String updateCart(
      @RequestParam Long cartId,
      @RequestParam(required = false) Long customerId,
      @RequestParam(required = false) String promoCode,
      @RequestParam(defaultValue = "false") boolean clearPromo,
      @RequestParam Boolean active,
      RedirectAttributes redirectAttributes) {
    Optional<Cart> cartOptional = cartRepository.findById(cartId);
    if (cartOptional.isEmpty()) {
      return redirectWithMessage(redirectAttributes, "Корзина не найдена", "carts");
    }

    Cart cart = cartOptional.get();
    if (customerId != null) {
      Optional<Customer> customer = customerRepository.findById(customerId);
      if (customer.isEmpty()) {
        return redirectWithMessage(redirectAttributes, "Покупатель не найден", "carts");
      }
      cart.updateCustomer(customer.get());
    }

    if (clearPromo) {
      cart.updatePromoCode(null);
    } else if (promoCode != null && !promoCode.isBlank()) {
      cart.updatePromoCode(promoCode);
    }

    if (active != null) {
      cart.updateActive(active);
    }

    cartRepository.save(cart);
    return redirectWithMessage(redirectAttributes, updatedEntityMessage(cart.getEntityLabel()), "carts");
  }

  @PostMapping("/manage/cart-item/update")
  public String updateCartItem(
      @RequestParam Long cartItemId,
      @RequestParam(required = false) Long cartId,
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false) Integer quantity,
      RedirectAttributes redirectAttributes) {
    Optional<CartItem> itemOptional = cartItemRepository.findById(cartItemId);
    if (itemOptional.isEmpty()) {
      return redirectWithMessage(redirectAttributes, "Позиция корзины не найдена", "carts");
    }

    CartItem item = itemOptional.get();
    if (cartId != null) {
      Optional<Cart> cart = cartRepository.findById(cartId);
      if (cart.isEmpty()) {
        return redirectWithMessage(redirectAttributes, "Корзина не найдена", "carts");
      }
      item.updateCart(cart.get());
    }

    if (productId != null) {
      Optional<Product> product = productRepository.findProductById(productId);
      if (product.isEmpty()) {
        return redirectWithMessage(redirectAttributes, "Товар не найден", "carts");
      }
      item.updateProductSnapshot(product.get());
    }

    if (quantity != null) {
      if (quantity <= 0) {
        return redirectWithMessage(redirectAttributes, "Количество должно быть больше нуля", "carts");
      }
      item.updateQuantity(quantity);
    }

    cartItemRepository.save(item);
    return redirectWithMessage(redirectAttributes, updatedEntityMessage(item.getEntityLabel()), "carts");
  }

  @PostMapping("/manage/promo/create")
  public String createPromo(
      @RequestParam String code,
      @RequestParam DiscountType discountType,
      @RequestParam BigDecimal discountValue,
      @RequestParam(defaultValue = "false") boolean active,
      @RequestParam(required = false) String applicableCategory,
      RedirectAttributes redirectAttributes) {
    PromoCode promoCode = new PromoCode();
    promoCode.setCode(code.trim());
    promoCode.setDiscountType(discountType);
    promoCode.setDiscountValue(discountValue);
    promoCode.setActive(active);
    promoCode.setApplicableCategory(
        applicableCategory == null || applicableCategory.isBlank() ? null : applicableCategory.trim());
    promoCodeRepository.save(promoCode);

    return redirectWithMessage(redirectAttributes, "Промокод создан", "promos");
  }

  @PostMapping("/manage/promo/delete")
  public String deletePromo(@RequestParam String code, RedirectAttributes redirectAttributes) {
    if (promoCodeRepository.existsById(code)) {
      promoCodeRepository.deleteById(code);
      return redirectWithMessage(redirectAttributes, "Промокод удален", "promos");
    }
    return redirectWithMessage(redirectAttributes, "Промокод не найден", "promos");
  }

  @PostMapping("/manage/promo/update")
  public String updatePromo(
      @RequestParam String code,
      @RequestParam(required = false) String discountType,
      @RequestParam(required = false) String discountValue,
      @RequestParam(required = false) String active,
      @RequestParam(required = false) String applicableCategory,
      @RequestParam(defaultValue = "false") boolean clearCategory,
      RedirectAttributes redirectAttributes) {
    Optional<PromoCode> promoOptional = promoCodeRepository.findById(code);
    if (promoOptional.isEmpty()) {
      return redirectWithMessage(redirectAttributes, "Промокод не найден", "promos");
    }

    PromoCode promoCode = promoOptional.get();
    if (discountType != null && !discountType.isBlank()) {
      promoCode.setDiscountType(DiscountType.valueOf(discountType.trim().toUpperCase()));
    }
    if (discountValue != null && !discountValue.isBlank()) {
      promoCode.setDiscountValue(new BigDecimal(discountValue));
    }
    Boolean activeValue = parseBoolean(active);
    if (activeValue != null) {
      promoCode.setActive(activeValue);
    }
    if (clearCategory) {
      promoCode.setApplicableCategory(null);
    } else if (applicableCategory != null && !applicableCategory.isBlank()) {
      promoCode.setApplicableCategory(applicableCategory.trim());
    }

    promoCodeRepository.save(promoCode);
    return redirectWithMessage(redirectAttributes, "Промокод обновлен", "promos");
  }

  @PostMapping("/manage/customer/search")
  public String searchCustomers(
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String loyaltyLevel,
      @RequestParam(required = false) String registeredFrom,
      @RequestParam(required = false) String registeredTo,
      Model model) {
    viewDataService.populateLists(model);
    model.addAttribute("customers",
        dataSearchService.searchCustomers(email, loyaltyLevel, registeredFrom, registeredTo));
    model.addAttribute("activeTab", "customers");
    return "index";
  }

  @PostMapping("/manage/product/search")
  public String searchProducts(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String inStock,
      @RequestParam(required = false) String minPrice,
      @RequestParam(required = false) String maxPrice,
      Model model) {
    viewDataService.populateLists(model);
    model.addAttribute("products",
        dataSearchService.searchProducts(name, category, inStock, minPrice, maxPrice));
    model.addAttribute("activeTab", "products");
    return "index";
  }

  @PostMapping("/manage/cart/search")
  public String searchCarts(
      @RequestParam(required = false) String customerId,
      @RequestParam(required = false) String active,
      @RequestParam(required = false) String promoCode,
      Model model) {
    viewDataService.populateLists(model);
    model.addAttribute("carts",
        dataSearchService.searchCarts(customerId, active, promoCode));
    model.addAttribute("activeTab", "carts");
    return "index";
  }

  @PostMapping("/manage/cart-item/search")
  public String searchCartItems(
      @RequestParam(required = false) String cartId,
      @RequestParam(required = false) String productId,
      @RequestParam(required = false) String minQuantity,
      Model model) {
    viewDataService.populateLists(model);
    model.addAttribute("cartItems",
        dataSearchService.searchCartItems(cartId, productId, minQuantity));
    model.addAttribute("activeTab", "carts");
    return "index";
  }

  @PostMapping("/manage/promo/search")
  public String searchPromos(
      @RequestParam(required = false) String discountType,
      @RequestParam(required = false) String active,
      @RequestParam(required = false) String applicableCategory,
      Model model) {
    viewDataService.populateLists(model);
    model.addAttribute("promoCodes",
        dataSearchService.searchPromos(discountType, active, applicableCategory));
    model.addAttribute("activeTab", "promos");
    return "index";
  }

  private String redirectWithMessage(
      RedirectAttributes redirectAttributes,
      String message,
      String activeTab) {
    redirectAttributes.addFlashAttribute("message", message);
    redirectAttributes.addFlashAttribute("activeTab", activeTab);
    return "redirect:/";
  }

  private Boolean parseBoolean(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return Boolean.parseBoolean(value);
  }

  private String updatedEntityMessage(String entityLabel) {
    return "Обновлено: " + entityLabel;
  }
}
