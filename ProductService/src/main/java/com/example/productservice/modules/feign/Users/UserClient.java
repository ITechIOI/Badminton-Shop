package com.example.productservice.modules.feign.Users;

import com.example.productservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(
//        name = "user-service",
//        url = "${application.config.user-url}"
//)

@FeignClient(name = "UserService", url = "${application.config.user-url}", configuration = FeignConfig.class)
public interface UserClient {
    @GetMapping("id/{id}")
    ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) ;
}