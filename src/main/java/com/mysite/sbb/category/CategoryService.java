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

    // 카테고리 생성
    public void createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        categoryRepository.save(category);
    }
}