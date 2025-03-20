package com.example.notificationservice.modules.feign.Users;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "UserService", url = "${application.config.user-url}")
public interface UserClient {
    @GetMapping("/id/{id}")
    ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) ;

//    @GetMapping("/id/{id}")
//    ResponseEntity<Users> getUserById(@PathVariable("id") Long id);

}