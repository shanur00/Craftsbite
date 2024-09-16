package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.repository.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderImplementation implements OrderService{
  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private CartRepository cartRepository;

  @Autowired
  private OrderItemRepository orderItemRepository;

  @Autowired
  private AddressRepository addressRepository;

  @Autowired
  private PaymentRepository paymentRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private CartService cartService;
  @Autowired
  private ModelMapper modelMapper;

  @Override
  @Transactional
  public OrderDTO placeOrder(String email, Long addressId, String paymentMethode, String pgName, String pgPaymentId,
                             String pgStatus, String pgResponseMessage) {

    Cart cart = cartRepository.findCartByEmail(email);
    if(cart==null){
      throw new APIException("Cart not found");
    }

    Address address = addressRepository.findById(addressId).orElseThrow(
      ()-> new ResourceNotFoundException("Cart", "Email", email)
    );

    Orders order = new Orders();
    order.setEmail(email);
    order.setOrderDate(LocalDate.now());
    order.setTotalAmount(cart.getTotalPrice());
    order.setOrderStatus("Order Accepted!");
    order.setAddress(address);

    Payment payment = new Payment(paymentMethode, pgPaymentId, pgStatus, pgResponseMessage, pgName);
    payment.setOrder(order);
    paymentRepository.save(payment);

    order.setPayment(payment);
    Orders savedOrder = orderRepository.save(order);

    List<CartItem> cartItems = cart.getCartItems();
    if(cartItems.isEmpty()){
      throw new APIException("Cart not found");
    }

    List<OrderItem> orderItems = new ArrayList<>();

    for(CartItem cartItem : cartItems){
      OrderItem orderItem = new OrderItem();
      orderItem.setProduct(cartItem.getProduct());
      orderItem.setQuantity(cartItem.getQuantity());
      orderItem.setDiscount(cartItem.getDiscount());
      orderItem.setOrderedProductPrice(cartItem.getProductPrice());
      orderItem.setOrder(savedOrder);

      orderItems.add(orderItem);
    }

    orderItemRepository.saveAll(orderItems);

    cart.getCartItems().forEach(cartItem -> {
      int quantity = cartItem.getQuantity();
      Product product = cartItem.getProduct();

      product.setQuantity(product.getQuantity() - quantity);

      productRepository.save(product);

      cartService.deleteProductFromCart(cart.getCartId(), cartItem.getProduct().getProductId());
    });

    OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);

    orderItems.forEach(orderItem -> orderDTO.getOrderItems().add(modelMapper.map(orderItem, OrderItemDTO.class)));

    return orderDTO;
  }
}
