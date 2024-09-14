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
    /*
      Find existing cart or create One.
     */
    Cart cart = createCart();

    /*
      Retrieve Product Details
     */
    Product product = productRepository.findById(productId)
      .orElseThrow(() -> new ResourceNotFoundException("Product", "productID", productId));

    /*
      Perform Validations (Check if product already in the cart, Enough stock, Availability)
     */
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

    /*
      Create Cart item. ( One item as single product is being added)
     */
    CartItem newCartItem = new CartItem();
    newCartItem.setProduct(product);
    newCartItem.setCart(cart);
    newCartItem.setQuantity(quantity);
    newCartItem.setDiscount(product.getDiscount());
    newCartItem.setProductPrice(product.getPrice());

    /*
      Save Cart Item.
     */
    cartItemRepository.save(newCartItem);

    /*
      return Updated cart.
     */
    product.setQuantity(product.getQuantity()-quantity);
    cart.setTotalPrice(cart.getTotalPrice()+(product.getSpacialPrice()*quantity));
    cartRepository.save(cart);

    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

    List<CartItem> cartItems = cart.getCartItems();

    Stream<ProductDTO> producStream = cartItems.stream().map(cartsItem->{
      ProductDTO productDTO = modelMapper.map(cartsItem, ProductDTO.class);
      productDTO.setQuantity(cartsItem.getQuantity());

      /*
        Returning each productDTO to Stream.
       */
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

    /*
      Cart ----> CartItem ----> Product
      List<CartDTO> cartDTOS
     */
    return cartList.stream().map(cart -> {

      /*
        The modelMapper.map(cart, CartDTO.class) maps the cart entity to a CartDTO
            {
              "cartId": 1,
              "totalPrice": 39.18,
              "products": []
            }
       */
      CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

      /*
        For each product in the cart (cart.getCartItems()), the Product is mapped to a ProductDTO:
            {
              "productId": 1,
              "productName": "Travel Pillow",
              "image": "pillow.jpg",
              "description": "Comfortable travel pillow",
              "quantity": 1,
              "price": 19.99,
              "discount": 2.0,
              "spacialPrice": 17.99
            }
       */
      List<ProductDTO> productDTOS = cart.getCartItems().stream().map(cartItem->
        modelMapper.map(cartItem.getProduct(), ProductDTO.class)).collect(Collectors.toList());

      /*
        After mapping all products, the list of ProductDTO objects is assigned to the CartDTO:
            {
              "cartId": 1,
              "totalPrice": 39.18,
              "products": [
                {
                  "productId": 1,
                  "productName": "Travel Pillow",
                  "image": "pillow.jpg",
                  "description": "Comfortable travel pillow",
                  "quantity": 1,
                  "price": 19.99,
                  "discount": 2.0,
                  "spacialPrice": 17.99
                }
              ]
            }
       */
      cartDTO.setProducts(productDTOS);

      /*
        The entire process is applied to each cart in the list, and the resulting CartDTO objects are collected into a
        List<CartDTO>, which is the final return value of the method.
       */
      return cartDTO;
    }).toList();

    /*
      The final output is a list of CartDTO objects, where each CartDTO contains the cart details and a list of
      associated ProductDTO objects:
         [
            {
              "cartId": 1,
              "totalPrice": 39.18,
              "products": [
                {
                  "productId": 1,
                  "productName": "Travel Pillow",
                  "image": "pillow.jpg",
                  "description": "Comfortable travel pillow",
                  "quantity": 1,
                  "price": 19.99,
                  "discount": 2.0,
                  "spacialPrice": 17.99
                }
              ]
            }
          ]
     */
    /* return cartDTOS; */
  }

  @Override
  public CartDTO getCart(String emailId, Long cartId) {
    Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
    if(cart==null){
      throw new ResourceNotFoundException("Cart","CartId",cartId);
    }

    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

    /*
      Set the quantity of the product in the cart that the user ordered instead of showing actual quantity of
      the product in Website.
     */

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

    cart.setTotalPrice(cart.getTotalPrice()-cartItem.getProductPrice()*cartItem.getQuantity());

    cartRepository.save(cart);

    cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

    return "Product removed From Cart";
  }

  @Transactional
  @Override
  public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
    String emailId = authUtil.loggedInEmail();
    Cart cartUser = cartRepository.findCartByEmail(emailId);
    Long cartId = cartUser.getCartId();

    /*
      In the first, you’re searching for the cart using an email (findCartByEmail).
      In the second, you’re ensuring the cart exists by cart ID (findById).

      If you have two pieces of information: the email and the cartId, you need to ensure that the cart associated with the
      email is indeed the same cart as the one identified by the cart ID.

      There’s a possibility that the cart found by the email doesn’t match the cart found by ID. This check ensures you are
      handling the correct cart.
     */
    Cart cart = cartRepository.findById(cartId).orElseThrow(
      ()-> new ResourceNotFoundException("Cart","CartId",cartId)
    );

    Product product = productRepository.findById(productId).orElseThrow(
      ()-> new ResourceNotFoundException("Product", "ProductId", productId)
    );

    /*
      We are using carId to fetch cartItem so we do not use cart.setCartItem().
     */
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

    cartRepository.save(cart);
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
