package com.example.productservice.modules.Catgories.service;

import com.example.productservice.models.Categories;
import com.example.productservice.modules.Catgories.dto.CreateCategoryDto;
import com.example.productservice.modules.Catgories.dto.UpdateCategoryDto;
import com.example.productservice.modules.Catgories.repository.CategoryRepository;
import com.example.productservice.utils.NotFoundException;
import com.example.productservice.utils.NullAwareBeanUtilsBean;
import com.example.productservice.utils.PagedResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Categories createCategory(@Valid CreateCategoryDto createCategoryDto) {
        try {
            Categories category = new Categories();

            Integer count = (createCategoryDto.getCount() == null) ? 0 : createCategoryDto.getCount();

            if (createCategoryDto.getName() == null || createCategoryDto.getName().isBlank() || count < 0) {
                throw new IllegalArgumentException("Invalid input data format");
            }

            // copy các field khác trước…
            BeanUtils.copyProperties(createCategoryDto, category);
            // …rồi ép count mặc định vào entity
            category.setCount(count);

            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Category name must be unique: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create category: " + e.getMessage(), e);
        }
    }


    public Categories findCategoryById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid category ID");
        }
        return categoryRepository.findByOneId(id).orElseThrow(
                () -> new NotFoundException("Category not found")
        );
    }

    // Do category chỉ có 1 thuộc tính name nên không cần xử lý logic bị ghi đè dữ liệu
    // khi sử dụng phương thức copyProperties() cho tha o tác update
    public Categories updateCategory(Long id, @Valid UpdateCategoryDto updateCategoryDto) {
        try {
            if (updateCategoryDto.getCount() != null && updateCategoryDto.getCount() < 0) {
                throw new IllegalArgumentException("Count cannot be negative");
            }
            // ràng buôc name
            if (updateCategoryDto.getName() != null && updateCategoryDto.getName().isEmpty()) {
                throw new IllegalArgumentException("Name cannot be empty");
            }
            Categories categories = findCategoryById(id);
            BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
            notNull.copyProperties(categories, updateCategoryDto);


            return categoryRepository.save(categories);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update category: " + e.getMessage());
        }
    }

    public PagedResponse<Categories> getAllCategories(int page, int limit) {
        if (page < 0 || limit <= 0) {
            throw new IllegalArgumentException("Invalid page or limit");
        }
        Pageable pageable = PageRequest.of(page, limit);
        Page<Categories> categories = categoryRepository.findAllCategories(pageable);
        if (categories.getContent().isEmpty()) {
            throw new NotFoundException("No category found");
        }
        System.out.print("List of categories: " + categories.getContent());
        return new PagedResponse<Categories>(categories.getContent(), categories.getTotalPages(), categories.getTotalElements());
    }

    public void deleteCategory(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid category ID");
        }
        Categories categories = findCategoryById(id);
        categoryRepository.softDeleteById(categories.getId());
    }
}
