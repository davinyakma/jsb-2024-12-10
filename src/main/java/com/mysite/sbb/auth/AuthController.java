package com.mysite.sbb.auth;

import com.mysite.sbb.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private final AuthService authService;

    // 소셜 로그인 시작 (Google, Kakao 등)
    @GetMapping("/login/{provider}")
    public String login(@PathVariable String provider) {
        // Spring Security에서 제공하는 OAuth2 로그인 URL로 리디렉션
        return "redirect:/login/oauth2/authorization/" + provider;
    }

    // 소셜 로그인 콜백 처리
    @GetMapping("/callback/{provider}")
    public String callback(@PathVariable String provider, @RequestParam String code, Model model) {
        try {
            // 콜백을 처리하여 로그인 후 리디렉션
            SiteUser siteUser = authService.handleSocialMediaCallback(provider, code);
            model.addAttribute("message", "로그인 성공");
            return "redirect:/user/profile";
        } catch (Exception e) {
            model.addAttribute("error", "소셜 로그인에 실패했습니다.");
            return "redirect:/user/login";
        }
    }
}