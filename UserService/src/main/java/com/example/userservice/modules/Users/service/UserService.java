package com.example.userservice.modules.Users.service;

import com.example.userservice.models.Roles;
import com.example.userservice.models.Users;
import com.example.userservice.modules.Roles.service.RoleService;
import com.example.userservice.modules.Users.dto.CreateUserDto;
import com.example.userservice.modules.Users.dto.UpdateUserDto;
import com.example.userservice.modules.Users.repository.UserRepository;
import com.example.userservice.utils.NotFoundException;
import com.example.userservice.utils.NullAwareBeanUtilsBean;
import com.example.userservice.utils.PagedResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final CloudinaryService cloudinaryService;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public Users createUser(CreateUserDto createUserDto) {
        Roles role = roleService.getRoleById(createUserDto.getRoleId());
        if (role == null) {
            throw new NotFoundException("Role does not exist");
        }

        Users user = Users.builder()
                .name(createUserDto.getName())
                .avatar(createUserDto.getAvatar())
                .email(createUserDto.getEmail())
                .gender(createUserDto.getGender())
                .username(createUserDto.getUsername())
                .password(createUserDto.getPassword())
                .role(roleService.getRoleById(createUserDto.getRoleId()))
                .build();
        return userRepository.save(user);
    }

    public PagedResponse<Users> getAllUsers(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Users> users = userRepository.findAllUsers(pageable);
        if (users.getContent().isEmpty()) {
            throw new NotFoundException("No user found");
        }
        return new PagedResponse<>(users.getContent(), users.getTotalPages(), users.getTotalElements());
    }

    public Users getUserById(Long id)  {
        Users user = userRepository.findOneById(id);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        logger.info("User created: {}", user);
        return user;
    }

    public Users getUserByUsername(String username) {
        Users user = userRepository.findOneByUsername(username);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    public Users getUserByEmail(String email) {
        Users user = userRepository.findOneByEmail(email);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }


    public Users updateUser(Long userId, UpdateUserDto updateUserDto) {
        Users user = userRepository.findUserById(userId);
        if (user == null) {
            throw new NotFoundException("User does not exist");
        }

        try {
            BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
            notNull.copyProperties(user, updateUserDto);
        } catch (Exception e) {
            throw new RuntimeException("Error copying properties", e);
        }

        if (updateUserDto.getRoleId() != null) {
            Roles role = roleService.getRoleById(updateUserDto.getRoleId());
            if (role == null) {
                throw new NotFoundException("Role does not exist");
            }
            user.setRole(role);
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        System.out.println("User deleted" + userId);
        Users user = userRepository.findUserById(userId);
        if (user == null) {
            throw new NotFoundException("User does not exist");
        }
        userRepository.softDeleteById(userId);
    }
}
