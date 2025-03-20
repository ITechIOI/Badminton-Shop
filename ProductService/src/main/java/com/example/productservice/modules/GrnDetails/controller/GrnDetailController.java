package com.example.productservice.modules.GrnDetails.controller;

import com.example.productservice.models.GRN_Details;
import com.example.productservice.modules.GrnDetails.dto.CreateGrnDetailDto;
import com.example.productservice.modules.GrnDetails.dto.UpdateGrnDetailDto;
import com.example.productservice.modules.GrnDetails.repository.GrnDetailRepository;
import com.example.productservice.modules.GrnDetails.service.GrnDetailService;
import com.example.productservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.hibernate.sql.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products/grn-details")
@AllArgsConstructor

public class GrnDetailController {
    private final GrnDetailService grnDetailService;

    @PostMapping("new")
    public ResponseEntity<GRN_Details> createGrnDetails(@RequestBody CreateGrnDetailDto createGrnDetailDto) {
        return ResponseEntity.ok(grnDetailService.createGrnDetail(createGrnDetailDto));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<GRN_Details> getGrnDetailById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(grnDetailService.findGrnDetailById(id));
    }

    @GetMapping("all")
    public ResponseEntity<PagedResponse<GRN_Details>> getAllGrnDetails(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(grnDetailService.getAllGrnDetails(page, limit));
    }

    @GetMapping("/grn/{grnId}")
    public ResponseEntity<PagedResponse<GRN_Details>> getGrnDetailsByGrnId(
            @PathVariable("grnId") Long grnId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(grnDetailService.getDetailsByGrnId(grnId, page, limit));
    }

    @GetMapping("product/{productId}")
    public ResponseEntity<PagedResponse<GRN_Details>> getGrnDetailsByProductId(
            @PathVariable("productId") Long productId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(grnDetailService.getDetailsByProductId(productId, page, limit));
    }

    @PutMapping("{id}")
    public ResponseEntity<GRN_Details> updateGrnDetail(@PathVariable("id")Long id, @RequestBody UpdateGrnDetailDto createGrnDetailDto) {
        return ResponseEntity.ok(grnDetailService.updateGrnDetail(id, createGrnDetailDto));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteGrnDetail (@PathVariable Long id) {
        grnDetailService.deleteGrnDetail(id);
        return ResponseEntity.ok(null);
    }
}
