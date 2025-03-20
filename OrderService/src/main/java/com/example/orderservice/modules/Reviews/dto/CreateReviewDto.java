package com.example.orderservice.modules.Reviews.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewDto {
    private String content;
    private Integer rating;
    private Long userId;
    private Long orderId;

    public String toString() {
        return "CreateReviewDto(content=" + this.getContent() + ", rating=" + this.getRating() + ", userId=" + this.getUserId() + ", productId=" + this.getOrderId() + ")";
    }
}
