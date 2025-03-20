package com.example.userservice.modules.Users.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserDto {
    private String name;
    private String avatar;
    private String gender;
    private String email;
    private String username;
    private String password;
    private Long roleId;
}
