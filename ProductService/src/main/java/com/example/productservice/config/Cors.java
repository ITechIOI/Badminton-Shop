package com.example.productservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Cors {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Áp dụng cho tất cả API
                        .allowedOrigins("*") // Domain được phép gọi API
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH") // Các phương thức được phép
                        .allowedHeaders("*") // Chấp nhận tất cả headers
                        .allowCredentials(false); // Cho phép gửi cookie, token
            }
        };
    }
}
