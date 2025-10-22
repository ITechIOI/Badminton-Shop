package com.example.productservice;

import com.example.productservice.models.Categories;
import com.example.productservice.modules.Catgories.dto.CreateCategoryDto;
import com.example.productservice.modules.Catgories.dto.UpdateCategoryDto;
import com.example.productservice.modules.Catgories.repository.CategoryRepository;
import com.example.productservice.modules.Catgories.service.CategoryService;
import com.example.productservice.utils.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit tests for CategoryService (no Spring context). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoryTest {

    // ---------------------------------------------------------------------
    // Mocks & SUT
    // ---------------------------------------------------------------------
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private CategoryService categoryService;

    // ---------------------------------------------------------------------
    // Helper factory methods
    // ---------------------------------------------------------------------
    private static CreateCategoryDto createDto(String name, Integer count) {
        return CreateCategoryDto.builder().name(name).count(count).build();
    }

    private static UpdateCategoryDto updateDto(String name, Integer count) {
        return UpdateCategoryDto.builder().name(name).count(count).build();
    }

    private static Categories category(long id, String name, int count) {
        Categories c = new Categories();
        c.setId(id);
        c.setName(name);
        c.setCount(count);
        return c;
    }

    private static Categories existingCategory() {
        return category(1L, "Electronics", 10);
    }

    // =====================================================================
    // createCategory
    // =====================================================================
    @Nested
    @DisplayName("createCategory")
    class CreateCategory {

        @Test
        @DisplayName("UTCC001: name='Bike', count=4 ⇒ success, returns persisted entity")
        void success_withExplicitCount() {
            when(categoryRepository.save(any(Categories.class))).thenAnswer(inv -> {
                Categories c = inv.getArgument(0);
                c.setId(100L); // simulate DB-generated ID
                return c;
            });

            CreateCategoryDto input = createDto("Bike", 4);

            Categories saved = categoryService.createCategory(input);

            ArgumentCaptor<Categories> cap = ArgumentCaptor.forClass(Categories.class);
            verify(categoryRepository).save(cap.capture());
            assertThat(cap.getValue().getName()).isEqualTo("Bike");
            assertThat(cap.getValue().getCount()).isEqualTo(4);

            assertThat(saved.getId()).isEqualTo(100L);
            assertThat(saved.getName()).isEqualTo("Bike");
            assertThat(saved.getCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("UTCC002: name='Bike', count=null ⇒ default count=0, success")
        void success_whenCountNull_defaultsToZero() {
            when(categoryRepository.save(any(Categories.class))).thenAnswer(inv -> inv.getArgument(0));
            CreateCategoryDto input = createDto("Bike", null);

            Categories saved = categoryService.createCategory(input);

            assertThat(saved.getName()).isEqualTo("Bike");
            assertThat(saved.getCount()).isEqualTo(0);
            // current service does NOT mutate the DTO
            assertThat(input.getCount()).isNull();
        }

        @Test
        @DisplayName("UTCC003: name=null, count=4 ⇒ IllegalArgumentException('Invalid input data format')")
        void fail_whenNameNull() {
            CreateCategoryDto input = createDto(null, 4);

            assertThatThrownBy(() -> categoryService.createCategory(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid input data format");

            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("UTCC004: name='Bike', count=-2 ⇒ IllegalArgumentException('Invalid input data format')")
        void fail_whenCountNegative() {
            CreateCategoryDto input = createDto("Bike", -2);

            assertThatThrownBy(() -> categoryService.createCategory(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid input data format");

            verify(categoryRepository, never()).save(any());
        }
    }

    // =====================================================================
    // findCategoryById
    // =====================================================================
    @Nested
    @DisplayName("findCategoryById")
    class FindCategoryById {

        @Test
        @DisplayName("UTID001: id=1 ⇒ returns Categories(name='Shoes', count=5)")
        void success_whenIdExists() {
            when(categoryRepository.findByOneId(1L))
                    .thenReturn(Optional.of(category(1L, "Shoes", 5)));

            Categories found = categoryService.findCategoryById(1L);

            assertThat(found.getId()).isEqualTo(1L);
            assertThat(found.getName()).isEqualTo("Shoes");
            assertThat(found.getCount()).isEqualTo(5);

            verify(categoryRepository).findByOneId(1L);
            verifyNoMoreInteractions(categoryRepository);
        }

        @Test
        @DisplayName("UTID002: id=100 ⇒ NotFoundException('Category not found')")
        void notFound_throwsNotFound() {
            when(categoryRepository.findByOneId(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findCategoryById(100L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Category not found");

            verify(categoryRepository).findByOneId(100L);
            verifyNoMoreInteractions(categoryRepository);
        }

        @Test
        @DisplayName("UTID003: id=null ⇒ IllegalArgumentException('Invalid category ID')")
        void nullId_throwsIllegalArgument() {
            assertThatThrownBy(() -> categoryService.findCategoryById(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid category ID");

            verify(categoryRepository, never()).findByOneId(anyLong());
        }
    }

    // =====================================================================
    // getAllCategories
    // =====================================================================
    @Nested
    @DisplayName("getAllCategories")
    class GetAllCategories {

        @Test
        @DisplayName("UTGA001: page=0, limit=2 ⇒ returns 2 items")
        void page0_limit2_success() {
            var pageable = PageRequest.of(0, 2);
            List<Categories> data = List.of(category(1, "Cat-1", 5), category(2, "Cat-2", 7));
            Page<Categories> pageObj = new PageImpl<>(data, pageable, 2);
            when(categoryRepository.findAllCategories(pageable)).thenReturn(pageObj);

            var resp = categoryService.getAllCategories(0, 2);
            assertThat(resp.getContent()).hasSize(2);
            assertThat(resp.getTotalElements()).isEqualTo(2);
            assertThat(resp.getTotalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("UTGA002: page=1, limit=2 ⇒ NotFoundException('No category found')")
        void page1_limit2_notFound() {
            var pageable = PageRequest.of(1, 2);
            Page<Categories> pageObj = new PageImpl<>(List.of(), pageable, 2);
            when(categoryRepository.findAllCategories(pageable)).thenReturn(pageObj);

            assertThatThrownBy(() -> categoryService.getAllCategories(1, 2))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("No category found");
        }

        @Test
        @DisplayName("UTGA003: page=-1, limit=2 ⇒ IllegalArgumentException('Invalid page or limit')")
        void pageNegative_invalid() {
            assertThatThrownBy(() -> categoryService.getAllCategories(-1, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid page or limit");
        }
    }

    // =====================================================================
    // updateCategory
    // =====================================================================
    @Nested
    @DisplayName("updateCategory")
    class UpdateCategory {

        @Test
        @DisplayName("UTUC001: id=1, name='Books', count=10 ⇒ success")
        void success_changeNameAndCount() {
            Categories stored = existingCategory();
            when(categoryRepository.findByOneId(1L)).thenReturn(Optional.of(stored));
            when(categoryRepository.save(any(Categories.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateCategoryDto input = updateDto("Books", 10);

            Categories updated = categoryService.updateCategory(1L, input);

            ArgumentCaptor<Categories> cap = ArgumentCaptor.forClass(Categories.class);
            verify(categoryRepository).save(cap.capture());

            assertThat(updated.getId()).isEqualTo(1L);
            assertThat(updated.getName()).isEqualTo("Books");
            assertThat(updated.getCount()).isEqualTo(10);

            assertThat(cap.getValue().getName()).isEqualTo("Books");
            assertThat(cap.getValue().getCount()).isEqualTo(10);
        }

        @Test
        @DisplayName("UTUC002: id=1, name='' ⇒ RuntimeException('... Name cannot be empty')")
        void fail_emptyName() {
            UpdateCategoryDto input = updateDto("", null);

            assertThatThrownBy(() -> categoryService.updateCategory(1L, input))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to update category: Name cannot be empty");

            verify(categoryRepository, never()).findByOneId(any());
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("UTUC003: id=1, count=-1 ⇒ RuntimeException('... Count cannot be negative')")
        void fail_negativeCount() {
            UpdateCategoryDto input = updateDto(null, -1);

            assertThatThrownBy(() -> categoryService.updateCategory(1L, input))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to update category: Count cannot be negative");

            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("UTUC004: id=2 (not found) ⇒ RuntimeException('... Category not found')")
        void fail_notFoundId() {
            when(categoryRepository.findByOneId(2L)).thenReturn(Optional.empty());

            UpdateCategoryDto input = updateDto("Books", 10);

            assertThatThrownBy(() -> categoryService.updateCategory(2L, input))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to update category: Category not found");

            verify(categoryRepository).findByOneId(2L);
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("UTUC005: id=1, name=null, count=null ⇒ keep original values (Electronics, 10)")
        void noChange_whenAllFieldsNull_keepOriginal() {
            Categories stored = existingCategory();
            when(categoryRepository.findByOneId(1L)).thenReturn(Optional.of(stored));
            when(categoryRepository.save(any(Categories.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateCategoryDto input = updateDto(null, null);

            Categories updated = categoryService.updateCategory(1L, input);

            assertThat(updated.getId()).isEqualTo(1L);
            assertThat(updated.getName()).isEqualTo("Electronics");
            assertThat(updated.getCount()).isEqualTo(10);
        }
    }

    // =====================================================================
    // deleteCategory
    // =====================================================================
    @Nested
    @DisplayName("deleteCategory")
    class DeleteCategory {

        @Test
        @DisplayName("UTDC001: id=1 ⇒ delete successfully, softDeleteById(1) called")
        void success_id1() {
            when(categoryRepository.findByOneId(1L))
                    .thenReturn(Optional.of(category(1L, "Electronics", 10)));

            categoryService.deleteCategory(1L);

            verify(categoryRepository).findByOneId(1L);
            verify(categoryRepository).softDeleteById(1L);
            verifyNoMoreInteractions(categoryRepository);
        }

        @Test
        @DisplayName("UTDC002: id=null ⇒ IllegalArgumentException('Invalid category ID')")
        void fail_invalidId_null() {
            assertThatThrownBy(() -> categoryService.deleteCategory(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid category ID");

            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("UTDC003: id=2 (not exists) ⇒ NotFoundException('Category not found')")
        void fail_notFound() {
            when(categoryRepository.findByOneId(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.deleteCategory(2L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Category not found");

            verify(categoryRepository).findByOneId(2L);
            verify(categoryRepository, never()).softDeleteById(anyLong());
        }
    }
}
