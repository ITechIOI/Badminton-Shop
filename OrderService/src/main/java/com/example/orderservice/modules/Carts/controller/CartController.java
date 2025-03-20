package com.example.orderservice.modules.Carts.controller;

import com.example.orderservice.models.Carts;
import com.example.orderservice.modules.Carts.dto.CreateCartDto;
import com.example.orderservice.modules.Carts.dto.UpdateCartDto;
import com.example.orderservice.modules.Carts.service.CartService;
import com.example.orderservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders/carts")
@AllArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/new")
    public ResponseEntity<Carts> createCart(@RequestBody CreateCartDto createCartDto) {
        return ResponseEntity.ok(cartService.createCart(createCartDto));
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<Carts>> getAllCarts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(cartService.getAllCarts(page, limit));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Carts> getCartById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(cartService.findCartById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PagedResponse<Carts>> getCartByUserId(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(cartService.findCartByUserId(userId, page, limit));
    }

    // Chỉ cho phép update số lượng sản phẩm trong giỏ hàng
    @PutMapping("/{id}")
    public ResponseEntity<Carts> updateCart(@PathVariable("id") Long id, @RequestBody UpdateCartDto updateCartDto) {
        return ResponseEntity.ok(cartService.updateCart(id, updateCartDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Carts> deleteCart(@PathVariable("id") Long id) {
        cartService.deleteCart(id);
        return ResponseEntity.ok(null);
    }
}
