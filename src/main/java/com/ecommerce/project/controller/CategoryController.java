package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CategoryController {
  @Autowired
  private CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

 /* @GetMapping("/echo")
  public ResponseEntity<String> echoMessage(@RequestParam(name = "message", defaultValue = "Hello World") String message){
    return new ResponseEntity<>("Echoed message: "+message, HttpStatus.OK);
  }*/

  @GetMapping("/public/categories")
  /* @RequestParam query parameters (যেমন, api/echo?message="John") এর জন্য ব্যবহৃত হয়। */
  //@RequestMapping(value = "/api/public/categories", method = RequestMethod.GET)
  public ResponseEntity<CategoryResponse> getAllCategory(@RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                         @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                         @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_CATEGORY_BY) String sortBy,
                                                         @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_ORDER) String sortOrder) {
    return new ResponseEntity<>(categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder), HttpStatus.OK);
  }

  @PostMapping("/public/categories")
  public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
    CategoryDTO savedCategoryDTO = categoryService.createCategory(categoryDTO);
    return new ResponseEntity<>(savedCategoryDTO, HttpStatus.CREATED);
  }

  @DeleteMapping("/admin/categories/{categoryId}")
  /* @PathVariable URI-র path-এর মধ্যে থাকা মান (যেমন, /{categoryId}) এর জন্য ব্যবহৃত হয়। */
  public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId){
    CategoryDTO categoryDTO = categoryService.deleteCategory(categoryId);
    return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
//    try {
//      String status = categoryService.deleteCategory(categoryId);
//      return new ResponseEntity<>(status, HttpStatus.OK);
//    } catch (ResponseStatusException responseStatusException){
//      return new ResponseEntity<>(responseStatusException.getReason(), responseStatusException.getStatusCode());
//    }
  }

  @PutMapping("/admin/categories/{categoriesId}")
  public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO, @PathVariable Long categoriesId){
    CategoryDTO savedCategoryDTO = categoryService.updateCategory(categoryDTO, categoriesId);
    return new ResponseEntity<>(savedCategoryDTO, HttpStatus.OK);
    /*try {
      Category savedCategory = categoryService.updateCategory(category, categoriesId);
      return new ResponseEntity<>("Category with id: "+categoriesId, HttpStatus.OK);
    } catch (ResponseStatusException responseStatusException){
      return new ResponseEntity<>(responseStatusException.getReason(), responseStatusException.getStatusCode());
    }*/
  }
}
