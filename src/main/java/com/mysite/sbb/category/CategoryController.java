package com.mysite.sbb.category;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        // 카테고리 목록 조회
        List<Category> categories = categoryService.getCategoryList();
        model.addAttribute("categories", categories); // 모델에 카테고리 목록 전달
        return "category_list"; // 카테고리 목록 HTML 파일 반환
    }

    // 카테고리 추가 페이지
    @GetMapping("/category/create")
    public String createCategoryForm() {
        return "category_create"; // 카테고리 추가 페이지 반환
    }

    // 카테고리 추가 처리
    @PostMapping("/category/create")
    public String createCategory(@RequestParam String name) {
        // 카테고리 추가
        categoryService.createCategory(name);
        return "redirect:/category/list"; // 카테고리 추가 후 목록 페이지로 리다이렉트
    }

    // 특정 카테고리의 질문 목록 페이지
    @GetMapping("/category/{id}")
    public String categoryQuestions(@PathVariable("id") Integer id,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    Model model) {
        // 페이지 번호가 음수일 경우 기본값 0으로 설정
        if (page < 0) {
            page = 0;
        }

        // 카테고리 조회
        Category category = categoryService.getCategoryById(id);
        if (category != null) {
            // 페이징 설정
            Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createDate"));
            // 카테고리에 속한 질문들 조회
            Page<Question> questions = questionService.getQuestionsByCategory(category, pageable);

            // 모델에 카테고리 및 질문 목록 전달
            model.addAttribute("category", category);
            model.addAttribute("questions", questions);
        } else {
            // 카테고리가 없을 경우 에러 메시지 전달
            model.addAttribute("error", "카테고리를 찾을 수 없습니다.");
        }
        return "category_questions"; // 질문 목록 HTML 파일 반환
    }
}