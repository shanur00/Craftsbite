package com.ecommerce.project.repository;

import com.ecommerce.project.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {
  @Query("SELECT c FROM Cart c WHERE c.user.email =?1")
  Cart findCartByEmail (String email);

  @Query("SELECT c FROM Cart c WHERE c.user.email =?1 AND c.cartId= ?2")
  Cart findCartByEmailAndCartId(String emailId, Long cartId);

  /*
     SELECT c FROM Cart c:

         This part selects the Cart (c) entity.
         c is an alias for the Cart entity in the query.

     JOIN FETCH c.cartItems ci:

         This performs a join between the Cart (c) and its cartItems (ci), which is the list of items in the cart.
         JOIN FETCH ensures that the related cartItems are fetched eagerly in a single query (avoiding lazy loading).

     JOIN FETCH ci.product p:

         This performs another join between the CartItem (ci) and the associated Product (p).
         p refers to the product linked to the cart item, and this is also fetched eagerly.

     WHERE p.productId = ?1:

         This filters the query based on the productId provided as a parameter (?1).
         It ensures the query only returns carts that contain a specific product.
   */
  @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems ci JOIN FETCH ci.product p WHERE p.productId =?1")
  List<Cart> findCartsByProductId(Long productId);
}
