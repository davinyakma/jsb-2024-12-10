package com.mysite.sbb.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserService userService;
    private final SocialProperties socialProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();  // Jackson ObjectMapper 인스턴스

    public String authenticateWithSocialMedia(String provider) {
        if ("google".equals(provider)) {
            String redirectUri = "http://localhost:8080/login/oauth2/code/google";
            String googleAuthUrl = "https://accounts.google.com/o/oauth2/auth?client_id=" + socialProperties.getGoogle().getClientId() +
                    "&redirect_uri=" + redirectUri +
                    "&response_type=code&scope=" + socialProperties.getGoogle().getScope();
            return "redirect:" + googleAuthUrl;
        } else if ("kakao".equals(provider)) {
            String redirectUri = "http://localhost:8080/login/oauth2/code/kakao";
            String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize?client_id=" + socialProperties.getKakao().getClientId() +
                    "&redirect_uri=" + redirectUri + "&response_type=code";
            return "redirect:" + kakaoAuthUrl;
        } else if ("naver".equals(provider)) {
            String redirectUri = "http://localhost:8080/login/oauth2/code/naver";
            String naverAuthUrl = "https://nid.naver.com/oauth2.0/authorize?client_id=" + socialProperties.getNaver().getClientId() +
                    "&redirect_uri=" + redirectUri + "&response_type=code";
            return "redirect:" + naverAuthUrl;
        }
        return "redirect:/user/login";
    }

    public SiteUser handleSocialMediaCallback(String provider, String code) {
        if ("google".equals(provider)) {
            String accessToken = getGoogleAccessToken(code);
            return getGoogleUserInfo(accessToken);
        } else if ("kakao".equals(provider)) {
            String accessToken = getKakaoAccessToken(code);
            return getKakaoUserInfo(accessToken);
        } else if ("naver".equals(provider)) {
            String accessToken = getNaverAccessToken(code);
            return getNaverUserInfo(accessToken);
        }
        throw new IllegalArgumentException("지원하지 않는 소셜 미디어입니다.");
    }

    private String getGoogleAccessToken(String code) {
        String googleTokenUrl = "https://oauth2.googleapis.com/token";
        String response = new RestTemplate().postForObject(googleTokenUrl, code, String.class);
        try {
            JsonNode tokenResponse = objectMapper.readTree(response);
            return tokenResponse.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Google access token parsing 실패", e);
        }
    }

    private SiteUser getGoogleUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken;
        String response = new RestTemplate().getForObject(userInfoUrl, String.class);
        try {
            JsonNode userInfo = objectMapper.readTree(response);
            String email = userInfo.get("email").asText();
            String username = userInfo.get("name").asText();

            SiteUser user = new SiteUser();
            user.setEmail(email);
            user.setUsername(username);
            return userService.create(user.getUsername(), user.getEmail(), "default_password");
        } catch (Exception e) {
            throw new RuntimeException("Google user info parsing 실패", e);
        }
    }

    private String getKakaoAccessToken(String code) {
        String kakaoTokenUrl = "https://kauth.kakao.com/oauth/token";
        String response = new RestTemplate().postForObject(kakaoTokenUrl, code, String.class);
        try {
            JsonNode tokenResponse = objectMapper.readTree(response);
            return tokenResponse.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Kakao access token parsing 실패", e);
        }
    }

    private SiteUser getKakaoUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me?access_token=" + accessToken;
        String response = new RestTemplate().getForObject(userInfoUrl, String.class);
        try {
            JsonNode userInfo = objectMapper.readTree(response);
            String email = userInfo.get("kakao_account").get("email").asText();  // 이메일 실제 응답 구조에 맞게 수정
            String username = userInfo.get("properties").get("nickname").asText();  // 닉네임

            SiteUser user = new SiteUser();
            user.setEmail(email);
            user.setUsername(username);
            return userService.create(user.getUsername(), user.getEmail(), "default_password");
        } catch (Exception e) {
            throw new RuntimeException("Kakao user info parsing 실패", e);
        }
    }

    private String getNaverAccessToken(String code) {
        String naverTokenUrl = "https://nid.naver.com/oauth2.0/token";
        String response = new RestTemplate().postForObject(naverTokenUrl, code, String.class);
        try {
            JsonNode tokenResponse = objectMapper.readTree(response);
            return tokenResponse.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Naver access token parsing 실패", e);
        }
    }

    private SiteUser getNaverUserInfo(String accessToken) {
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me?access_token=" + accessToken;
        String response = new RestTemplate().getForObject(userInfoUrl, String.class);
        try {
            JsonNode userInfo = objectMapper.readTree(response).get("response");
            String email = userInfo.get("email").asText();
            String username = userInfo.get("name").asText();

            SiteUser user = new SiteUser();
            user.setEmail(email);
            user.setUsername(username);
            return userService.create(user.getUsername(), user.getEmail(), "default_password");
        } catch (Exception e) {
            throw new RuntimeException("Naver user info parsing 실패", e);
        }
    }
}