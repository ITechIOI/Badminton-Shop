package com.example.orderservice.modules.Reviews.service;

import com.example.orderservice.models.Discounts;
import com.example.orderservice.models.Orders;
import com.example.orderservice.models.Reviews;
import com.example.orderservice.modules.Orders.service.OrderService;
import com.example.orderservice.modules.Reviews.dto.CreateReviewDto;
import com.example.orderservice.modules.Reviews.dto.UpdateReviewDto;
import com.example.orderservice.modules.Reviews.dto.output.ProductRatingDto;
import com.example.orderservice.modules.Reviews.dto.output.ProductRatingRecord;
import com.example.orderservice.modules.Reviews.repository.ReviewRepository;
import com.example.orderservice.modules.feign.ProductFeign.ProductClient;
import com.example.orderservice.modules.feign.ProductFeign.ProductResponse;
import com.example.orderservice.modules.feign.UserFeign.UserClient;
import com.example.orderservice.modules.feign.UserFeign.UserResponse;
import com.example.orderservice.utils.NotFoundException;
import com.example.orderservice.utils.PagedResponse;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserClient userClient;
    private final OrderService orderService;
    private final ProductClient productClient;

    public Reviews createReview(CreateReviewDto createReviewDto) {
        if (createReviewDto.getRating() < 1 || createReviewDto.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        Reviews review = new Reviews();
        UserResponse user;
        try {
            user = userClient.getUserById(createReviewDto.getUserId()).getBody();
        } catch (FeignException.NotFound ex) {
            throw new NotFoundException("User not found!");
        }

        try {
            ProductResponse product = productClient.getProductById(createReviewDto.getProductId()).getBody();
        } catch (FeignException.NotFound ex) {
            throw new NotFoundException("Product not found!");
        }

        Orders order = orderService.findOrderById(createReviewDto.getOrderId());

        if (order == null) {
            throw new NotFoundException("Order not found!");
        }

        review.setContent(createReviewDto.getContent());
        review.setRating(createReviewDto.getRating());
        review.setUserId(createReviewDto.getUserId());
        review.setProductId(createReviewDto.getProductId());
        review.setOrders(order);

        return reviewRepository.save(review);
    }

    public Reviews findReviewById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }
        return reviewRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Review not found"));
    }

    public PagedResponse<Reviews> findReviewByUserId(Long userId, int page, int limit) {
        UserResponse user = userClient.getUserById(userId).getBody();
        Pageable pageable = PageRequest.of(page, limit);
        Page<Reviews> reviewPage = reviewRepository.findReviewByUserId(userId, pageable);
        if (reviewPage.isEmpty()) {
            throw new NotFoundException("Review not found");
        }
        return new PagedResponse<>(reviewPage.getContent(), reviewPage.getTotalPages(), reviewPage.getTotalElements());
    }

    public PagedResponse<Reviews> getAllReviews(int page, int limit) {
        if (page < 0 || limit <= 0) {
            throw new IllegalArgumentException("Invalid page or limit");
        }
        Pageable pageable = PageRequest.of(page, limit);
        Page<Reviews> reviewPage = reviewRepository.findAll(pageable);
        if (reviewPage.isEmpty()) {
            throw new NotFoundException("Review not found");
        }
        return new PagedResponse<>(reviewPage.getContent(), reviewPage.getTotalPages(), reviewPage.getTotalElements());
    }

    public PagedResponse<Reviews> findReviewsByOrderId(Long orderId, int page, int limit) {
        Orders order = orderService.findOrderById(orderId);
        Pageable pageable = PageRequest.of(page, limit);
        Page<Reviews> reviewPage = reviewRepository.findReviewByOrderId(orderId, pageable);

        if (reviewPage.isEmpty()) {
            throw new NotFoundException("Review not found");
        }

        return new PagedResponse<>(
                reviewPage.getContent(),
                reviewPage.getTotalPages(),
                reviewPage.getTotalElements()
        );
    }

    // Tìm kiếm đánh giá theo productId
    public PagedResponse<Reviews> findReviewsByProductId(Long productId, int page, int limit) {
        if (page < 0 || limit <= 0) {
            throw new IllegalArgumentException("Invalid page or limit");
        }
        try {
            ProductResponse product = productClient.getProductById(productId).getBody();
        } catch (FeignException.NotFound ex) {
            throw new NotFoundException("Product not found!");
        }
        Pageable pageable = PageRequest.of(page, limit);
        Page<Reviews> reviewPage = reviewRepository.findReviewByProductId(productId, pageable);

        if (reviewPage.isEmpty()) {
            throw new NotFoundException("Review not found");
        }

        return new PagedResponse<>(
                reviewPage.getContent(),
                reviewPage.getTotalPages(),
                reviewPage.getTotalElements()
        );
    }

    // Tìm kiếm sản phẩm dựa trên rating đánh giá
    public List<ProductRatingRecord> findProductsByRatingThreshold(Integer rating) {
        if ( rating == null || rating < 1 || rating > 5 ) {
            throw new NotFoundException("Rating must be between 1 and 5");
        }
        List<ProductRatingRecord> reviewPage = reviewRepository.findProductByRating(rating);
        System.out.println("Product rating: " + reviewPage);

        if (reviewPage.isEmpty()) {
            throw new NotFoundException("Review not found");
        }

        return reviewPage;
    }

    public Reviews updateReview(Long id, UpdateReviewDto createReviewDto) {
        if (createReviewDto.getRating() < 1 || createReviewDto.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }
        Reviews review = reviewRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Review not found"));
        if (createReviewDto.getContent() != null) {
            review.setContent(createReviewDto.getContent());
        }
        if (createReviewDto.getRating() != null) {
            review.setRating(createReviewDto.getRating());
        }
        return reviewRepository.save(review);
    }

    public void deleteReview(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }


        Reviews review = findReviewById(id);
        reviewRepository.softDeleteById(id);
    }

}
