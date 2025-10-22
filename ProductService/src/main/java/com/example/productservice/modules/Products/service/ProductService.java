package com.example.productservice.modules.Products.service;

import com.example.productservice.models.Categories;
import com.example.productservice.models.Products;
import com.example.productservice.modules.Catgories.service.CategoryService;
import com.example.productservice.modules.Products.dto.CreateProductDto;
import com.example.productservice.modules.Products.dto.ProductResponse;
import com.example.productservice.modules.Products.dto.UpdateProductDto;
import com.example.productservice.modules.Products.repository.ProductRepository;
import com.example.productservice.utils.NotFoundException;
import com.example.productservice.utils.NullAwareBeanUtilsBean;
import com.example.productservice.utils.PagedResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    public Products createProduct(@Valid CreateProductDto createProductDto) {
        if (createProductDto.getQuantity() < 0 || createProductDto.getPrice() < 0) {
            throw new IllegalArgumentException("Quantity and Price must be non-negative");
        }
        Categories category =  categoryService.findCategoryById(createProductDto.getCategoryId());
        if (category == null) {
            throw new NotFoundException("Category not found");
        }
        Products product = new Products();
        BeanUtils.copyProperties(createProductDto, product);
        product.setCategory(category);
        return productRepository.save(product);
    }

    public List<Products> findAllProductsNotPaginate() {
        List<Products> products = productRepository.findAllProductsNoPage();
        if (products.isEmpty()) {
            throw new NotFoundException("No product found");
        }
        return products;
    }

    public Products findProductById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
        return productRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Product not found"));
    }

    public ProductResponse findProductByIdForService (Long id) {
        Products products = findProductById(id);
        return new ProductResponse(
                products.getName(),
                products.getBrand(),
                products.getDescription(),
                products.getPrice(),
                products.getImageUrl(),
                products.getVideoUrl(),
                products.getAvailable(),
                products.getQuantity(),
                products.getCategory().getId()
        );
    }

    @CircuitBreaker(
            name = "getAllProductsCB",
            fallbackMethod = "fallbackGetAllProducts"
    )
    public PagedResponse<Products> getAllProducts(int page, int limit) {
        if (page < 0 || limit <= 0) {
            throw new IllegalArgumentException("Invalid page or limit");
        }
        Pageable pageable = PageRequest.of(page, limit);
        Page<Products> products = productRepository.findAllProducts(pageable);
        if (products.getContent().isEmpty()) {
            throw new NotFoundException("No product found");
        }
        return new PagedResponse<>(products.getContent(), products.getTotalPages(), products.getTotalElements());
    }

    public PagedResponse<Products> fallbackGetAllProducts(
            int page, int limit, Throwable t
    ) {
        logger.warn("getAllProducts service unavailable: {}", t.toString());
        return new PagedResponse<>(
                Collections.emptyList(),
                0,
                0
        );
    }

    public PagedResponse<Products> getProductsByName(String name, int page, int limit) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be empty");
        }

        if (page < 0 || limit <= 0) {
            throw new IllegalArgumentException("Invalid page or limit");
        }

        String searchPattern = "%" + name.trim() + "%";
        Pageable pageable = PageRequest.of(page, limit);
        Page<Products> products = productRepository.findByNameLike(searchPattern.toLowerCase(), pageable);

        if (products.getContent().isEmpty()) {
            throw new NotFoundException("No product found");
        }

        return new PagedResponse<>(products.getContent(), products.getTotalPages(), products.getTotalElements());
    }

    public PagedResponse<Products> getProductsByBrand(String brand, int page, int limit) {
        if (brand == null || brand.trim().isEmpty()) {
            throw new IllegalArgumentException("Brand cannot be empty");
        }
        if (page < 0 || limit <= 0) {
            throw new IllegalArgumentException("Invalid page or limit");
        }
        Pageable pageable = PageRequest.of(page, limit);
        Page<Products> products = productRepository.findProductsByBrand(brand, pageable);
        if (products.getContent().isEmpty()) {
            throw new NotFoundException("No product found");
        }
        return new PagedResponse<>(products.getContent(), products.getTotalPages(), products.getTotalElements());
    }

    public PagedResponse<Products> getProductsByCategoryId(Long categoryId, int page, int limit) {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("Invalid category ID");
        }
        Pageable pageable = PageRequest.of(page, limit);
        Page<Products> products = productRepository.findByCategoryId(categoryId, pageable);
        if (products.getContent().isEmpty()) {
            throw new NotFoundException("No product found");
        }
        return new PagedResponse<>(products.getContent(), products.getTotalPages(), products.getTotalElements());
    }

//    public PagedResponse<GRN_Details> getDetailsByProductId(Long productId, int page, int limit) {
//        Pageable pageable = PageRequest.of(page, limit);
//        Page<GRN_Details> grnDetails = grnDetailRepository.findByProductId(productId, pageable);
//        if (grnDetails.getContent().isEmpty()) {
//            throw new NotFoundException("No Grn Detail found");
//        }
//        return new PagedResponse<>(grnDetails.getContent(), grnDetails.getTotalPages(), grnDetails.getTotalElements());
//    }


    public Products updateProduct(Long id, UpdateProductDto updateProductDto) {
        // Kiểm tra id
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
        // Kiểm tra các trường số
        if (updateProductDto.getQuantity() != null && updateProductDto.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }
        if (updateProductDto.getPrice() != null && updateProductDto.getPrice() < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        Products product = findProductById(id);
        try {
            BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
            notNull.copyProperties(product, updateProductDto);
        } catch (Exception e) {
            throw new RuntimeException("Error copying properties", e);
        }
        if (updateProductDto.getCategoryId() != null) {
            Categories category = categoryService.findCategoryById(updateProductDto.getCategoryId());
            product.setCategory(category);
        }
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
        Products product = findProductById(id);
        productRepository.softDeleteById(product.getId());
    }

}
