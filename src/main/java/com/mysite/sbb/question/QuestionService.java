package com.mysite.sbb.question;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.category.Category;
import com.mysite.sbb.user.SiteUser;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    private Specification<Question> search(String kw) {
        return new Specification<>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);  // 중복을 제거
                Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
                Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
                Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
                return cb.or(cb.like(q.get("subject"), "%" + kw + "%"), // 제목
                        cb.like(q.get("content"), "%" + kw + "%"),      // 내용
                        cb.like(u1.get("username"), "%" + kw + "%"),    // 질문 작성자
                        cb.like(a.get("content"), "%" + kw + "%"),      // 답변 내용
                        cb.like(u2.get("username"), "%" + kw + "%"));   // 답변 작성자
            }
        };
    }

    public Question getQuestion(Integer id) {
        Optional<Question> question = this.questionRepository.findById(id);
        if (question.isPresent()) {
            return question.get();
        } else {
            throw new DataNotFoundException("question not found");
        }
    }

    public Page<Question> getList(int page, String kw, String sortBy) {
        Pageable pageable = PageRequest.of(page, 10);

        // sortBy에 따라 다른 정렬 기준 설정
        if ("recommend".equals(sortBy)) {
            return this.questionRepository.findAllByKeywordOrderByVoter(kw, pageable);
        } else {
            return this.questionRepository.findAllByKeyword(kw, pageable);
        }
    }

    public void create(String subject, String content, SiteUser user, Category category) {
        Question q = new Question();
        q.setSubject(subject);
        q.setContent(content);
        q.setCreateDate(LocalDateTime.now());
        q.setAuthor(user);
        q.setCategory(category);
        this.questionRepository.save(q);
    }

    public void modify(Question question, String subject, String content, Category category) {
        question.setSubject(subject);
        question.setContent(content);
        question.setCategory(category);
        question.setModifyDate(LocalDateTime.now());
        this.questionRepository.save(question);
    }

    public void delete(Question question) {
        this.questionRepository.delete(question);
    }

    @Transactional
    public void vote(Question question, SiteUser siteUser) {
        question.getVoter().add(siteUser);
        this.questionRepository.save(question);
    }

    // 카테고리별 질문 목록 조회
    public Page<Question> getQuestionsByCategory(Category category, Pageable pageable) {
        return this.questionRepository.findByCategory(category, pageable);
    }

    // 키워드와 카테고리로 검색
    public Page<Question> getQuestionsByKeywordAndCategory(String keyword, Category category, int page) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createDate"));
        return this.questionRepository.findByKeywordAndCategory(keyword, category, pageable);
    }

    // 사용자가 작성한 질문 목록을 가져오는 메소드
    public List<Question> getQuestionsByUser(SiteUser user) {
        return questionRepository.findByAuthor(user);  // 'author'를 기준으로 변경
    }

    // 조회수 증가 메소드
    @Transactional
    public void incrementViewCount(Integer id) {
        // 조회수 증가
        questionRepository.incrementViewCount(id);
    }
}