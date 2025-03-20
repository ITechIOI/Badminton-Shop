package com.example.productservice.modules.Catgories.service;

import com.example.productservice.models.Categories;
import com.example.productservice.modules.Catgories.dto.CreateCategoryDto;
import com.example.productservice.modules.Catgories.dto.UpdateCategoryDto;
import com.example.productservice.modules.Catgories.repository.CategoryRepository;
import com.example.productservice.utils.NotFoundException;
import com.example.productservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Categories createCategory(CreateCategoryDto createCategoryDto) {
        Categories categories = new Categories();
        BeanUtils.copyProperties(createCategoryDto, categories);
        return categoryRepository.save(categories);
    }

    public Categories findCategoryById(Long id) {
        return categoryRepository.findByOneId(id).orElseThrow(
                () -> new NotFoundException("Category not found")
        );
    }

    // Do category chỉ có 1 thuộc tính name nên không cần xử lý logic bị ghi đè dữ liệu
    // khi sử dụng phương thức copyProperties() cho thao tác update
    public Categories updateCategory(Long id, UpdateCategoryDto updateCategoryDto) {
        Categories categories = findCategoryById(id);
        BeanUtils.copyProperties(updateCategoryDto, categories);
        return categoryRepository.save(categories);
    }

    public PagedResponse<Categories> getAllCategories(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Categories> categories = categoryRepository.findAllCategories(pageable);
        if (categories.getContent().isEmpty()) {
            throw new NotFoundException("No category found");
        }
        return new PagedResponse<Categories>(categories.getContent(), categories.getTotalPages(), categories.getTotalElements());
    }

    public void deleteCategory(Long id) {
        Categories categories = findCategoryById(id);
        categoryRepository.softDeleteById(categories.getId());
    }
}
