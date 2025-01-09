package com.mysite.sbb.user;

import com.mysite.sbb.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // 이메일 전송 서비스
    // 로그를 위한 Logger 객체 생성
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public SiteUser create(String username, String email, String password) {
        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(user);
        return user;
    }

    public SiteUser getUser(String username) {
        Optional<SiteUser> siteUser = this.userRepository.findByusername(username);
        if (siteUser.isPresent()) {
            return siteUser.get();
        } else {
            throw new DataNotFoundException("siteuser not found");
        }
    }

    // 임시 비밀번호 전송
    @Transactional
    public void sendTemporaryPassword(String email) {
        // 이메일로 사용자 조회
        Optional<SiteUser> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.error("임시 비밀번호 전송 실패: 등록되지 않은 이메일({})입니다.", email);
            throw new IllegalArgumentException("등록되지 않은 이메일입니다.");
        }

        // 임시 비밀번호 생성
        String temporaryPassword = generateTemporaryPassword();
        String encodedPassword = passwordEncoder.encode(temporaryPassword);

        // 비밀번호 업데이트
        userRepository.updatePasswordByEmail(email, encodedPassword);

        // 이메일 전송
        try {
            emailService.sendEmail(email, "임시 비밀번호 안내",
                    "임시 비밀번호: " + temporaryPassword + "\n로그인 후 즉시 변경해주세요.");
            logger.info("임시 비밀번호가 이메일({})로 전송되었습니다.", email);
        } catch (Exception e) {
            logger.error("이메일 전송 실패: {}", e.getMessage());
            throw new RuntimeException("임시 비밀번호 전송에 실패했습니다. 다시 시도해주세요.");
        }
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(String currentPassword, String newPassword, String email) {
        // 이메일로 사용자 조회
        Optional<SiteUser> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        SiteUser user = userOptional.get();

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        // 새 비밀번호 암호화 및 저장
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // 임시 비밀번호 생성 메서드
    private String generateTemporaryPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom(); // 보안적으로 더 안전한 랜덤 생성기
        StringBuilder temporaryPassword = new StringBuilder();

        for (int i = 0; i < 12; i++) { // 12자리 임시 비밀번호
            temporaryPassword.append(characters.charAt(random.nextInt(characters.length())));
        }

        return temporaryPassword.toString();
    }
}