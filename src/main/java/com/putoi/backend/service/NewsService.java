/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.service;

import com.putoi.backend.dto.ApiResponse;
import com.putoi.backend.dto.NewsDto.ImageUpdateRequest;
import com.putoi.backend.dto.NewsDto.NewsDeleteRequest;
import com.putoi.backend.dto.NewsDto.NewsDetailRequest;
import com.putoi.backend.dto.NewsDto.NewsDetailResponse;
import com.putoi.backend.dto.NewsDto.NewsImageDetailResponse;
import com.putoi.backend.dto.NewsDto.NewsPaginationRequest;
import com.putoi.backend.dto.NewsDto.NewsPaginationResponse;
import com.putoi.backend.dto.NewsDto.NewsRequest;
import com.putoi.backend.dto.NewsDto.NewsResponse;
import com.putoi.backend.dto.NewsDto.NewsUpdateRequest;
import com.putoi.backend.dto.NewsDto.NewsUpdateResponse;
import com.putoi.backend.exception.BadRequestException;
import com.putoi.backend.exception.ConflictException;
import com.putoi.backend.exception.DataNotFoundException;
import com.putoi.backend.models.News;
import com.putoi.backend.models.NewsImage;
import com.putoi.backend.models.User;
import com.putoi.backend.repository.NewsImageRepository;
import com.putoi.backend.repository.NewsRepository;
import com.putoi.backend.repository.UserRepository;
import java.io.IOException;
import jakarta.transaction.Transactional;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author alfia
 */
@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final NewsImageRepository newsImageRepository;

    @Value("${file.upload.news-image-dir}")
    private String newsImageUploadDir;

    @Value("${file.upload.news-image-public-path}")
    private String newsImagePublicPath;

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String normalize(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t.toLowerCase();
    }

    @Transactional
    public ApiResponse<NewsResponse> creatNews(
            NewsRequest request,
            MultipartFile[] images,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));

        if (newsRepository.existsByTitle(request.getTitle())) {
            throw new ConflictException("Title is already in use");
        }

        if (isBlank(request.getTitle())) {
            throw new BadRequestException("Title is required");
        }

        if (isBlank(request.getDescription())) {
            throw new BadRequestException("Description is required");
        }

        News news = modelMapper.map(request, News.class);
        news.setAuthor(user.getName());
        news.setUser(user);
        newsRepository.save(news);

        if (images != null && images.length > 0) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {

                    File dir = new File(newsImageUploadDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    String originalFilename = image.getOriginalFilename();
                    String extension = "";

                    if (originalFilename != null && originalFilename.contains(".")) {
                        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    }

                    String fileName = UUID.randomUUID() + "_" + originalFilename;
                    Path filePath = Paths.get(newsImageUploadDir, fileName);

                    try {
                        Files.write(filePath, image.getBytes());
                    } catch (java.io.IOException e) {
                        throw new RuntimeException("Failed to upload image", e);
                    }

                    NewsImage newsImage = NewsImage.builder()
                            .imageName(fileName)
                            .imagePath(newsImagePublicPath + fileName)
                            .news(news)
                            .build();

                    newsImageRepository.save(newsImage);
                }
            }
        }

        NewsResponse response = modelMapper.map(news, NewsResponse.class);
        response.setAuthor(user.getName());

        return ApiResponse.<NewsResponse>builder()
                .code("00")
                .message("Success")
                .data(response)
                .build();
    }

    public ApiResponse<List<NewsPaginationResponse>> getNewsPagination(NewsPaginationRequest request) {

        int limit;
        try {
            limit = Integer.parseInt(request.getLimit());
        } catch (Exception e) {
            limit = 10;
        }
        if (limit <= 0) {
            limit = 10;
        }

        int pageClient;
        try {
            pageClient = Integer.parseInt(request.getPage());
        } catch (Exception e) {
            pageClient = 1;
        }
        if (pageClient <= 0) {
            pageClient = 1;
        }
        int page = pageClient - 1;

        String sortBy = (request.getSortBy() == null || request.getSortBy().isBlank()) ? "id" : request.getSortBy();
        String sortOrder = (request.getSortOrder() == null || request.getSortOrder().isBlank()) ? "desc" : request.getSortOrder();
        Sort.Direction dir = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        PageRequest pageable = PageRequest.of(page, limit, Sort.by(dir, sortBy));

        Map<String, String> f = Optional.ofNullable(request.getFilters()).orElse(Collections.emptyMap());
        String title = normalize(f.get("title"));
        String author = normalize(f.get("author"));

        Specification<News> spec = Specification.where(null);
        if (title != null) {
            spec = spec.and((root, q, cb)
                    -> cb.like(cb.lower(root.get("title")), "%" + title + "%"));
        }
        if (author != null) {
            spec = spec.and((root, q, cb)
                    -> cb.like(cb.lower(root.get("author")), "%" + author + "%"));
        }
        Page<News> pageResult = newsRepository.findAll(spec, pageable);

        if (pageResult.isEmpty()) {
            throw new DataNotFoundException("Data Not Found");
        }

        List<NewsPaginationResponse> items = pageResult.getContent().stream()
                .map(u -> NewsPaginationResponse.builder()
                .id(u.getId())
                .title(u.getTitle())
                .description(u.getDescription())
                .author(u.getAuthor())
                .createdAt(u.getCreatedAt())
                .updateAt(u.getUpdateAt())
                .build())
                .collect(Collectors.toList());

        ApiResponse<List<NewsPaginationResponse>> resp = new ApiResponse<>();
        resp.setCode("00");
        resp.setMessage("Success");
        resp.setData(items);
        resp.setPage(pageResult.getNumber() + 1);
        resp.setTotalPages(pageResult.getTotalPages());
        resp.setCountData((int) pageResult.getTotalElements());
        return resp;
    }

    public ApiResponse<NewsDetailResponse> getDetailNews(NewsDetailRequest request) {

        if (request.getId() == null) {
            throw new BadRequestException("Id is required");
        }

        News news = newsRepository.findById(request.getId())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));

        NewsDetailResponse response = modelMapper.map(news, NewsDetailResponse.class);

        if (news.getImages() != null && !news.getImages().isEmpty()) {
            List<NewsImageDetailResponse> imageDetails = news.getImages()
                    .stream()
                    .map(img -> NewsImageDetailResponse.builder()
                    .id(img.getId())
                    .imageName(img.getImageName())
                    .imagePath(img.getImagePath())
                    .build())
                    .toList();
            response.setImages(imageDetails);
        }

        return ApiResponse.<NewsDetailResponse>builder()
                .code("00")
                .message("Success")
                .data(response)
                .build();
    }

    @Transactional
    public ApiResponse<NewsUpdateResponse> updateNews(
            NewsUpdateRequest request,
            Authentication authentication) {

        if (request.getId() == null) {
            throw new BadRequestException("Id is required");
        }

        // Ambil user
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        // Ambil berita
        News news = newsRepository.findById(request.getId())
                .orElseThrow(() -> new DataNotFoundException("News not found"));

        // Update title & description
        if (!isBlank(request.getTitle())) {
            news.setTitle(request.getTitle());
        }
        if (!isBlank(request.getDescription())) {
            news.setDescription(request.getDescription());
        }
        news.setAuthor(user.getName());


        // Update / tambah images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
// buat map existing images
            Map<Long, NewsImage> existingById = news.getImages().stream()
                    .collect(Collectors.toMap(NewsImage::getId, Function.identity()));

            Set<Long> keepIds = new HashSet<>();

            for (ImageUpdateRequest imgDto : request.getImages()) {
                Long imgId = imgDto.getImageId();
                MultipartFile file = imgDto.getFile();

                if (imgId != null) {
                    // update file pada entitas yang sudah ada (tidak membuat entitas baru)
                    NewsImage oldImage = existingById.get(imgId);
                    if (oldImage == null) {
                        throw new DataNotFoundException("Image not found: " + imgId);
                    }
                    // tandai untuk dipertahankan
                    keepIds.add(imgId);

                    if (file != null && !file.isEmpty()) {
                        try {
                            Path oldPath = Paths.get(newsImageUploadDir, oldImage.getImageName());
                            Files.deleteIfExists(oldPath);

                            String newFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                            Path path = Paths.get(newsImageUploadDir, newFileName);
                            Files.write(path, file.getBytes());

                            oldImage.setImageName(newFileName);
                            oldImage.setImagePath(newsImagePublicPath + newFileName);
                            newsImageRepository.save(oldImage);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to replace file: " + oldImage.getImageName(), e);
                        }
                    }
                    // jika file null -> biarkan oldImage apa adanya
                } else {
                    // tambahkan gambar baru
                    if (file != null && !file.isEmpty()) {
                        NewsImage newImage = saveNewsImage(file, news);
                        newsImageRepository.save(newImage);
                        news.getImages().add(newImage); // penting: tambahkan ke koleksi entitas
                        keepIds.add(newImage.getId()); // optional: baru diketahui setelah save
                    }
                }
            }
        }

        newsRepository.save(news);

        // Mapping response
        NewsUpdateResponse response = modelMapper.map(news, NewsUpdateResponse.class);

        return ApiResponse.<NewsUpdateResponse>builder()
                .code("00")
                .message("Success Update")
                .data(response)
                .build();
    }

// Simpan file baru
    private NewsImage saveNewsImage(MultipartFile file, News news) {
        try {
            File dir = new File(newsImageUploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalFilename;
            Path path = Paths.get(newsImageUploadDir, fileName);

            Files.write(path, file.getBytes());

            return NewsImage.builder()
                    .imageName(fileName)
                    .imagePath(newsImagePublicPath + fileName)
                    .news(news)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    @Transactional
    public ApiResponse<String> deleteNews(NewsDeleteRequest request) {

        if (request.getId() == null) {
            throw new BadRequestException("Id is required");
        }

        News news = newsRepository.findById(request.getId())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));

        if (news.getImages() != null) {
            for (NewsImage img : news.getImages()) {
                try {
                    Path path = Paths.get(newsImageUploadDir, img.getImageName());
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    System.err.println("Failed to delete file: " + img.getImageName());
                }
            }
        }

        newsRepository.delete(news);

        return ApiResponse.<String>builder()
                .code("00")
                .message("Success Delete")
                .data(null)
                .build();
    }
}
