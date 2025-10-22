package com.example.productservice;

import com.example.productservice.models.*;
import com.example.productservice.modules.FlashSale.service.FlashSaleService;
import com.example.productservice.modules.FlashSaleDetails.dto.CreateFlashSaleDetailDto;
import com.example.productservice.modules.FlashSaleDetails.dto.UpdateFlashSaleDetailDto;
import com.example.productservice.modules.FlashSaleDetails.repository.FlashSaleDetailRepository;
import com.example.productservice.modules.FlashSaleDetails.service.FlashSaleDetailService;
import com.example.productservice.modules.Products.service.ProductService;
import com.example.productservice.utils.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashDetailTest {

    @Mock FlashSaleDetailRepository flashSaleDetailRepository;
    @Mock FlashSaleService flashSaleService;
    @Mock ProductService productService;
    @InjectMocks FlashSaleDetailService flashSaleDetailService;

    private Products product(long id) {
        Products p = new Products();
        p.setId(id);
        p.setName("Product " + id);
        return p;
    }

    private Flash_Sale sale(long id) {
        Flash_Sale f = new Flash_Sale();
        f.setId(id);
        f.setName("FlashSale " + id);
        return f;
    }

    private Flash_Sale_Details detail(long id, long saleId, long productId) {
        Flash_Sale_Details d = new Flash_Sale_Details();
        d.setId(id);
        d.setOriginalPrice(100_000);
        d.setSalePrice(80_000);
        d.setQuantity(5);
        d.setFlashSale(sale(saleId));
        d.setProduct(product(productId));
        return d;
    }

    private Flash_Sale_Details storedDetail() {
        Flash_Sale_Details d = new Flash_Sale_Details();
        d.setId(2L);
        d.setOriginalPrice(100_000);
        d.setSalePrice(80_000);
        d.setQuantity(10);
        d.setFlashSale(sale(2L));
        d.setProduct(product(2L));
        return d;
    }

    private Flash_Sale_Details detail(long id, String label) {
        long saleId = 300 + id;
        long productId = 200 + id;
        Flash_Sale_Details d = new Flash_Sale_Details();
        d.setId(id);
        d.setOriginalPrice(100_000);
        d.setSalePrice(80_000);
        d.setQuantity(5);
        d.setFlashSale(sale(saleId));
        d.setProduct(product(productId));
        return d;
    }

    @Nested
    @DisplayName("createFlashSaleDetail")
    class CreateFlashSaleDetail {

        @Test
        @DisplayName("UTFSD001: Valid input ⇒ return FlashSaleDetail(originalPrice=100000, salePrice=80000, quantity=10, productId=2, flashSaleId=2)")
        void success_createDetail() {
            CreateFlashSaleDetailDto dto = new CreateFlashSaleDetailDto(
                    100_000, 80_000, 10, 2L, 2L
            );
            when(productService.findProductById(2L)).thenReturn(product(2L));
            when(flashSaleService.findFlashSaleById(2L)).thenReturn(sale(2L));
            when(flashSaleDetailRepository.save(any(Flash_Sale_Details.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Flash_Sale_Details out = flashSaleDetailService.createFlashSaleDetail(dto);

            assertThat(out.getOriginalPrice()).isEqualTo(100_000);
            assertThat(out.getSalePrice()).isEqualTo(80_000);
            assertThat(out.getQuantity()).isEqualTo(10);
            assertThat(out.getProduct().getId()).isEqualTo(2L);
            assertThat(out.getFlashSale().getId()).isEqualTo(2L);

            verify(productService).findProductById(2L);
            verify(flashSaleService).findFlashSaleById(2L);
            verify(flashSaleDetailRepository).save(any(Flash_Sale_Details.class));
            verifyNoMoreInteractions(productService, flashSaleService, flashSaleDetailRepository);
        }

        @Test
        @DisplayName("UTFSD002: originalPrice < 0 ⇒ IllegalArgumentException('Original price, sale price and quantity must be non-negative')")
        void negativeOriginalPrice_throws() {
            CreateFlashSaleDetailDto dto = new CreateFlashSaleDetailDto(-100, 80_000, 10, 2L, 2L);

            assertThatThrownBy(() -> flashSaleDetailService.createFlashSaleDetail(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Original price, sale price and quantity must be non-negative");

            verifyNoInteractions(productService, flashSaleService, flashSaleDetailRepository);
        }

        @Test
        @DisplayName("UTFSD003: salePrice < 0 ⇒ IllegalArgumentException(...)")
        void negativeSalePrice_throws() {
            CreateFlashSaleDetailDto dto = new CreateFlashSaleDetailDto(100_000, -1, 10, 2L, 2L);

            assertThatThrownBy(() -> flashSaleDetailService.createFlashSaleDetail(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Original price, sale price and quantity must be non-negative");

            verifyNoInteractions(productService, flashSaleService, flashSaleDetailRepository);
        }

        @Test
        @DisplayName("UTFSD004: quantity < 0 ⇒ IllegalArgumentException(...)")
        void negativeQuantity_throws() {
            CreateFlashSaleDetailDto dto = new CreateFlashSaleDetailDto(100_000, 80_000, -10, 2L, 2L);

            assertThatThrownBy(() -> flashSaleDetailService.createFlashSaleDetail(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Original price, sale price and quantity must be non-negative");

            verifyNoInteractions(productService, flashSaleService, flashSaleDetailRepository);
        }

        @Test
        @DisplayName("UTFSD005: productId không tồn tại ⇒ NotFoundException('Product not found')")
        void productNotFound_throws() {
            CreateFlashSaleDetailDto dto = new CreateFlashSaleDetailDto(100_000, 80_000, 10, 5L, 2L);
            when(productService.findProductById(5L)).thenReturn(null);

            assertThatThrownBy(() -> flashSaleDetailService.createFlashSaleDetail(dto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Product not found");

            verify(productService).findProductById(5L);
            verifyNoMoreInteractions(productService);
            verifyNoInteractions(flashSaleService, flashSaleDetailRepository);
        }

        @Test
        @DisplayName("UTFSD006: flashSaleId không tồn tại ⇒ NotFoundException('Flash sale not found')")
        void flashSaleNotFound_throws() {
            CreateFlashSaleDetailDto dto = new CreateFlashSaleDetailDto(100_000, 80_000, 10, 2L, 5L);
            when(productService.findProductById(2L)).thenReturn(product(2L));
            when(flashSaleService.findFlashSaleById(5L)).thenReturn(null);

            assertThatThrownBy(() -> flashSaleDetailService.createFlashSaleDetail(dto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Flash sale not found");

            verify(productService).findProductById(2L);
            verify(flashSaleService).findFlashSaleById(5L);
            verifyNoMoreInteractions(productService, flashSaleService);
            verifyNoInteractions(flashSaleDetailRepository);
        }
    }

    @Nested
    @DisplayName("findAllByFlashSaleId")
    class FindAllByFlashSaleId {

        @Test
        @DisplayName("UTFSD-FS001: flashSaleId=2 ⇒ return [d1,d2]")
        void success_return_list() {
            long fsId = 2L;
            when(flashSaleService.findFlashSaleById(fsId)).thenReturn(sale(fsId));
            List<Flash_Sale_Details> data = List.of(
                    detail(11L, fsId, 101L),
                    detail(12L, fsId, 102L)
            );
            when(flashSaleDetailRepository.findAllFlashSaleDetailsByFlashSaleId(fsId))
                    .thenReturn(data);

            List<Flash_Sale_Details> out = flashSaleDetailService.findAllByFlashSaleId(fsId);

            assertThat(out).hasSize(2);
            assertThat(out).extracting(Flash_Sale_Details::getId)
                    .containsExactlyInAnyOrder(11L, 12L);

            verify(flashSaleService).findFlashSaleById(fsId);
            verify(flashSaleDetailRepository).findAllFlashSaleDetailsByFlashSaleId(fsId);
            verifyNoMoreInteractions(flashSaleService, flashSaleDetailRepository);
        }

        @Test
        @DisplayName("UTFSD-FS002: flashSaleId=null ⇒ IllegalArgumentException('Invalid flash sale ID')")
        void invalid_null_throws() {
            assertThatThrownBy(() -> flashSaleDetailService.findAllByFlashSaleId(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid flash sale ID");

            verifyNoInteractions(flashSaleService, flashSaleDetailRepository);
        }

        @Test
        @DisplayName("UTFSD-FS003: flashSaleId=10 ⇒ NotFoundException('Flash sale not found')")
        void notFound_saleId10() {
            when(flashSaleService.findFlashSaleById(10L)).thenReturn(null);

            assertThatThrownBy(() -> flashSaleDetailService.findAllByFlashSaleId(10L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Flash sale not found");

            verify(flashSaleService).findFlashSaleById(10L);
            verifyNoMoreInteractions(flashSaleService);
            verifyNoInteractions(flashSaleDetailRepository);
        }

        @Test
        @DisplayName("UTFSD-FS004: flashSaleId=100 ⇒ NotFoundException('Flash sale not found')")
        void notFound_saleId100() {
            when(flashSaleService.findFlashSaleById(100L)).thenReturn(null);

            assertThatThrownBy(() -> flashSaleDetailService.findAllByFlashSaleId(100L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Flash sale not found");

            verify(flashSaleService).findFlashSaleById(100L);
            verifyNoMoreInteractions(flashSaleService);
            verifyNoInteractions(flashSaleDetailRepository);
        }

        @Test
        @DisplayName("UTFSD-FS005 (boundary): flashSaleId=2 nhưng repo rỗng ⇒ NotFoundException('Flash sale detail not found')")
        void saleExists_butDetailsEmpty() {
            long fsId = 2L;
            when(flashSaleService.findFlashSaleById(fsId)).thenReturn(sale(fsId));
            when(flashSaleDetailRepository.findAllFlashSaleDetailsByFlashSaleId(fsId))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> flashSaleDetailService.findAllByFlashSaleId(fsId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Flash sale detail not found");

            verify(flashSaleService).findFlashSaleById(fsId);
            verify(flashSaleDetailRepository).findAllFlashSaleDetailsByFlashSaleId(fsId);
            verifyNoMoreInteractions(flashSaleService, flashSaleDetailRepository);
        }
    }

    @Nested
    @DisplayName("findAllByProductId")
    class FindAllByProductId {

        @Test
        @DisplayName("UTFSD-P001: productId=2 ⇒ return [d1,d2]")
        void success_return_list() {
            long productId = 2L;
            when(productService.findProductById(productId)).thenReturn(product(productId));
            List<Flash_Sale_Details> data = List.of(
                    detail(21L, 200L, productId),
                    detail(22L, 201L, productId)
            );
            when(flashSaleDetailRepository.findAllFlashSaleDetailsByProductId(productId))
                    .thenReturn(data);

            List<Flash_Sale_Details> out = flashSaleDetailService.findAllByProductId(productId);

            assertThat(out).hasSize(2);
            assertThat(out).extracting(Flash_Sale_Details::getId)
                    .containsExactlyInAnyOrder(21L, 22L);

            verify(productService).findProductById(productId);
            verify(flashSaleDetailRepository).findAllFlashSaleDetailsByProductId(productId);
            verifyNoMoreInteractions(productService, flashSaleDetailRepository);
            verifyNoInteractions(flashSaleService);
        }

        @Test
        @DisplayName("UTFSD-P002: productId=null ⇒ IllegalArgumentException('Invalid product ID')")
        void invalid_null_throws() {
            assertThatThrownBy(() -> flashSaleDetailService.findAllByProductId(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid product ID");

            verifyNoInteractions(productService, flashSaleDetailRepository, flashSaleService);
        }

        @Test
        @DisplayName("UTFSD-P003: productId=5 ⇒ NotFoundException('Product not found')")
        void productNotFound_5() {
            when(productService.findProductById(5L)).thenReturn(null);

            assertThatThrownBy(() -> flashSaleDetailService.findAllByProductId(5L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Product not found");

            verify(productService).findProductById(5L);
            verifyNoMoreInteractions(productService);
            verifyNoInteractions(flashSaleDetailRepository, flashSaleService);
        }

        @Test
        @DisplayName("UTFSD-P004: productId=100 ⇒ NotFoundException('Product not found')")
        void productNotFound_100() {
            when(productService.findProductById(100L)).thenReturn(null);

            assertThatThrownBy(() -> flashSaleDetailService.findAllByProductId(100L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Product not found");

            verify(productService).findProductById(100L);
            verifyNoMoreInteractions(productService);
            verifyNoInteractions(flashSaleDetailRepository, flashSaleService);
        }
    }

    @Nested
    @DisplayName("findAllByFlashSaleDetail(page, limit)")
    class findAllByFlashSaleDetail {

        @Test
        @DisplayName("UTFSD-ALL001: page=0, limit=2 ⇒ content=[Detail1, Detail2] (size=2)")
        void page0_limit2_success() {
            Pageable pageable = PageRequest.of(0, 2);
            List<Flash_Sale_Details> data = List.of(
                    detail(1L, "Detail1"),
                    detail(2L, "Detail2")
            );
            Page<Flash_Sale_Details> pageObj = new PageImpl<>(data, pageable, 2);

            when(flashSaleDetailRepository.findAllFlashSaleDetails(pageable))
                    .thenReturn(pageObj);

            var out = flashSaleDetailService.findAllByFlashSaleDetail(0, 2);

            assertThat(out).hasSize(2);
            assertThat(out).extracting(Flash_Sale_Details::getId)
                    .containsExactlyInAnyOrder(1L, 2L);

            verify(flashSaleDetailRepository).findAllFlashSaleDetails(pageable);
            verifyNoMoreInteractions(flashSaleDetailRepository);
            verifyNoInteractions(flashSaleService, productService);
        }

        @Test
        @DisplayName("UTFSD-ALL002: page=1, limit=2 ⇒ NotFoundException('No flash sale detail found')")
        void page1_limit2_notFound() {
            Pageable pageable = PageRequest.of(1, 2);
            Page<Flash_Sale_Details> empty = new PageImpl<>(List.of(), pageable, 2);

            when(flashSaleDetailRepository.findAllFlashSaleDetails(pageable))
                    .thenReturn(empty);

            assertThatThrownBy(() -> flashSaleDetailService.findAllByFlashSaleDetail(1, 2))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("No flash sale detail found");

            verify(flashSaleDetailRepository).findAllFlashSaleDetails(pageable);
            verifyNoMoreInteractions(flashSaleDetailRepository);
            verifyNoInteractions(flashSaleService, productService);
        }

        @Test
        @DisplayName("UTFSD-ALL003: page=-1, limit=2 ⇒ IllegalArgumentException('Invalid page or limit')")
        void pageNegative_invalid() {
            assertThatThrownBy(() -> flashSaleDetailService.findAllByFlashSaleDetail(-1, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid page or limit");

            verifyNoInteractions(flashSaleDetailRepository, flashSaleService, productService);
        }
    }

    @Nested
    @DisplayName("updateDetails")
    class updateDetails {

        @Test
        @DisplayName("UTUD001: id=2, original=120000, sale=90000, qty=15 ⇒ cập nhật cả 3 trường")
        void update_all_fields_success() {
            Flash_Sale_Details existing = storedDetail();
            when(flashSaleDetailRepository.findByFlashSaleDetailId(2L)).thenReturn(existing);
            when(flashSaleDetailRepository.save(any(Flash_Sale_Details.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UpdateFlashSaleDetailDto dto = new UpdateFlashSaleDetailDto(120_000, 90_000, 15);

            Flash_Sale_Details out = flashSaleDetailService.updateDetails(2L, dto);

            assertThat(out.getId()).isEqualTo(2L);
            assertThat(out.getOriginalPrice()).isEqualTo(120_000);
            assertThat(out.getSalePrice()).isEqualTo(90_000);
            assertThat(out.getQuantity()).isEqualTo(15);

            verify(flashSaleDetailRepository).findByFlashSaleDetailId(2L);
            verify(flashSaleDetailRepository).save(existing);
            verifyNoMoreInteractions(flashSaleDetailRepository);
        }

        @Test
        @DisplayName("UTUD002: id=2, original=null, sale=90000, qty=15 ⇒ giữ nguyên original, cập nhật 2 trường còn lại")
        void update_skip_null_original_success() {
            Flash_Sale_Details existing = storedDetail();
            when(flashSaleDetailRepository.findByFlashSaleDetailId(2L)).thenReturn(existing);
            when(flashSaleDetailRepository.save(any(Flash_Sale_Details.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UpdateFlashSaleDetailDto dto = new UpdateFlashSaleDetailDto(null, 90_000, 15);

            Flash_Sale_Details out = flashSaleDetailService.updateDetails(2L, dto);

            assertThat(out.getOriginalPrice()).isEqualTo(100_000);
            assertThat(out.getSalePrice()).isEqualTo(90_000);
            assertThat(out.getQuantity()).isEqualTo(15);
        }

        @Test
        @DisplayName("UTUD003: id=0 ⇒ IllegalArgumentException('Invalid flash sale detail ID')")
        void invalid_id_zero_throws() {
            UpdateFlashSaleDetailDto dto = new UpdateFlashSaleDetailDto(120_000, 90_000, 15);

            assertThatThrownBy(() -> flashSaleDetailService.updateDetails(0L, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid flash sale detail ID");

            verifyNoInteractions(flashSaleDetailRepository);
        }

        @Test
        @DisplayName("UTUD004: id=6 (không tồn tại) ⇒ NotFoundException('Flash sale detail not found')")
        void id_not_found_throws() {
            when(flashSaleDetailRepository.findByFlashSaleDetailId(6L)).thenReturn(null);

            UpdateFlashSaleDetailDto dto = new UpdateFlashSaleDetailDto(120_000, 90_000, 15);

            assertThatThrownBy(() -> flashSaleDetailService.updateDetails(6L, dto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Flash sale detail not found");

            verify(flashSaleDetailRepository).findByFlashSaleDetailId(6L);
            verifyNoMoreInteractions(flashSaleDetailRepository);
        }

        @Test
        @DisplayName("UTUD005: originalPrice=-10000 ⇒ IllegalArgumentException('Original price must be non-negative')")
        void negative_original_throws() {
            UpdateFlashSaleDetailDto dto = new UpdateFlashSaleDetailDto(-10_000, 90_000, 15);

            assertThatThrownBy(() -> flashSaleDetailService.updateDetails(2L, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Original price must be non-negative");

            verifyNoInteractions(flashSaleDetailRepository);
        }

        @Test
        @DisplayName("UTUD006: salePrice=-10 ⇒ IllegalArgumentException('Sale price must be non-negative')")
        void negative_sale_throws() {
            UpdateFlashSaleDetailDto dto = new UpdateFlashSaleDetailDto(120_000, -10, 15);

            assertThatThrownBy(() -> flashSaleDetailService.updateDetails(2L, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Sale price must be non-negative");

            verifyNoInteractions(flashSaleDetailRepository);
        }

        @Test
        @DisplayName("UTUD007: quantity=-1000 ⇒ IllegalArgumentException('Quantity must be non-negative')")
        void negative_quantity_throws() {
            UpdateFlashSaleDetailDto dto = new UpdateFlashSaleDetailDto(120_000, 90_000, -1_000);

            assertThatThrownBy(() -> flashSaleDetailService.updateDetails(2L, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Quantity must be non-negative");

            verifyNoInteractions(flashSaleDetailRepository);
        }
    }

    @Nested
    @DisplayName("deleteFlashSaleDetail")
    class deleteDetails {

        @Test
        @DisplayName("UTDFSD001: id=2 ⇒ delete successfully (soft delete được gọi)")
        void delete_success_id2() {
            when(flashSaleDetailRepository.findByFlashSaleDetailId(2L))
                    .thenReturn(detail(2L, "Sale 1"));

            Flash_Sale_Details out = flashSaleDetailService.deleteFlashSaleDetail(2L);

            assertThat(out).isNotNull();
            assertThat(out.getId()).isEqualTo(2L);

            verify(flashSaleDetailRepository).findByFlashSaleDetailId(2L);
            verify(flashSaleDetailRepository).softDeleteByIdFlashSaleDetail(2L);
            verifyNoMoreInteractions(flashSaleDetailRepository);
            verifyNoInteractions(flashSaleService, productService);
        }

        @Test
        @DisplayName("UTDFSD002: id=null ⇒ IllegalArgumentException('Invalid flash sale detail ID')")
        void delete_invalid_null() {
            assertThatThrownBy(() -> flashSaleDetailService.deleteFlashSaleDetail(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid flash sale detail ID");

            verifyNoInteractions(flashSaleDetailRepository, flashSaleService, productService);
        }

        @Test
        @DisplayName("UTDFSD003: id=100 ⇒ NotFoundException('Flash sale detail not found')")
        void delete_not_found() {
            when(flashSaleDetailRepository.findByFlashSaleDetailId(100L))
                    .thenReturn(null);

            assertThatThrownBy(() -> flashSaleDetailService.deleteFlashSaleDetail(100L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Flash sale detail not found");

            verify(flashSaleDetailRepository).findByFlashSaleDetailId(100L);
            verify(flashSaleDetailRepository, never()).softDeleteByIdFlashSaleDetail(anyLong());
            verifyNoMoreInteractions(flashSaleDetailRepository);
            verifyNoInteractions(flashSaleService, productService);
        }
    }
}
