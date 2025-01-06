package com.mysite.sbb.category;

import com.mysite.sbb.question.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private QuestionService questionService;

    // 카테고리 목록 페이지
    @GetMapping("/category/list")
    public String categoryList(Model model) {
        List<Category> categories = categoryService.getCategoryList();
        model.addAttribute("categories", categories); // 데이터 바인딩 확인
        return "category_list";
    }

    // 카테고리 생성 페이지
    @GetMapping("/category/create")
    public String categoryCreate() {
        return "category_create";
    }

    // 카테고리 저장
    @PostMapping("/category/create")
    public String categorySave(@RequestParam String name) {
        categoryService.createCategory(name);
        return "redirect:/category/list"; // 카테고리 저장 후 목록으로 리다이렉트
    }

    // 특정 카테고리의 질문 목록 페이지
    @GetMapping("/category/{id}")
    public String categoryQuestions(@PathVariable("id") Integer id, Model model) {
        Category category = categoryService.getCategoryById(id);
        if (category != null) {
            model.addAttribute("category", category);
            model.addAttribute("questions", questionService.getQuestionsByCategory(category));
        }
        return "category_questions";
    }
}