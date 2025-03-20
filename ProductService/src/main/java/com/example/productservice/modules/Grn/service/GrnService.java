package com.example.productservice.modules.Grn.service;

import com.example.productservice.models.GRN;
import com.example.productservice.models.Suppliers;
import com.example.productservice.modules.Grn.dto.CreateGrnDto;
import com.example.productservice.modules.Grn.dto.UpdateGrnDto;
import com.example.productservice.modules.feign.Users.UserClient;
import com.example.productservice.modules.feign.Users.UserResponse;
import com.example.productservice.modules.Grn.repository.GrnRepository;
import com.example.productservice.modules.Suppliers.service.SupplierService;
import com.example.productservice.utils.NotFoundException;
import com.example.productservice.utils.NullAwareBeanUtilsBean;
import com.example.productservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GrnService {
    private final GrnRepository grnRepository;
    private final UserClient userClient;
    private final SupplierService supplierService;

    public GRN createGrn(CreateGrnDto createGrnDto){
        GRN grn = new GRN();
        ResponseEntity<UserResponse> user = this.userClient.getUserById(createGrnDto.getUserId());

//        if (user.getBody() == null) {
//            throw new NotFoundException("User not found!");
//        }
        System.out.println(user.getBody());

        Suppliers suppliers = supplierService.findSupplierById(createGrnDto.getSupplierId());
        BeanUtils.copyProperties(createGrnDto, grn);
        grn.setSupplier(suppliers);

        BeanUtils.copyProperties(createGrnDto, grn);
        return grnRepository.save(grn);
    }

    public GRN findGrnById(Long id){
        return grnRepository.findGrnById(id).orElseThrow(
            () -> new NotFoundException("Grn not found")
        );
    }

    public PagedResponse<GRN> findGrnBySupplierId(Long supplierId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<GRN> grn = grnRepository.findGrnBySupplierId(supplierId, pageable);
        if (grn.getContent().isEmpty()) {
            throw new NotFoundException("No grn found");
        }
        return new PagedResponse<GRN>(grn.getContent(), grn.getTotalPages(), grn.getTotalElements());
    }

    public PagedResponse<GRN> getAllGrns(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<GRN> grn = grnRepository.findAllGrns(pageable);
        if (grn.getContent().isEmpty()) {
            throw new NotFoundException("No grn found");
        }
        return new PagedResponse<GRN>(grn.getContent(), grn.getTotalPages(), grn.getTotalElements());
    }


    public GRN deleteGrn(Long id){
        GRN grn = findGrnById(id);
        grnRepository.softDeleteById(id);
        return grn;
    }

    public GRN updateGrn(Long id, UpdateGrnDto updateGrnDto) {
        GRN grn = findGrnById(id);
        try {
            BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
            notNull.copyProperties(grn, updateGrnDto);
        } catch (Exception e) {
            throw new RuntimeException("Error copying properties", e);
        }
        if (updateGrnDto.getSupplierId() != null) {
            grn.setSupplier(supplierService.findSupplierById(updateGrnDto.getSupplierId()));
        }
        if (updateGrnDto.getUserId() != null) {
            ResponseEntity<UserResponse> user = this.userClient.getUserById(updateGrnDto.getUserId());
            if (user.getBody() == null) {
                throw new NotFoundException("User not found!");
            }
            grn.setUserId(updateGrnDto.getUserId());
        }
        System.out.println("GRN" + grn.toString());
        return grnRepository.save(grn);
    }

//    public Users updateUser(Long userId, UpdateUserDto updateUserDto) {
//        Users user = userRepository.findUserById(userId);
//        if (user == null) {
//            throw new NotFoundException("User does not exist");
//        }
//
//        try {
//            BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
//            notNull.copyProperties(user, updateUserDto);
//        } catch (Exception e) {
//            throw new RuntimeException("Error copying properties", e);
//        }
//
//        if (updateUserDto.getRoleId() != null) {
//            Roles role = roleService.getRoleById(updateUserDto.getRoleId());
//            if (role == null) {
//                throw new NotFoundException("Role does not exist");
//            }
//            user.setRole(role);
//        }
//
//        return userRepository.save(user);
//    }

}
