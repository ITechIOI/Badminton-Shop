package com.example.userbehaviorservice.modules.Favorite.controller;

import com.example.userbehaviorservice.models.Favorites;
import com.example.userbehaviorservice.modules.Favorite.dto.CreateFavoriteDto;
import com.example.userbehaviorservice.modules.Favorite.service.FavoriteService;
import com.example.userbehaviorservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/behaviors/favorites")
@AllArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @PostMapping("/new")
    public ResponseEntity<Favorites> createFavorite(@RequestBody CreateFavoriteDto createFavoriteDto) {
        return ResponseEntity.ok(favoriteService.createFavorite(createFavoriteDto));
    }

    @GetMapping("/userId/{userId}")
    public ResponseEntity<PagedResponse<Favorites>> getFavoritesByUserId(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(favoriteService.findFavoritesByUserId(userId, page, limit));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Favorites> getFavoriteById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(favoriteService.findFavoriteById(id));
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable("id") Long id) {
        favoriteService.deleteFavorite(id);
        return ResponseEntity.noContent().build();
    }

}
