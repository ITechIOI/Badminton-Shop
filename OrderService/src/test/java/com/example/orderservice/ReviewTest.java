package com.example.orderservice;

import com.example.orderservice.models.Orders;
import com.example.orderservice.models.Reviews;
import com.example.orderservice.modules.Reviews.dto.CreateReviewDto;
import com.example.orderservice.modules.Reviews.dto.UpdateReviewDto;
import com.example.orderservice.modules.Reviews.dto.output.ProductRatingRecord;
import com.example.orderservice.modules.Reviews.repository.ReviewRepository;
import com.example.orderservice.modules.Reviews.service.ReviewService;
import com.example.orderservice.modules.feign.ProductFeign.ProductClient;
import com.example.orderservice.modules.feign.ProductFeign.ProductResponse;
import com.example.orderservice.modules.feign.UserFeign.UserClient;
import com.example.orderservice.modules.feign.UserFeign.UserResponse;
import com.example.orderservice.modules.Orders.service.OrderService;
import com.example.orderservice.utils.NotFoundException;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.example.orderservice.utils.PagedResponse;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewTest {
    @Mock private ReviewRepository reviewRepository;
    @Mock private UserClient userClient;
    @Mock private OrderService orderService;
    @Mock private ProductClient productClient;
    @InjectMocks private ReviewService reviewService;

    private static CreateReviewDto dto(Integer rating, Long userId, Long orderId, Long productId, String content) {
        return new CreateReviewDto(content, rating, userId, orderId, productId);
    }

    private static UserResponse mockUser(Long id) {
        UserResponse u = new UserResponse(id, "User 1", "Female", "http://avatar.png", "1234567890", "user1@gmail.com", "user123", "admin");
        return u;
    }

    private static Orders mockOrder(Long id) {
        Orders o = new Orders();
        o.setId(id);
        return o;
    }

    private static ProductResponse mockProduct(Long id) {
        ProductResponse p = new ProductResponse("Product 1",
                "Nike", "Description 1", 100,
                "http://image.png", "http://video.png",
                "available", 10, 1L);
        return p;
    }

    private static Reviews mockReview(Long id) {
        Reviews r = new Reviews();
        r.setId(id);
        r.setRating(5);
        r.setContent("Good product");
        return r;
    }

    private static ProductRatingRecord mockProduct(Long id, Double avg) {
        return new ProductRatingRecord(id, avg);
    }

    @Nested
    @DisplayName("createReview")
    class CreateReview {

        // ===============================================================
        // Normal Case: rating=2, userId=2, orderId=2, productId=2
        // ===============================================================
        @Test
        @DisplayName("UTCR001: rating=2, userId=2, orderId=2, productId=2 ⇒ success")
        void success_validInputs() {
            when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(mockUser(2L)));
            when(orderService.findOrderById(2L)).thenReturn(mockOrder(2L));
            when(productClient.getProductById(2L)).thenReturn(ResponseEntity.ok(mockProduct(2L)));
            when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Reviews r = reviewService.createReview(dto(2, 2L, 2L, 2L, "Good product"));

            assertThat(r.getRating()).isEqualTo(2);
            assertThat(r.getUserId()).isEqualTo(2L);
            assertThat(r.getOrders().getId()).isEqualTo(2L);
            assertThat(r.getProductId()).isEqualTo(2L);
            assertThat(r.getContent()).isEqualTo("Good product");
            verify(reviewRepository).save(any());
        }

        // ===============================================================
        // Abnormal Case: rating=-2
        // ===============================================================
        @Test
        @DisplayName("UTCR002: rating=-2 ⇒ IllegalArgumentException('Rating must be between 1 and 5')")
        void fail_ratingBelow1() {
            CreateReviewDto input = dto(-2, 2L, 2L, 2L, "Good product");

            assertThatThrownBy(() -> reviewService.createReview(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Rating must be between 1 and 5");

            verifyNoInteractions(userClient, orderService, reviewRepository);
        }

        // ===============================================================
        // Abnormal Case: user not found
        // ===============================================================
        @Test
        @DisplayName("UTCR003: userId=100 ⇒ NotFoundException('User not found!')")
        void fail_userNotFound() {
            when(userClient.getUserById(100L)).thenThrow(FeignException.NotFound.class);

            CreateReviewDto input = dto(2, 100L, 2L, 2L, "Good product");

            assertThatThrownBy(() -> reviewService.createReview(input))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found!");
        }

        // ===============================================================
        // Abnormal Case: order not found
        // ===============================================================
        @Test
        @DisplayName("UTCR004: orderId=100 ⇒ NotFoundException('Order not found!')")
        void fail_orderNotFound() {
            when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(mockUser(2L)));
            when(productClient.getProductById(2L)).thenReturn(ResponseEntity.ok(mockProduct(2L)));
            when(orderService.findOrderById(100L)).thenReturn(null);

            CreateReviewDto input = dto(2, 2L, 100L, 2L, "Good product");

            assertThatThrownBy(() -> reviewService.createReview(input))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Order not found!");
        }

        // ===============================================================
        // Abnormal Case: product not found
        // ===============================================================
        @Test
        @DisplayName("UTCR005: productId=100 ⇒ NotFoundException('Product not found!')")
        void fail_productNotFound() {
            when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(mockUser(2L)));
            when(productClient.getProductById(100L)).thenThrow(FeignException.NotFound.class);

            // Giả định rằng nếu service có kiểm tra productClient trong tương lai, ta sẽ chặn tại đây
            CreateReviewDto input = dto(2, 2L, 2L, 100L, "Good product");

            assertThatThrownBy(() -> reviewService.createReview(input))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Product not found!");

        }
    }

    @Nested
    @DisplayName("findReviewById")
    class FindReviewById {
        @Test
        @DisplayName("UTFR001: id=1 ⇒ return review successfully")
        void success_validId() {
            when(reviewRepository.findOneById(1L)).thenReturn(Optional.of(mockReview(1L)));

            Reviews result = reviewService.findReviewById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getContent()).isEqualTo("Good product");
            verify(reviewRepository).findOneById(1L);
        }

        @Test
        @DisplayName("UTFR002: id=null ⇒ IllegalArgumentException('Invalid review ID')")
        void fail_idIsNull() {
            assertThatThrownBy(() -> reviewService.findReviewById(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid review ID");

            verifyNoInteractions(reviewRepository);
        }

        @Test
        @DisplayName("UTFR003: id=100 ⇒ NotFoundException('Review not found')")
        void fail_notFound() {
            when(reviewRepository.findOneById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.findReviewById(100L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Review not found");

            verify(reviewRepository).findOneById(100L);
        }
    }

    @Nested
    @DisplayName("getAllReviews()")
    class GetAllReviews {

        // -----------------------------------------------------------
        // Normal Case: page=0, limit=2 -> return 2 reviews
        // -----------------------------------------------------------
        @Test
        @DisplayName("UTGA001: page=0, limit=2 ⇒ returns 2 reviews successfully")
        void success_page0_limit2() {
            Pageable pageable = PageRequest.of(0, 2);
            List<Reviews> reviews = List.of(
                    mockReview(1L),
                    mockReview(2L)
            );
            Page<Reviews> reviewPage = new PageImpl<>(reviews, pageable, 2);

            when(reviewRepository.findAll(pageable)).thenReturn(reviewPage);

            PagedResponse<Reviews> result = reviewService.getAllReviews(0, 2);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getTotalElements()).isEqualTo(2);

            // Verify correctness of mapping
            assertThat(result.getContent().get(0).getContent()).isEqualTo("Good product");
            verify(reviewRepository).findAll(pageable);
        }

        @Test
        @DisplayName("UTGA002: page=1, limit=2 ⇒ NotFoundException('Review not found')")
        void fail_notFound() {
            Pageable pageable = PageRequest.of(1, 2);
            Page<Reviews> emptyPage = new PageImpl<>(List.of(), pageable, 2);
            when(reviewRepository.findAll(pageable)).thenReturn(emptyPage);

            assertThatThrownBy(() -> reviewService.getAllReviews(1, 2))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Review not found");

            verify(reviewRepository).findAll(pageable);
        }

        @Test
        @DisplayName("UTGA003: page=-1, limit=2 ⇒ IllegalArgumentException('Invalid page or limit')")
        void fail_invalidPage() {
            assertThatThrownBy(() -> reviewService.getAllReviews(-1, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid page or limit");

            verifyNoInteractions(reviewRepository);
        }
    }

    @Nested
    @DisplayName("findReviewsByProductId")
    class FindReviewsByProductId {

        @Test
        @DisplayName("UTRP001: page=0, limit=2, productId=2 ⇒ returns PagedResponse<Reviews>")
        void success_validInputs() {
            Pageable pageable = PageRequest.of(0, 2);
            List<Reviews> list = List.of(
                    mockReview(1L),
                    mockReview(2L)
            );
            Page<Reviews> reviewPage = new PageImpl<>(list, pageable, 2);

            when(productClient.getProductById(2L)).thenReturn(ResponseEntity.ok(mockProduct(2L)));
            when(reviewRepository.findReviewByProductId(2L, pageable)).thenReturn(reviewPage);

            PagedResponse<Reviews> result = reviewService.findReviewsByProductId(2L, 0, 2);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().get(0).getContent()).isEqualTo("Good product");

            verify(productClient).getProductById(2L);
            verify(reviewRepository).findReviewByProductId(2L, pageable);
        }

        @Test
        @DisplayName("UTRP002: page=1, limit=2, productId=2 ⇒ NotFoundException('Review not found')")
        void fail_noReviewFound() {
            Pageable pageable = PageRequest.of(1, 2);
            Page<Reviews> empty = new PageImpl<>(List.of(), pageable, 2);

            when(productClient.getProductById(2L)).thenReturn(ResponseEntity.ok(mockProduct(2L)));
            when(reviewRepository.findReviewByProductId(2L, pageable)).thenReturn(empty);

            assertThatThrownBy(() -> reviewService.findReviewsByProductId(2L, 1, 2))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Review not found");

            verify(productClient).getProductById(2L);
            verify(reviewRepository).findReviewByProductId(2L, pageable);
        }

        @Test
        @DisplayName("UTRP003: page=-1 ⇒ IllegalArgumentException('Invalid page or limit')")
        void fail_invalidPage() {
            assertThatThrownBy(() -> reviewService.findReviewsByProductId(2L, 0, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid page or limit");

            verifyNoInteractions(productClient, reviewRepository);
        }

        @Test
        @DisplayName("UTRP004: productId=-2 ⇒ NotFoundException('Product not found!')")
        void fail_productNotFound() {
            doThrow(FeignException.NotFound.class).when(productClient).getProductById(100L);

            assertThatThrownBy(() -> reviewService.findReviewsByProductId(100L, 0, 2))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Product not found!");

            verify(productClient).getProductById(100L);
            verifyNoInteractions(reviewRepository);
        }
    }

    @Nested
    @DisplayName("findProductsByRatingThreshold()")
    class FindProductsByRatingThreshold {

        @Test
        @DisplayName("UTFR001: rating=3 ⇒ return [Product(10, 3.5), Product(20, 4.2)]")
        void success_validRating() {
            List<ProductRatingRecord> mockList = List.of(
                    mockProduct(10L, 3.5),
                    mockProduct(20L, 4.2)
            );
            when(reviewRepository.findProductByRating(3)).thenReturn(mockList);

            List<ProductRatingRecord> result = reviewService.findProductsByRatingThreshold(3);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).productId()).isEqualTo(10L);
            assertThat(result.get(1).avgRating()).isEqualTo(4.2);
            verify(reviewRepository).findProductByRating(3);
        }

        @Test
        @DisplayName("UTFR002: rating=null ⇒ NotFoundException('Rating must be between 1 and 5')")
        void fail_ratingNull() {
            assertThatThrownBy(() -> reviewService.findProductsByRatingThreshold(null))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Rating must be between 1 and 5");

            verifyNoInteractions(reviewRepository);
        }

        @Test
        @DisplayName("UTFR003: rating=5 ⇒ NotFoundException('Review not found')")
        void fail_noProductsFound() {
            when(reviewRepository.findProductByRating(5)).thenReturn(List.of());

            assertThatThrownBy(() -> reviewService.findProductsByRatingThreshold(5))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Review not found");

            verify(reviewRepository).findProductByRating(5);
        }
    }

    @Nested
    @DisplayName("updateReview")
    class UpdateReview {

        // -----------------------------------------------------------
        // ✅ Normal Case: id=1, content="Bad product", rating=3
        // -----------------------------------------------------------
        @Test
        @DisplayName("UTUR001: id=1, content='Bad product', rating=3 ⇒ success")
        void success_validUpdate() {
            Reviews review = mockReview(1L);
            when(reviewRepository.findOneById(1L)).thenReturn(Optional.of(review));
            when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UpdateReviewDto dto = new UpdateReviewDto("Bad product", 3);
            Reviews updated = reviewService.updateReview(1L, dto);

            assertThat(updated.getContent()).isEqualTo("Bad product");
            assertThat(updated.getRating()).isEqualTo(3);
            verify(reviewRepository).findOneById(1L);
            verify(reviewRepository).save(any());
        }

        @Test
        @DisplayName("UTUR002: id=null ⇒ IllegalArgumentException('Invalid review ID')")
        void fail_invalidId() {
            UpdateReviewDto dto = new UpdateReviewDto("Bad product", 3);

            assertThatThrownBy(() -> reviewService.updateReview(null, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid review ID");

            verifyNoInteractions(reviewRepository);
        }

        @Test
        @DisplayName("UTUR003: id=100 ⇒ NotFoundException('Review not found')")
        void fail_reviewNotFound() {
            when(reviewRepository.findOneById(100L)).thenReturn(Optional.empty());
            UpdateReviewDto dto = new UpdateReviewDto("Bad product", 3);

            assertThatThrownBy(() -> reviewService.updateReview(100L, dto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Review not found");

            verify(reviewRepository).findOneById(100L);
        }

        @Test
        @DisplayName("UTUR004: rating=0 ⇒ IllegalArgumentException('Rating must be between 1 and 5')")
        void fail_ratingZero() {
            UpdateReviewDto dto = new UpdateReviewDto("Bad product", 0);

            assertThatThrownBy(() -> reviewService.updateReview(1L, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Rating must be between 1 and 5");

            verifyNoInteractions(reviewRepository);
        }
    }

    @Nested
    @DisplayName("deleteReview()")
    class DeleteReview {

        @Test
        @DisplayName("UTDR001: id=1 ⇒ delete successfully")
        void success_deleteReview() {
            // Mock behavior
            Reviews existing = mockReview(1L);
            when(reviewRepository.findOneById(1L)).thenReturn(Optional.of(existing));

            // Gọi hàm deleteReview()
            reviewService.deleteReview(1L);

            // Kiểm tra tương tác
            verify(reviewRepository).findOneById(1L);
            verify(reviewRepository).softDeleteById(1L);
        }

        @Test
        @DisplayName("UTDR002: id=null ⇒ IllegalArgumentException('Invalid review ID')")
        void fail_invalidId() {
            assertThatThrownBy(() -> reviewService.deleteReview(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid review ID");

            verifyNoInteractions(reviewRepository);
        }

        @Test
        @DisplayName("UTDR003: id=2 ⇒ NotFoundException('Review not found')")
        void fail_reviewNotFound() {
            when(reviewRepository.findOneById(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.deleteReview(2L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Review not found");

            verify(reviewRepository).findOneById(2L);
            verify(reviewRepository, never()).softDeleteById(anyLong());
        }
    }
}
