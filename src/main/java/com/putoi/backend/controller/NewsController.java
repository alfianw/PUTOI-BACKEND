/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.putoi.backend.dto.ApiResponse;
import com.putoi.backend.dto.NewsDto.ImageUpdateRequest;
import com.putoi.backend.dto.NewsDto.NewsDeleteRequest;
import com.putoi.backend.dto.NewsDto.NewsDetailRequest;
import com.putoi.backend.dto.NewsDto.NewsDetailResponse;
import com.putoi.backend.dto.NewsDto.NewsPaginationRequest;
import com.putoi.backend.dto.NewsDto.NewsPaginationResponse;
import com.putoi.backend.dto.NewsDto.NewsRequest;
import com.putoi.backend.dto.NewsDto.NewsResponse;
import com.putoi.backend.dto.NewsDto.NewsUpdateRequest;
import com.putoi.backend.dto.NewsDto.NewsUpdateResponse;
import com.putoi.backend.service.NewsService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author alfia
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<NewsResponse>> createNews(
            @RequestPart("data") String data,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            Authentication authentication) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        NewsRequest request = mapper.readValue(data, NewsRequest.class);

        return ResponseEntity.ok(
                newsService.creatNews(request, images, authentication)
        );
    }

    @PostMapping("/paginaton")
    public ResponseEntity<ApiResponse<List<NewsPaginationResponse>>> newsPagination(@RequestBody NewsPaginationRequest request) {
        return ResponseEntity.ok(newsService.getNewsPagination(request));
    }

    @PostMapping("/detail")
    public ResponseEntity<ApiResponse<NewsDetailResponse>> getDetailNews(@RequestBody NewsDetailRequest request) {
        return ResponseEntity.ok(newsService.getDetailNews(request));
    }

    @PreAuthorize("hasAuthority('superadmin')")
    @PutMapping(
            value = "/update",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<NewsUpdateResponse>> updateNews(
            @RequestPart("data") String data,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            Authentication authentication) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        NewsUpdateRequest request = mapper.readValue(data, NewsUpdateRequest.class);

        // Cocokkan file dengan imageId
        if (images != null && images.length > 0) {
            List<ImageUpdateRequest> imageRequests = new ArrayList<>();
            int fileIndex = 0;
            for (ImageUpdateRequest imgDto : request.getImages()) {
                MultipartFile file = null;
                if (Boolean.TRUE.equals(imgDto.getHasFile()) && fileIndex < images.length) {
                    file = images[fileIndex++];
                }
                imageRequests.add(ImageUpdateRequest.builder()
                        .imageId(imgDto.getImageId())
                        .file(file)
                        .hasFile(imgDto.getHasFile())
                        .build());
            }
            request.setImages(imageRequests);
        }

        ApiResponse<NewsUpdateResponse> response = newsService.updateNews(request, authentication);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('superadmin')")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteNews(@RequestBody NewsDeleteRequest request) {
        return ResponseEntity.ok(newsService.deleteNews(request));
    }

}
