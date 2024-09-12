package com.ecommerce.project.service;

import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {
  String uploadImage(String path, MultipartFile file) throws IOException;

  ProductResponse getAllResponse (List<Product>products, Page<Product> productPage);
}
