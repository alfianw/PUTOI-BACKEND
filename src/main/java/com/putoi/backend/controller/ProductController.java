/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.controller;

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
import com.putoi.backend.service.ProductService;
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
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasAuthority('superadmin')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@RequestBody ProductRequest request, Authentication authentication) {
        return ResponseEntity.ok(productService.createProduct(request, authentication));
    }

    @PostMapping("/pagination")
    public ResponseEntity<ApiResponse<List<ProductPaginationResponse>>> getProductPagination(@RequestBody ProductPaginationRequest request) {
        return ResponseEntity.ok(productService.getProductPagination(request));
    }

    @PostMapping("/detail")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> productDetail(@RequestBody ProductDetailRequest request) {
        return ResponseEntity.ok(productService.productDetail(request));
    }

    @PreAuthorize("hasAuthority('superadmin')")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<ProductEditResponse>> editProduct(@RequestBody ProductEditRequest request, Authentication authentication) {
        return ResponseEntity.ok(productService.editProduct(request, authentication));
    }

    @PreAuthorize("hasAuthority('superadmin')")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@RequestBody ProductDeleteRequest request) {
        return ResponseEntity.ok(productService.deleteProduct(request));
    }
}
