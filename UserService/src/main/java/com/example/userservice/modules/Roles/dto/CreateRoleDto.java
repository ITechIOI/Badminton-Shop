package com.example.userservice.modules.Roles.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoleDto {
    private String name;

    public CreateRoleDto() {}

    public CreateRoleDto(String name) {
        this.name = name;
    }
}