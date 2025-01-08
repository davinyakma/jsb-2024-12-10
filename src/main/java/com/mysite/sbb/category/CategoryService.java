package com.mysite.sbb.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // 모든 카테고리 목록 반환
    public List<Category> getCategoryList() {
        return categoryRepository.findAll();
    }

    // 카테고리 ID로 카테고리 반환
    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name).orElseThrow(() ->
                new IllegalArgumentException("Category with name " + name + " not found")
        );
    }

    // 카테고리 생성
    public void createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        categoryRepository.save(category);
    }

    public Category getDefaultCategory() {
        return categoryRepository.findByName("미분류")
                .orElseThrow(() -> new RuntimeException("Default category '미분류' not found"));
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}