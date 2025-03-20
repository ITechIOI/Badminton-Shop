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
        discount.setPercent(createDiscountDto.getPercent());
        discount.setMinOrderValue(createDiscountDto.getMinOrderValue());
        discount.setCount(createDiscountDto.getCount());
        discount.setStartTime(createDiscountDto.getStartTime());
        discount.setEndTime(createDiscountDto.getEndTime());
        discount.setStatus(createDiscountDto.getStatus());
        return discountRepository.save(discount);
    }

    public Discounts findDiscountById(Long id) {
        return discountRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Discount not found"));
    }

    public Discounts findDiscountByCode(String code) {
        return discountRepository.findOneByCode(code).orElseThrow(() -> new NotFoundException("Discount not found"));
    }

    public PagedResponse<Discounts> getAllDiscount(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Discounts> discount = discountRepository.findAllDiscounts(pageable);
        if (discount.getContent().isEmpty()) {
            throw new NotFoundException("Discount not found");
        }
        return new PagedResponse<>(discount.getContent(), discount.getTotalPages(), discount.getTotalElements());
    }

    public Discounts updateDiscount(Long id, UpdateDiscountDto updateDiscountDto) {
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
        Discounts discount = findDiscountById(id);
        discountRepository.softDeleteById(id);
    }

}
