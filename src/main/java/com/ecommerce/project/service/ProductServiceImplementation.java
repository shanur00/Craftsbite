package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.EmptyReturnListException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImplementation implements ProductService {
  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private FileService fileService;

  @Autowired
  private CartRepository cartRepository;

  @Autowired
  private CartService cartService;

  @Value("${project.image}")
  private String path;
  @Override
  public ProductDTO addProduct(Long categoryID, ProductDTO productDTO) {
    Product product = modelMapper.map(productDTO, Product.class);

    Category category = categoryRepository.findById(categoryID)
      .orElseThrow(()->new ResourceNotFoundException("Category", "CategoryId", categoryID));

    List<Product> products = category.getProducts();
    boolean ifProductNotPresent = false;
    for (Product value : products) {
      if (value.getProductName().equals(product.getProductName())) {
        throw new APIException("Product with the name " + product.getProductName() + " already exists");
      }
    }

    product.setCategory(category);
    double spacialPrice = product.getPrice() - (product.getDiscount()*0.01) * product.getPrice();
    product.setSpacialPrice(spacialPrice);
    product.setImage("default.png");
    Product savedProduct = productRepository.save(product);
    return modelMapper.map(savedProduct, ProductDTO.class);
  }

  @Override
  public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

    Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
      ? Sort.by(sortBy).ascending()
      : Sort.by(sortBy).descending();

    Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

    Page<Product> productPage = productRepository.findAll(pageDetails);

    List<Product> products = productPage.getContent();

    if(products.isEmpty()){
      throw new EmptyReturnListException("Product List is Empty");
    }

    return fileService.getAllResponse(products, productPage);
  }

  @Override
  public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

    Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
      ? Sort.by(sortBy).ascending()
      : Sort.by(sortBy).descending();

    Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

    Category category = categoryRepository.findById(categoryId)
      .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));

    Page<Product> productPage = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);
    List<Product> products = productPage.getContent();

    if(products.isEmpty()){
      throw new APIException("Product List is Empty with " + category.getCategoryName() + " Category");
    }

    return fileService.getAllResponse(products, productPage);
  }

  @Override
  public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

    Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
      ? Sort.by(sortBy).ascending()
      : Sort.by(sortBy).descending();

    Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

    Page<Product> productPage = productRepository.findByProductNameLikeIgnoreCase('%'+ keyword +'%', pageDetails);

    List<Product> products = productPage.getContent();

    if(products.isEmpty()){
      throw new APIException("Product not Found with the keyWord: " + keyword);  //Concat the String
    }

    return fileService.getAllResponse(products, productPage);
  }

  @Override
  public ProductDTO updateProduct(ProductDTO productDTO, Long productId) {
    Product product = modelMapper.map(productDTO,Product.class);
    Product myProduct = productRepository.findById(productId).
      orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));

    myProduct.setProductName(product.getProductName());
    myProduct.setDescription(product.getDescription());
    myProduct.setQuantity(product.getQuantity());
    myProduct.setDiscount(product.getDiscount());
    myProduct.setPrice(product.getPrice());
    myProduct.setSpacialPrice(product.getSpacialPrice());

    Product savedProduct = productRepository.save(myProduct);

    List<Cart> carts = cartRepository.findCartsByProductId(productId);

    List<CartDTO> cartDTOS = carts.stream().map(
      cart -> {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<ProductDTO> productDTOS = cart.getCartItems().stream().map(
          cartsItem -> modelMapper.map(cartsItem.getProduct(), ProductDTO.class)).toList();

        cartDTO.setProducts(productDTOS);

        return cartDTO;
      }
    ).toList();

    cartDTOS.forEach(cartDTO -> cartService.updateProductInCarts(cartDTO.getCartId(), productId));

    return modelMapper.map(savedProduct, ProductDTO.class);
  }

  @Override
  public ProductDTO deleteProduct(Long productId) {
    Product product = productRepository.findById(productId)
      .orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));

    List<Cart> carts = cartRepository.findCartsByProductId(productId);
    carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));

    productRepository.delete(product);
    return modelMapper.map(product, ProductDTO.class);
  }

  @Override
  public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
    Product product = productRepository.findById(productId)
      .orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));

    String fileName = fileService.uploadImage(path, image);

    product.setImage(fileName);

    Product updatedProduct = productRepository.save(product);

    return modelMapper.map(updatedProduct, ProductDTO.class);
  }
}
