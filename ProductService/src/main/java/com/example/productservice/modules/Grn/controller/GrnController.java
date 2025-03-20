package com.example.productservice.modules.Grn.controller;

import com.example.productservice.models.GRN;
import com.example.productservice.modules.Grn.dto.CreateGrnDto;
import com.example.productservice.modules.Grn.dto.UpdateGrnDto;
import com.example.productservice.modules.Grn.service.GrnService;
import com.example.productservice.utils.PagedResponse;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products/grns")
@AllArgsConstructor
@Getter
@Setter
public class GrnController {
    private final GrnService grnService;

    @PostMapping("/new")
    public ResponseEntity<GRN> createGrn(@RequestBody CreateGrnDto grnDto) {
        return ResponseEntity.ok(grnService.createGrn(grnDto));
    }

    @GetMapping("id/{id}")
    public ResponseEntity<GRN> getGrnById(@PathVariable Long id) {
        return ResponseEntity.ok(grnService.findGrnById(id));
    }

    @GetMapping("supplier/{supplierId}")
    public ResponseEntity<PagedResponse<GRN>> getGrnBySupplierId(
            @PathVariable Long supplierId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(grnService.findGrnBySupplierId(supplierId, page, limit));
    }

    @GetMapping("all")
    public ResponseEntity<PagedResponse<GRN>> getAllGrns(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(grnService.getAllGrns(page, limit));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<GRN> deleteGrn(@PathVariable("id") Long id) {
        grnService.deleteGrn(id);
        return ResponseEntity.ok(null);
    }

    @PutMapping("{id}")
    public ResponseEntity<GRN> updateGrn(@PathVariable("id") Long id, @RequestBody UpdateGrnDto grnDto) {
        return ResponseEntity.ok(grnService.updateGrn(id, grnDto));
    }

}
