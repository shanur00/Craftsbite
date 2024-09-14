package com.ecommerce.project.controller;

import com.ecommerce.project.model.Cart;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
public class CartController {

  @Autowired
  private CartService cartService;

  @Autowired
  AuthUtil authUtil;

  @Autowired
  CartRepository cartRepository;

  @PostMapping("/product/{productId}/quantity/{quantity}")
  private ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId,
                                                   @PathVariable Integer quantity){
    CartDTO cartDTO = cartService.addProductToCart(productId, quantity);

    return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.CREATED);
  }

  @GetMapping()
  public ResponseEntity<List<CartDTO>> getAllCarts(){
    List<CartDTO> cartDTOList = cartService.getAllCarts();
    return new ResponseEntity<List<CartDTO>>(cartDTOList, HttpStatus.FOUND);
  }

  @GetMapping("/user/cart")
  public ResponseEntity<CartDTO> getCartById(){
    String emailId = authUtil.loggedInEmail();
    Cart cart = cartRepository.findCartByEmail(emailId);

    Long cartId = cart.getCartId();

    CartDTO cartDTO = cartService.getCart(emailId, cartId);

    return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
  }

  @PutMapping("/product/{productId}/quantity/{operation}")
  public ResponseEntity<CartDTO> updateCartProduct(@PathVariable Long productId,
                                                   @PathVariable String operation){
    CartDTO cartDTO = cartService.updateProductQuantityInCart(productId,
      operation.equalsIgnoreCase("delete")? -1 : 1);

    return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
  }


  @DeleteMapping("/{cartId}/product/{productId}")
  private ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId,
                                                       @PathVariable Long productId){
    String status = cartService.deleteProductFromCart(cartId, productId);

    return new ResponseEntity<String>(status, HttpStatus.OK);
  }
}
