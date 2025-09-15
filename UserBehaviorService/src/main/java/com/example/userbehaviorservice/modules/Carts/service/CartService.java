package com.example.userbehaviorservice.modules.Carts.service;

import com.example.userbehaviorservice.models.Carts;
import com.example.userbehaviorservice.modules.Carts.dto.CreateCartDto;
import com.example.userbehaviorservice.modules.Carts.dto.UpdateCartDto;
import com.example.userbehaviorservice.modules.Carts.repository.CartRepository;
import com.example.userbehaviorservice.modules.feign.ProductFeign.ProductClient;
import com.example.userbehaviorservice.modules.feign.ProductFeign.ProductResponse;
import com.example.userbehaviorservice.modules.feign.UserFeign.UserClient;
import com.example.userbehaviorservice.modules.feign.UserFeign.UserResponse;
import com.example.userbehaviorservice.utils.NotFoundException;
import com.example.userbehaviorservice.utils.NullAwareBeanUtilsBean;
import com.example.userbehaviorservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductClient productClient;
    private final UserClient userClient;

    public Carts createCart(CreateCartDto createCartDto) {
        Carts cart = new Carts();
        UserResponse user = userClient.getUserById(createCartDto.getUserId()).getBody();
        if (user == null) {
            throw new NotFoundException("User not found!");
        }
        System.out.println("User Client" + user.toString());
        ProductResponse product = productClient.getProductById(createCartDto.getProductId()).getBody();
        if (product == null) {
            throw new NotFoundException("Product not found!");
        }

        // Kiểm tra xem giỏ hàng đã có sản phẩm này chưa
        Carts existingCart = cartRepository.findByUserIdAndProductId(createCartDto.getUserId(), createCartDto.getProductId()).orElse(null);
        if (existingCart != null) {
            // Nếu có, cập nhật số lượng sản phẩm
            existingCart.setQuantity(existingCart.getQuantity() + createCartDto.getQuantity());
            return cartRepository.save(existingCart);
        } else {
            cart.setProductId(createCartDto.getProductId());
            cart.setUserId(createCartDto.getUserId());
            cart.setQuantity(createCartDto.getQuantity());
            return cartRepository.save(cart);
        }
    }

    public Carts findCartById(Long id) {
        return cartRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Cart not found"));
    }

    public PagedResponse<Carts> findCartByUserId(Long userId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Carts> carts = cartRepository.findByUserId(userId, pageable);
        if (carts.getContent().isEmpty()) {
            throw new NotFoundException("Cart not found");
        }
        return new PagedResponse<> (
            carts.getContent(),
            carts.getTotalPages(),
            carts.getTotalElements()
        );
    }

    public PagedResponse<Carts> getAllCarts(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Carts> carts = cartRepository.findAllCart(pageable);
        if (carts.getContent().isEmpty()) {
            throw new NotFoundException("No cart found");
        }
        return new PagedResponse<>(
            carts.getContent(),
            carts.getTotalPages(),
            carts.getTotalElements()
        );
    }

    public Carts updateCart(Long id, UpdateCartDto updateCartDto) {
        System.out.println("Update Cart" + updateCartDto.toString());
        Carts cart = findCartById(id);
        if (updateCartDto.getUserId() != null) {
            UserResponse user = userClient.getUserById(updateCartDto.getUserId()).getBody();
            if (user == null) {
                throw new NotFoundException("User not found!");
            }
        }
        if (updateCartDto.getProductId() != null) {
            ProductResponse product = productClient.getProductById(updateCartDto.getProductId()).getBody();
            if (product == null) {
                throw new NotFoundException("Product not found!");
            }
        }
        try {
            BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
            notNull.copyProperties(cart, updateCartDto);
        } catch (Exception e) {
            throw new RuntimeException("Error copying properties", e);
        }
        return cartRepository.save(cart);
    }

    public void deleteCart(Long id) {
        Carts cart = cartRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Cart not found"));
        cartRepository.softDeleteById(cart.getId());
    }


}
