package com.example.productservice.modules.FlashSale.service;

import com.example.productservice.models.Flash_Sale;
import com.example.productservice.models.GRN;
import com.example.productservice.modules.FlashSale.dto.CreateFlashSaleDto;
import com.example.productservice.modules.FlashSale.dto.UpdateFlashSaleDto;
import com.example.productservice.modules.FlashSale.repository.FlashSaleRepository;
import com.example.productservice.utils.NotFoundException;
import com.example.productservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
@AllArgsConstructor
public class FlashSaleService {
    private final FlashSaleRepository flashSaleRepository;

    public Flash_Sale createFlashSale(CreateFlashSaleDto createFlashSaleDto) {
        Flash_Sale flashSale = new Flash_Sale();
        flashSale.setName(createFlashSaleDto.getName());
        flashSale.setDescription(createFlashSaleDto.getDescription());
        flashSale.setStartTime(createFlashSaleDto.getStartTime());
        flashSale.setEndTime(createFlashSaleDto.getEndTime());

        return flashSaleRepository.save(flashSale);
    }

    public Flash_Sale findFlashSaleById(Long id) {
        Flash_Sale flashSale = flashSaleRepository.findOneById(id);
        if (flashSale == null) {
            throw new NotFoundException("Flash sale not found");
        }
        return flashSale;
    }

    public Flash_Sale findFlashSaleByTime(LocalDateTime startTime, LocalDateTime endTime) {
        Flash_Sale flashSale = flashSaleRepository.findFlashSaleByTime(startTime, endTime);
        if (flashSale == null) {
            throw new NotFoundException("Flash sale not found");
        }
        return flashSale;
    }

    public PagedResponse<Flash_Sale> getAllFlashSales(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Flash_Sale> flashSales = flashSaleRepository.findAllFlashSales(pageable);
        if (flashSales.getContent().isEmpty()) {
            throw new NotFoundException("No flash sale found");
        }
        return new PagedResponse<>(flashSales.getContent(), flashSales.getTotalPages(), flashSales.getTotalElements());
    }

    public Flash_Sale updateFlashSale(Long id, UpdateFlashSaleDto updateFlashSaleDto) {
        Flash_Sale flashSale = findFlashSaleById(id);

        if (updateFlashSaleDto.getDescription() != null) {
            flashSale.setDescription(updateFlashSaleDto.getDescription());
        }

        if (updateFlashSaleDto.getName() != null) {
            flashSale.setName(updateFlashSaleDto.getName());
        }

        if (updateFlashSaleDto.getStartTime() != null) {
            flashSale.setStartTime(updateFlashSaleDto.getStartTime());
        }

        if (updateFlashSaleDto.getEndTime() != null) {
            flashSale.setEndTime(updateFlashSaleDto.getEndTime());
        }

        return flashSaleRepository.save(flashSale);
    }

    public void deleteFlashSale(Long id) {
        Flash_Sale flashSale = findFlashSaleById(id);
        flashSaleRepository.softDeleteByIdFlashSale(flashSale.getId());
    }
}
