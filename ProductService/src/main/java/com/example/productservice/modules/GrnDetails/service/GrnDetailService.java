package com.example.productservice.modules.GrnDetails.service;

import com.example.productservice.models.GRN;
import com.example.productservice.models.GRN_Details;
import com.example.productservice.models.Products;
import com.example.productservice.modules.Grn.repository.GrnRepository;
import com.example.productservice.modules.Grn.service.GrnService;
import com.example.productservice.modules.GrnDetails.dto.CreateGrnDetailDto;
import com.example.productservice.modules.GrnDetails.dto.UpdateGrnDetailDto;
import com.example.productservice.modules.GrnDetails.repository.GrnDetailRepository;
import com.example.productservice.modules.Products.dto.UpdateProductDto;
import com.example.productservice.modules.Products.service.ProductService;
import com.example.productservice.utils.NotFoundException;
import com.example.productservice.utils.NullAwareBeanUtilsBean;
import com.example.productservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
@AllArgsConstructor
public class GrnDetailService {
    private final GrnDetailRepository grnDetailRepository;
    private final GrnService grnService;
    private final ProductService productService;
    private final GrnRepository grnRepository;

    public GRN_Details createGrnDetail(CreateGrnDetailDto grnDetailDto) {
        Products product = productService.findProductById(grnDetailDto.getProductId());
        UpdateProductDto updateProductDto = new UpdateProductDto();
        updateProductDto.setQuantity(product.getQuantity() + grnDetailDto.getQuantity());
        updateProductDto.setPrice(grnDetailDto.getPrice());
        Products updateInventory = productService.updateProduct(product.getId(), updateProductDto);
        GRN_Details grnDetail = new GRN_Details();
        grnDetail.setGrn(grnService.findGrnById(grnDetailDto.getGrnId()));
        grnDetail.setQuantity(grnDetailDto.getQuantity());
        grnDetail.setProduct(productService.findProductById(grnDetailDto.getProductId()));
        grnDetail.setPrice(grnDetailDto.getPrice());
        return grnDetailRepository.save(grnDetail);
    }

    public GRN_Details findGrnDetailById(Long id) {
        return grnDetailRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Grn Detail not found"));
    }

    public PagedResponse<GRN_Details> getAllGrnDetails(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<GRN_Details> grnDetails = grnDetailRepository.findAllGrnDetails(pageable);
        if (grnDetails.getContent().isEmpty()) {
            throw new NotFoundException("No Grn Detail found");
        }
        return new PagedResponse<>(grnDetails.getContent(), grnDetails.getTotalPages(), grnDetails.getTotalElements());
    }

    public PagedResponse<GRN_Details> getDetailsByGrnId(Long grnId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<GRN_Details> grnDetails = grnDetailRepository.findByGrnId(grnId, pageable);
        if (grnDetails.getContent().isEmpty()) {
            throw new NotFoundException("No Grn Detail found");
        }
        return new PagedResponse<>(grnDetails.getContent(), grnDetails.getTotalPages(), grnDetails.getTotalElements());
    }

    public PagedResponse<GRN_Details> getDetailsByProductId(Long productId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<GRN_Details> grnDetails = grnDetailRepository.findByProductId(productId, pageable);
        if (grnDetails.getContent().isEmpty()) {
            throw new NotFoundException("No Grn Detail found");
        }
        return new PagedResponse<>(grnDetails.getContent(), grnDetails.getTotalPages(), grnDetails.getTotalElements());
    }

    public void deleteGrnDetail(Long id) {
        GRN_Details grnDetail = findGrnDetailById(id);
        Products product = productService.findProductById(grnDetail.getProduct().getId());
        // Cập nhật số lượng tồn kho
        UpdateProductDto updateProductDto = new UpdateProductDto();
        updateProductDto.setQuantity(product.getQuantity() - grnDetail.getQuantity());
        Products updateInventory = productService.updateProduct(product.getId(), updateProductDto);
        grnDetailRepository.softDeleteById(grnDetail.getId());
    }

    // Không được phép chỉnh sửa productI và grnId
    public GRN_Details updateGrnDetail(Long id, UpdateGrnDetailDto grnDetailDto) {
        GRN_Details grnDetail = grnDetailRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Grn Detail not found"));
        int oldQuantity = grnDetail.getQuantity();
        Products product = productService.findProductById(grnDetail.getProduct().getId());
       // System.out.println("Product" + product.toString());

        try {
            BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
            notNull.copyProperties(grnDetail, grnDetailDto);
        } catch (Exception e) {
            throw new RuntimeException("Error copying properties", e);
        }

        System.out.println("GrnDetail" + grnDetail.toString());

        // Cập nhật số lượng tồn kho
        UpdateProductDto updateProductDto = new UpdateProductDto();
        if (grnDetailDto.getQuantity() != null) {
            updateProductDto.setQuantity(product.getQuantity() - oldQuantity + grnDetailDto.getQuantity());
            grnDetail.setQuantity(grnDetailDto.getQuantity());
        }

        if (grnDetailDto.getPrice() != null) {
            grnDetail.setPrice(grnDetailDto.getPrice());
            updateProductDto.setPrice(grnDetailDto.getPrice());
        }

        Products updateInventory = productService.updateProduct(product.getId(), updateProductDto);
       // System.out.println("Update Inventory" + updateProductDto.toString());
        return grnDetailRepository.save(grnDetail);
    }

}
