/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.service;

import com.putoi.backend.dto.ApiResponse;
import com.putoi.backend.dto.ProductDto.ProductDeleteRequest;
import com.putoi.backend.dto.ProductDto.ProductDetailRequest;
import com.putoi.backend.dto.ProductDto.ProductDetailResponse;
import com.putoi.backend.dto.ProductDto.ProductEditRequest;
import com.putoi.backend.dto.ProductDto.ProductEditResponse;
import com.putoi.backend.dto.ProductDto.ProductPaginationRequest;
import com.putoi.backend.dto.ProductDto.ProductPaginationResponse;
import com.putoi.backend.dto.ProductDto.ProductRequest;
import com.putoi.backend.dto.ProductDto.ProductResponse;
import com.putoi.backend.exception.BadRequestException;
import com.putoi.backend.exception.ConflictException;
import com.putoi.backend.exception.DataNotFoundException;
import com.putoi.backend.models.Product;
import com.putoi.backend.models.User;
import com.putoi.backend.repository.ProductRepository;
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
public class ProductService {
    
    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
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
    public ApiResponse<ProductResponse> createProduct(ProductRequest request, Authentication authentication) {
        
        String email = authentication.getName();
        
        if (isBlank(request.getTitle())) {
            throw new BadRequestException("Title is required");
        }
        
        if (isBlank(request.getCategory())) {
            throw new BadRequestException("Category is required");
        }
        
        if (isBlank(request.getDescription())) {
            throw new BadRequestException("Description is required");
        }
        
        if (productRepository.existsByTitle(request.getTitle())) {
            throw new ConflictException("Title is already in use:" + request.getTitle());
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));

        Product product = modelMapper.map(request, Product.class);
        product.setAuthor(user.getName());
        product.setUser(user);
        
        productRepository.save(product);
        
        ProductResponse response = modelMapper.map(product, ProductResponse.class);
        
        return ApiResponse.<ProductResponse>builder()
                .code("00")
                .message("Success")
                .data(response)
                .build();
    }
    
    public ApiResponse<List<ProductPaginationResponse>> getProductPagination(ProductPaginationRequest request) {
        
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
        String category = normalize(f.get("category"));
        
        Specification<Product> spec = Specification.where(null);
        if (title != null) {
            spec = spec.and((root, q, cb)
                    -> cb.like(cb.lower(root.get("title")), "%" + title + "%"));
        }
        if (author != null) {
            spec = spec.and((root, q, cb)
                    -> cb.like(cb.lower(root.get("author")), "%" + author + "%"));
        }
        if (category != null) {
            spec = spec.and((root, q, cb)
                    -> cb.like(cb.lower(root.get("category")), "%" + category + "%"));
        }
        
        Page<Product> pageResult = productRepository.findAll(spec, pageable);
        
        if (pageResult.isEmpty()) {
            throw new DataNotFoundException("Data Not Found");
        }
        
        List<ProductPaginationResponse> items = pageResult.getContent().stream()
                .map(u -> ProductPaginationResponse.builder()
                .id(u.getId())
                .title(u.getTitle())
                .category(u.getCategory())
                .description(u.getDescription())
                .author(u.getAuthor())
                .createdAt(u.getCreatedAt())
                .updateAt(u.getUpdateAt())
                .build())
                .collect(Collectors.toList());
        
        ApiResponse<List<ProductPaginationResponse>> response = new ApiResponse<>();
        response.setCode("00");
        response.setMessage("Success");
        response.setData(items);
        response.setPage(pageResult.getNumber() + 1);
        response.setTotalPages(pageResult.getTotalPages());
        response.setCountData((int) pageResult.getTotalElements());
        return response;
    }
    
    @Transactional
    public ApiResponse<ProductDetailResponse> productDetail(ProductDetailRequest request) {
        
        if (request.getId() == null) {
            throw new BadRequestException("Id is required");
        }
        
        Product product = productRepository.findById(request.getId())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));
        
        ProductDetailResponse response = modelMapper.map(product, ProductDetailResponse.class);
        
        return ApiResponse.<ProductDetailResponse>builder()
                .code("00")
                .message("Success")
                .data(response)
                .build();
        
    }
    
    @Transactional
    public ApiResponse<ProductEditResponse> editProduct(ProductEditRequest request, Authentication authentication) {
        
        String emai = authentication.getName();
        
        User user = userRepository.findByEmail(emai)
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));
        
        Product product = productRepository.findById(request.getId())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));
        
        if (!isBlank(request.getTitle())) {
            product.setTitle(request.getTitle());
        }
        
        if (!isBlank(request.getDescription())) {
            product.setDescription(request.getDescription());
        }
        
        if (!isBlank(request.getCategory())) {
            product.setCategory(request.getCategory());
        }
        
        product.setAuthor(user.getName());
        
        productRepository.save(product);
        
        ProductEditResponse response = modelMapper.map(product, ProductEditResponse.class);
        
        return ApiResponse.<ProductEditResponse>builder()
                .code("00")
                .message("Success")
                .data(response)
                .build();
    }
    
    @Transactional
    public ApiResponse<String> deleteProduct(ProductDeleteRequest request) {
        
        if (request.getId() == null) {
            throw new BadRequestException("Id is required");
        }
        
        Product product = productRepository.findById(request.getId())
                .orElseThrow(() -> new DataNotFoundException("Data Not Found"));
        
        productRepository.delete(product);
        
        return ApiResponse.<String>builder()
                .code("00")
                .message("Success Delete")
                .data(null)
                .build();
        
    }
}
