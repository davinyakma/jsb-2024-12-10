package com.mysite.sbb;

import com.mysite.sbb.auth.AuthService;
import com.mysite.sbb.user.SiteUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SocialLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate(); // 테스트 환경에서 사용할 RestTemplate 빈을 정의
        }
    }

    @Test
    void testGoogleLoginRedirect() throws Exception {
        // Google 로그인 리디렉션 테스트
        mockMvc.perform(get("/auth/login/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login/oauth2/authorization/google*"));
    }

    @Test
    void testKakaoLoginRedirect() throws Exception {
        // Kakao 로그인 리디렉션 테스트
        mockMvc.perform(get("/auth/login/kakao"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login/oauth2/authorization/kakao*"));
    }

    @Test
    void testNaverLoginRedirect() throws Exception {
        // Naver 로그인 리디렉션 테스트
        mockMvc.perform(get("/auth/login/naver"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login/oauth2/authorization/naver*"));
    }

    @Test
    void testCallbackSuccess() throws Exception {
        // 소셜 로그인 콜백 성공 테스트
        String provider = "google";
        String code = "testCode";  // 이 코드가 실제로 Google OAuth2 인증 서버에서 받은 코드로 대체되어야 합니다.

        // 콜백 처리 성공 시, profile 페이지로 리디렉션됨
        when(authService.handleSocialMediaCallback(provider, code)).thenReturn(new SiteUser());  // 성공적인 콜백 처리 시나리오

        mockMvc.perform(get("/auth/callback/{provider}", provider)
                        .param("code", code))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/profile"));
    }

    @Test
    void testCallbackFailure() throws Exception {
        // 소셜 로그인 콜백 실패 테스트
        String provider = "google";
        String code = "testCode";

        // 콜백 처리 실패 시, 로그인 페이지로 리디렉션됨
        when(authService.handleSocialMediaCallback(provider, code)).thenThrow(new RuntimeException("로그인 실패"));

        mockMvc.perform(get("/auth/callback/{provider}", provider)
                        .param("code", code))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/login"));
    }
}