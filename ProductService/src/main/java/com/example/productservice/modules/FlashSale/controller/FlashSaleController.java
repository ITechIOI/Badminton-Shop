package com.example.productservice.modules.FlashSale.controller;

import com.example.productservice.models.Flash_Sale;
import com.example.productservice.modules.FlashSale.dto.CreateFlashSaleDto;
import com.example.productservice.modules.FlashSale.dto.UpdateFlashSaleDto;
import com.example.productservice.modules.FlashSale.service.FlashSaleService;
import com.example.productservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;

@RestController
@RequestMapping("/products/flash-sale")
@AllArgsConstructor
@Getter
@Setter
public class FlashSaleController {
    private final FlashSaleService flashSaleService;

    @PostMapping("/new")
    public ResponseEntity<Flash_Sale> createFlashSale(@RequestBody CreateFlashSaleDto createFlashSaleDto) {
        return ResponseEntity.ok(flashSaleService.createFlashSale(createFlashSaleDto));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Flash_Sale> findFlashSaleById(@PathVariable Long id) {
        return ResponseEntity.ok(flashSaleService.findFlashSaleById(id));
    }

    @GetMapping("/startTime/{startTime}/endTime/{endTime}")
    public ResponseEntity<Flash_Sale> findFlashSaleByTime(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        return ResponseEntity.ok(flashSaleService.findFlashSaleByTime(startTime, endTime));
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<Flash_Sale>> getAllFlashSales(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(flashSaleService.getAllFlashSales(page, limit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Flash_Sale> updateFlashSale(
            @PathVariable Long id,
            @RequestBody UpdateFlashSaleDto updateFlashSaleDto
    ) {
        return ResponseEntity.ok(flashSaleService.updateFlashSale(id, updateFlashSaleDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlashSale(@PathVariable Long id) {
        flashSaleService.deleteFlashSale(id);
        return ResponseEntity.noContent().build();
    }
}
