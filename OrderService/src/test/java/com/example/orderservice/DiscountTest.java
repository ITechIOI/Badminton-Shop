package com.example.orderservice;

import com.example.orderservice.models.Discounts;
import com.example.orderservice.modules.Discounts.dto.CreateDiscountDto;
import com.example.orderservice.modules.Discounts.dto.UpdateDiscountDto;
import com.example.orderservice.modules.Discounts.repository.DiscountRepository;
import com.example.orderservice.modules.Discounts.service.DiscountService;
import com.example.orderservice.utils.NotFoundException;
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
import com.example.orderservice.utils.PagedResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class DiscountTest {
    @Mock
    private DiscountRepository discountRepository;

    @InjectMocks
    private DiscountService discountService;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private CreateDiscountDto makeDto(Integer percent, Integer minOrderValue, Integer count,
                                      String start, String end, String status) throws Exception {
        return new CreateDiscountDto(
                "SALE2025",
                "Autumn Discount",
                percent,
                minOrderValue,
                count,
                start != null ? sdf.parse(start) : null,
                end != null ? sdf.parse(end) : null,
                status
        );
    }

    private Discounts mockDiscount(long id) {
        Discounts d = new Discounts();
        d.setId(id);
        d.setCode("SALE10");
        d.setDescription("10% Autumn Discount");
        d.setPercent(10);
        return d;
    }

    private Discounts mockDiscountByCode(String code) {
        Discounts d = new Discounts();
        d.setId(2L);
        d.setCode(code);
        d.setDescription("Discount for VIP users");
        d.setPercent(20);
        return d;
    }

    private Discounts mockDiscountFullAttribute(long id, String code) {
        Discounts d = new Discounts();
        d.setId(id);
        d.setCode(code);
        d.setPercent(10);
        d.setDescription("Test Discount " + id);
        return d;
    }

    private UpdateDiscountDto dto(Integer percent, Integer minOrder, Integer count, Date start, Date end, String status) {
        return new UpdateDiscountDto("DIS1", "Sale Occasion", percent, minOrder, count, start, end, status);
    }

    @Nested
    @DisplayName("createDiscount() validation")
    class CreateDiscountValidation {

        @Test
        @DisplayName("UTCD001: Valid discount ⇒ should save successfully")
        void success_createDiscount() throws Exception {
            CreateDiscountDto dto = makeDto(10, 10000, 5,
                    "2025-09-27 16:41:00", "2025-10-01 16:41:00", "available");

            Discounts mockSaved = new Discounts();
            mockSaved.setId(1L);
            when(discountRepository.save(any())).thenReturn(mockSaved);

            Discounts result = discountService.createDiscount(dto);

            assertThat(result).isNotNull();
            verify(discountRepository).save(any());
        }

        @Test
        @DisplayName("UTCD002: Percent < 0 ⇒ 'Percent must be between 0 and 100'")
        void fail_percentNegative() throws Exception {
            CreateDiscountDto dto = makeDto(-10, 10000, 5,
                    "2025-09-27 16:41:00", "2025-10-01 16:41:00", "available");

            assertThatThrownBy(() -> discountService.createDiscount(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Percent must be between 0 and 100");

            verifyNoInteractions(discountRepository);
        }

        @Test
        @DisplayName("UTCD003: MinOrderValue < 0 ⇒ 'Min order value must be non-negative'")
        void fail_minOrderValueNegative() throws Exception {
            CreateDiscountDto dto = makeDto(10, -1000, 5,
                    "2025-09-27 16:41:00", "2025-10-01 16:41:00", "available");

            assertThatThrownBy(() -> discountService.createDiscount(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Min order value must be non-negative");

            verifyNoInteractions(discountRepository);
        }

        @Test
        @DisplayName("UTCD004: Count < 0 ⇒ 'Count must be non-negative'")
        void fail_countNegative() throws Exception {
            CreateDiscountDto dto = makeDto(10, 10000, -5,
                    "2025-09-27 16:41:00", "2025-10-01 16:41:00", "available");

            assertThatThrownBy(() -> discountService.createDiscount(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Count must be non-negative");

            verifyNoInteractions(discountRepository);
        }

        @Test
        @DisplayName("UTCD005: EndTime = null ⇒ 'Invalid start time or end time'")
        void fail_endTimeNull() throws Exception {
            CreateDiscountDto dto = makeDto(10, 10000, 5,
                    "2025-09-27 16:41:00", null, "available");

            assertThatThrownBy(() -> discountService.createDiscount(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid start time or end time");

            verifyNoInteractions(discountRepository);
        }

        @Test
        @DisplayName("UTCD006: EndTime before StartTime ⇒ 'Invalid start time or end time'")
        void fail_endBeforeStart() throws Exception {
            CreateDiscountDto dto = makeDto(10, 10000, 5,
                    "2025-10-08 16:41:00", "2025-09-27 16:41:00", "available");

            assertThatThrownBy(() -> discountService.createDiscount(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid start time or end time");

            verifyNoInteractions(discountRepository);
        }
    }

    @Nested
    @DisplayName("findDiscountById()")
    class FindDiscountById {

        @Test
        @DisplayName("UTFDI001: Valid id=1 ⇒ Return Discount object")
        void success_findDiscountById() {
            Discounts discount = mockDiscount(1L);
            when(discountRepository.findOneById(1L)).thenReturn(Optional.of(discount));

            Discounts result = discountService.findDiscountById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCode()).isEqualTo("SALE10");
            verify(discountRepository).findOneById(1L);
        }

        @Test
        @DisplayName("UTFDI002: id=null ⇒ throw IllegalArgumentException('Invalid discount ID')")
        void fail_invalidId_null() {
            assertThatThrownBy(() -> discountService.findDiscountById(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid discount ID");

            verifyNoInteractions(discountRepository);
        }

        @Test
        @DisplayName("UTFDI003: id=100 ⇒ throw NotFoundException('Discount not found')")
        void fail_notFound() {
            when(discountRepository.findOneById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> discountService.findDiscountById(100L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Discount not found");

            verify(discountRepository).findOneById(100L);
        }
    }

    @Nested
    @DisplayName("findDiscountByCode()")
    class FindDiscountByCode {

        @Test
        @DisplayName("UTFDC001: code='DIS2' ⇒ Return discount object")
        void success_findDiscountByCode() {
            Discounts discount = mockDiscountByCode("DIS2");
            when(discountRepository.findOneByCode("DIS2")).thenReturn(Optional.of(discount));

            Discounts result = discountService.findDiscountByCode("DIS2");

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("DIS2");
            verify(discountRepository).findOneByCode("DIS2");
        }

        @Test
        @DisplayName("UTFDC002: code=null ⇒ throw IllegalArgumentException('Invalid discount code')")
        void fail_invalidCode_null() {
            assertThatThrownBy(() -> discountService.findDiscountByCode(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid discount code");

            verifyNoInteractions(discountRepository);
        }

        @Test
        @DisplayName("UTFDC003: code='DIS100' ⇒ throw NotFoundException('Discount not found')")
        void fail_discountNotFound() {
            when(discountRepository.findOneByCode("DIS100")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> discountService.findDiscountByCode("DIS100"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Discount not found");

            verify(discountRepository).findOneByCode("DIS100");
        }
    }

    @Nested
    @DisplayName("getAllDiscount()")
    class GetAllDiscountTests {

        @Test
        @DisplayName("UTGAD001: page=0, limit=2 ⇒ return list of 2 discounts")
        void success_getAllDiscounts() {
            List<Discounts> discounts = Arrays.asList(
                    mockDiscountFullAttribute(1, "DIS1"),
                    mockDiscountFullAttribute(2, "DIS2")
            );
            Page<Discounts> page = new PageImpl<>(discounts, PageRequest.of(0, 2), 2);
            when(discountRepository.findAllDiscounts(PageRequest.of(0, 2))).thenReturn(page);

            PagedResponse<Discounts> result = discountService.getAllDiscount(0, 2);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(discountRepository).findAllDiscounts(PageRequest.of(0, 2));
        }

        @Test
        @DisplayName("UTGAD002: page=-1 ⇒ 'Invalid page or limit'")
        void fail_pageNegative() {
            assertThatThrownBy(() -> discountService.getAllDiscount(-1, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid page or limit");

            verifyNoInteractions(discountRepository);
        }

        @Test
        @DisplayName("UTGAD004: page=1, limit=2 ⇒ 'Discount not found'")
        void fail_noDiscountFound() {
            Page<Discounts> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 2), 0);
            when(discountRepository.findAllDiscounts(PageRequest.of(1, 2))).thenReturn(emptyPage);

            assertThatThrownBy(() -> discountService.getAllDiscount(1, 2))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Discount not found");

            verify(discountRepository).findAllDiscounts(PageRequest.of(1, 2));
        }
    }

    @Nested
    @DisplayName("updateDiscount()")
    class UpdateDiscountTests {

        @Test
        @DisplayName("UTUD001: Valid id=2, percent=10 ⇒ Success")
        void success_updateDiscount() {
            Discounts discount = mockDiscount(2L);
            Date start = new Date(2025, 8, 27, 16, 41, 43); // 27/09/2025
            Date end = new Date(2025, 9, 27, 16, 41, 43);   // 27/10/2025
            when(discountRepository.findOneById(2L)).thenReturn(Optional.of(discount));
            when(discountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Discounts result = discountService.updateDiscount(2L, dto(10, 10000, 5, start, end, "available"));
            assertThat(result.getPercent()).isEqualTo(10);
            verify(discountRepository).save(any());
        }

        @Test
        @DisplayName("UTUD002: id=0 ⇒ 'Invalid discount ID'")
        void fail_invalidId_zero() {
            assertThatThrownBy(() -> discountService.updateDiscount(0L, dto(10, 100, 5, null, null, "available")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid discount ID");
            verifyNoInteractions(discountRepository);
        }

        @Test
        @DisplayName("UTUD003: percent=-1 ⇒ 'Percent must be between 0 and 100'")
        void fail_percentInvalid() {
            assertThatThrownBy(() -> discountService.updateDiscount(2L, dto(-1, 10000, 5, null, null, "available")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Percent must be between 0 and 100");
        }

        @Test
        @DisplayName("UTUD004: minOrderValue=-100 ⇒ 'Min order value must be non-negative'")
        void fail_minOrderInvalid() {
            assertThatThrownBy(() -> discountService.updateDiscount(2L, dto(10, -100, 5, null, null, "available")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Min order value must be non-negative");
        }

        @Test
        @DisplayName("UTUD005: count=-1 ⇒ 'Count must be non-negative'")
        void fail_countInvalid() {
            assertThatThrownBy(() -> discountService.updateDiscount(2L, dto(10, 10000, -1, null, null, "available")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Count must be non-negative");
        }

        @Test
        @DisplayName("UTUD006: startTime!=null, endTime=null ⇒ 'Both start time and end time must be provided'")
        void fail_missingEndTime() {
            Date start = new Date(2025, 8, 27, 16, 41, 43);
            assertThatThrownBy(() -> discountService.updateDiscount(2L, dto(10, 10000, 5, start, null, "available")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Both start time and end time must be provided");
        }

        @Test
        @DisplayName("UTUD007: endTime < startTime ⇒ 'End time must be after start time'")
        void fail_endBeforeStart() {
            Date start = new Date(2025, 9, 27, 16, 41, 43); // 27/10
            Date end = new Date(2025, 8, 27, 16, 41, 43);   // 27/09
            assertThatThrownBy(() -> discountService.updateDiscount(2L, dto(10, 10000, 5, start, end, "available")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("End time must be after start time");
        }
    }

    @Nested
    @DisplayName("deleteDiscount()")
    class DeleteDiscountTests {

        @Test
        @DisplayName("UTDD001: id=2 ⇒ delete successfully")
        void success_deleteDiscount() {
            when(discountRepository.findOneById(2L)).thenReturn(Optional.of(mockDiscount(2L)));

            // Thực thi hành động xóa
            doNothing().when(discountRepository).softDeleteById(2L);

            discountService.deleteDiscount(2L);

            verify(discountRepository).softDeleteById(2L);
        }

        @Test
        @DisplayName("UTDD002: id=null ⇒ IllegalArgumentException('Invalid discount ID')")
        void fail_invalidId_null() {
            assertThatThrownBy(() -> discountService.deleteDiscount(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid discount ID");

            verifyNoInteractions(discountRepository);
        }

        @Test
        @DisplayName("UTDD003: id=100 ⇒ NotFoundException('Discount not found')")
        void fail_discountNotFound() {
            when(discountRepository.findOneById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> discountService.deleteDiscount(100L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Discount not found");
        }
    }
}
