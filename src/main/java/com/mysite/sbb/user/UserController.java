package com.mysite.sbb.user;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.comment.Comment;
import com.mysite.sbb.comment.CommentService;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private CommentService commentService;

    // 회원가입 폼
    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }

    // 로그인 폼
    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }

        try {
            userService.create(userCreateForm.getUsername(),
                    userCreateForm.getEmail(), userCreateForm.getPassword1());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup_form";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }

        return "redirect:/";
    }

    // 비밀번호 찾기 폼
    @GetMapping("/forgot_password")
    public String forgotPasswordForm() {
        return "forgot_password_form";
    }

    // 비밀번호 찾기 처리
    @PostMapping("/forgot_password")
    public String forgotPassword(@RequestParam("email") String email, Model model) {
        try {
            userService.sendTemporaryPassword(email); // 서비스 계층에서 임시 비밀번호 전송 로직
            model.addAttribute("message", "임시 비밀번호가 이메일로 발송되었습니다.");
            return "redirect:/user/login";
        } catch (Exception e) {
            model.addAttribute("error", "비밀번호를 찾을 수 없습니다. 이메일을 확인해주세요.");
            return "forgot_password_form";
        }
    }

    // 비밀번호 변경 폼
    @GetMapping("/change_password")
    public String changePasswordForm() {
        return "change_password_form";
    }

    // 비밀번호 변경 처리
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change_password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmNewPassword") String confirmNewPassword,
            @AuthenticationPrincipal SiteUser currentUser,
            Model model) {

        // 새 비밀번호와 확인 비밀번호가 일치하는지 확인
        if (!newPassword.equals(confirmNewPassword)) {
            model.addAttribute("error", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            return "change_password_form";
        }

        try {
            // 서비스 계층에서 비밀번호 변경 로직 호출
            userService.changePassword(currentUser, currentPassword, newPassword);
            model.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");

            // 비밀번호 변경 후 success 페이지로 리디렉션
            return "redirect:/user/change_password_success"; // change_password_success 페이지로 리디렉션
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "change_password_form";
        } catch (Exception e) {
            model.addAttribute("error", "비밀번호 변경 중 문제가 발생했습니다.");
            return "change_password_form";
        }
    }

    @GetMapping("/change_password_success")
    public String changePasswordSuccess() {
        return "change_password_success"; // change_password_success 페이지로 이동
    }

    @GetMapping("/profile")
    public String getUserProfile(Model model, Authentication authentication) {
        // 로그인한 사용자 정보 가져오기
        String username = authentication.getName();
        SiteUser user = userService.getUserByUsername(username);

        // 사용자가 작성한 질문, 답변, 댓글 가져오기
        List<Question> userQuestions = questionService.getQuestionsByUser(user);
        List<Answer> userAnswers = answerService.getAnswersByAuthor(user);
        List<Comment> userComments = commentService.getCommentsByAuthor(user);

        // 모델에 데이터 추가
        model.addAttribute("user", user);
        model.addAttribute("userQuestions", userQuestions);
        model.addAttribute("userAnswers", userAnswers);
        model.addAttribute("userComments", userComments);

        return "user_profile"; // 프로필 화면을 렌더링
    }
}