package com.example.userbehaviorservice.modules.Favorite.service;

import com.example.userbehaviorservice.models.Favorites;
import com.example.userbehaviorservice.modules.Favorite.dto.CreateFavoriteDto;
import com.example.userbehaviorservice.modules.Favorite.repository.FavoriteRepository;
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

    public Favorites createFavorite(CreateFavoriteDto createFavoriteDto) {
        Favorites favorite = new Favorites();
        favorite.setUserId(createFavoriteDto.getUserId());
        favorite.setProductId(createFavoriteDto.getProductId());
        return favoriteRepository.save(favorite);
    }

    public Favorites findFavoriteById(Long id) {
        Favorites favorites = favoriteRepository.findOneById(id);
        if (favorites == null) {
            throw new NotFoundException("Favorite not found");
        }
        return favorites;
    }

    public PagedResponse<Favorites> findFavoritesByUserId(Long userId, int page, int limit) {
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
        Favorites favorite = findFavoriteById(id);
        if (favorite == null) {
            throw new NotFoundException("Favorite not found");
        }
        favoriteRepository.deleteByUserIdAndProductId(id);
    }
}
