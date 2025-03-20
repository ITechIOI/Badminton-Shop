package com.example.orderservice.modules.Discounts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDiscountDto {
    private String code;
    private String description;
    private Integer percent;
    private Integer minOrderValue;
    private Integer count;
    private Date startTime;
    private Date endTime;
    private String status;

    public String toString() {
        return "UpdateDiscountDto(code=" + this.getCode() +
                ", description=" + this.getDescription() + ", percent=" + this.getPercent() +
                ", minOrderValue=" + this.getMinOrderValue() + ", count=" + this.getCount() +
                ", startTime=" + this.getStartTime() + ", endTime=" + this.getEndTime() +
                ", status=" + this.getStatus() + ")";
    }
}
