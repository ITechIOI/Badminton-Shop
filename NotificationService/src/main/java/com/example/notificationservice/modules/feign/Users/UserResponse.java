package com.example.notificationservice.modules.feign.Users;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserResponse(
        Long id,
        String name,
        String avatar,
        String gender,
        String email,
        String username,
        String password,
        Long roleId
) {
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'';
    }
}