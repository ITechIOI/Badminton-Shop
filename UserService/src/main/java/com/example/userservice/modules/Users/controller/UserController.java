package com.example.userservice.modules.Users.controller;

import com.example.userservice.models.Users;
import com.example.userservice.modules.Users.dto.CreateUserDto;
import com.example.userservice.modules.Users.dto.UserResponse;
import com.example.userservice.modules.Users.service.CloudinaryService;
import com.example.userservice.modules.Users.service.UserService;
import com.example.userservice.utils.PagedResponse;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.example.userservice.modules.Users.dto.UpdateUserDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/users/users")

public class UserController {

    private final UserService userService;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/new")
    public ResponseEntity<Users> createUser(
            @ModelAttribute CreateUserDto user) {
        System.out.println("Create new user: " + user.toString());
        return ResponseEntity.ok(userService.createUser(user));
    }

    @GetMapping("/id/{id}")
//    @PreAuthorize("hasRole('client_user')") // Uncomment this if Spring Security is enabled
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {

        UserResponse userResponse = userService.getUserById(id);

        return ResponseEntity.ok(userResponse);
    }

    // Phương thức này được dùng để lấy thông tin của người dùng thông qua jwt
    @GetMapping("profile")
    public UserResponse getProfile(@AuthenticationPrincipal Jwt jwt) {
        // Lấy keycloakId từ claim "sub"
        String keycloakId = jwt.getClaim("sub");
        return userService.getUserByKeycloakId(keycloakId);
    }

    @PutMapping()
    public ResponseEntity<Void> updateUser(
            @AuthenticationPrincipal Jwt jwt,
            @ModelAttribute UpdateUserDto updateUserDto
    ) {
        String keycloakId = jwt.getClaim("sub");
        userService.updateUser(keycloakId, updateUserDto);
        // Trả về response 204
        return ResponseEntity.noContent().build();
    }

    @GetMapping("all")
    public ResponseEntity<PagedResponse<UserResponse>> getAllUsers(
            @RequestParam (value = "page", defaultValue = "0") int page,
            @RequestParam (value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(userService.getAllUsers(page, limit));
    }

    @DeleteMapping()
    public ResponseEntity<Users> deleteUser(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getClaim("sub");
        userService.deleteUser(keycloakId);
        return ResponseEntity.ok(null);
    }
}
