package com.example.productservice.modules.FlashSaleDetails.service;

import com.example.productservice.models.Flash_Sale;
import com.example.productservice.models.Flash_Sale_Details;
import com.example.productservice.models.Products;
import com.example.productservice.modules.FlashSale.service.FlashSaleService;
import com.example.productservice.modules.FlashSaleDetails.dto.CreateFlashSaleDetailDto;
import com.example.productservice.modules.FlashSaleDetails.dto.UpdateFlashSaleDetailDto;
import com.example.productservice.modules.FlashSaleDetails.repository.FlashSaleDetailRepository;
import com.example.productservice.modules.Products.service.ProductService;
import com.example.productservice.utils.NotFoundException;
import com.example.productservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashSaleDetailService {
    private final FlashSaleDetailRepository flashSaleDetailRepository;
    private final FlashSaleService flashSaleService;
    private final ProductService productService;

    public Flash_Sale_Details createFlashSaleDetail(CreateFlashSaleDetailDto flashSaleDetails) {
        if (flashSaleDetails.getOriginalPrice() < 0 || flashSaleDetails.getSalePrice() < 0 || flashSaleDetails.getQuantity() < 0) {
            throw new IllegalArgumentException("Original price, sale price and quantity must be non-negative");
        }

        Products product = productService.findProductById(flashSaleDetails.getProductId());
        if (product == null) {
            throw new NotFoundException("Product not found");
        }
        Flash_Sale flashSale = flashSaleService.findFlashSaleById(flashSaleDetails.getFlashSaleId());
        if (flashSale == null) {
            throw new NotFoundException("Flash sale not found");
        }

        Flash_Sale_Details createFlashSale = new Flash_Sale_Details();
        createFlashSale.setOriginalPrice(flashSaleDetails.getOriginalPrice());
        createFlashSale.setSalePrice(flashSaleDetails.getSalePrice());
        createFlashSale.setQuantity(flashSaleDetails.getQuantity());
        createFlashSale.setFlashSale(flashSale);
        createFlashSale.setProduct(product);

        return flashSaleDetailRepository.save(createFlashSale);
    }

    public Flash_Sale_Details findOneById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid flash sale detail ID");
        }
        Flash_Sale_Details flashSaleDetails = flashSaleDetailRepository.findByFlashSaleDetailId(id);
        if (flashSaleDetails == null) {
            throw new NotFoundException("Flash sale detail not found");
        }
        return flashSaleDetails;
    }

    public List<Flash_Sale_Details> findAllByFlashSaleId(Long flashSaleId) {
        if (flashSaleId == null || flashSaleId <= 0) {
            throw new IllegalArgumentException("Invalid flash sale ID");
        }
        Flash_Sale flashSale = flashSaleService.findFlashSaleById(flashSaleId);
        if (flashSale == null) {
            throw new NotFoundException("Flash sale not found");
        }
        List<Flash_Sale_Details> details = flashSaleDetailRepository.findAllFlashSaleDetailsByFlashSaleId(flashSaleId);
        if (details.isEmpty()) {
            throw new NotFoundException("Flash sale detail not found");
        }
        return details;
    }

    public List<Flash_Sale_Details> findAllByProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
        Products product = productService.findProductById(productId);
        if (product == null) {
            throw new NotFoundException("Product not found");
        }
        List<Flash_Sale_Details> details = flashSaleDetailRepository.findAllFlashSaleDetailsByProductId(productId);
        if  (details.isEmpty()) {
            throw new NotFoundException("Flash sale detail not found");
        }
        return details;
    }

    public List<Flash_Sale_Details> findAllByFlashSaleDetail(int page, int limit) {
        if (page < 0 || limit <= 0) {
            throw new IllegalArgumentException("Invalid page or limit");
        }
        Pageable pageable = PageRequest.of(page, limit);
        Page<Flash_Sale_Details> flashSaleDetails = flashSaleDetailRepository.findAllFlashSaleDetails(pageable);
        if (flashSaleDetails.getContent().isEmpty()) {
            throw new NotFoundException("No flash sale detail found");
        }
        return flashSaleDetails.getContent();
    }

    // Không cho phép cập nhật productId và flashSaleId
    public Flash_Sale_Details updateDetails(Long id, UpdateFlashSaleDetailDto updateFlashSaleDetailDto) {
        if (updateFlashSaleDetailDto.getOriginalPrice() != null && updateFlashSaleDetailDto.getOriginalPrice() < 0) {
            throw new IllegalArgumentException("Original price must be non-negative");
        }
        if (updateFlashSaleDetailDto.getSalePrice() != null && updateFlashSaleDetailDto.getSalePrice() < 0) {
            throw new IllegalArgumentException("Sale price must be non-negative");
        }

        if (updateFlashSaleDetailDto.getQuantity() != null && updateFlashSaleDetailDto.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid flash sale detail ID");
        }

        Flash_Sale_Details details = findOneById(id);

        if (details == null) {
            throw new NotFoundException("Flash sale detail not found");
        }

        if (updateFlashSaleDetailDto.getOriginalPrice() != null) {
            details.setOriginalPrice(updateFlashSaleDetailDto.getOriginalPrice());
        }
        if (updateFlashSaleDetailDto.getSalePrice() != null) {
            details.setSalePrice(updateFlashSaleDetailDto.getSalePrice());
        }
        if (updateFlashSaleDetailDto.getQuantity() != null) {
            details.setQuantity(updateFlashSaleDetailDto.getQuantity());
        }

        return flashSaleDetailRepository.save(details);
    }

    public Flash_Sale_Details deleteFlashSaleDetail(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid flash sale detail ID");
        }
        Flash_Sale_Details details = findOneById(id);
        flashSaleDetailRepository.softDeleteByIdFlashSaleDetail(details.getId());
        return details;
    }

}

