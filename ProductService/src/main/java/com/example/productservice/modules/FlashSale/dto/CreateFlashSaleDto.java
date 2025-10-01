package com.example.productservice.modules.FlashSale.dto;

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
public class CreateFlashSaleDto {
    private String name;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public String toString() {
        return "CreateFlashSaleDto{" +
                "name='" + name + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
