package com.project.shopapp.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProductListResponse {
    private List<ProductResponse> products;
    private int totalPage;
}
