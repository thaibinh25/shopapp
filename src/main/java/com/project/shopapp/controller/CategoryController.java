package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.models.Category;
import com.project.shopapp.response.UpdateCategoryResponse;
import com.project.shopapp.services.CategoryService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final LocalizationUtils localizationUtils;

    @GetMapping("") //http://localhost:8088/api/v1/categories?page=1&limit=10
    public ResponseEntity<List<Category>> getAllCategory(
            @RequestParam("page")   int page,
            @RequestParam("limit")  int limit
    ){
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("")
    @Transactional
    public ResponseEntity<?> creatCategory(@Valid @RequestBody CategoryDTO categoryDTO,
                                                BindingResult result){
        if (result.hasErrors()){
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(errorMessages);
        }
        categoryService.createCategory(categoryDTO);
        return ResponseEntity.ok(localizationUtils.getLocalizedMessage(MessageKeys.INSERT_CATEGORY_SUCCESSFULLY));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<UpdateCategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryDTO categoryDTO){
        categoryService.updateCategory(id,categoryDTO);
        return ResponseEntity.ok(UpdateCategoryResponse.builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_CATEGORY_SUCCESSFULLY));
    }
}
