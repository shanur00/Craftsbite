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
    /* যদি sortOrder "asc" হয় তবে সেভাবেই সর্ট করো নইলে অন্যভাবে sortBy এর ভ্যালু কে ব্যবহার করে। */
    Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
      ? Sort.by(sortBy).ascending()
      : Sort.by(sortBy).descending();
    /* এখানে Pageable হচ্ছে একটি ইন্টারফেইস। আর PageRequest হচ্ছে সেটার ইমপ্লিমেন্টেশন। */
    Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
    /* এখানে আমরা ডেটাবেইজ থেকে ডেটা কালেক্ট করেছি মডেল ক্লাসের মাধ্যমে। আমাদের মডেল ক্লাস হচ্ছে Category */
    Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
    /* categoryPage এর থেকে Category অবজেক্টগুলোকে কালেক্ট করেছি একটা লিস্টের মাধ্যমে। */
    List<Category> categoryList = categoryPage.getContent();
    if(categoryList.isEmpty()){
      throw new EmptyReturnListException("Category List is Empty");
    }
    /* মডেল ক্লাসের অবজেক্ট কে আমরা স্ট্রিমের মাধ্যমে DTO ক্লাসে কনভার্ট করেছি। */
    List<CategoryDTO> categoryDTOList = categoryList.stream()
      .map(category -> modelMapper.map(category, CategoryDTO.class))
      .toList();
    /* রেসপন্সকে আমরা CategoryResponse ক্লাসের অবজেক্টের মাধ্যমে রিটার্ন করেছি END USER এর কাছে। */
    CategoryResponse categoryResponse = new CategoryResponse();
    /* শুধু CategoryDTO টাইপের লিস্টকে হ্যান্ডেল করে। নিজেকে সেট করতে হবে। */
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
    /* এখনে CategoryDTO থেকে modelMapper এর মাধ্যমে Category ক্লসে কনভার্ট করেছি। */
    Category category = modelMapper.map(categoryDTO, Category.class);
    /* ডেটাবেইজ থেকে categoryName এর মাধ্যমে Category এক্সিস্ট করে কি না তা যাচাই করেছি।  */
    Category findCategory = categoryRepository.findByCategoryName(category.getCategoryName());
    if(findCategory!=null){
      throw new APIException("category with the name " + findCategory.getCategoryName() + " already exist ! ");
    }
    Category savedCategory = categoryRepository.save(category);
    /* Category --> DTO */
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
