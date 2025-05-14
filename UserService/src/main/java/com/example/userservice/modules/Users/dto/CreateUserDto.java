package com.example.userservice.modules.Users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserDto {
    private String firstName;
    private String lastName;
    private String gender;
    private String avatar;
    private String phone;
    private String email;
    private String username;
    private String roles;
    private String password;
}
