package com.mysite.sbb.answer;

import com.mysite.sbb.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    Page<Answer> findAll(Pageable pageable);
    Page<Answer> findByQuestionId(Integer questionId, Pageable pageable);

    // 사용자별 답변을 가져오는 메소드
    List<Answer> findByAuthor(SiteUser author);
}