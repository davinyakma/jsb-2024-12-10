package com.mysite.sbb;

import com.mysite.sbb.category.Category;
import com.mysite.sbb.category.CategoryRepository;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionRepository;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserRepository;
import com.mysite.sbb.user.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
class SbbApplicationTests {

	@Autowired
	private QuestionService questionService;
	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserService userService;  // UserService에 있는 sendTemporaryPassword 메소드 사용

	@Autowired
	private JavaMailSender mailSender;  // 이메일 전송을 위한 JavaMailSender

    @Test
	void testJpa() {
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
			user.setPassword(passwordEncoder.encode("admin123"));
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

		// 샘플 질문이 없는 경우 추가
		if (questionRepository.count() == 0) {
			for (int i = 1; i <= 12; i++) {
				Question question = new Question();
				question.setSubject("샘플 질문 " + i);
				question.setContent("샘플 내용 " + i);
				question.setCreateDate(LocalDateTime.now());
				question.setModifyDate(LocalDateTime.now());
				question.setAuthor(defaultUser);
				question.setCategory(defaultCategory);
				questionRepository.save(question);
			}
		}
	}

	// 이메일 전송 테스트 메소드
	@Test
	void testSendEmail() throws MessagingException, MessagingException {
		// 이메일 전송 대상
		String toEmail = "yakmatwin3652@gmail.com";
		String subject = "테스트 이메일 제목";
		String text = "이것은 테스트 이메일입니다.";

		// JavaMailSender를 사용하여 이메일 발송 테스트
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
		helper.setFrom("yakmatwin3652@gmail.com");  // 보낸 사람
		helper.setTo(toEmail);  // 받는 사람
		helper.setSubject(subject);  // 제목
		helper.setText(text);  // 내용

		// 실제 이메일 전송
		mailSender.send(mimeMessage);

		// 테스트 결과를 로그에 남기거나 이메일 발송 성공을 확인할 수 있는 방법을 추가
		System.out.println("테스트 이메일이 성공적으로 발송되었습니다.");
	}

	// 임시 비밀번호 전송 테스트 메소드
	@Test
	void testSendTemporaryPassword() {
		String email = "yakmatwin3652@gmail.com";  // 이메일 주소 입력

		try {
			// 서비스 계층에서 임시 비밀번호 전송
			userService.sendTemporaryPassword(email);
			System.out.println("임시 비밀번호가 이메일로 발송되었습니다.");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("임시 비밀번호 전송에 실패했습니다.");
		}
	}
}