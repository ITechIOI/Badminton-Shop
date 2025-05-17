package com.example.orderservice.modules.OrderDetails.dto.output;

public record MonthlyRevenueDto(
        int month,
        Long revenue
) {
    public String toString() {
        return "MonthlyRevenueDto{" +
                "month=" + month +
                ", revenue=" + revenue +
                '}';
    }
}