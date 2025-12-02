package com.example.orderservice;

import com.example.orderservice.kafka.OrderProducer;
import com.example.orderservice.models.Discounts;
import com.example.orderservice.models.OrderDetails;
import com.example.orderservice.models.Orders;
import com.example.orderservice.modules.Discounts.dto.UpdateDiscountDto;
import com.example.orderservice.modules.Discounts.service.DiscountService;
import com.example.orderservice.modules.OrderDetails.dto.CreateOrderDetailDto;
import com.example.orderservice.modules.OrderDetails.repository.OrderDetailRepository;
import com.example.orderservice.modules.Orders.dto.CreateOrderDto;
import com.example.orderservice.modules.Orders.dto.OrderResponse;
import com.example.orderservice.modules.Orders.repository.OrderRepository;
import com.example.orderservice.modules.Orders.service.OrderService;
import com.example.orderservice.modules.Orders.dto.output.OrderByDateDto;
import com.example.orderservice.modules.feign.Payments.PaymentClient;
import com.example.orderservice.modules.feign.Payments.PaymentResponse;
import com.example.orderservice.modules.feign.ProductFeign.ProductClient;
import com.example.orderservice.modules.feign.ProductFeign.ProductResponse;
import com.example.orderservice.modules.feign.UserFeign.UserClient;
import com.example.orderservice.modules.feign.UserFeign.UserResponse;
import com.example.orderservice.utils.NotFoundException;
import com.example.orderservice.utils.PagedResponse;
import feign.FeignException;
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

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private DiscountService discountService;
    @Mock
    private UserClient userClient;
    @Mock
    private OrderProducer orderProducer;
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private ProductClient productClient;
    @Mock
    private PaymentClient paymentClient;
    @InjectMocks
    private OrderService orderService;

    // Helper method tạo discount mẫu
    private static Discounts createDiscount(Long id, Integer count) {
        Discounts discount = new Discounts();
        discount.setId(id);
        discount.setCode("DISCOUNT10");
        discount.setPercent(10);
        discount.setCount(count);
        discount.setMinOrderValue(0);
        discount.setStatus("available");
        discount.setStartTime(new Date());
        discount.setEndTime(new Date(System.currentTimeMillis() + 86400000));
        return discount;
    }

    // Helper method tạo UserResponse mẫu
    private static UserResponse createUserResponse(Long userId) {
        return new UserResponse(userId, "John Doe", "male", "avatar.png", "0123456789", "john@example.com", "john_doe",
                "USER");
    }

    // Helper method tạo ProductResponse mẫu
    private static ProductResponse createProductResponse(Long productId, Integer price) {
        return new ProductResponse("Product " + productId, "Brand", "Description", price, "img.png", "video.mp4",
                "available", 10, 1L);
    }

    @Nested
    @DisplayName("createOrder")
    class createOrder {

        // ==========================================================
        // UTCO01 — Precondition: Server works stably, product id=1 exists, user id=2
        // exists, discount id=2 exists
        // totalPrice=100, status="pending", address="Binh Dinh", phone="034888877",
        // userId=2, discountId=2, details=[{quantity:1, price:20, productId:1}]
        // Expected: Order created successfully
        // ==========================================================
        @Test
        @DisplayName("UTCO01: Valid order (totalPrice=100, userId=2, discountId=2, 1 detail with productId=1) ⇒ success")
        void createOrder_UTCO01_success() {
            // Given
            Long userId = 2L;
            Long discountId = 2L;
            Long productId = 1L;

            Discounts discount = createDiscount(discountId, 5);
            UserResponse userResponse = createUserResponse(userId);
            ProductResponse productResponse = createProductResponse(productId, 20);

            CreateOrderDetailDto detailDto = new CreateOrderDetailDto(1, 20, productId, null);
            CreateOrderDto input = new CreateOrderDto();
            input.setTotalPrice(100);
            input.setStatus("pending");
            input.setAddress("Binh Dinh");
            input.setPhone("034888877");
            input.setUserId(userId);
            input.setDiscountId(discountId);
            input.setDetails(List.of(detailDto));

            // Mock behaviors
            when(discountService.findDiscountById(discountId)).thenReturn(discount);
            when(discountService.updateDiscount(eq(discountId), any(UpdateDiscountDto.class))).thenReturn(discount);
            when(userClient.getUserById(userId)).thenReturn(ResponseEntity.ok(userResponse));
            when(orderRepository.save(any(Orders.class))).thenAnswer(inv -> {
                Orders order = inv.getArgument(0);
                order.setId(999L);
                return order;
            });
            when(productClient.getProductById(productId)).thenReturn(ResponseEntity.ok(productResponse));
            when(orderDetailRepository.save(any(OrderDetails.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Orders savedOrder = orderService.createOrder(input);

            // Then
            assertThat(savedOrder.getId()).isEqualTo(999L);
            assertThat(savedOrder.getTotalPrice()).isEqualTo(100);
            assertThat(savedOrder.getStatus()).isEqualTo("pending");
            assertThat(savedOrder.getAddress()).isEqualTo("Binh Dinh");
            assertThat(savedOrder.getPhone()).isEqualTo("034888877");
            assertThat(savedOrder.getUserId()).isEqualTo(userId);
            assertThat(savedOrder.getDiscount()).isNotNull();
            assertThat(savedOrder.getDiscount().getId()).isEqualTo(discountId);

            verify(discountService).findDiscountById(discountId);
            verify(discountService).updateDiscount(eq(discountId), any(UpdateDiscountDto.class));
            verify(userClient).getUserById(userId);
            verify(orderRepository).save(any(Orders.class));
            verify(productClient).getProductById(productId);
            verify(orderDetailRepository).save(any(OrderDetails.class));
            verify(orderProducer).sendOrderConfirmation(any(OrderResponse.class));
        }

        // ==========================================================
        // UTCO02 — totalPrice=-100 (negative)
        // details=[{quantity:1, price:20, productId:1}]
        // Expected: IllegalArgumentException("Total price must be non-negative")
        // ==========================================================
        @Test
        @DisplayName("UTCO02: totalPrice=-100 ⇒ IllegalArgumentException")
        void createOrder_UTCO02_negativeTotalPrice() {
            CreateOrderDetailDto detailDto = new CreateOrderDetailDto(1, 20, 1L, null);
            CreateOrderDto input = new CreateOrderDto();
            input.setTotalPrice(-100);
            input.setStatus("pending");
            input.setAddress("Binh Dinh");
            input.setPhone("034888877");
            input.setUserId(2L);
            input.setDiscountId(2L);
            input.setDetails(List.of(detailDto));

            assertThatThrownBy(() -> orderService.createOrder(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Total price must be non-negative");

            verifyNoInteractions(discountService, userClient, orderRepository, productClient, orderDetailRepository,
                    orderProducer);
        }

        // ==========================================================
        // UTCO05 — userId=5 (not exists)
        // details=[{quantity:1, price:20, productId:1}]
        // Expected: NotFoundException("User not found with ID: 5")
        // ==========================================================
        @Test
        @DisplayName("UTCO05: userId=5 (not exists) ⇒ NotFoundException")
        void createOrder_UTCO05_userNotFound() {
            Long userId = 5L;
            Long discountId = 2L;

            Discounts discount = createDiscount(discountId, 5);
            CreateOrderDetailDto detailDto = new CreateOrderDetailDto(1, 20, 1L, null);

            CreateOrderDto input = new CreateOrderDto();
            input.setTotalPrice(100);
            input.setStatus("pending");
            input.setAddress("Binh Dinh");
            input.setPhone("034888877");
            input.setUserId(userId);
            input.setDiscountId(discountId);
            input.setDetails(List.of(detailDto));

            when(discountService.findDiscountById(discountId)).thenReturn(discount);
            when(discountService.updateDiscount(eq(discountId), any(UpdateDiscountDto.class))).thenReturn(discount);
            when(userClient.getUserById(userId))
                    .thenThrow(mock(FeignException.FeignClientException.class));

            assertThatThrownBy(() -> orderService.createOrder(input))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found with ID: 5");

            verify(userClient).getUserById(userId);
            verify(orderRepository, never()).save(any());
        }

        // ==========================================================
        // UTCO03 — discountId=null
        // details=[{quantity:1, price:20, productId:1}]
        // Expected: Order created successfully without discount
        // ==========================================================
        @Test
        @DisplayName("UTCO03: discountId=null ⇒ success without discount")
        void createOrder_UTCO03_noDiscount() {
            Long userId = 2L;
            Long productId = 1L;

            UserResponse userResponse = createUserResponse(userId);
            ProductResponse productResponse = createProductResponse(productId, 20);
            CreateOrderDetailDto detailDto = new CreateOrderDetailDto(1, 20, productId, null);

            CreateOrderDto input = new CreateOrderDto();
            input.setTotalPrice(100);
            input.setStatus("pending");
            input.setAddress("Binh Dinh");
            input.setPhone("034888877");
            input.setUserId(userId);
            input.setDiscountId(null);
            input.setDetails(List.of(detailDto));

            when(userClient.getUserById(userId)).thenReturn(ResponseEntity.ok(userResponse));
            when(orderRepository.save(any(Orders.class))).thenAnswer(inv -> {
                Orders order = inv.getArgument(0);
                order.setId(999L);
                return order;
            });
            when(productClient.getProductById(productId)).thenReturn(ResponseEntity.ok(productResponse));
            when(orderDetailRepository.save(any(OrderDetails.class))).thenAnswer(inv -> inv.getArgument(0));

            Orders savedOrder = orderService.createOrder(input);

            assertThat(savedOrder.getId()).isEqualTo(999L);
            assertThat(savedOrder.getDiscount()).isNull();

            verify(discountService, never()).findDiscountById(any());
            verify(userClient).getUserById(userId);
            verify(orderRepository).save(any(Orders.class));
            verify(productClient).getProductById(productId);
            verify(orderDetailRepository).save(any(OrderDetails.class));
            verify(orderProducer).sendOrderConfirmation(any(OrderResponse.class));
        }

        // ==========================================================
        // UTCO04 — discountId=3 (not exists)
        // details=[{quantity:1, price:20, productId:1}]
        // Expected: NullPointerException (service doesn't handle null discount)
        // ==========================================================
        @Test
        @DisplayName("UTCO04: discountId=3 (not exists) ⇒ NullPointerException")
        void createOrder_UTCO04_discountNotFound() {
            Long userId = 2L;
            Long discountId = 3L;

            CreateOrderDetailDto detailDto = new CreateOrderDetailDto(1, 20, 1L, null);
            CreateOrderDto input = new CreateOrderDto();
            input.setTotalPrice(100);
            input.setStatus("pending");
            input.setAddress("Binh Dinh");
            input.setPhone("034888877");
            input.setUserId(userId);
            input.setDiscountId(discountId);
            input.setDetails(List.of(detailDto));

            when(discountService.findDiscountById(discountId)).thenReturn(null);

            assertThatThrownBy(() -> orderService.createOrder(input))
                    .isInstanceOf(NullPointerException.class);
        }

        // ==========================================================
        // UTCO07 — discountId=-1 (invalid)
        // details=[{quantity:1, price:20, productId:1}]
        // Expected: NullPointerException (invalid discount ID)
        // ==========================================================
        @Test
        @DisplayName("UTCO07: discountId=-1 (invalid) ⇒ NullPointerException")
        void createOrder_UTCO07_invalidDiscountId() {
            Long userId = 2L;
            Long discountId = -1L;

            CreateOrderDetailDto detailDto = new CreateOrderDetailDto(1, 20, 1L, null);
            CreateOrderDto input = new CreateOrderDto();
            input.setTotalPrice(100);
            input.setStatus("pending");
            input.setAddress("Binh Dinh");
            input.setPhone("034888877");
            input.setUserId(userId);
            input.setDiscountId(discountId);
            input.setDetails(List.of(detailDto));

            when(discountService.findDiscountById(discountId)).thenReturn(null);

            assertThatThrownBy(() -> orderService.createOrder(input))
                    .isInstanceOf(NullPointerException.class);
        }

        // ==========================================================
        // UTCO06 — productId=5 (not exists)
        // details=[{quantity:1, price:20, productId:5}]
        // Expected: NotFoundException("Product not found with ID: 5")
        // ==========================================================
        @Test
        @DisplayName("UTCO06: productId=5 (not exists) ⇒ NotFoundException")
        void createOrder_UTCO06_productNotFound() {
            Long userId = 2L;
            Long discountId = 2L;
            Long productId = 5L;

            Discounts discount = createDiscount(discountId, 5);
            UserResponse userResponse = createUserResponse(userId);
            CreateOrderDetailDto detailDto = new CreateOrderDetailDto(1, 20, productId, null);

            CreateOrderDto input = new CreateOrderDto();
            input.setTotalPrice(100);
            input.setStatus("pending");
            input.setAddress("Binh Dinh");
            input.setPhone("034888877");
            input.setUserId(userId);
            input.setDiscountId(discountId);
            input.setDetails(List.of(detailDto));

            when(discountService.findDiscountById(discountId)).thenReturn(discount);
            when(discountService.updateDiscount(eq(discountId), any(UpdateDiscountDto.class))).thenReturn(discount);
            when(userClient.getUserById(userId)).thenReturn(ResponseEntity.ok(userResponse));
            when(orderRepository.save(any(Orders.class))).thenAnswer(inv -> {
                Orders order = inv.getArgument(0);
                order.setId(999L);
                return order;
            });
            when(productClient.getProductById(productId))
                    .thenThrow(mock(FeignException.FeignClientException.class));

            assertThatThrownBy(() -> orderService.createOrder(input))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Product not found with ID: 5");

            verify(productClient).getProductById(productId);
            verify(orderDetailRepository, never()).save(any(OrderDetails.class));
        }
    }

    @Nested
    @DisplayName("findOrderByUserId")
    class findOrderByUserId {

        // Helper method tạo Order mẫu
        private static Orders createOrder(Long id, Long userId, Integer totalPrice) {
            Orders order = new Orders();
            order.setId(id);
            order.setUserId(userId);
            order.setTotalPrice(totalPrice);
            order.setStatus("pending");
            order.setAddress("Test Address");
            order.setPhone("0123456789");
            return order;
        }

        // ==========================================================
        // UTFOBU01 — Precondition: 3 orders in DB, 2 owned by userId=2, 1 owned by
        // userId=3
        // userId=2, page=0, limit=2
        // Expected: Returns 2 orders, totalPages=1, totalElements=2
        // ==========================================================
        @Test
        @DisplayName("UTFOBU01: userId=2, page=0, limit=2 ⇒ returns 2 orders")
        void findOrderByUserId_UTFOBU01_success() {
            Long userId = 2L;
            int page = 0;
            int limit = 2;

            List<Orders> ordersList = List.of(
                    createOrder(1L, userId, 100),
                    createOrder(2L, userId, 200));
            Pageable pageable = PageRequest.of(page, limit);
            Page<Orders> ordersPage = new PageImpl<>(ordersList, pageable, 2);

            when(orderRepository.findOrderByUserId(userId, pageable)).thenReturn(ordersPage);

            PagedResponse<Orders> result = orderService.findOrderByUserId(userId, page, limit);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).extracting(Orders::getUserId)
                    .containsOnly(userId);

            verify(orderRepository).findOrderByUserId(userId, pageable);
        }

        // ==========================================================
        // UTFOBU02 — userId=2, page=1, limit=2
        // Expected: NotFoundException("Order not found")
        // ==========================================================
        @Test
        @DisplayName("UTFOBU02: userId=2, page=1, limit=3 ⇒ NotFoundException")
        void findOrderByUserId_UTFOBU02_pageOutOfRange() {
            Long userId = 2L;
            int page = 1;
            int limit = 3;
            Pageable pageable = PageRequest.of(page, limit);
            Page<Orders> ordersPage = new PageImpl<>(List.of(), pageable, 2);

            when(orderRepository.findOrderByUserId(userId, pageable)).thenReturn(ordersPage);

            assertThatThrownBy(() -> orderService.findOrderByUserId(userId, page, limit))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Order not found");

            verify(orderRepository).findOrderByUserId(userId, pageable);
        }

        // ==========================================================
        // UTFOBU04 — userId=null
        // Expected: IllegalArgumentException("Invalid user ID")
        // ==========================================================
        @Test
        @DisplayName("UTFOBU04: userId=null ⇒ IllegalArgumentException")
        void findOrderByUserId_UTFOBU04_nullUserId() {
            assertThatThrownBy(() -> orderService.findOrderByUserId(null, 0, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid user ID");

            verifyNoInteractions(orderRepository);
        }

        // ==========================================================
        // UTFOBU03 — page=-1 (invalid)
        // Expected: IllegalArgumentException("Invalid page or limit")
        // ==========================================================
        @Test
        @DisplayName("UTFOBU03: page=-1 ⇒ IllegalArgumentException")
        void findOrderByUserId_UTFOBU03_invalidPage() {
            assertThatThrownBy(() -> orderService.findOrderByUserId(2L, -1, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid page or limit");

            verifyNoInteractions(orderRepository);
        }
    }

    @Nested
    @DisplayName("orderStatisticsByTime")
    class orderStatisticsByTime {

        // Helper method tạo Order với createdAt mẫu
        private static Orders createOrderWithDate(Long id, Long userId, Integer totalPrice, LocalDateTime createdAt) {
            Orders order = new Orders();
            order.setId(id);
            order.setUserId(userId);
            order.setTotalPrice(totalPrice);
            order.setAddress("Test Address");
            order.setPhone("0123456789");
            order.setStatus("completed");
            order.setCreatedAt(createdAt);
            return order;
        }

        // Helper method tạo PaymentResponse mẫu
        private static PaymentResponse createPaymentResponse(String paymentMethod) {
            PaymentResponse payment = new PaymentResponse();
            payment.setPaymentMethod(paymentMethod);
            payment.setTransactionId("TXN123");
            payment.setOrderId(1L);
            payment.setStatus("completed");
            payment.setAmount(100.0);
            return payment;
        }

        // ==========================================================
        // UTOBT01 — Precondition: Server works stably
        // page=0, limit=2, year=0, month=10, day=25
        // Expected: Returns order statistics successfully
        // ==========================================================
        @Test
        @DisplayName("UTOBT01: page=0, limit=2, year=0, month=10, day=25 ⇒ success")
        void orderStatisticsByTime_UTOBT01_success() {
            // Given
            Integer year = 0;
            Integer month = 10;
            Integer day = 25;
            int page = 0;
            int limit = 2;

            java.time.LocalDateTime orderDate = java.time.LocalDateTime.of(year, month, day, 10, 0);
            Orders order1 = createOrderWithDate(1L, 2L, 100, orderDate);
            Orders order2 = createOrderWithDate(2L, 3L, 200, orderDate);

            UserResponse user1 = createUserResponse(2L);
            UserResponse user2 = createUserResponse(3L);
            PaymentResponse payment1 = createPaymentResponse("Credit Card");
            PaymentResponse payment2 = createPaymentResponse("Cash");

            Pageable pageable = PageRequest.of(page, limit);
            Page<Orders> ordersPage = new PageImpl<>(List.of(order1, order2), pageable, 2);

            // Mock behaviors
            when(orderRepository.findOrdersByYearAndMonthAndDay(year, month, day, pageable)).thenReturn(ordersPage);
            when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(user1));
            when(userClient.getUserById(3L)).thenReturn(ResponseEntity.ok(user2));
            when(paymentClient.getPaymentByIdForMicroservices(2L)).thenReturn(ResponseEntity.ok(payment1));
            when(paymentClient.getPaymentByIdForMicroservices(3L)).thenReturn(ResponseEntity.ok(payment2));

            // When
            PagedResponse<OrderByDateDto> result = orderService.orderStatisticsByTime(year, month, day, page, limit);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().get(0).id()).isEqualTo(1L);
            assertThat(result.getContent().get(0).paymentMethod()).isEqualTo("Credit Card");
            assertThat(result.getContent().get(1).id()).isEqualTo(2L);
            assertThat(result.getContent().get(1).paymentMethod()).isEqualTo("Cash");

            verify(orderRepository).findOrdersByYearAndMonthAndDay(year, month, day, pageable);
            verify(userClient).getUserById(2L);
            verify(userClient).getUserById(3L);
            verify(paymentClient).getPaymentByIdForMicroservices(2L);
            verify(paymentClient).getPaymentByIdForMicroservices(3L);
        }

        // ==========================================================
        // UTOBT02 — page=-1, limit=2, year=0, month=10, day=25
        // Expected: IllegalArgumentException("Invalid page or limit")
        // ==========================================================
        @Test
        @DisplayName("UTOBT02: page=-1 ⇒ IllegalArgumentException")
        void orderStatisticsByTime_UTOBT02_invalidPage() {
            assertThatThrownBy(() -> orderService.orderStatisticsByTime(0, 10, 25, -1, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid page or limit");

            verifyNoInteractions(orderRepository, userClient, paymentClient);
        }

        // ==========================================================
        // UTOBT03 — Precondition: Only 1 order in database
        // page=1, limit=3, year=2025, month=10, day=25
        // Expected: NotFoundException("No top selling products found")
        // ==========================================================
        @Test
        @DisplayName("UTOBT03: page=1, limit=3 with only 1 order ⇒ NotFoundException")
        void orderStatisticsByTime_UTOBT03_pageOutOfRange() {
            // Given
            Integer year = 2025;
            Integer month = 10;
            Integer day = 25;
            int page = 1;
            int limit = 3;

            Pageable pageable = PageRequest.of(page, limit);
            Page<Orders> ordersPage = new PageImpl<>(List.of(), pageable, 1);

            // Mock behaviors
            when(orderRepository.findOrdersByYearAndMonthAndDay(year, month, day, pageable)).thenReturn(ordersPage);

            // When & Then
            assertThatThrownBy(() -> orderService.orderStatisticsByTime(year, month, day, page, limit))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("No top selling products found");

            verify(orderRepository).findOrdersByYearAndMonthAndDay(year, month, day, pageable);
            verifyNoInteractions(userClient, paymentClient);
        }

        // ==========================================================
        // UTOBT04 — year=0 (invalid year)
        // Expected: IllegalArgumentException("Invalid date parameters")
        // ==========================================================
        @Test
        @DisplayName("UTOBT04: year=0 ⇒ IllegalArgumentException")
        void orderStatisticsByTime_UTOBT04_invalidYear() {
            assertThatThrownBy(() -> orderService.orderStatisticsByTime(0, 10, 25, 0, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid date parameters");

            verifyNoInteractions(orderRepository, userClient, paymentClient);
        }

        // ==========================================================
        // UTOBT05 — year=2026, no orders found
        // Expected: NotFoundException("No top selling products found")
        // ==========================================================
        @Test
        @DisplayName("UTOBT05: year=2026 with no orders ⇒ NotFoundException")
        void orderStatisticsByTime_UTOBT05_noOrdersFound() {
            // Given
            Integer year = 2026;
            Integer month = null;
            Integer day = null;
            int page = 0;
            int limit = 2;

            Pageable pageable = PageRequest.of(page, limit);
            Page<Orders> ordersPage = new PageImpl<>(List.of(), pageable, 0);

            // Mock behaviors
            when(orderRepository.findOrdersByYear(year, pageable)).thenReturn(ordersPage);

            // When & Then
            assertThatThrownBy(() -> orderService.orderStatisticsByTime(year, month, day, page, limit))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("No top selling products found");

            verify(orderRepository).findOrdersByYear(year, pageable);
            verifyNoInteractions(userClient, paymentClient);
        }

    }

    @Nested
    @DisplayName("deleteOrder")
    class deleteOrder {

        // ==========================================================
        // UTDO01 — Precondition: Record with id=2 exists, Server works stably
        // id=100 (not exists)
        // Expected: NotFoundException("Order not found")
        // ==========================================================
        @Test
        @DisplayName("UTDO01: id=100 (not exists) ⇒ NotFoundException")
        void deleteOrder_UTDO01_orderNotFound() {
            // Given
            Long orderId = 100L;

            // Mock behaviors
            when(orderRepository.findOneById(orderId)).thenReturn(java.util.Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderService.deleteOrder(orderId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Order not found");

            verify(orderRepository).findOneById(orderId);
            verify(orderRepository, never()).softDeleteById(any());
        }

        // ==========================================================
        // UTDO02 — id=null
        // Expected: IllegalArgumentException("Invalid order ID")
        // ==========================================================
        @Test
        @DisplayName("UTDO02: id=null ⇒ IllegalArgumentException")
        void deleteOrder_UTDO02_nullId() {
            assertThatThrownBy(() -> orderService.deleteOrder(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid order ID");

            verifyNoInteractions(orderRepository);
        }

        // ==========================================================
        // UTDO03 — id=2 (exists in database)
        // Expected: Order deleted successfully
        // ==========================================================
        @Test
        @DisplayName("UTDO03: id=2 ⇒ success")
        void deleteOrder_UTDO03_existingId() {
            // Given
            Long orderId = 2L;
            Orders order = new Orders();
            order.setId(orderId);
            order.setUserId(2L);
            order.setTotalPrice(150);
            order.setAddress("Address 2");
            order.setPhone("0987654321");
            order.setStatus("pending");

            // Mock behaviors
            when(orderRepository.findOneById(orderId)).thenReturn(java.util.Optional.of(order));
            doNothing().when(orderRepository).softDeleteById(orderId);

            // When
            orderService.deleteOrder(orderId);

            // Then
            verify(orderRepository).findOneById(orderId);
            verify(orderRepository).softDeleteById(orderId);
        }

    }
}
