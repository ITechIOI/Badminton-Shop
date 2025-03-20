package com.example.productservice.modules.Suppliers.service;

import com.example.productservice.models.Products;
import com.example.productservice.models.Suppliers;
import com.example.productservice.modules.Suppliers.dto.CreateSupplierDto;
import com.example.productservice.modules.Suppliers.dto.UpdateSupplierDto;
import com.example.productservice.modules.Suppliers.repository.SupplierRepository;
import com.example.productservice.utils.NotFoundException;
import com.example.productservice.utils.NullAwareBeanUtilsBean;
import com.example.productservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public Suppliers createSupplier(CreateSupplierDto createSupplierDto) {
        Suppliers supplier = new Suppliers();
        supplier.setName(createSupplierDto.getName());
        supplier.setPhone(createSupplierDto.getPhone());
        supplier.setAddress(createSupplierDto.getAddress());
        return supplierRepository.save(supplier);
    }

    public Suppliers findSupplierById(Long id) {
        return supplierRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Supplier not found"));
    }

    public Suppliers findSupplierByName(String name) {
        return supplierRepository.findByName(name).orElseThrow(() -> new NotFoundException("Supplier not found"));
    }

    public Suppliers updateSupplier(Long id, UpdateSupplierDto updateSupplierDto) {
        Suppliers supplier = supplierRepository.findById(id).orElseThrow(() -> new NotFoundException("Supplier not found"));
        try {
            BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
            notNull.copyProperties(supplier, updateSupplierDto);
        } catch (Exception e) {
            throw new RuntimeException("Error copying properties", e);
        }
        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(Long id) {
        Suppliers supplier = findSupplierById(id);
        supplierRepository.softDeleteById(supplier.getId());
    }

    public PagedResponse<Suppliers> getAllSuppliers(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Suppliers> suppliers = supplierRepository.findAllSuppliers(pageable);
        if (suppliers.getContent().isEmpty()) {
            throw new NotFoundException("No supplier found");
        }
        return new PagedResponse<>(suppliers.getContent(), suppliers.getTotalPages(), suppliers.getTotalElements());
    }


}
