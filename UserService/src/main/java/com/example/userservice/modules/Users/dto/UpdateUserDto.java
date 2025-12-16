package com.example.userservice.modules.Users.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UpdateUserDto {
    private String firstName;
    private String lastName;
    private String gender;
    private String avatar;
    private String phone;
    private String email;
    private String username;
    private String password;
}
