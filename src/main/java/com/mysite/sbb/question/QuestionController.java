package com.mysite.sbb.question;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerForm;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.category.Category;
import com.mysite.sbb.category.CategoryService;
import com.mysite.sbb.comment.Comment;
import com.mysite.sbb.comment.CommentForm;
import com.mysite.sbb.comment.CommentService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RequestMapping("/question")
@RequiredArgsConstructor
@Controller
public class QuestionController {

    private final QuestionService questionService;
    private final AnswerService answerService;
    private final CommentService commentService;
    private final UserService userService;
    private final CategoryService categoryService;

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw,
                       @RequestParam(value = "sortBy", defaultValue = "createDate") String sortBy,
                       @RequestParam(value = "categoryName", required = false) String categoryName) {

        Page<Question> paging;
        // 최근 답변과 댓글 가져오기
        List<Answer> recentAnswers = answerService.getRecentAnswers();
        List<Comment> recentComments = commentService.getRecentComments();

        if (categoryName != null && !categoryName.isEmpty()) {
            Category category = categoryService.getCategoryByName(categoryName);
            paging = this.questionService.getQuestionsByKeywordAndCategory(kw, category, page);
            model.addAttribute("categoryName", categoryName);
        } else {
            paging = this.questionService.getList(page, kw, sortBy);
        }

        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("recentAnswers", recentAnswers);
        model.addAttribute("recentComments", recentComments);

        return "question_list";
    }

    @GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, AnswerForm answerForm, CommentForm commentForm,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "sortBy", defaultValue = "latest") String sortBy) {
        if (page < 0) {
            page = 0; // 음수 페이지 요청에 대한 기본값 설정
        }
        Question question = this.questionService.getQuestion(id);

        // 조회수 증가 (서비스 메소드에서 처리)
        this.questionService.incrementViewCount(id);

        Page<Answer> answerPaging = answerService.getAnswersByQuestionId(id, page, sortBy);
        Page<Comment> commentPaging = commentService.getCommentsByQuestionId(id, page, sortBy);
        model.addAttribute("question", question);
        model.addAttribute("answerPaging", answerPaging);
        model.addAttribute("commentPaging", commentPaging);
        model.addAttribute("sortBy", sortBy); // sortBy 파라미터 추가
        return "question_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(@RequestParam(required = false) Integer categoryId, Model model) {
        List<Category> categories = categoryService.getAllCategories(); // 모든 카테고리 가져오기
        model.addAttribute("categories", categories); // 카테고리 리스트 추가
        model.addAttribute("questionForm", new QuestionForm());

        if (categoryId != null) {
            Category category = categoryService.getCategoryById(categoryId);
            model.addAttribute("category", category);
        }
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(
            @Valid QuestionForm questionForm,
            BindingResult bindingResult,
            Principal principal,
            @RequestParam(required = false) Integer categoryId) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }

        SiteUser siteUser = this.userService.getUser(principal.getName());
        Category category;

        // 사용자가 카테고리를 선택하지 않은 경우 "미분류" 카테고리 설정
        if (categoryId == null || categoryId == 0) {
            category = categoryService.getDefaultCategory(); // "미분류" 카테고리 가져오기
        } else {
            category = categoryService.getCategoryById(categoryId);
        }

        this.questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser, category);
        return "redirect:/question/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm, @PathVariable("id") Integer id, Principal principal, Model model) {
        Question question = this.questionService.getQuestion(id);

        // 수정 권한 체크
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        // 기존 질문 정보 설정
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        questionForm.setCategoryId(Long.valueOf(question.getCategory().getId()));  // 기존 카테고리 ID 설정

        // 카테고리 목록을 모델에 추가
        model.addAttribute("categories", this.categoryService.getAllCategories());

        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult,
                                 Principal principal, @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }

        Question question = this.questionService.getQuestion(id);

        // 수정 권한 체크
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        // 수정된 질문 데이터 적용
        Category category = this.categoryService.getCategoryById(Math.toIntExact(questionForm.getCategoryId()));

        // questionService.modify 호출 시 카테고리까지 전달
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent(), category);

        // 수정된 질문 저장 후 상세 페이지로 리다이렉트
        return String.format("redirect:/question/detail/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }
}