package com.example.productservice;

import com.example.productservice.models.Flash_Sale;
import com.example.productservice.modules.FlashSale.dto.CreateFlashSaleDto;
import com.example.productservice.modules.FlashSale.dto.UpdateFlashSaleDto;
import com.example.productservice.modules.FlashSale.repository.FlashSaleRepository;
import com.example.productservice.modules.FlashSale.service.FlashSaleService;
import com.example.productservice.utils.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FlashSaleTest {
    @Mock private FlashSaleRepository flashSaleRepository;
    @InjectMocks private FlashSaleService flashSaleService;

    // ===== Thời gian dùng cho create & findByTime =====
    private static final LocalDateTime FUTURE_START = LocalDateTime.of(2025, 12, 20, 9, 0, 0);
    private static final LocalDateTime PAST_START   = LocalDateTime.of(2025,  9, 30, 9, 0, 0);
    private static final LocalDateTime FUTURE_END   = LocalDateTime.of(2025, 12, 31, 23, 59, 59);
    private static final LocalDateTime EARLIER_END  = LocalDateTime.of(2025, 11, 29, 9, 0, 0);
    private static final LocalDateTime LATE_START   = LocalDateTime.of(2025, 12, 30, 9, 0, 0); // dùng cho case end < start

    // ===== Thời gian dùng cho update =====
    private static final LocalDateTime OLD_START = LocalDateTime.of(2025, 11, 20, 9, 0, 0);
    private static final LocalDateTime OLD_END   = LocalDateTime.of(2025, 12, 20, 23, 59, 59);
    private static final LocalDateTime NEW_START = LocalDateTime.of(2025, 11, 22, 9, 0, 0);
    private static final LocalDateTime NEW_END   = LocalDateTime.of(2025, 12, 22, 23, 59, 59);

    @Captor ArgumentCaptor<Flash_Sale> flashSaleCaptor;

    private Flash_Sale fs(long id, String name) {
        Flash_Sale f = new Flash_Sale();
        f.setId(id);
        f.setName(name);
        return f;
    }

    private Flash_Sale existingFs() {
        Flash_Sale f = new Flash_Sale();
        f.setId(2L);
        f.setName("Sale 1");
        f.setDescription("Old Desc");
        f.setStartTime(OLD_START);
        f.setEndTime(OLD_END);
        return f;
    }

    @Nested
    @DisplayName("createFlashSale")
    class createFlashSale {
        @Test
        @DisplayName("should create flash sale successfully")
        void success_withExplicitFlashSale () {
            CreateFlashSaleDto dto = mock(CreateFlashSaleDto.class);
            when(dto.getName()).thenReturn("Sale 1");
            when(dto.getDescription()).thenReturn("Great");
            when(dto.getStartTime()).thenReturn(FUTURE_START);
            when(dto.getEndTime()).thenReturn(FUTURE_END);

            when(flashSaleRepository.save(any(Flash_Sale.class))).thenAnswer(inv -> {
                Flash_Sale fs = inv.getArgument(0);
                fs.setId(1L);
                return fs;
            });

            Flash_Sale saved = flashSaleService.createFlashSale(dto);

            verify(flashSaleRepository).save(flashSaleCaptor.capture());
            Flash_Sale toSave = flashSaleCaptor.getValue();

            assertThat(toSave.getName()).isEqualTo("Sale 1");
            assertThat(toSave.getDescription()).isEqualTo("Great");
            assertThat(toSave.getStartTime()).isEqualTo(FUTURE_START);
            assertThat(toSave.getEndTime()).isEqualTo(FUTURE_END);
            assertThat(saved.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("UTFSL002: endTime < startTime ⇒ IllegalArgumentException('End time must be after start time')")
        void createFlashSale_endBeforeStart_throws() {
            CreateFlashSaleDto dto = mock(CreateFlashSaleDto.class);
            when(dto.getStartTime()).thenReturn(FUTURE_START);
            when(dto.getEndTime()).thenReturn(EARLIER_END);

            assertThatThrownBy(() -> flashSaleService.createFlashSale(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("End time must be after start time");

            verifyNoInteractions(flashSaleRepository);
        }

        @Test
        @DisplayName("UTFSL003: startTime quá khứ ⇒ IllegalArgumentException('Start time must be in the future')")
        void createFlashSale_startInPast_throws() {
            CreateFlashSaleDto dto = mock(CreateFlashSaleDto.class);
            when(dto.getStartTime()).thenReturn(PAST_START);
            when(dto.getEndTime()).thenReturn(FUTURE_END);

            assertThatThrownBy(() -> flashSaleService.createFlashSale(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Start time must be in the future");

            verifyNoInteractions(flashSaleRepository);
        }
    }

    @Nested
    @DisplayName("findFlashSaleById")
    class FindById {
        @Test
        @DisplayName("UTFSLBID001: id=1 ⇒ trả về Flash Sale information")
        void findById_success_id1() {
            when(flashSaleRepository.findOneById(1L)).thenReturn(fs(1L, "Black Friday"));
            Flash_Sale out = flashSaleService.findFlashSaleById(1L);

            assertThat(out.getId()).isEqualTo(1L);
            assertThat(out.getName()).isEqualTo("Black Friday");
            verify(flashSaleRepository).findOneById(1L);
            verifyNoMoreInteractions(flashSaleRepository);
        }

        @Test
        @DisplayName("UTFSLBID002: id=100 ⇒ NotFoundException('Flash sale not found')")
        void findById_notFound_id100() {
            when(flashSaleRepository.findOneById(100L)).thenReturn(null);

            assertThatThrownBy(() -> flashSaleService.findFlashSaleById(100L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Flash sale not found");

            verify(flashSaleRepository).findOneById(100L);
            verifyNoMoreInteractions(flashSaleRepository);
        }

        @Test
        @DisplayName("UTFSLBID003: id=null ⇒ IllegalArgumentException('Invalid flash sale ID')")
        void findById_invalid_null() {
            assertThatThrownBy(() -> flashSaleService.findFlashSaleById(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid flash sale ID");

            verifyNoInteractions(flashSaleRepository);
        }
    }

    @Nested
    @DisplayName("findFlashSaleByTime")
    class FindByTime {
        private Flash_Sale make(long id, LocalDateTime s, LocalDateTime e) {
            Flash_Sale f = new Flash_Sale();
            f.setId(id);
            f.setName("FlashSale");
            f.setStartTime(s);
            f.setEndTime(e);
            return f;
        }

        @Test
        @DisplayName("UTFSLTIME001: start=2025-11-30T09:00, end=2025-12-31T23:59:59 ⇒ trả về bản ghi")
        void success_found() {
            when(flashSaleRepository.findFlashSaleByTime(FUTURE_START, FUTURE_END))
                    .thenReturn(make(1L, FUTURE_START, FUTURE_END));

            Flash_Sale out = flashSaleService.findFlashSaleByTime(FUTURE_START, FUTURE_END);

            assertThat(out).isNotNull();
            assertThat(out.getId()).isEqualTo(1L);
            assertThat(out.getStartTime()).isEqualTo(FUTURE_START);
            assertThat(out.getEndTime()).isEqualTo(FUTURE_END);
            verify(flashSaleRepository).findFlashSaleByTime(FUTURE_START, FUTURE_END);
            verifyNoMoreInteractions(flashSaleRepository);
        }

        @Test
        @DisplayName("UTFSLTIME002: end < start ⇒ IllegalArgumentException('End time must be after start time')")
        void endBeforeStart_throws() {
            assertThatThrownBy(() -> flashSaleService.findFlashSaleByTime(LATE_START, EARLIER_END))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("End time must be after start time");
            verifyNoInteractions(flashSaleRepository);
        }

        @Test
        @DisplayName("UTFSLTIME003: start=null ⇒ IllegalArgumentException('Start time and end time cannot be null')")
        void startNull_throws() {
            assertThatThrownBy(() -> flashSaleService.findFlashSaleByTime(null, FUTURE_END))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Start time and end time cannot be null");
            verifyNoInteractions(flashSaleRepository);
        }

        @Test
        @DisplayName("UTFSLTIME004: end=null ⇒ IllegalArgumentException('Start time and end time cannot be null')")
        void endNull_throws() {
            assertThatThrownBy(() -> flashSaleService.findFlashSaleByTime(FUTURE_START, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Start time and end time cannot be null");
            verifyNoInteractions(flashSaleRepository);
        }

        @Test
        @DisplayName("UTFSLTIME005: repo trả null ⇒ NotFoundException('Flash sale not found')")
        void notFound_throws() {
            when(flashSaleRepository.findFlashSaleByTime(FUTURE_START, FUTURE_END)).thenReturn(null);

            assertThatThrownBy(() -> flashSaleService.findFlashSaleByTime(FUTURE_START, FUTURE_END))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Flash sale not found");

            verify(flashSaleRepository).findFlashSaleByTime(FUTURE_START, FUTURE_END);
            verifyNoMoreInteractions(flashSaleRepository);
        }
    }

    @Nested
    @DisplayName("getAllFlashSales")
    class GetAll {
        @Test
        @DisplayName("UTGAFS001: page=0, limit=2 ⇒ content=[Detail1, Detail2], totalPages=1, totalElements=2")
        void page0_limit2_success() {
            Pageable pageable = PageRequest.of(0, 2);
            List<Flash_Sale> data = List.of(fs(1L, "Detail1"), fs(2L, "Detail2"));
            Page<Flash_Sale> page = new PageImpl<>(data, pageable, 2);

            when(flashSaleRepository.findAllFlashSales(pageable)).thenReturn(page);

            var resp = flashSaleService.getAllFlashSales(0, 2);

            assertThat(resp.getContent()).extracting(Flash_Sale::getName)
                    .containsExactlyInAnyOrder("Detail1", "Detail2");
            assertThat(resp.getTotalPages()).isEqualTo(1);
            assertThat(resp.getTotalElements()).isEqualTo(2);

            verify(flashSaleRepository).findAllFlashSales(pageable);
            verifyNoMoreInteractions(flashSaleRepository);
        }

        @Test
        @DisplayName("UTGAFS002: page=1, limit=2 ⇒ NotFoundException('No flash sale found')")
        void page1_limit2_notFound() {
            Pageable pageable = PageRequest.of(1, 2);
            Page<Flash_Sale> emptyPage = new PageImpl<>(List.of(), pageable, 2);

            when(flashSaleRepository.findAllFlashSales(pageable)).thenReturn(emptyPage);

            assertThatThrownBy(() -> flashSaleService.getAllFlashSales(1, 2))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("No flash sale found");

            verify(flashSaleRepository).findAllFlashSales(pageable);
            verifyNoMoreInteractions(flashSaleRepository);
        }

        @Test
        @DisplayName("UTGAFS003: page=-1, limit=2 ⇒ IllegalArgumentException('Invalid page or limit')")
        void pageNegative_invalid() {
            assertThatThrownBy(() -> flashSaleService.getAllFlashSales(-1, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid page or limit");

            verifyNoInteractions(flashSaleRepository);
        }
    }

    @Nested
    @DisplayName("updateFlashSale")
    class UpdateFlashSale {

        @Test
        @DisplayName("UTUFS001: id=2, name='Sale 2' ⇒ name được cập nhật, các field khác giữ nguyên")
        void update_name_only() {
            Flash_Sale stored = existingFs();
            when(flashSaleRepository.findOneById(2L)).thenReturn(stored);
            when(flashSaleRepository.save(any(Flash_Sale.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UpdateFlashSaleDto dto = new UpdateFlashSaleDto(
                    "Sale 2", null, null, null
            );

            Flash_Sale updated = flashSaleService.updateFlashSale(2L, dto);

            assertThat(updated.getId()).isEqualTo(2L);
            assertThat(updated.getName()).isEqualTo("Sale 2");
            assertThat(updated.getDescription()).isEqualTo("Old Desc");
            assertThat(updated.getStartTime()).isEqualTo(OLD_START);
            assertThat(updated.getEndTime()).isEqualTo(OLD_END);

            verify(flashSaleRepository).findOneById(2L);
            verify(flashSaleRepository).save(stored);
            verifyNoMoreInteractions(flashSaleRepository);
        }

        @Test
        @DisplayName("UTUFS002: id=2, description='Summer Sale' ⇒ description được cập nhật")
        void update_description_only() {
            Flash_Sale stored = existingFs();
            when(flashSaleRepository.findOneById(2L)).thenReturn(stored);
            when(flashSaleRepository.save(any(Flash_Sale.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UpdateFlashSaleDto dto = new UpdateFlashSaleDto(
                    null, "Summer Sale", null, null
            );

            Flash_Sale updated = flashSaleService.updateFlashSale(2L, dto);

            assertThat(updated.getName()).isEqualTo("Sale 1");
            assertThat(updated.getDescription()).isEqualTo("Summer Sale");
            assertThat(updated.getStartTime()).isEqualTo(OLD_START);
            assertThat(updated.getEndTime()).isEqualTo(OLD_END);
        }

        @Test
        @DisplayName("UTUFS003: id=2, startTime=2025-11-22T09:00 ⇒ startTime được cập nhật")
        void update_startTime_only() {
            Flash_Sale stored = existingFs();
            when(flashSaleRepository.findOneById(2L)).thenReturn(stored);
            when(flashSaleRepository.save(any(Flash_Sale.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UpdateFlashSaleDto dto = new UpdateFlashSaleDto(
                    null, null, NEW_START, null
            );

            Flash_Sale updated = flashSaleService.updateFlashSale(2L, dto);

            assertThat(updated.getStartTime()).isEqualTo(NEW_START);
            assertThat(updated.getEndTime()).isEqualTo(OLD_END);
            assertThat(updated.getName()).isEqualTo("Sale 1");
            assertThat(updated.getDescription()).isEqualTo("Old Desc");
        }

        @Test
        @DisplayName("UTUFS004: id=2, endTime=2025-12-22T23:59:59 ⇒ endTime được cập nhật")
        void update_endTime_only() {
            Flash_Sale stored = existingFs();
            when(flashSaleRepository.findOneById(2L)).thenReturn(stored);
            when(flashSaleRepository.save(any(Flash_Sale.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UpdateFlashSaleDto dto = new UpdateFlashSaleDto(
                    null, null, null, NEW_END
            );

            Flash_Sale updated = flashSaleService.updateFlashSale(2L, dto);

            assertThat(updated.getEndTime()).isEqualTo(NEW_END);
            assertThat(updated.getStartTime()).isEqualTo(OLD_START);
            assertThat(updated.getName()).isEqualTo("Sale 1");
            assertThat(updated.getDescription()).isEqualTo("Old Desc");
        }

        @Test
        @DisplayName("UTUFS005: id=100 ⇒ NotFoundException('Flash sale not found')")
        void id_not_found() {
            when(flashSaleRepository.findOneById(100L)).thenReturn(null);

            UpdateFlashSaleDto dto = new UpdateFlashSaleDto(
                    "Sale 2", "Summer Sale", NEW_START, NEW_END
            );

            assertThatThrownBy(() -> flashSaleService.updateFlashSale(100L, dto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Flash sale not found");

            verify(flashSaleRepository).findOneById(100L);
            verify(flashSaleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteFlashSale")
    class DeleteFlashSale {

        @Test
        @DisplayName("UTDFS001: id=1 ⇒ Delete successfully (softDeleteByIdFlashSale được gọi)")
        void delete_success_id1() {
            // Precondition: DB có bản ghi id=1
            when(flashSaleRepository.findOneById(1L)).thenReturn(fs(1L, "Sale 1"));

            // Act
            flashSaleService.deleteFlashSale(1L);

            // Assert: gọi tìm và gọi soft delete đúng id
            verify(flashSaleRepository).findOneById(1L);
            verify(flashSaleRepository).softDeleteByIdFlashSale(1L);
            verifyNoMoreInteractions(flashSaleRepository);
        }

        @Test
        @DisplayName("UTDFS002: id=null ⇒ IllegalArgumentException('Invalid flash sale ID')")
        void delete_invalidId_null() {
            assertThatThrownBy(() -> flashSaleService.deleteFlashSale(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid flash sale ID");

            verifyNoInteractions(flashSaleRepository);
        }

        @Test
        @DisplayName("UTDFS003: id=2 (không tồn tại) ⇒ NotFoundException('Flash sale not found')")
        void delete_notFound_id2() {
            when(flashSaleRepository.findOneById(2L)).thenReturn(null);

            assertThatThrownBy(() -> flashSaleService.deleteFlashSale(2L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Flash sale not found");

            verify(flashSaleRepository).findOneById(2L);
            verify(flashSaleRepository, never()).softDeleteByIdFlashSale(anyLong());
            verifyNoMoreInteractions(flashSaleRepository);
        }
    }
}
