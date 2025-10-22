package com.example.orderservice.modules.Discounts.service;

import com.example.orderservice.models.Discounts;
import com.example.orderservice.modules.Discounts.dto.CreateDiscountDto;
import com.example.orderservice.modules.Discounts.dto.UpdateDiscountDto;
import com.example.orderservice.modules.Discounts.repository.DiscountRepository;
import com.example.orderservice.utils.NotFoundException;
import com.example.orderservice.utils.NullAwareBeanUtilsBean;
import com.example.orderservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DiscountService {
    private final DiscountRepository discountRepository;

    public Discounts createDiscount(CreateDiscountDto createDiscountDto) {
        Discounts discount = new Discounts();
        discount.setCode(createDiscountDto.getCode());
        discount.setDescription(createDiscountDto.getDescription());
        if (createDiscountDto.getPercent() < 0 || createDiscountDto.getPercent() > 100) {
            throw new IllegalArgumentException("Percent must be between 0 and 100");
        }
        discount.setPercent(createDiscountDto.getPercent());

        if (createDiscountDto.getMinOrderValue() < 0) {
            throw new IllegalArgumentException("Min order value must be non-negative");
        }
        discount.setMinOrderValue(createDiscountDto.getMinOrderValue());

        if (createDiscountDto.getCount() < 0) {
            throw new IllegalArgumentException("Count must be non-negative");
        }
        discount.setCount(createDiscountDto.getCount());

        if (createDiscountDto.getStartTime() == null || createDiscountDto.getEndTime() == null ||
            createDiscountDto.getEndTime().before(createDiscountDto.getStartTime())) {
            throw new IllegalArgumentException("Invalid start time or end time");
        }
        discount.setStartTime(createDiscountDto.getStartTime());
        discount.setEndTime(createDiscountDto.getEndTime());
        discount.setStatus(createDiscountDto.getStatus());
        return discountRepository.save(discount);
    }

    public Discounts findDiscountById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid discount ID");
        }
        return discountRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Discount not found"));
    }

    public Discounts findDiscountByCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Invalid discount code");
        }
        return discountRepository.findOneByCode(code).orElseThrow(() -> new NotFoundException("Discount not found"));
    }

    public PagedResponse<Discounts> getAllDiscount(int page, int limit) {
        if (page < 0 || limit <= 0) {
            throw new IllegalArgumentException("Invalid page or limit");
        }
        Pageable pageable = PageRequest.of(page, limit);
        Page<Discounts> discount = discountRepository.findAllDiscounts(pageable);
        if (discount.getContent().isEmpty()) {
            throw new NotFoundException("Discount not found");
        }
        return new PagedResponse<>(discount.getContent(), discount.getTotalPages(), discount.getTotalElements());
    }

    public Discounts updateDiscount(Long id, UpdateDiscountDto updateDiscountDto) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid discount ID");
        }
        if (updateDiscountDto.getPercent() != null &&
            (updateDiscountDto.getPercent() < 0 || updateDiscountDto.getPercent() > 100)) {
            throw new IllegalArgumentException("Percent must be between 0 and 100");
        }
        if (updateDiscountDto.getMinOrderValue() != null && updateDiscountDto.getMinOrderValue() < 0) {
            throw new IllegalArgumentException("Min order value must be non-negative");
        }
        if (updateDiscountDto.getCount() != null && updateDiscountDto.getCount() < 0) {
            throw new IllegalArgumentException("Count must be non-negative");
        }
        if ((updateDiscountDto.getStartTime() != null && updateDiscountDto.getEndTime() == null) ||
            (updateDiscountDto.getStartTime() == null && updateDiscountDto.getEndTime() != null)) {
            throw new IllegalArgumentException("Both start time and end time must be provided");
        }
        if (updateDiscountDto.getStartTime() != null && updateDiscountDto.getEndTime() != null &&
            updateDiscountDto.getEndTime().before(updateDiscountDto.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Discounts discount = discountRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Discount not found"));
        try {
            BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
            notNull.copyProperties(discount, updateDiscountDto);
        } catch (Exception e) {
            throw new RuntimeException("Error copying properties", e);
        }
        return discountRepository.save(discount);
    }

    public void deleteDiscount(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid discount ID");
        }
        Discounts discount = findDiscountById(id);
        discountRepository.softDeleteById(id);
    }

}
