package com.example.notificationservice.modules.feign.Users;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "UserService", url = "${application.config.user-url}" + "users", contextId = "userClient")
public interface UserClient {
    @GetMapping("/id/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) ;

    @GetMapping("/keycloakId/{keycloakId}")
    public ResponseEntity<UserResponse> getUserByKeycloakId(@PathVariable String keycloakId);

//    @GetMapping("/id/{id}")
//    ResponseEntity<Users> getUserById(@PathVariable("id") Long id);

}