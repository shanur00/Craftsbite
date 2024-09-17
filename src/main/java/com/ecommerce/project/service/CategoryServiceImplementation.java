package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.EmptyReturnListException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImplementation implements CategoryService {

  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private ModelMapper modelMapper;

  @Override
  public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

    Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
      ? Sort.by(sortBy).ascending()
      : Sort.by(sortBy).descending();

    Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

    Page<Category> categoryPage = categoryRepository.findAll(pageDetails);

    List<Category> categoryList = categoryPage.getContent();
    if(categoryList.isEmpty()){
      throw new EmptyReturnListException("Category List is Empty");
    }

    List<CategoryDTO> categoryDTOList = categoryList.stream()
      .map(category -> modelMapper.map(category, CategoryDTO.class))
      .toList();

    CategoryResponse categoryResponse = new CategoryResponse();
    categoryResponse.setCategoryDTOList(categoryDTOList);
    categoryResponse.setPageNumber(categoryPage.getNumber());
    categoryResponse.setPageSize(categoryPage.getSize());
    categoryResponse.setTotalElements(categoryPage.getTotalElements());
    categoryResponse.setTotalPages(categoryPage.getTotalPages());
    categoryResponse.setLastPage(categoryPage.isLast());

    return categoryResponse;
  }

  @Override
  public CategoryDTO createCategory(CategoryDTO categoryDTO) {
    Category category = modelMapper.map(categoryDTO, Category.class);
    Category findCategory = categoryRepository.findByCategoryName(category.getCategoryName());
    if(findCategory!=null){
      throw new APIException("category with the name " + findCategory.getCategoryName() + " already exist ! ");
    }
    Category savedCategory = categoryRepository.save(category);
    return modelMapper.map(savedCategory, CategoryDTO.class);
  }

  @Override
  public CategoryDTO deleteCategory(Long categoryId) {
    Category category = categoryRepository.findById(categoryId)
      .orElseThrow(()->new ResourceNotFoundException("category","categoryId",categoryId));
    categoryRepository.delete(category);
    return modelMapper.map(category, CategoryDTO.class);
  }

  @Override
  public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
    Optional<Category> savedCategoryOptional = categoryRepository.findById(categoryId);
    Category findCategory = savedCategoryOptional
      .orElseThrow(()->new ResourceNotFoundException("category","categoryId",categoryId));
    Category savedCategory = modelMapper.map(categoryDTO, Category.class);
    savedCategory.setCategoryId(categoryId);
    categoryRepository.save(savedCategory);
    return modelMapper.map(savedCategory, CategoryDTO.class);
  }
}
