package com.example.userservice.modules.Roles.service;

import com.example.userservice.models.Roles;
import com.example.userservice.modules.Roles.dto.CreateRoleDto;
import com.example.userservice.modules.Roles.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    public Roles createRole(CreateRoleDto roleDto){
        Roles newRole = new Roles(roleDto);
        return roleRepository.save(newRole);
    }

    public List<Roles> getAllRoles(){
        return roleRepository.findAll();
    }

    public Roles getRoleById(Long id) {
        return roleRepository.findRoleById(id);
    }
}
