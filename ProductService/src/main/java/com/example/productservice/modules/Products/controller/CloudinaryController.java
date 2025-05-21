package com.example.productservice.modules.Products.controller;
import com.example.productservice.models.Products;
import com.example.productservice.modules.Products.service.CloudinaryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products/cloudinary")
@AllArgsConstructor
public class CloudinaryController {
    private final CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            List<String> imageUrl = cloudinaryService.uploadMedia(file);
            return ResponseEntity.ok(imageUrl.get(0) + " " + imageUrl.get(1));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload media");
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteImage(@RequestParam("publicId") String publicId, @RequestParam("type") String type) {
        if (cloudinaryService.deleteByType(publicId, type)) {
            return ResponseEntity.ok("Image deleted successfully");
        } else {
            return ResponseEntity.ok("Failed to delete media");
        }
    }
}