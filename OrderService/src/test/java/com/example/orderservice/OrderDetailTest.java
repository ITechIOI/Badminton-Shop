package com.example.orderservice;

import com.example.orderservice.models.Discounts;
import com.example.orderservice.models.OrderDetails;
import com.example.orderservice.models.Orders;
import com.example.orderservice.modules.OrderDetails.dto.CreateOrderDetailDto;
import com.example.orderservice.modules.OrderDetails.dto.UpdateOrderDetailDto;
import com.example.orderservice.modules.OrderDetails.dto.output.RawTopProductDto;
import com.example.orderservice.modules.OrderDetails.dto.output.TopProductDto;
import com.example.orderservice.modules.OrderDetails.repository.OrderDetailRepository;
import com.example.orderservice.modules.OrderDetails.service.OrderDetailService;
import com.example.orderservice.modules.Orders.service.OrderService;
import com.example.orderservice.modules.feign.ProductFeign.ProductClient;
import com.example.orderservice.modules.feign.ProductFeign.ProductResponse;
import com.example.orderservice.utils.NotFoundException;
import com.example.orderservice.utils.PagedResponse;
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
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderDetailTest {

    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private OrderService orderService;
    @Mock
    private ProductClient productClient;
    @InjectMocks
    private OrderDetailService orderDetailService;

    // Helper method tạo Orders mẫu
    private static Orders createOrder(Long id, Long userId, Discounts discount) {
        Orders order = new Orders();
        order.setId(id);
        order.setUserId(userId);
        order.setTotalPrice(100);
        order.setAddress("Test Address");
        order.setPhone("0123456789");
        order.setStatus("pending");
        order.setDiscount(discount);
        return order;
    }

    // Helper method tạo ProductResponse mẫu
    private static ProductResponse createProductResponse(Long productId, Integer price) {
        return new ProductResponse("Product " + productId, "Brand", "Description", price, "img.png", "video.mp4",
                "available", 10, 1L);
    }

    // Helper method tạo OrderDetail mẫu
    private static OrderDetails createOrderDetail(Long id, Integer quantity, Integer price, Long productId,
            Long orderId) {
        OrderDetails detail = new OrderDetails();
        detail.setId(id);
        detail.setQuantity(quantity);
        detail.setPrice(price);
        detail.setProudctId(productId);
        Orders order = createOrder(orderId, 1L, null);
        detail.setOrder(order);
        return detail;
    }

    @Nested
    @DisplayName("createDetails")
    class createDetails {

        // ==========================================================
        // UTCOD01 — Precondition: Server works stably, product with id=2 and order with
        // id=2 exist
        // quantity=-10
        // Expected: IllegalArgumentException("Quantity must be greater than 0")
        // ==========================================================
        @Test
        @DisplayName("UTCOD01: quantity=-10 ⇒ IllegalArgumentException")
        void createDetails_UTCOD01_negativeQuantity() {
            CreateOrderDetailDto input = new CreateOrderDetailDto();
            input.setQuantity(-10);
            input.setPrice(10);
            input.setProductId(2L);
            input.setOrderId(2L);

            assertThatThrownBy(() -> orderDetailService.createDetails(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Quantity must be greater than 0");

            verifyNoInteractions(orderService, productClient, orderDetailRepository);
        }

        // ==========================================================
        // UTCOD02 — quantity=10, price=10, productId=2, orderId=2
        // Expected: OrderDetails created successfully
        // ==========================================================
        @Test
        @DisplayName("UTCOD02: quantity=10, price=10, productId=2, orderId=2 ⇒ success")
        void createDetails_UTCOD02_success() {
            // Given
            Long orderId = 2L;
            Long productId = 2L;
            Integer quantity = 10;
            Integer price = 10;

            Orders order = createOrder(orderId, 1L, null);
            ProductResponse product = createProductResponse(productId, price);
            CreateOrderDetailDto input = new CreateOrderDetailDto();
            input.setQuantity(quantity);
            input.setPrice(price);
            input.setProductId(productId);
            input.setOrderId(orderId);

            // Mock behaviors
            when(orderService.findOrderById(orderId)).thenReturn(order);
            when(productClient.getProductById(productId)).thenReturn(ResponseEntity.ok(product));
            when(orderDetailRepository.save(any(OrderDetails.class))).thenAnswer(inv -> {
                OrderDetails od = inv.getArgument(0);
                od.setId(1L);
                return od;
            });

            // When
            OrderDetails result = orderDetailService.createDetails(input);

            // Then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getQuantity()).isEqualTo(quantity);
            assertThat(result.getPrice()).isEqualTo(price);
            assertThat(result.getProudctId()).isEqualTo(productId);
            assertThat(result.getOrder()).isNotNull();
            assertThat(result.getOrder().getId()).isEqualTo(orderId);

            verify(orderService).findOrderById(orderId);
            verify(productClient).getProductById(productId);
            verify(orderDetailRepository).save(any(OrderDetails.class));
        }

        // ==========================================================
        // UTCOD03 — quantity=10, price=-10, productId=2, orderId=2
        // Expected: IllegalArgumentException("Price must be non-negative")
        // ==========================================================
        @Test
        @DisplayName("UTCOD03: price=-10 ⇒ IllegalArgumentException")
        void createDetails_UTCOD03_negativePrice() {
            CreateOrderDetailDto input = new CreateOrderDetailDto();
            input.setQuantity(10);
            input.setPrice(-10);
            input.setProductId(2L);
            input.setOrderId(2L);

            assertThatThrownBy(() -> orderDetailService.createDetails(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Price must be non-negative");

            verifyNoInteractions(orderService, productClient, orderDetailRepository);
        }

        // ==========================================================
        // UTCOD04 — quantity=10, price=10, productId=5 (not exists), orderId=2
        // Expected: NotFoundException("Product not found with id: 5")
        // ==========================================================
        @Test
        @DisplayName("UTCOD04: productId=5 (not exists) ⇒ NotFoundException")
        void createDetails_UTCOD04_productNotFound() {
            // Given
            Long orderId = 2L;
            Long productId = 5L;
            Integer quantity = 10;
            Integer price = 10;

            Orders order = createOrder(orderId, 1L, null);
            CreateOrderDetailDto input = new CreateOrderDetailDto();
            input.setQuantity(quantity);
            input.setPrice(price);
            input.setProductId(productId);
            input.setOrderId(orderId);

            // Mock behaviors
            when(orderService.findOrderById(orderId)).thenReturn(order);
            when(productClient.getProductById(productId)).thenThrow(new RuntimeException("Product not found"));

            // When & Then
            assertThatThrownBy(() -> orderDetailService.createDetails(input))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Product not found with id: 5");

            verify(orderService).findOrderById(orderId);
            verify(productClient).getProductById(productId);
            verify(orderDetailRepository, never()).save(any(OrderDetails.class));
        }

        // ==========================================================
        // UTCOD05 — quantity=10, price=10, productId=2, orderId=5 (not exists)
        // Expected: NotFoundException("Order not found") or
        // IllegalArgumentException("Invalid order ID")
        // ==========================================================
        @Test
        @DisplayName("UTCOD05: orderId=5 (not exists) ⇒ NotFoundException")
        void createDetails_UTCOD05_orderNotFound() {
            // Given
            Long orderId = 5L;
            Long productId = 2L;
            Integer quantity = 10;
            Integer price = 10;

            CreateOrderDetailDto input = new CreateOrderDetailDto();
            input.setQuantity(quantity);
            input.setPrice(price);
            input.setProductId(productId);
            input.setOrderId(orderId);

            // Mock behaviors
            when(orderService.findOrderById(orderId)).thenThrow(new NotFoundException("Order not found"));

            // When & Then
            assertThatThrownBy(() -> orderDetailService.createDetails(input))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Order not found");

            verify(orderService).findOrderById(orderId);
            verifyNoInteractions(productClient, orderDetailRepository);
        }
    }

    @Nested
    @DisplayName("findDetailsByOrderId")
    class findDetailsByOrderId {

        // Helper method tạo OrderDetails mẫu
        private static OrderDetails createOrderDetail(Long id, Long orderId, Long productId, Integer quantity,
                Integer price) {
            OrderDetails detail = new OrderDetails();
            detail.setId(id);
            detail.setQuantity(quantity);
            detail.setPrice(price);
            detail.setProudctId(productId);

            Orders order = new Orders();
            order.setId(orderId);
            detail.setOrder(order);

            return detail;
        }

        // ==========================================================
        // UTFDOBOI01 — Precondition: 3 details in database, 2 details owned by
        // orderId=2, 1 detail owned by orderId=3
        // Server works stably
        // orderId=2, page=0, limit=2
        // Expected: Returns 2 details, totalPages=1, totalElements=2
        // ==========================================================
        @Test
        @DisplayName("UTFDOBOI01: orderId=2, page=0, limit=2 ⇒ returns 2 details")
        void findDetailsByOrderId_UTFDOBOI01_success() {
            // Given
            Long orderId = 2L;
            int page = 0;
            int limit = 2;

            Orders order = createOrder(orderId, 1L, null);
            List<OrderDetails> detailsList = List.of(
                    createOrderDetail(1L, orderId, 1L, 10, 100),
                    createOrderDetail(2L, orderId, 2L, 5, 50));

            Pageable pageable = PageRequest.of(page, limit);
            Page<OrderDetails> detailsPage = new PageImpl<>(detailsList, pageable, 2);

            // Mock behaviors
            when(orderService.findOrderById(orderId)).thenReturn(order);
            when(orderDetailRepository.findDetailsByOrderId(orderId, pageable)).thenReturn(detailsPage);

            // When
            PagedResponse<OrderDetails> result = orderDetailService.findDetailsByOrderId(orderId, page, limit);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().get(0).getOrder().getId()).isEqualTo(orderId);
            assertThat(result.getContent().get(1).getOrder().getId()).isEqualTo(orderId);

            verify(orderService).findOrderById(orderId);
            verify(orderDetailRepository).findDetailsByOrderId(orderId, pageable);
        }

        // ==========================================================
        // UTFDOBOI02 — orderId=null
        // Expected: IllegalArgumentException("Invalid order ID")
        // ==========================================================
        @Test
        @DisplayName("UTFDOBOI02: orderId=null ⇒ IllegalArgumentException")
        void findDetailsByOrderId_UTFDOBOI02_nullOrderId() {
            assertThatThrownBy(() -> orderDetailService.findDetailsByOrderId(null, 0, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid order ID");

            verifyNoInteractions(orderService, orderDetailRepository);
        }

        // ==========================================================
        // UTFDOBOI03 — orderId=2, page=1, limit=1
        // Expected: NotFoundException("Order Details not found")
        // ==========================================================
        @Test
        @DisplayName("UTFDOBOI03: orderId=2, page=1, limit=1 ⇒ NotFoundException")
        void findDetailsByOrderId_UTFDOBOI03_pageOutOfRange() {
            // Given
            Long orderId = 2L;
            int page = 1;
            int limit = 1;

            Orders order = createOrder(orderId, 1L, null);
            Pageable pageable = PageRequest.of(page, limit);
            Page<OrderDetails> detailsPage = new PageImpl<>(List.of(), pageable, 2);

            // Mock behaviors
            when(orderService.findOrderById(orderId)).thenReturn(order);
            when(orderDetailRepository.findDetailsByOrderId(orderId, pageable)).thenReturn(detailsPage);

            // When & Then
            assertThatThrownBy(() -> orderDetailService.findDetailsByOrderId(orderId, page, limit))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Order Details not found");

            verify(orderService).findOrderById(orderId);
            verify(orderDetailRepository).findDetailsByOrderId(orderId, pageable);
        }

        // ==========================================================
        // UTFDOBOI06 — orderId=2, page=-1, limit=3
        // Expected: IllegalArgumentException("Invalid page or limit")
        // ==========================================================
        @Test
        @DisplayName("UTFDOBOI06: page=-1 ⇒ IllegalArgumentException")
        void findDetailsByOrderId_UTFDOBOI06_invalidPage() {
            assertThatThrownBy(() -> orderDetailService.findDetailsByOrderId(2L, -1, 3))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid page or limit");

            verifyNoInteractions(orderService, orderDetailRepository);
        }
    }

    @Nested
    @DisplayName("findTopSellingProducts")
    class findTopSellingProducts {

        // ==========================================================
        // UTFTSP01 — Precondition: Only 1 product sold on 25/11/2025
        // page=0, limit=2, year=2025, month=11, day=25
        // Expected: Returns top selling products successfully
        // ==========================================================
        @Test
        @DisplayName("UTFTSP01: page=0, limit=2, year=2025, month=11, day=25 ⇒ success")
        void findTopSellingProducts_UTFTSP01_success() {
            // Given
            int page = 0;
            int limit = 2;
            Integer year = 2025;
            Integer month = 11;
            Integer day = 25;

            Long productId = 1L;
            ProductResponse product = createProductResponse(productId, 100);
            RawTopProductDto rawDto = new RawTopProductDto(productId, 10L);

            Pageable pageable = PageRequest.of(page, limit);
            Page<RawTopProductDto> rawPage = new PageImpl<>(List.of(rawDto), pageable, 1);

            // Mock behaviors
            when(orderDetailRepository.findTopSellingProductsByDay(year, month, day, pageable)).thenReturn(rawPage);
            when(productClient.getProductById(productId)).thenReturn(ResponseEntity.ok(product));

            // When
            PagedResponse<TopProductDto> result = orderDetailService.findTopSellingProducts(page, limit, year, month,
                    day);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).product().name()).isEqualTo("Product 1");
            assertThat(result.getContent().get(0).totalSold()).isEqualTo(10L);

            verify(orderDetailRepository).findTopSellingProductsByDay(year, month, day, pageable);
            verify(productClient).getProductById(productId);
        }

        // ==========================================================
        // UTFTSP02 — page=-2, limit=2, year=2025, month=11, day=25
        // Expected: IllegalArgumentException("Invalid page or limit")
        // ==========================================================
        @Test
        @DisplayName("UTFTSP02: page=-2 ⇒ IllegalArgumentException")
        void findTopSellingProducts_UTFTSP02_invalidPage() {
            assertThatThrownBy(() -> orderDetailService.findTopSellingProducts(-2, 2, 2025, 11, 25))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid page or limit");

            verifyNoInteractions(orderDetailRepository, productClient);
        }

        // ==========================================================
        // UTFTSP03 — page=0, limit=2, year=1950, month=11, day=25
        // Expected: IllegalArgumentException("Invalid year")
        // ==========================================================
        @Test
        @DisplayName("UTFTSP03: year=1950 ⇒ IllegalArgumentException")
        void findTopSellingProducts_UTFTSP03_invalidYear() {
            assertThatThrownBy(() -> orderDetailService.findTopSellingProducts(0, 2, 1950, 11, 25))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid year");

            verifyNoInteractions(orderDetailRepository, productClient);
        }

        // ==========================================================
        // UTFTSP04 — page=0, limit=2, year=2025, month=0, day=25
        // Expected: IllegalArgumentException("Invalid month")
        // ==========================================================
        @Test
        @DisplayName("UTFTSP04: month=0 ⇒ IllegalArgumentException")
        void findTopSellingProducts_UTFTSP04_invalidMonth() {
            assertThatThrownBy(() -> orderDetailService.findTopSellingProducts(0, 2, 2025, 0, 25))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid month");

            verifyNoInteractions(orderDetailRepository, productClient);
        }

        // ==========================================================
        // UTFTSP05 — page=0, limit=2, year=2025, month=5, day=25
        // Expected: NotFoundException("No top selling products found")
        // ==========================================================
        @Test
        @DisplayName("UTFTSP05: month=5, no products ⇒ NotFoundException")
        void findTopSellingProducts_UTFTSP05_noProductsFound() {
            // Given
            int page = 0;
            int limit = 2;
            Integer year = 2025;
            Integer month = 5;
            Integer day = 25;

            Pageable pageable = PageRequest.of(page, limit);
            Page<RawTopProductDto> rawPage = new PageImpl<>(List.of(), pageable, 0);

            // Mock behaviors
            when(orderDetailRepository.findTopSellingProductsByDay(year, month, day, pageable)).thenReturn(rawPage);

            // When & Then
            assertThatThrownBy(() -> orderDetailService.findTopSellingProducts(page, limit, year, month, day))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("No top selling products found");

            verify(orderDetailRepository).findTopSellingProductsByDay(year, month, day, pageable);
            verifyNoInteractions(productClient);
        }

        // ==========================================================
        // UTFTSP06 — page=0, limit=2, year=2025, month=11, day=32
        // Expected: IllegalArgumentException("Invalid day")
        // ==========================================================
        @Test
        @DisplayName("UTFTSP06: day=32 ⇒ IllegalArgumentException")
        void findTopSellingProducts_UTFTSP06_invalidDay() {
            assertThatThrownBy(() -> orderDetailService.findTopSellingProducts(0, 2, 2025, 11, 32))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid day");

            verifyNoInteractions(orderDetailRepository, productClient);
        }
    }

    @Nested
    @DisplayName("getRevenue")
    class getRevenue {

        // ==========================================================
        // UTGR01 — Precondition: Server works stably
        // year=2025, month=11, day=25
        // Expected: Returns revenue successfully
        // ==========================================================
        @Test
        @DisplayName("UTGR01: year=2025, month=11, day=25 ⇒ success")
        void getRevenue_UTGR01_success() {
            // Given
            Integer year = 2025;
            Integer month = 11;
            Integer day = 25;
            Long expectedRevenue = 500L;

            // Mock behaviors
            when(orderDetailRepository.getRevenueByDay(year, month, day)).thenReturn(expectedRevenue);

            // When
            Long result = orderDetailService.getRevenue(year, month, day);

            // Then
            assertThat(result).isEqualTo(expectedRevenue);

            verify(orderDetailRepository).getRevenueByDay(year, month, day);
        }

        // ==========================================================
        // UTGR02 — year=1950, month=11, day=25
        // Expected: IllegalArgumentException("Invalid year")
        // ==========================================================
        @Test
        @DisplayName("UTGR02: year=1950 ⇒ IllegalArgumentException")
        void getRevenue_UTGR02_invalidYear() {
            assertThatThrownBy(() -> orderDetailService.getRevenue(1950, 11, 25))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid year");

            verifyNoInteractions(orderDetailRepository);
        }

        // ==========================================================
        // UTGR03 — year=null, month=11, day=25
        // Expected: Returns revenue for all time
        // ==========================================================
        @Test
        @DisplayName("UTGR03: year=null, month=11, day=25 ⇒ returns all time revenue")
        void getRevenue_UTGR03_nullYear() {
            // Given
            Integer year = null;
            Integer month = 11;
            Integer day = 25;
            Long expectedRevenue = 3000L;

            // Mock behaviors
            when(orderDetailRepository.getRevenueAllTime()).thenReturn(expectedRevenue);

            // When
            Long result = orderDetailService.getRevenue(year, month, day);

            // Then
            assertThat(result).isEqualTo(expectedRevenue);

            verify(orderDetailRepository).getRevenueAllTime();
        }

        // ==========================================================
        // UTGR04 — year=2025, month=0, day=25
        // Expected: IllegalArgumentException("Invalid month")
        // ==========================================================
        @Test
        @DisplayName("UTGR04: month=0 ⇒ IllegalArgumentException")
        void getRevenue_UTGR04_invalidMonth() {
            assertThatThrownBy(() -> orderDetailService.getRevenue(2025, 0, 25))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid month");

            verifyNoInteractions(orderDetailRepository);
        }

        // ==========================================================
        // UTGR05 — year=2025, month=11, day=32
        // Expected: IllegalArgumentException("Invalid day")
        // ==========================================================
        @Test
        @DisplayName("UTGR05: day=32 ⇒ IllegalArgumentException")
        void getRevenue_UTGR05_invalidDay() {
            assertThatThrownBy(() -> orderDetailService.getRevenue(2025, 11, 32))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid day");

            verifyNoInteractions(orderDetailRepository);
        }
    }

    // ==========================================================
    // TEST METHOD: updateOrderDetails
    // ==========================================================
    @Nested
    @DisplayName("updateOrderDetails")
    class updateOrderDetails {
        // Precondition: có 1 bản ghi có id = 4, quantity = 10, price = 10000, productId
        // = 2, orderId = 2

        // Test case 01: UTUOD01
        // Input: id=4, quantity=10, price=10000, productId=2, orderId=5
        // Expected: Update thành công
        // ==========================================================
        @Test
        @DisplayName("UTUOD01: id=4, quantity=10, price=10000, productId=2, orderId=5 ⇒ success")
        void updateOrderDetails_UTUOD01_success() {
            // Arrange
            Long id = 4L;
            UpdateOrderDetailDto updateDto = new UpdateOrderDetailDto(10, 10000, 2L, 5L);
            OrderDetails existingDetail = createOrderDetail(4L, 10, 10000, 2L, 2L);
            Orders newOrder = createOrder(5L, 1L, null);
            ProductResponse product = createProductResponse(2L, 100);

            when(orderDetailRepository.findOneById(id)).thenReturn(java.util.Optional.of(existingDetail));
            when(orderService.findOrderById(5L)).thenReturn(newOrder);
            when(productClient.getProductById(2L)).thenReturn(org.springframework.http.ResponseEntity.ok(product));
            when(orderDetailRepository.save(any(OrderDetails.class))).thenReturn(existingDetail);

            // Act
            OrderDetails result = orderDetailService.updateOrderDetails(id, updateDto);

            // Assert
            assertThat(result).isNotNull();
            verify(orderDetailRepository).findOneById(id);
            verify(orderService).findOrderById(5L);
            verify(productClient).getProductById(2L);
            verify(orderDetailRepository).save(any(OrderDetails.class));
        }

        // Test case 02: UTUOD02
        // Input: id=4, quantity=null, price=null, productId=null, orderId=null
        // Expected: Update thành công (không thay đổi gì)
        // ==========================================================
        @Test
        @DisplayName("UTUOD02: id=4, all fields null ⇒ success")
        void updateOrderDetails_UTUOD02_allFieldsNull() {
            // Arrange
            Long id = 4L;
            UpdateOrderDetailDto updateDto = new UpdateOrderDetailDto(null, null, null, null);
            OrderDetails existingDetail = createOrderDetail(4L, 10, 10000, 2L, 2L);

            when(orderDetailRepository.findOneById(id)).thenReturn(java.util.Optional.of(existingDetail));
            when(orderDetailRepository.save(any(OrderDetails.class))).thenReturn(existingDetail);

            // Act
            OrderDetails result = orderDetailService.updateOrderDetails(id, updateDto);

            // Assert
            assertThat(result).isNotNull();
            verify(orderDetailRepository).findOneById(id);
            verify(orderDetailRepository).save(any(OrderDetails.class));
            verifyNoInteractions(orderService);
            verifyNoInteractions(productClient);
        }

        // Test case 03: UTUOD03
        // Input: id=4, quantity=10, price=10000, productId=2, orderId=2
        // Expected: Update thành công
        // ==========================================================
        @Test
        @DisplayName("UTUOD03: id=4, quantity=10, price=10000, productId=2, orderId=2 ⇒ success")
        void updateOrderDetails_UTUOD03_updateSameValues() {
            // Arrange
            Long id = 4L;
            UpdateOrderDetailDto updateDto = new UpdateOrderDetailDto(10, 10000, 2L, 2L);
            OrderDetails existingDetail = createOrderDetail(4L, 10, 10000, 2L, 2L);
            Orders order = createOrder(2L, 1L, null);
            ProductResponse product = createProductResponse(2L, 100);

            when(orderDetailRepository.findOneById(id)).thenReturn(java.util.Optional.of(existingDetail));
            when(orderService.findOrderById(2L)).thenReturn(order);
            when(productClient.getProductById(2L)).thenReturn(org.springframework.http.ResponseEntity.ok(product));
            when(orderDetailRepository.save(any(OrderDetails.class))).thenReturn(existingDetail);

            // Act
            OrderDetails result = orderDetailService.updateOrderDetails(id, updateDto);

            // Assert
            assertThat(result).isNotNull();
            verify(orderDetailRepository).findOneById(id);
            verify(orderService).findOrderById(2L);
            verify(productClient).getProductById(2L);
            verify(orderDetailRepository).save(any(OrderDetails.class));
        }

        // Test case 04: UTUOD04
        // Input: id=4, quantity=10, price=10000, productId=5, orderId=2
        // Expected: Update thành công
        // ==========================================================
        @Test
        @DisplayName("UTUOD04: id=4, productId=5 (change productId) ⇒ success")
        void updateOrderDetails_UTUOD04_changeProductId() {
            // Arrange
            Long id = 4L;
            UpdateOrderDetailDto updateDto = new UpdateOrderDetailDto(10, 10000, 5L, 2L);
            OrderDetails existingDetail = createOrderDetail(4L, 10, 10000, 2L, 2L);
            Orders order = createOrder(2L, 1L, null);
            ProductResponse product = createProductResponse(5L, 100);

            when(orderDetailRepository.findOneById(id)).thenReturn(java.util.Optional.of(existingDetail));
            when(orderService.findOrderById(2L)).thenReturn(order);
            when(productClient.getProductById(5L)).thenReturn(org.springframework.http.ResponseEntity.ok(product));
            when(orderDetailRepository.save(any(OrderDetails.class))).thenReturn(existingDetail);

            // Act
            OrderDetails result = orderDetailService.updateOrderDetails(id, updateDto);

            // Assert
            assertThat(result).isNotNull();
            verify(orderDetailRepository).findOneById(id);
            verify(orderService).findOrderById(2L);
            verify(productClient).getProductById(5L);
            verify(orderDetailRepository).save(any(OrderDetails.class));
        }

        // Test case 05: UTUOD05
        // Input: id=4, quantity=10, price=null, productId=2, orderId=5
        // Expected: NotFoundException("Product not found")
        // ==========================================================
        @Test
        @DisplayName("UTUOD05: id=4, productId not exists ⇒ NotFoundException")
        void updateOrderDetails_UTUOD05_productNotFound() {
            // Arrange
            Long id = 4L;
            UpdateOrderDetailDto updateDto = new UpdateOrderDetailDto(10, null, 2L, 5L);
            OrderDetails existingDetail = createOrderDetail(4L, 10, 10000, 2L, 2L);
            Orders order = createOrder(5L, 1L, null);

            when(orderDetailRepository.findOneById(id)).thenReturn(java.util.Optional.of(existingDetail));
            when(orderService.findOrderById(5L)).thenReturn(order);
            when(productClient.getProductById(2L)).thenThrow(feign.FeignException.FeignClientException.class);

            // Act & Assert
            assertThatThrownBy(() -> orderDetailService.updateOrderDetails(id, updateDto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Product not found with id: 2");

            verify(orderDetailRepository).findOneById(id);
            verify(orderService).findOrderById(5L);
            verify(productClient).getProductById(2L);
            verify(orderDetailRepository, never()).save(any(OrderDetails.class));
        }
    }
}
