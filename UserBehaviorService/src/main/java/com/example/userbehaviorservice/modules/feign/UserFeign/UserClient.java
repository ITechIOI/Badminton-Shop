package com.example.userbehaviorservice.modules.feign.UserFeign;

import com.example.userbehaviorservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "UserService", url = "${application.config.user-url}", configuration = FeignConfig.class)
public interface UserClient {
    @GetMapping("/id/{id}")
    ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) ;
}


