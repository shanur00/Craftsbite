package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.utils.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImplementation implements CartService{
  @Autowired
  private CartRepository cartRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private AuthUtil authUtil;

  @Autowired
  private CartItemRepository cartItemRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Override
  public CartDTO addProductToCart(Long productId, Integer quantity) {

    Cart cart = createCart();


    Product product = productRepository.findById(productId)
      .orElseThrow(() -> new ResourceNotFoundException("Product", "productID", productId));


    CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);
    if(cartItem!=null){
      throw new APIException("Product "+ product.getProductName() +" is already in the cart");
    }

    if(product.getQuantity()==0){
      throw new APIException(product.getProductName() +" is not available");
    }

    if(product.getQuantity()<quantity){
      throw new APIException(product.getProductName() +" is out of stock");
    }


    CartItem newCartItem = new CartItem();
    newCartItem.setProduct(product);
    newCartItem.setCart(cart);
    newCartItem.setQuantity(quantity);
    newCartItem.setDiscount(product.getDiscount());
    newCartItem.setProductPrice(product.getPrice());


    cartItemRepository.save(newCartItem);

    product.setQuantity(product.getQuantity()-quantity);
    cart.setTotalPrice(cart.getTotalPrice()+(product.getSpacialPrice()*quantity));
    cartRepository.save(cart);

    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

    List<CartItem> cartItems = cart.getCartItems();

    Stream<ProductDTO> producStream = cartItems.stream().map(cartsItem->{
      ProductDTO productDTO = modelMapper.map(cartsItem, ProductDTO.class);
      productDTO.setQuantity(cartsItem.getQuantity());

      return productDTO;
    });

    cartDTO.setProducts(producStream.toList());
    return cartDTO;
  }

  @Override
  public List<CartDTO> getAllCarts() {
    List<Cart> cartList = cartRepository.findAll();

    if(cartList.isEmpty()){
      throw new APIException("No Cart Exist");
    }

    return cartList.stream().map(cart -> {

      CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

      List<ProductDTO> productDTOS = cart.getCartItems().stream().map(cartItem->{
        ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
        productDTO.setQuantity(cartItem.getQuantity());
        return productDTO;
      }).collect(Collectors.toList());

      cartDTO.setProducts(productDTOS);

      return cartDTO;
    }).toList();
  }

  @Override
  public CartDTO getCart(String emailId, Long cartId) {
    Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
    if(cart==null){
      throw new ResourceNotFoundException("Cart","CartId",cartId);
    }

    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);


    cart.getCartItems().forEach(cartItem -> cartItem.getProduct().setQuantity(cartItem.getQuantity()));

    List<ProductDTO> productDTOS = cart.getCartItems().stream()
      .map(cartItem -> modelMapper.map(cartItem.getProduct(), ProductDTO.class)).toList();
    cartDTO.setProducts(productDTOS);
    return cartDTO;
  }

  @Transactional
  @Override
  public String deleteProductFromCart(Long cartId, Long productId) {
    Cart cart = cartRepository.findById(cartId).orElseThrow(
      ()-> new ResourceNotFoundException("Cart","cartID", cartId)
    );

    CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

    if(cartItem==null){
      throw new ResourceNotFoundException("Product","productID",productId);
    }

    cart.setTotalPrice(cart.getTotalPrice()-(cartItem.getProduct().getSpacialPrice()*cartItem.getQuantity()));

    cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

    return "Product removed From Cart";
  }

  @Transactional
  @Override
  public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
    String emailId = authUtil.loggedInEmail();
    Cart cartUser = cartRepository.findCartByEmail(emailId);
    Long cartId = cartUser.getCartId();

    Cart cart = cartRepository.findById(cartId).orElseThrow(
      ()-> new ResourceNotFoundException("Cart","CartId",cartId)
    );

    Product product = productRepository.findById(productId).orElseThrow(
      ()-> new ResourceNotFoundException("Product", "ProductId", productId)
    );

    if(product.getQuantity()==0){
      throw new APIException("Product is not available!!");
    }

    if(product.getQuantity()<quantity){
      throw new APIException("Please, make an order of the " + product.getProductName()
        + " less than or equal to the quantity " + product.getQuantity() + ".");
    }

    CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

    if(cartItem==null){
      throw new APIException(product.getProductName() +" is not available in the cart!!!");
    }

    int newQuantity = cartItem.getQuantity()+quantity;
    if(newQuantity<0){
      throw new APIException("Quantity can not be negative!!");
    }

    if(newQuantity==0){
      deleteProductFromCart(cartId,productId);
    }

    else {
      cartItem.setProductPrice(product.getSpacialPrice());
      cartItem.setQuantity(cartItem.getQuantity() + quantity);
      cartItem.setDiscount(product.getDiscount());

      cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));

      cartRepository.save(cart);
    }

    CartItem cartItemUpdated = cartItemRepository.save(cartItem);

    if(cartItemUpdated.getQuantity()==0){
      cartItemRepository.deleteById(cartItemUpdated.getCartItemId());
    }

    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

    List<CartItem> cartItems = cart.getCartItems();

    Stream<ProductDTO> productStream = cartItems.stream().map(
      cartsItem->{
        ProductDTO productDTO = modelMapper.map(cartsItem.getProduct(), ProductDTO.class);
        productDTO.setQuantity(cartsItem.getQuantity());
        return productDTO;
      }
    );

    cartDTO.setProducts(productStream.toList());
    return cartDTO;
  }

  @Override
  public void updateProductInCarts(Long cartId, Long productId) {
    Cart cart = cartRepository.findById(cartId).orElseThrow(
      ()-> new ResourceNotFoundException("Cart", "CartId", cartId)
    );

    Product product = productRepository.findById(productId).orElseThrow(
      ()-> new ResourceNotFoundException("Product", "ProductId", productId)
    );

    CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

    if(cartItem==null){
      throw new APIException("Product Not available!!");
    }

    double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice()*cartItem.getQuantity());

    cartItem.setProductPrice(product.getSpacialPrice());

    cart.setTotalPrice(cartPrice + (cartItem.getProductPrice()*cartItem.getQuantity()));

    cartItemRepository.save(cartItem);
  }


  private Cart createCart() {
    Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
    if(userCart!=null){
      return userCart;
    }

    Cart cart = new Cart();
    cart.setTotalPrice(0.0);
    cart.setUser(authUtil.loggedInUser());

    return cartRepository.save(cart);
  }
}
