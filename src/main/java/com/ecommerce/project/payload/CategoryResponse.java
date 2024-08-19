package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
  /*CategoryDTO টাইপের লিস্টকে এখানে SET এবং GET করা হবে। */
  private List<CategoryDTO> categoryDTOList;
  private Integer pageNumber;
  private Integer pageSize;
  private Long totalElements;
  private Integer totalPages;
  private boolean lastPage;
}
