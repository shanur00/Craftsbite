package com.ecommerce.project.repository;

import com.ecommerce.project.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
  @Query("SELECT c FROM CartItem c WHERE c.cart.cartId = ?1 AND c.product.productId=?2")
  CartItem findCartItemByProductIdAndCartId(Long cartId, Long productId);

  @Modifying
  @Query("DELETE FROM CartItem c WHERE c.cart.cartId =?1 AND c.product.productId =?2")
  void deleteCartItemByProductIdAndCartId(Long cartId, Long productId);
}
