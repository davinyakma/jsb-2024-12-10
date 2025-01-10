package com.mysite.sbb.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration")
public class SocialProperties {

    private Registration google;
    private Registration kakao;
    private Registration naver;

    // Google, Kakao, Naver 각각의 clientId, scope 등을 가져오는 메소드
    public String getGoogleClientId() {
        return google != null ? google.getClientId() : null;
    }

    public String getGoogleScope() {
        return google != null ? google.getScope() : null;
    }

    public String getKakaoClientId() {
        return kakao != null ? kakao.getClientId() : null;
    }

    public String getNaverClientId() {
        return naver != null ? naver.getClientId() : null;
    }

    @Getter
    @Setter
    public static class Registration {
        private String clientId;
        private String clientSecret;
        private String scope;
        private String redirectUri;
        private String authorizationGrantType;
        private String clientName;

        // Getter와 Setter는 @Getter, @Setter로 자동 생성됨
    }
}