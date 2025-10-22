package com.example.orderservice.modules.Reviews.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReviewDto {
    private String content;
    private Integer rating;

    public String toString() {
        return "CreateReviewDto(content=" + this.getContent() + ", rating=" + this.getRating();
    }
}
