package com.mysite.sbb.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

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
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot_password_form";
    }

    // 비밀번호 찾기 처리
    @PostMapping("/forgot-password")
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
    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "change_password_form";
    }

    // 비밀번호 변경 처리
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmNewPassword") String confirmNewPassword,
            @AuthenticationPrincipal SiteUser currentUser, // 로그인된 사용자 정보
            Model model) {

        // 새 비밀번호와 확인 비밀번호가 일치하는지 확인
        if (!newPassword.equals(confirmNewPassword)) {
            model.addAttribute("error", "새 비밀번호가 일치하지 않습니다.");
            return "change_password_form";
        }

        try {
            // 서비스 계층에서 비밀번호 변경 로직 호출
            userService.changePassword(currentPassword, newPassword, currentUser.getEmail());
            model.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
            return "change_password_form"; // 리다이렉트 대신 메시지를 바로 표시
        } catch (Exception e) {
            // 비밀번호 변경 실패 시 오류 메시지 처리
            model.addAttribute("error", "비밀번호 변경에 실패했습니다. 현재 비밀번호를 확인해주세요.");
            return "change_password_form";
        }
    }
}