package com.example.userservice.modules.Roles.repository;

import com.example.userservice.models.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface RoleRepository extends JpaRepository<Roles, Long> {

    Roles findRoleById(Long id);
    Roles findRoleByName(String name);
    List<Roles> findAllBy();
}
