/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.dto.ProductDto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author alfia
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductPaginationRequest {

    private String sortBy;
    private String sortOrder;
    private String limit;
    private String page;
    private Map<String, String> filters;
}
