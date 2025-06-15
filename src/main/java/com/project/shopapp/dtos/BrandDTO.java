package com.project.shopapp.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BrandDTO {
    @NotEmpty(message = "brand`s name cannot be empty")
    private String name;
}
