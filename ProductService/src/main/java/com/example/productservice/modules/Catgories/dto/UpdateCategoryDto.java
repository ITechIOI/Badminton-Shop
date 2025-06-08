package com.example.productservice.modules.Catgories.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCategoryDto {
    private String name;
    private Integer count;
}
