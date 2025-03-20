package com.example.productservice.modules.Suppliers.controller;

import com.example.productservice.models.Suppliers;
import com.example.productservice.modules.Suppliers.dto.CreateSupplierDto;
import com.example.productservice.modules.Suppliers.dto.UpdateSupplierDto;
import com.example.productservice.modules.Suppliers.service.SupplierService;
import com.example.productservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products/suppliers")
@AllArgsConstructor
@Getter
@Setter
public class SupplierController {
    private final SupplierService supplierService;

    @PostMapping("/new")
    public ResponseEntity<Suppliers> createSupplier(@RequestBody CreateSupplierDto createSupplierDto) {
       return ResponseEntity.ok(supplierService.createSupplier(createSupplierDto));
    }

    @GetMapping("id/{id}")
    public ResponseEntity<Suppliers> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.findSupplierById(id));
    }

    @GetMapping("all")
    public ResponseEntity<PagedResponse<Suppliers>> getAllSuppliers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(supplierService.getAllSuppliers(page, limit));
    }

    @PutMapping("{id}")
    public ResponseEntity<Suppliers> updateSupplier(@PathVariable("id") Long id, @RequestBody UpdateSupplierDto updateSupplierDto) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, updateSupplierDto));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable("id")  Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(null);
    }
}
