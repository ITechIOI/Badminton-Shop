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