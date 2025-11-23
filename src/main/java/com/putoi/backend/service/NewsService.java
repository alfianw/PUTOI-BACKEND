/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.service;

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
import com.putoi.backend.exception.BadRequestException;
import com.putoi.backend.exception.ConflictException;
import com.putoi.backend.exception.DataNotFoundException;
import com.putoi.backend.models.News;
import com.putoi.backend.models.User;
import com.putoi.backend.repository.NewsRepository;
import com.putoi.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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
    public ApiResponse<NewsResponse> creatNews(NewsRequest request, Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));

        if (newsRepository.existsByTitle(request.getTitle())) {
            throw new ConflictException("Title is already in use:" + request.getTitle());
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

        return ApiResponse.<NewsDetailResponse>builder()
                .code("00")
                .message("Success")
                .data(response)
                .build();
    }

    @Transactional
    public ApiResponse<NewsUpdateResponse> updateNews(NewsUpdateRequest request, Authentication authentication) {

        if (request.getId() == null) {
            throw new BadRequestException("Id is required");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));

        News news = newsRepository.findById(request.getId())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));

        if (!isBlank(request.getTitle())) {
            news.setTitle(request.getTitle());
        }

        if (!isBlank(request.getDescription())) {
            news.setDescription(request.getDescription());
        }

        news.setAuthor(user.getName());

        newsRepository.save(news);

        NewsUpdateResponse response = modelMapper.map(news, NewsUpdateResponse.class);

        return ApiResponse.<NewsUpdateResponse>builder()
                .code("00")
                .message("Success Update")
                .data(response)
                .build();
    }

    @Transactional
    public ApiResponse<String> deleteNews(NewsDeleteRequest request) {

        if (request.getId() == null) {
            throw new BadRequestException("Id is required");
        }

        News news = newsRepository.findById(request.getId())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));

        newsRepository.delete(news);

        return ApiResponse.<String>builder()
                .code("00")
                .message("Success Delete")
                .data(null)
                .build();
    }

}
