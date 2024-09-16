package com.ecommerce.project.service;

import com.ecommerce.project.payload.OrderDTO;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {
  OrderDTO placeOrder(String email, Long addressId, String paymentMethode, String pgName, String pgPaymentId,
                      String pgStatus, String pgResponseMessage);
}
