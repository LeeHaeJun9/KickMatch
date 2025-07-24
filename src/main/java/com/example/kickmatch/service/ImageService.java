package com.example.kickmatch.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    private final String uploadDir = "C:\\Users\\user\\IdeaProjects\\kickmatch\\src\\main\\resources\\static\\uploads";

    public String saveImage(MultipartFile file) {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }

        return "/uploads/" + filename;
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        // 예: "/uploads/파일명.png" → 파일명만 추출
        String filename = Paths.get(imageUrl).getFileName().toString();
        Path filePath = Paths.get(uploadDir).resolve(filename);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            // 삭제 실패 시 로그만 출력하고 무시
        }
    }


    public String updateImage(MultipartFile newImage, String oldImageUrl) {
        // 기존 이미지 삭제
        if (oldImageUrl != null && !oldImageUrl.isBlank()) {
            deleteImage(oldImageUrl);
        }

        // 새 이미지 저장
        return saveImage(newImage);
    }

}

