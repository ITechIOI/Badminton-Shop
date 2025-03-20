package com.example.userservice.modules.Users.controller;

import com.example.userservice.modules.Users.service.CloudinaryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/cloudinary")
@AllArgsConstructor
public class CloudinaryController {
    private final CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            List<String> imageUrl = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(imageUrl.get(0) + "  " + imageUrl.get(1));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image");
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteImage(@RequestParam("publicId") String publicId) {
        if (cloudinaryService.deleteImage(publicId)) {
            return ResponseEntity.ok("Image deleted successfully");
        } else {
            return ResponseEntity.ok("Failed to delete image");
        }
    }
}
