package com.example.userservice.modules.Users.controller;

import com.example.userservice.models.Users;
import com.example.userservice.modules.Users.dto.CreateUserDto;
import com.example.userservice.modules.Users.dto.UserResponse;
import com.example.userservice.modules.Users.service.CloudinaryService;
import com.example.userservice.modules.Users.service.UserService;
import com.example.userservice.utils.PagedResponse;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.userservice.modules.Users.dto.UpdateUserDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/users/users")

public class UserController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    public UserController(UserService userService, CloudinaryService cloudinaryService) {
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/new")
    public ResponseEntity<Users> createUser(
            @ModelAttribute CreateUserDto user,
            @RequestPart("file") MultipartFile file) {

        try {
            List<String> imageUrl = cloudinaryService.uploadImage(file);
            System.out.println("Image URL: " + imageUrl.get(0) + " " + imageUrl.get(1));
            user.setAvatar(imageUrl.get(0) + " " + imageUrl.get(1));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }

        return ResponseEntity.ok(userService.createUser(user));
    }

    @PutMapping("{userId}")
    public ResponseEntity<Users> updateUser(
            @PathVariable("userId") Long userId,
            @ModelAttribute UpdateUserDto updateUserDto,
            @RequestPart("file") MultipartFile file
    ) {
        Users user = userService.getUserById(userId);
        try {
            boolean isDeleted = cloudinaryService.deleteImage(user.getAvatar().split(" ")[1]);
            List<String> imageUrl = cloudinaryService.uploadImage(file);
            updateUserDto.setAvatar(imageUrl.get(0) + " " + imageUrl.get(1));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image");
        }
        return ResponseEntity.ok(userService.updateUser(userId, updateUserDto));
    }

    @GetMapping("/id/{id}")
//    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        Users user = userService.getUserById(id);

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getGender(),
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                user.getRole() != null ? user.getRole().getId() : null
        );

        System.out.println("Create new user: " + userResponse.toString());

        return ResponseEntity.ok(userResponse);
    }


    @GetMapping("username/{username}")
    public ResponseEntity<Users> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("email/{email}")
    public ResponseEntity<Users> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("all")
    public ResponseEntity<PagedResponse<Users>> getAllUsers(
            @RequestParam (value = "page", defaultValue = "0") int page,
            @RequestParam (value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(userService.getAllUsers(page, limit));
    }

    @DeleteMapping("{userId}")
    public ResponseEntity<Users> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(null);
    }

//    Sử dụng kiểu Map<String, String> để trả về một custom message body
//    @DeleteMapping("deleteSome")
//    public ResponseEntity<Map<String, String>> getMessage() {
//        return ResponseEntity.ok(Map.of("message", "Hello, this is a message!"));
//    }

}
