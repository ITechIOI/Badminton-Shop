package com.example.userservice.modules.Subscriptions.service;

import com.example.userservice.models.Subscriptions;
import com.example.userservice.models.Users;
import com.example.userservice.modules.Subscriptions.dto.CreateSubscriptionDto;
import com.example.userservice.modules.Subscriptions.dto.UpdateSubscriptionDto;
import com.example.userservice.modules.Subscriptions.repository.SubscriptionRepository;
import com.example.userservice.modules.Users.dto.UserResponse;
import com.example.userservice.modules.Users.service.UserService;
import com.example.userservice.utils.NotFoundException;
import com.example.userservice.utils.NullAwareBeanUtilsBean;
import com.example.userservice.utils.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;

    public Subscriptions createSubscription(CreateSubscriptionDto createSubscriptionDto) {
        Subscriptions oldSubscription = subscriptionRepository.findByUserId(createSubscriptionDto.getUserId()).orElse(null);
        if (oldSubscription != null) {
            subscriptionRepository.softDeleteByIdSubscription(oldSubscription.getId());
        }
        Users userResponse;
        try {
            userResponse = userService.findRawById(createSubscriptionDto.getUserId());
        } catch (Exception e) {
            throw new IllegalArgumentException("User not found with id: " + createSubscriptionDto.getUserId());
        }

        Subscriptions subscription = new Subscriptions();
        BeanUtils.copyProperties(createSubscriptionDto, subscription);

        Users user = new Users();
        user.setKeycloakId(userResponse.getKeycloakId());
        user.setId(createSubscriptionDto.getUserId());
        subscription.setUser(user);

        return subscriptionRepository.save(subscription);
    }

    public Subscriptions findSubscriptionById(Long id) {
        return subscriptionRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Subscription not found"));
    }

    public Subscriptions findSubscriptionByUserId(Long userId) {
        try {
            Users userResponse = userService.findRawById(userId);
            System.out.println("User found: " + userResponse);
        } catch (Exception e) {
            throw new NotFoundException("User not found with id: " + userId);
        }
        return subscriptionRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("Subscription not found"));
    }

    public PagedResponse<Subscriptions> getAllSubscriptions(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Subscriptions> subscriptions = subscriptionRepository.findAll(pageable);
        if (subscriptions.getContent().isEmpty()) {
            throw new IllegalArgumentException("No subscription found");
        }
        return new PagedResponse<>(subscriptions.getContent(), subscriptions.getTotalPages(), subscriptions.getTotalElements());
    }

    public Subscriptions updateSubscription(Long id, UpdateSubscriptionDto updateSubscriptionDto) {

        Subscriptions existingSubscription = findSubscriptionById(id);
        try {
            BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
            notNull.copyProperties(existingSubscription, updateSubscriptionDto);
        } catch (Exception e) {
            throw new RuntimeException("Error copying properties", e);
        }
        return subscriptionRepository.save(existingSubscription);
    }

    public void deleteSubscription(Long id) {
        Subscriptions subscription = findSubscriptionById(id);
        subscriptionRepository.softDeleteByIdSubscription(subscription.getId());
    }
}

//
//public Products createProduct(CreateProductDto createProductDto) {
//    Categories category =  categoryService.findCategoryById(createProductDto.getCategoryId());
//    if (category == null) {
//        throw new NotFoundException("Category not found");
//    }
//    Products product = new Products();
//    BeanUtils.copyProperties(createProductDto, product);
//    product.setCategory(category);
//    return productRepository.save(product);
//}
//
//public Products findProductById(Long id) {
//    logger.info("Product created: {}", productRepository.findOneById(id));
//    return productRepository.findOneById(id).orElseThrow(() -> new NotFoundException("Product not found"));
//}
//
//public ProductResponse findProductByIdForService (Long id) {
//    Products products = findProductById(id);
//    return new ProductResponse(
//            products.getName(),
//            products.getBrand(),
//            products.getDescription(),
//            products.getPrice(),
//            products.getImageUrl(),
//            products.getVideoUrl(),
//            products.getAvailable(),
//            products.getQuantity(),
//            products.getCategory().getId()
//    );
//}
//
//public PagedResponse<Products> getAllProducts(int page, int limit) {
//    Pageable pageable = PageRequest.of(page, limit);
//    Page<Products> products = productRepository.findAllProducts(pageable);
//    if (products.getContent().isEmpty()) {
//        throw new NotFoundException("No product found");
//    }
//    return new PagedResponse<>(products.getContent(), products.getTotalPages(), products.getTotalElements());
//}
//
//public PagedResponse<Products> getProductsByBrand(String brand, int page, int limit) {
//    Pageable pageable = PageRequest.of(page, limit);
//    Page<Products> products = productRepository.findProductsByBrand(brand, pageable);
//    if (products.getContent().isEmpty()) {
//        throw new NotFoundException("No product found");
//    }
//    return new PagedResponse<>(products.getContent(), products.getTotalPages(), products.getTotalElements());
//}
//
//public PagedResponse<Products> getProductsByCategoryId(Long categoryId, int page, int limit) {
//    Pageable pageable = PageRequest.of(page, limit);
//    Page<Products> products = productRepository.findByCategoryId(categoryId, pageable);
//    if (products.getContent().isEmpty()) {
//        throw new NotFoundException("No product found");
//    }
//    return new PagedResponse<>(products.getContent(), products.getTotalPages(), products.getTotalElements());
//}
//
////    public PagedResponse<GRN_Details> getDetailsByProductId(Long productId, int page, int limit) {
////        Pageable pageable = PageRequest.of(page, limit);
////        Page<GRN_Details> grnDetails = grnDetailRepository.findByProductId(productId, pageable);
////        if (grnDetails.getContent().isEmpty()) {
////            throw new NotFoundException("No Grn Detail found");
////        }
////        return new PagedResponse<>(grnDetails.getContent(), grnDetails.getTotalPages(), grnDetails.getTotalElements());
////    }
//
//
//public Products updateProduct(Long id, UpdateProductDto updateProductDto) {
//    Products product = findProductById(id);
//    try {
//        BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
//        notNull.copyProperties(product, updateProductDto);
//    } catch (Exception e) {
//        throw new RuntimeException("Error copying properties", e);
//    }
//    if (updateProductDto.getCategoryId() != null) {
//        Categories category = categoryService.findCategoryById(updateProductDto.getCategoryId());
//        product.setCategory(category);
//    }
//    return productRepository.save(product);
//}
//
//public void deleteProduct(Long id) {
//    Products product = findProductById(id);
//    productRepository.softDeleteById(product.getId());
//}