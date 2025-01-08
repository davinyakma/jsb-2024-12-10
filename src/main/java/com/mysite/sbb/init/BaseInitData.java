package com.mysite.sbb.init;

import com.mysite.sbb.category.Category;
import com.mysite.sbb.category.CategoryRepository;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionRepository;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class BaseInitData implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // PasswordEncoder를 주입받습니다.

    public BaseInitData(CategoryRepository categoryRepository, QuestionRepository questionRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // '미분류' 카테고리가 없으면 생성
        Optional<Category> optionalCategory = categoryRepository.findByName("미분류");
        Category defaultCategory;
        if (optionalCategory.isEmpty()) {
            defaultCategory = new Category();
            defaultCategory.setName("미분류");
            defaultCategory = categoryRepository.save(defaultCategory);
        } else {
            defaultCategory = optionalCategory.get();
        }

        // 기본 유저 생성 (작성자)
        SiteUser defaultUser = userRepository.findByusername("admin").orElseGet(() -> {
            SiteUser user = new SiteUser();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("admin123"));  // BCryptPasswordEncoder로 비밀번호 암호화
            user.setEmail("admin@example.com");
            return userRepository.save(user);
        });

        // 기본 유저 생성 (guest01 ~ guest05)
        for (int i = 1; i <= 5; i++) {
            String username = String.format("guest%02d", i); // guest01, guest02, ..., guest05
            String email = String.format("guest%02d@example.com", i); // guest01@example.com, ...
            String password = "guest123"; // 공통 비밀번호

            userRepository.findByusername(username).orElseGet(() -> {
                SiteUser user = new SiteUser();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(password)); // BCryptPasswordEncoder로 비밀번호 암호화
                user.setEmail(email);
                return userRepository.save(user);
            });
        }

        // 샘플 질문이 없는 경우 추가
        if (questionRepository.count() == 0) {
            for (int i = 1; i <= 12; i++) {
                Question question = new Question();
                question.setSubject("샘플 질문 " + i);
                question.setContent("샘플 내용 " + i);
                question.setCreateDate(LocalDateTime.now());
                question.setModifyDate(LocalDateTime.now()); // 수정 날짜 추가
                question.setAuthor(defaultUser); // 기본 작성자 추가
                question.setCategory(defaultCategory);
                questionRepository.save(question);
            }
        }
    }
}