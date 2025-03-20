package com.example.userservice.modules.Roles.controller;

import com.example.userservice.models.Roles;
import com.example.userservice.modules.Roles.dto.CreateRoleDto;
import com.example.userservice.modules.Roles.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(@RequestBody RoleService roleService){
        this.roleService = roleService;
    }

    @PostMapping("/new")
    public Roles createRole(@RequestBody CreateRoleDto roleDto){
        return roleService.createRole(roleDto);
    }

    @GetMapping("all")
    public List<Roles> getAllRoles(){
        return roleService.getAllRoles();
    }
}