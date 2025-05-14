package com.example.userservice.models;

import com.example.userservice.modules.Roles.dto.CreateRoleDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
public class Roles extends AbstractModel {

    @NotBlank(message = "Role name is required")
    @Column(nullable = false)
    private String name;

    public Roles() {}

    public Roles(String name) {
        this.name = name;
    }

    public Roles(CreateRoleDto createRoleDto ) {
        this.name = createRoleDto.getName();
    }
}