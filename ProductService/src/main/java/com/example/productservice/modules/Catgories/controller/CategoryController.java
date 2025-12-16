package com.example.productservice.modules.Catgories.controller;

import com.example.productservice.models.Categories;
import com.example.productservice.modules.Catgories.dto.CreateCategoryDto;
import com.example.productservice.modules.Catgories.dto.UpdateCategoryDto;
import com.example.productservice.modules.Catgories.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products/categories")
@AllArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/new")
    public ResponseEntity<Categories> createCategory(@RequestBody @Valid CreateCategoryDto categoryDto) {
        return ResponseEntity.ok(categoryService.createCategory(categoryDto));
    }

    @GetMapping("id/{id}")
    public ResponseEntity<Categories> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.findCategoryById(id));
    }

    @GetMapping("all")
    public ResponseEntity<?> getAllCategories(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(categoryService.getAllCategories(page, limit));
    }

    @DeleteMapping("{categoryId}")
    public ResponseEntity<Categories> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(null);
    }

    @PutMapping("{categoryId}")
    public ResponseEntity<Categories> updateCategory(
            @PathVariable("categoryId") Long categoryId,
            @RequestBody @Valid UpdateCategoryDto updateCategoryDto
    ) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryId, updateCategoryDto));
    }

}
