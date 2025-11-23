/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.controller;

import com.putoi.backend.dto.ApiResponse;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author alfia
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    @PreAuthorize("hasAuthority('superadmin')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<NewsResponse>> createNews(@RequestBody NewsRequest request, Authentication authentication) {
        return ResponseEntity.ok(newsService.creatNews(request, authentication));
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
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<NewsUpdateResponse>> updateNews(@RequestBody NewsUpdateRequest request, Authentication authentication) {
        return ResponseEntity.ok(newsService.updateNews(request, authentication));
    }

    @PreAuthorize("hasAuthority('superadmin')")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteNews(@RequestBody NewsDeleteRequest request) {
        return ResponseEntity.ok(newsService.deleteNews(request));
    }

}
