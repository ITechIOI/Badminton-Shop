package com.example.orderservice.modules.Discounts.controller;

import com.example.orderservice.models.Discounts;
import com.example.orderservice.modules.Discounts.dto.CreateDiscountDto;
import com.example.orderservice.modules.Discounts.dto.UpdateDiscountDto;
import com.example.orderservice.modules.Discounts.service.DiscountService;
import com.example.orderservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders/discounts")
@AllArgsConstructor
public class DiscountController {
    private final DiscountService discountService;

    @PostMapping("/new")
    public ResponseEntity<Discounts> createDiscount(@RequestBody CreateDiscountDto discountDto) {
        return ResponseEntity.ok(discountService.createDiscount(discountDto));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Discounts> getDiscountById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(discountService.findDiscountById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Discounts> getDiscountByCode(@PathVariable("code") String code) {
        return ResponseEntity.ok(discountService.findDiscountByCode(code));
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<Discounts>> getAllDiscounts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(discountService.getAllDiscount(page, limit));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Discounts> deleteDiscount(@PathVariable("id") Long id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.ok(null);
    }

    @PutMapping("{id}")
    public ResponseEntity<Discounts> updateDiscount(@PathVariable("id") Long id, @RequestBody UpdateDiscountDto discountDto) {
        return ResponseEntity.ok(discountService.updateDiscount(id, discountDto));
    }
}
