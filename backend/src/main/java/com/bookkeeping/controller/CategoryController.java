package com.bookkeeping.controller;

import com.bookkeeping.entity.Category;
import com.bookkeeping.service.CategoryService;
import com.bookkeeping.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> getUserCategories(@RequestParam Long userId) {
        return Result.success(categoryService.getUserCategories(userId));
    }

    @PostMapping
    public Result<Category> createCategory(@RequestBody Category category) {
        return Result.success(categoryService.createCategory(category));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success(null);
    }
}
