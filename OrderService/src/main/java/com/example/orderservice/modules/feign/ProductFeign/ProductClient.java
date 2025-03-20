package com.example.orderservice.modules.feign.ProductFeign;

import com.example.orderservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(name = "UserService", url = "${application.config.user-url}")
//public interface UserClient {
//    @GetMapping("id/{id}")
//    ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) ;
//}

@FeignClient(name = "ProductService", url = "${application.config.product-url}", configuration = FeignConfig.class)
public interface ProductClient {
    @GetMapping("/service/{id}")
    ResponseEntity<ProductResponse> getProductById(@PathVariable("id") Long id);
}
