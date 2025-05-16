package com.example.productservice.modules.FlashSaleDetails.controller;

import com.example.productservice.models.Flash_Sale_Details;
import com.example.productservice.modules.FlashSaleDetails.dto.CreateFlashSaleDetailDto;
import com.example.productservice.modules.FlashSaleDetails.dto.UpdateFlashSaleDetailDto;
import com.example.productservice.modules.FlashSaleDetails.service.FlashSaleDetailService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products/flash-sale-detail")
@AllArgsConstructor
@Getter
@Setter
public class FlashSaleDetailController {
    private final FlashSaleDetailService flashSaleDetailService;

    @PostMapping("/new")
    public ResponseEntity<Flash_Sale_Details> createDetails(@RequestBody CreateFlashSaleDetailDto createFlashSaleDetailDto) {
        return ResponseEntity.ok(flashSaleDetailService.createFlashSaleDetail(createFlashSaleDetailDto));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Flash_Sale_Details> findFlashSaleDetailById(@PathVariable Long id) {
        return ResponseEntity.ok(flashSaleDetailService.findOneById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<Iterable<Flash_Sale_Details>> getAllFlashSaleDetails(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(flashSaleDetailService.findAllByFlashSaleDetail(page, limit));
    }

    @GetMapping("/flash-sale-id/{flashSaleId}")
    public ResponseEntity<Iterable<Flash_Sale_Details>> findFlashSaleDetailsByFlashSaleId(@PathVariable Long flashSaleId) {
        return ResponseEntity.ok(flashSaleDetailService.findAllByFlashSaleId(flashSaleId));
    }

    @GetMapping("/product-id/{productId}")
    public ResponseEntity<Iterable<Flash_Sale_Details>> findFlashSaleDetailsByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(flashSaleDetailService.findAllByProductId(productId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Flash_Sale_Details> updateFlashSaleDetail(
            @PathVariable Long id,
            @RequestBody UpdateFlashSaleDetailDto updateFlashSaleDetailDto
    ) {
        return ResponseEntity.ok(flashSaleDetailService.updateDetails(id, updateFlashSaleDetailDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFlashSaleDetail(@PathVariable Long id) {
        flashSaleDetailService.deleteFlashSaleDetail(id);
        return ResponseEntity.ok("Flash sale detail deleted successfully");
    }
}
