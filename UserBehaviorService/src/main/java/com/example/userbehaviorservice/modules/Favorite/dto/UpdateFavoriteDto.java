package com.example.userbehaviorservice.modules.Favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFavoriteDto {
    private Long userId;
    private Long productId;

    public String toString() {
        return "CreateFavoriteDto(userId=" + this.getUserId() + ", productId=" + this.getProductId() + ")";
    }

}
