package com.example.orderservice.modules.Reviews.controller;

import com.example.orderservice.models.Reviews;
import com.example.orderservice.modules.Reviews.dto.CreateReviewDto;
import com.example.orderservice.modules.Reviews.dto.output.ProductRatingDto;
import com.example.orderservice.modules.Reviews.service.ReviewService;
import com.example.orderservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders/reviews")
@AllArgsConstructor
public class ReviewController {
    @Autowired
    private final ReviewService reviewService;

    @PostMapping("/new")
    public ResponseEntity<Reviews> createReview(@RequestBody CreateReviewDto createReviewDto) {
        return ResponseEntity.ok(reviewService.createReview(createReviewDto));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Reviews> getReviewById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reviewService.findReviewById(id));
    }

    @GetMapping("/orderId/{orderId}")
    public ResponseEntity<PagedResponse<Reviews>> getReviewByOrderId(
            @PathVariable("orderId") Long orderId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(reviewService.findReviewsByOrderId(orderId, page, limit));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PagedResponse<Reviews>> getReviewByUserId(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(reviewService.findReviewByUserId(userId, page, limit));
    }

    @GetMapping("all")
    public ResponseEntity<PagedResponse<Reviews>> getAllReviews(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(reviewService.getAllReviews(page, limit));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<PagedResponse<Reviews>> getReviewByProductId(
            @PathVariable("productId") Long productId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(reviewService.findReviewsByProductId(productId, page, limit));
    }

    @GetMapping("/product/rating/{rating}")
    public ResponseEntity<List<ProductRatingDto>> getReviewByProductIdAndRating(
            @PathVariable(value = "rating") Integer rating
    ) {
        return ResponseEntity.ok(reviewService.findProductsByRatingThreshold(rating));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Reviews> deleteReview(@PathVariable("id") Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(null);
    }

    @PutMapping("{id}")
    public ResponseEntity<Reviews> updateReview(@PathVariable("id") Long id, @RequestBody CreateReviewDto createReviewDto) {
        return ResponseEntity.ok(reviewService.updateReview(id, createReviewDto));
    }

}
