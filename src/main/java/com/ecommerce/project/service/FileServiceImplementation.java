package com.ecommerce.project.service;

import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import org.springframework.data.domain.Page;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImplementation implements FileService{
  private final ModelMapper modelMapper;

  public FileServiceImplementation(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
  }

  @Override
  public String uploadImage(String path, MultipartFile file) throws IOException {
    String originalFileName = file.getOriginalFilename();

    String randomId = UUID.randomUUID().toString();

    String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf(".")));

    String filePath = path + File.separator + fileName;

    File folder = new File(path);
    if(!folder.exists()){
      folder.mkdir();
    }

    Files.copy(file.getInputStream(), Paths.get(filePath));
    return fileName;
  }

  @Override
  public ProductResponse getAllResponse(List<Product> products, Page<Product> productPage) {
    List<ProductDTO> productDTOS = products.stream()
      .map(product -> modelMapper.map(product, ProductDTO.class))
      .toList();

    ProductResponse productResponse = new ProductResponse();

    productResponse.setContent(productDTOS);
    productResponse.setPageNumber(productPage.getNumber());
    productResponse.setPageSize(productPage.getSize());
    productResponse.setTotalElements(productPage.getTotalElements());
    productResponse.setTotalPage(productPage.getTotalPages());
    productResponse.setLastPage(productPage.isLast());

    return productResponse;
  }
}
