package com.example.productservice.modules.feign.Users;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserResponse(
        Long id,
        String name,
        String username,
        String password,
        String email,
        String gender,
        Long roleId
) {}