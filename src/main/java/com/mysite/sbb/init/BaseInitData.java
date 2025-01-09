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

@Component
public class BaseInitData implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public BaseInitData(CategoryRepository categoryRepository, QuestionRepository questionRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // '미분류' 카테고리가 없으면 생성
        Category defaultCategory = categoryRepository.findByName("미분류")
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName("미분류");
                    return categoryRepository.save(category);
                });

        // 기본 유저 생성 (admin)
        userRepository.findByusername("admin").orElseGet(() -> {
            SiteUser user = new SiteUser();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("admin123!")); // 고유 비밀번호
            user.setEmail("admin@example.com");
            return userRepository.save(user);
        });

        // guest01 ~ guest05 생성
        for (int i = 1; i <= 5; i++) {
            String username = String.format("guest%02d", i);
            String email = String.format("guest%02d@example.com", i);
            String password = String.format("guest%02d!", i); // 고유 비밀번호

            userRepository.findByusername(username).orElseGet(() -> {
                SiteUser user = new SiteUser();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(password));
                user.setEmail(email);
                return userRepository.save(user);
            });
        }

        // guest06 생성
        userRepository.findByusername("guest06").orElseGet(() -> {
            SiteUser user = new SiteUser();
            user.setUsername("guest06");
            user.setPassword(passwordEncoder.encode("guest06!"));
            user.setEmail("yakmatwin3652@gmail.com");
            return userRepository.save(user);
        });

        // guest07 생성
        userRepository.findByusername("guest07").orElseGet(() -> {
            SiteUser user = new SiteUser();
            user.setUsername("guest07");
            user.setPassword(passwordEncoder.encode("guest07!"));
            user.setEmail("poohcdv3652@naver.com");
            return userRepository.save(user);
        });

        // 샘플 질문 생성
        if (questionRepository.count() == 0) {
            for (int i = 1; i <= 12; i++) {
                Question question = new Question();
                question.setSubject("샘플 질문 " + i);
                question.setContent("샘플 내용 " + i);
                question.setCreateDate(LocalDateTime.now());
                question.setModifyDate(LocalDateTime.now());
                question.setAuthor(userRepository.findByusername("admin").orElse(null)); // 기본 작성자
                question.setCategory(defaultCategory);
                questionRepository.save(question);
            }
        }
    }
}