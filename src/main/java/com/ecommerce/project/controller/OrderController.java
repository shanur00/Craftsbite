package com.ecommerce.project.controller;

import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderRequestDTO;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {
  @Autowired
  private OrderService orderService;

  @Autowired
  private AuthUtil authUtil;

  @PostMapping("/order/users/payment/{paymentMethode}")
  public ResponseEntity<OrderDTO> orderProduct(@PathVariable String paymentMethode, @RequestBody OrderRequestDTO orderRequestDTO) {
    String email = authUtil.loggedInEmail();

    OrderDTO orderDTO = orderService.placeOrder(
      email,
      orderRequestDTO.getAddressId(),
      paymentMethode,
      orderRequestDTO.getPgName(),
      orderRequestDTO.getPgPaymentId(),
      orderRequestDTO.getPgStatus(),
      orderRequestDTO.getPgResponseMessage()
    );

    return new ResponseEntity<OrderDTO>(orderDTO, HttpStatus.CREATED);
  }
}
