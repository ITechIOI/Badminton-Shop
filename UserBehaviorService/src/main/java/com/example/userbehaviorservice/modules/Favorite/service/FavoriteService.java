package com.example.userbehaviorservice.modules.Favorite.service;

import com.example.userbehaviorservice.models.Favorites;
import com.example.userbehaviorservice.modules.Favorite.dto.CreateFavoriteDto;
import com.example.userbehaviorservice.modules.Favorite.repository.FavoriteRepository;
import com.example.userbehaviorservice.modules.feign.UserFeign.UserClient;
import com.example.userbehaviorservice.modules.feign.UserFeign.UserResponse;
import com.example.userbehaviorservice.utils.NotFoundException;
import com.example.userbehaviorservice.utils.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserClient userClient;

    public Favorites createFavorite(CreateFavoriteDto createFavoriteDto) {

        Favorites existingFavorite = favoriteRepository.findByUserIdAndProductId(createFavoriteDto.getUserId(), createFavoriteDto.getProductId());
        if (existingFavorite != null) {
            throw new IllegalArgumentException("Product already in favorites.");
        }

        Favorites favorite = new Favorites();
        favorite.setUserId(createFavoriteDto.getUserId());
        favorite.setProductId(createFavoriteDto.getProductId());
        return favoriteRepository.save(favorite);
    }

    public Favorites findFavoriteById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid favorite ID");
        }
        Favorites favorites = favoriteRepository.findOneById(id);
        if (favorites == null) {
            throw new NotFoundException("Favorite not found");
        }
        return favorites;
    }

    public PagedResponse<Favorites> findFavoritesByUserId(Long userId, int page, int limit) {
        UserResponse user = userClient.getUserById(userId).getBody();
        if (user == null) {
            throw new NotFoundException("User not found!");
        }
        // Kiểm tra giá trị của page và limit
        if (page < 0 || limit <= 0) {
            throw new IllegalArgumentException("Invalid page or limit");
        }

        Pageable pageable = PageRequest.of(page, limit);
        Page<Favorites> favoritesPage = favoriteRepository.findAllByUserId(userId, pageable);
        PagedResponse<Favorites> pagedResponse = new PagedResponse<>(
                favoritesPage.getContent(),
                favoritesPage.getTotalPages(),
                favoritesPage.getTotalElements()
        );
        return pagedResponse;
    }

    public void deleteFavorite(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid favorite ID");
        }
        Favorites favorite = findFavoriteById(id);
        if (favorite == null) {
            throw new NotFoundException("Favorite not found");
        }
        favoriteRepository.deleteByUserIdAndProductId(id);
    }
}
