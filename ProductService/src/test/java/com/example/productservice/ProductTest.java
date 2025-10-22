package com.example.productservice;
import com.example.productservice.modules.Products.dto.UpdateProductDto;
import org.junit.jupiter.api.DisplayName;
import com.example.productservice.models.Categories;
import com.example.productservice.models.Products;
import com.example.productservice.modules.Catgories.service.CategoryService;
import com.example.productservice.modules.Products.dto.CreateProductDto;
import com.example.productservice.modules.Products.repository.ProductRepository;
import com.example.productservice.modules.Products.service.ProductService;
import com.example.productservice.utils.NotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductTest {
    @Mock private ProductRepository productRepository;
    @Mock private CategoryService categoryService;
    @InjectMocks private ProductService productService;

    private static Products existingProduct() {
        Products p = new Products();
        p.setId(1L);
        p.setName("Old Name");
        p.setBrand("Old Brand");
        p.setDescription("Old Desc");
        p.setPrice(1000);
        p.setImageUrl("old.png");
        p.setVideoUrl("old.mp4");
        p.setAvailable("old");
        p.setQuantity(3);
        Categories c = new Categories();
        c.setId(1L);
        c.setName("Electronics");
        p.setCategory(c);
        return p;
    }

    private static Products p(long id, String name) {
        Products pr = new Products();
        pr.setId(id);
        pr.setName(name);
        pr.setBrand("Dell");
        pr.setDescription("This product is necessary");
        pr.setPrice(100000);
        pr.setQuantity(10);
        pr.setAvailable("available");
        pr.setCategory(new Categories());
        return pr;
    }

    @Nested
    @DisplayName("createFlashSale")
    class createFlashSale {
        // ==========================================================
        // UTCP001 — Happy path: valid data, categoryId exists (=1)
        // ==========================================================
        @Test
        @DisplayName("UTCP001: Valid input (Laptop/Dell/price=100000/qty=10/categoryId=1) ⇒ saved with category")
        void createProduct_success_allValid() {
            // Given a category with id=1 exists
            when(categoryService.findCategoryById(1L)).thenReturn(new Categories(1L, "Electronics"));

            // Simulate DB-generated id on save
            when(productRepository.save(any(Products.class))).thenAnswer(inv -> {
                Products p = inv.getArgument(0);
                p.setId(999L);
                return p;
            });

            CreateProductDto input = new CreateProductDto(
                    "Laptop", "Dell", "This product is necessary",
                    100_000, "http://avatar.png", "http://video.mp4",
                    "available", 10, 1L
            );

            // When
            Products saved = productService.createProduct(input);

            // Then
            ArgumentCaptor<Products> cap = ArgumentCaptor.forClass(Products.class);
            verify(productRepository).save(cap.capture());

            Products toSave = cap.getValue();
            assertThat(toSave.getName()).isEqualTo("Laptop");
            assertThat(toSave.getBrand()).isEqualTo("Dell");
            assertThat(toSave.getDescription()).isEqualTo("This product is necessary");
            assertThat(toSave.getPrice()).isEqualTo(100_000);
            assertThat(toSave.getImageUrl()).isEqualTo("http://avatar.png");
            assertThat(toSave.getVideoUrl()).isEqualTo("http://video.mp4");
            assertThat(toSave.getAvailable()).isEqualTo("available");
            assertThat(toSave.getQuantity()).isEqualTo(10);
            assertThat(toSave.getCategory()).isNotNull();
            assertThat(toSave.getCategory().getId()).isEqualTo(1L);

            assertThat(saved.getId()).isEqualTo(999L);
        }

        // ==========================================================
        // UTCP002 — price negative ⇒ IllegalArgumentException
        // ==========================================================
        @Test
        @DisplayName("UTCP002: price = -100000 ⇒ IllegalArgumentException('Quantity and Price must be non-negative')")
        void createProduct_fail_negativePrice() {
            CreateProductDto input = new CreateProductDto(
                    "Laptop", "Dell", "This product is necessary",
                    -100_000, "http://avatar.png", "http://video.mp4",
                    "available", 10, 1L
            );

            assertThatThrownBy(() -> productService.createProduct(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Quantity and Price must be non-negative");

            verifyNoInteractions(categoryService, productRepository);
        }

        // ==========================================================
        // UTCP003 — quantity negative ⇒ IllegalArgumentException
        // ==========================================================
        @Test
        @DisplayName("UTCP003: quantity = -1 ⇒ IllegalArgumentException('Quantity and Price must be non-negative')")
        void createProduct_fail_negativeQuantity() {
            CreateProductDto input = new CreateProductDto(
                    "Laptop", "Dell", "This product is necessary",
                    100_000, "http://avatar.png", "http://video.mp4",
                    "available", -1, 1L
            );

            assertThatThrownBy(() -> productService.createProduct(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Quantity and Price must be non-negative");

            verifyNoInteractions(categoryService, productRepository);
        }

        // ==========================================================
        // UTCP004 — category not found (e.g., id=100)
        // ==========================================================
        @Test
        @DisplayName("UTCP004: categoryId=100 (not exists) ⇒ NotFoundException('Category not found')")
        void createProduct_fail_categoryNotFound() {
            // Return null to trigger service-side NotFoundException
            when(categoryService.findCategoryById(100L)).thenReturn(null);

            CreateProductDto input = new CreateProductDto(
                    "Laptop", "Dell", "This product is necessary",
                    100_000, "http://avatar.png", "http://video.mp4",
                    "available", 10, 100L
            );

            assertThatThrownBy(() -> productService.createProduct(input))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Category not found");

            verify(categoryService).findCategoryById(100L);
            verify(productRepository, never()).save(any());
        }

        // ==========================================================
        // UTCP005 — boundary: price=0, quantity=0 (valid)
        // ==========================================================
        @Test
        @DisplayName("UTCP005: price=0, quantity=0, categoryId=1 ⇒ success")
        void createProduct_success_zeroPriceAndQuantity() {
            when(categoryService.findCategoryById(1L)).thenReturn(new Categories(1L, "Electronics"));
            when(productRepository.save(any(Products.class))).thenAnswer(inv -> inv.getArgument(0));

            CreateProductDto input = new CreateProductDto(
                    "Laptop", "Dell", "This product is necessary",
                    0, "http://avatar.png", "http://video.mp4",
                    "available", 0, 1L
            );

            Products saved = productService.createProduct(input);

            assertThat(saved.getPrice()).isEqualTo(0);
            assertThat(saved.getQuantity()).isEqualTo(0);
            assertThat(saved.getCategory().getId()).isEqualTo(1L);
        }

        // ==========================================================
        // UTCP006 — verify URLs/available copied as-is
        // ==========================================================
        @Test
        @DisplayName("UTCP006: fields copy check (imageUrl, videoUrl, available)")
        void createProduct_success_fieldsCopied() {
            when(categoryService.findCategoryById(1L)).thenReturn(new Categories(1L, "Electronics"));
            when(productRepository.save(any(Products.class))).thenAnswer(inv -> inv.getArgument(0));

            CreateProductDto input = new CreateProductDto(
                    "Laptop", "Dell", "This product is necessary",
                    100_000, "http://avatar.png", "http://video.mp4",
                    "available", 10, 1L
            );

            Products saved = productService.createProduct(input);

            assertThat(saved.getImageUrl()).isEqualTo("http://avatar.png");
            assertThat(saved.getVideoUrl()).isEqualTo("http://video.mp4");
            assertThat(saved.getAvailable()).isEqualTo("available");
        }
    }

    @Nested
    @DisplayName("getProductByName")
    class getProductByName {

        // -------- helpers --------
        private static Products p(long id, String name) {
            Products pr = new Products();
            pr.setId(id);
            pr.setName(name);
            pr.setBrand("Brand");
            pr.setDescription("Desc");
            pr.setPrice(1);
            pr.setQuantity(1);
            pr.setAvailable("available");
            pr.setCategory(new Categories());
            return pr;
        }

        // UTGPB001 — Success row (name="LapTop", page=1, limit=2)
        @Test
        @DisplayName("UTGPB001: name='LapTop', page=1, limit=2 ⇒ returns 2 items, totalPages=2, totalElements=4")
        void getByName_success_exactSheetRow() {
            String expectedPattern = "%laptop%"; // service lower-cases
            Pageable pageable = PageRequest.of(1, 2);
            List<Products> content = List.of(p(2, "Laptop 2"), p(3, "Laptop 3"));
            Page<Products> page = new PageImpl<>(content, pageable, 4);
            when(productRepository.findByNameLike(eq(expectedPattern), eq(pageable)))
                    .thenReturn(page);

            var resp = productService.getProductsByName("LapTop", 1, 2);

            assertThat(resp.getContent()).hasSize(2);
            assertThat(resp.getTotalPages()).isEqualTo(2);
            assertThat(resp.getTotalElements()).isEqualTo(4);
            assertThat(resp.getContent())
                    .extracting(Products::getName)
                    .containsExactlyInAnyOrder("Laptop 2", "Laptop 3");
        }

        // UTGPB002 — name = null
        @Test
        @DisplayName("UTGPB002: name=null ⇒ BadRequestException('Keyword cannot be empty')")
        void getByName_fail_nameNull() {
            assertThatThrownBy(() -> productService.getProductsByName(null, 0, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Keyword cannot be empty");

            verifyNoInteractions(productRepository);
        }

        // UTGPB003 — name = "notfound"
        @Test
        @DisplayName("UTGPB003: name='notfound' ⇒ NotFoundException('No product found')")
        void getByName_fail_noProductFound() {
            String pattern = "%notfound%";
            Pageable pageable = PageRequest.of(0, 2);
            when(productRepository.findByNameLike(eq(pattern), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 0));

            assertThatThrownBy(() -> productService.getProductsByName("notfound", 0, 2))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("No product found");
        }

        // UTGPB004 — page = -1 (invalid)
        @Test
        @DisplayName("UTGPB004: page=-1 ⇒ IllegalArgumentException('Invalid page or limit')")
        void getByName_fail_pageNegative() {
            assertThatThrownBy(() -> productService.getProductsByName("LapTop", -1, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid page or limit");

            verifyNoInteractions(productRepository);
        }
    }

    @Nested
    @DisplayName("getProductByBrand")
    class getProductByBrand {
        // UTGB001 — brand="Nike", page=1, limit=2  -> returns 2 items, totalPages=2, totalElements=4
        @Test
        @DisplayName("UTGB001: brand='Nike', page=1, limit=2 ⇒ returns page [p1,p2], totalPages=2, totalElements=4")
        void getByBrand_success_secondPage() {
            String brand = "Nike";
            Pageable pageable = PageRequest.of(1, 2);
            List<Products> content = List.of(p(2, "Shoes 2"), p(3, "Shoes 3"));
            Page<Products> page = new PageImpl<>(content, pageable, 4);

            when(productRepository.findProductsByBrand(eq(brand), eq(pageable))).thenReturn(page);

            var resp = productService.getProductsByBrand(brand, 1, 2);

            assertThat(resp.getContent()).hasSize(2);
            assertThat(resp.getTotalPages()).isEqualTo(2);
            assertThat(resp.getTotalElements()).isEqualTo(4);
            assertThat(resp.getContent()).extracting(Products::getName)
                    .containsExactlyInAnyOrder("Shoes 2", "Shoes 3");
        }

        // UTGB002 — brand is empty -> BadRequest ("Brand cannot be empty")
        // If you refactored to IllegalArgumentException (recommended), change the assertion accordingly.
        @Test
        @DisplayName("UTGB002: brand='' ⇒ BadRequestException('Brand cannot be empty')")
        void getByBrand_fail_brandEmpty() {
            // current service throws jakarta.ws.rs.BadRequestException
            assertThatThrownBy(() -> productService.getProductsByBrand("", 0, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Brand cannot be empty");

            verifyNoInteractions(productRepository);
        }

        // UTGB003 — page invalid (-1) -> IllegalArgumentException("Invalid page or limit")
        @Test
        @DisplayName("UTGB003: page=-1 ⇒ IllegalArgumentException('Invalid page or limit')")
        void getByBrand_fail_pageNegative() {
            assertThatThrownBy(() -> productService.getProductsByBrand("Nike", -1, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid page or limit");

            verifyNoInteractions(productRepository);
        }

        // UTGB004 — brand unknown -> NotFound("No product found")
        @Test
        @DisplayName("UTGB004: brand='Unknown' ⇒ NotFoundException('No product found')")
        void getByBrand_fail_notFound() {
            String brand = "Unknown";
            Pageable pageable = PageRequest.of(0, 2);
            when(productRepository.findProductsByBrand(eq(brand), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 0));

            assertThatThrownBy(() -> productService.getProductsByBrand(brand, 0, 2))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("No product found");
        }

        // UTGB005 — extreme page (e.g., 100) -> empty result -> NotFound
        @Test
        @DisplayName("UTGB005: brand='Nike', page=100, limit=2 ⇒ NotFoundException('No product found')")
        void getByBrand_fail_pageTooLarge() {
            String brand = "Nike";
            Pageable pageable = PageRequest.of(100, 2);
            when(productRepository.findProductsByBrand(eq(brand), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 4));

            assertThatThrownBy(() -> productService.getProductsByBrand(brand, 100, 2))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("No product found");
        }
    }

    @Nested
    @DisplayName("findProductById")
    class findProductById {
        // UTIP001 — id=1 exists → return product info
        @Test
        @DisplayName("UTIP001: id=1 ⇒ returns product info")
        void findById_success() {
            when(productRepository.findOneById(1L))
                    .thenReturn(Optional.of(p(1L, "Shoes")));

            Products found = productService.findProductById(1L);

            assertThat(found.getId()).isEqualTo(1L);
            assertThat(found.getName()).isEqualTo("Shoes");

            verify(productRepository).findOneById(1L);
            verifyNoMoreInteractions(productRepository);
        }

        // UTIP002 — id=100 not present → NotFoundException("Product not found")
        @Test
        @DisplayName("UTIP002: id=100 ⇒ NotFoundException('Product not found')")
        void findById_notFound() {
            when(productRepository.findOneById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.findProductById(100L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Product not found");

            verify(productRepository).findOneById(100L);
            verifyNoMoreInteractions(productRepository);
        }

        // UTIP003 — id=null → IllegalArgumentException("Invalid product ID")
        @Test
        @DisplayName("UTIP003: id=null ⇒ IllegalArgumentException('Invalid product ID')")
        void findById_nullId() {
            assertThatThrownBy(() -> productService.findProductById(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid product ID");

            verify(productRepository, never()).findOneById(any());
        }
    }

    @Nested
    @DisplayName("getAllProducts")
    class getAllProducts {
        // UTGAP001 — page=0, limit=2 -> return 2 elements
        @Test
        @DisplayName("UTGAP001: page=0, limit=2 ⇒ returns {content=[p1,p2], totalPages=1, totalElements=2}")
        void getAll_page0_limit2_success() {
            Pageable pageable = PageRequest.of(0, 2);
            List<Products> data = List.of(p(1, "p1"), p(2, "p2"));
            Page<Products> pageObj = new PageImpl<>(data, pageable, 2);
            when(productRepository.findAllProducts(pageable)).thenReturn(pageObj);

            var resp = productService.getAllProducts(0, 2);

            assertThat(resp.getContent()).extracting(Products::getName)
                    .containsExactlyInAnyOrder("p1", "p2");
            assertThat(resp.getTotalPages()).isEqualTo(1);
            assertThat(resp.getTotalElements()).isEqualTo(2);

            verify(productRepository).findAllProducts(pageable);
            verifyNoMoreInteractions(productRepository);
        }

        // UTGAP002 — page=1, limit=2 -> empty page => NotFoundException("No product found")
        @Test
        @DisplayName("UTGAP002: page=1, limit=2 ⇒ NotFoundException('No product found')")
        void getAll_page1_limit2_notFound() {
            Pageable pageable = PageRequest.of(1, 2);
            when(productRepository.findAllProducts(pageable))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 2));

            assertThatThrownBy(() -> productService.getAllProducts(1, 2))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("No product found");

            verify(productRepository).findAllProducts(pageable);
            verifyNoMoreInteractions(productRepository);
        }

        // UTGAP003 — page=-1, limit=2 -> IllegalArgumentException("Invalid page or limit")
        @Test
        @DisplayName("UTGAP003: page=-1, limit=2 ⇒ IllegalArgumentException('Invalid page or limit')")
        void getAll_pageNegative_invalid() {
            assertThatThrownBy(() -> productService.getAllProducts(-1, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid page or limit");

            verifyNoInteractions(productRepository);
        }
    }

    @Nested
    @DisplayName("updateProduct")
    class updateProduct {
        // UTUP001 — Happy path: update all valid fields
        @Test
        @DisplayName("UTUP001: id=1 valid fields ⇒ updated successfully")
        void update_success_fullFields() {
            Products stored = existingProduct();
            when(productRepository.findOneById(1L)).thenReturn(Optional.of(stored));
            when(categoryService.findCategoryById(1L)).thenReturn(stored.getCategory());
            when(productRepository.save(any(Products.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateProductDto input = new UpdateProductDto(
                    "Macbook pro", "Apple", null,
                    5000, "img.png", null,
                    "available", 7, 1L
            );

            Products updated = productService.updateProduct(1L, input);

            assertThat(updated.getName()).isEqualTo("Macbook pro");
            assertThat(updated.getBrand()).isEqualTo("Apple");
            assertThat(updated.getPrice()).isEqualTo(5000);
            assertThat(updated.getImageUrl()).isEqualTo("img.png");
            assertThat(updated.getAvailable()).isEqualTo("available");
            assertThat(updated.getQuantity()).isEqualTo(7);
            assertThat(updated.getCategory().getId()).isEqualTo(1L);

            ArgumentCaptor<Products> cap = ArgumentCaptor.forClass(Products.class);
            verify(productRepository).save(cap.capture());
            assertThat(cap.getValue().getName()).isEqualTo("Macbook pro");
        }

        // UTUP002 — Invalid product ID (-1)
        @Test
        @DisplayName("UTUP002: id=-1 ⇒ IllegalArgumentException('Invalid product ID')")
        void update_fail_invalidId() {
            UpdateProductDto dto = new UpdateProductDto("Macbook pro", "Apple", null, 5000, "img.png", null, "available", 7, 1L);

            assertThatThrownBy(() -> productService.updateProduct(-1L, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid product ID");

            verifyNoInteractions(productRepository, categoryService);
        }

        // UTUP003 — Product not found
        @Test
        @DisplayName("UTUP003: id=2 not exists ⇒ NotFoundException('Product not found')")
        void update_fail_notFound() {
            when(productRepository.findOneById(2L)).thenReturn(Optional.empty());
            UpdateProductDto dto = new UpdateProductDto("Macbook pro", "Apple", null, 5000, "img.png", null, "available", 7, 1L);

            assertThatThrownBy(() -> productService.updateProduct(2L, dto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Product not found");

            verify(productRepository).findOneById(2L);
            verify(productRepository, never()).save(any());
        }

        // UTUP004 — Price negative
        @Test
        @DisplayName("UTUP004: price=-1000 ⇒ IllegalArgumentException('Price must be non-negative')")
        void update_fail_negativePrice() {
            UpdateProductDto dto = new UpdateProductDto(null, null, null, -1000, null, null, null, null, null);

            assertThatThrownBy(() -> productService.updateProduct(1L, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Price must be non-negative");

            verifyNoInteractions(productRepository);
        }

        // UTUP005 — Quantity negative
        @Test
        @DisplayName("UTUP005: quantity=-10 ⇒ IllegalArgumentException('Quantity must be non-negative')")
        void update_fail_negativeQuantity() {
            UpdateProductDto dto = new UpdateProductDto(null, null, null, null, null, null, null, -10, null);

            assertThatThrownBy(() -> productService.updateProduct(1L, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Quantity must be non-negative");

            verifyNoInteractions(productRepository);
        }

        // UTUP006 — Category not found (categoryId=2)
        @Test
        @DisplayName("UTUP006: categoryId=2 not exists ⇒ NotFoundException('Category not found')")
        void update_fail_categoryNotFound() {
            Products stored = existingProduct();
            when(productRepository.findOneById(1L)).thenReturn(Optional.of(stored));
            when(categoryService.findCategoryById(2L)).thenThrow(new NotFoundException("Category not found"));

            UpdateProductDto dto = new UpdateProductDto(null, "Apple", null, 5000, null, null, "available", 7, 2L);

            assertThatThrownBy(() -> productService.updateProduct(1L, dto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Category not found");

            verify(productRepository, never()).save(any());
        }

        // UTUP007 — Partial update, null fields kept as old
        @Test
        @DisplayName("UTUP007: null fields ⇒ keep old values, only update non-null")
        void update_partial_keepOldValues() {
            Products stored = existingProduct();
            when(productRepository.findOneById(1L)).thenReturn(Optional.of(stored));
            when(productRepository.save(any(Products.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateProductDto dto = new UpdateProductDto(
                    null, "Apple", null,
                    5000, null, null,
                    "available", null, null
            );

            Products updated = productService.updateProduct(1L, dto);

            assertThat(updated.getName()).isEqualTo("Old Name");
            assertThat(updated.getDescription()).isEqualTo("Old Desc");
            assertThat(updated.getImageUrl()).isEqualTo("old.png");
            assertThat(updated.getVideoUrl()).isEqualTo("old.mp4");
            assertThat(updated.getQuantity()).isEqualTo(3);
            assertThat(updated.getBrand()).isEqualTo("Apple");
            assertThat(updated.getPrice()).isEqualTo(5000);
            assertThat(updated.getAvailable()).isEqualTo("available");
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    class deleteProduct {
        private static Products product(long id) {
            Products p = new Products();
            p.setId(id);
            p.setName("Sample");
            p.setBrand("Brand");
            p.setQuantity(1);
            p.setPrice(1);
            p.setAvailable("available");
            p.setCategory(new Categories());
            return p;
        }

        // UTDP001: id=1 → delete successfully
        @Test
        @DisplayName("UTDP001: id=1 ⇒ softDeleteById(1) is called")
        void delete_success_id1() {
            when(productRepository.findOneById(1L)).thenReturn(Optional.of(product(1L)));

            productService.deleteProduct(1L);

            verify(productRepository).findOneById(1L); // via service.findProductById
            verify(productRepository).softDeleteById(1L);
            verifyNoMoreInteractions(productRepository);
        }

        // UTDP002: id=null → IllegalArgumentException("Invalid product ID")
        @Test
        @DisplayName("UTDP002: id=null ⇒ IllegalArgumentException('Invalid product ID')")
        void delete_fail_invalidId_null() {
            assertThatThrownBy(() -> productService.deleteProduct(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid product ID");

            verifyNoInteractions(productRepository);
        }

        // UTDP003: id=100 (not exists) → NotFoundException("Product not found")
        @Test
        @DisplayName("UTDP003: id=100 ⇒ NotFoundException('Product not found')")
        void delete_fail_notFound() {
            when(productRepository.findOneById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(100L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Product not found");

            verify(productRepository).findOneById(100L);
            verify(productRepository, never()).softDeleteById(anyLong());
        }
    }
}
