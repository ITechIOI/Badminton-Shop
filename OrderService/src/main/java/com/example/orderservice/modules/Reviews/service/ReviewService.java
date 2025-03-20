package com.example.orderservice.modules.Reviews.service;

import com.example.orderservice.models.Discounts;
import com.example.orderservice.models.Orders;
import com.example.orderservice.models.Reviews;
import com.example.orderservice.modules.Orders.service.OrderService;
import com.example.orderservice.modules.Reviews.dto.CreateReviewDto;
import com.example.orderservice.modules.Reviews.repository.ReviewRepository;
import com.example.orderservice.modules.feign.UserFeign.UserClient;
import com.example.orderservice.modules.feign.UserFeign.UserResponse;
import com.example.orderservice.utils.NotFoundException;
import com.example.orderservice.utils.PagedResponse;
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

    public Reviews createReview(CreateReviewDto createReviewDto) {
        Reviews review = new Reviews();

        UserResponse user = userClient.getUserById(createReviewDto.getUserId()).getBody();
        if (user == null) {
            throw new NotFoundException("User not found!");
        }
        Orders order = orderService.findOrderById(createReviewDto.getOrderId());

        review.setContent(createReviewDto.getContent());
        review.setRating(createReviewDto.getRating());
        review.setUserId(createReviewDto.getUserId());
        review.setOrders(order);

        return reviewRepository.save(review);
    }

    public Reviews findReviewById(Long id) {
        return reviewRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Review not found"));
    }

    public PagedResponse<Reviews> findReviewByUserId(Long userId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Reviews> reviewPage = reviewRepository.findReviewByUserId(userId, pageable);
        if (reviewPage.isEmpty()) {
            throw new NotFoundException("Review not found");
        }
        return new PagedResponse<>(reviewPage.getContent(), reviewPage.getTotalPages(), reviewPage.getTotalElements());
    }

    public PagedResponse<Reviews> getAllReviews(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Reviews> reviewPage = reviewRepository.findAll(pageable);
        if (reviewPage.isEmpty()) {
            throw new NotFoundException("Review not found");
        }
        return new PagedResponse<>(reviewPage.getContent(), reviewPage.getTotalPages(), reviewPage.getTotalElements());
    }

    public PagedResponse<Reviews> findReviewsByOrderId(Long orderId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Reviews> reviewPage = reviewRepository.findReviewByOrderId(orderId, pageable);

        return new PagedResponse<>(
                reviewPage.getContent(),
                reviewPage.getTotalPages(),
                reviewPage.getTotalElements()
        );
    }

    public Reviews updateReview(Long id, CreateReviewDto createReviewDto) {
        Reviews review = reviewRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Review not found"));
        if (createReviewDto.getUserId() != null) {
            UserResponse user = userClient.getUserById(createReviewDto.getUserId()).getBody();
            if (user == null) {
                throw new NotFoundException("User not found!");
            }
            review.setUserId(createReviewDto.getUserId());
        }
        if (createReviewDto.getOrderId() != null) {
            Orders order = orderService.findOrderById(createReviewDto.getOrderId());
            review.setOrders(order);
        }
        review.setContent(createReviewDto.getContent());
        review.setRating(createReviewDto.getRating());
        return reviewRepository.save(review);
    }

    public void deleteReview(Long id) {
        Reviews review = findReviewById(id);
        reviewRepository.softDeleteById(id);
    }
}
