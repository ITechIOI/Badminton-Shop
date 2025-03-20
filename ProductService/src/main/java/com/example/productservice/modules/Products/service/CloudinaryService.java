package com.example.productservice.modules.Products.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public List<String> uploadImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", "uploads"));

        String secureUrl = uploadResult.get("secure_url").toString();
        String publicId = uploadResult.get("public_id").toString();

        List<String> list = new ArrayList<>();
        list.add(secureUrl);
        list.add(publicId);
        return list;
    }

    public boolean deleteImage(String publicId) {
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return "ok".equals(result.get("result"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
