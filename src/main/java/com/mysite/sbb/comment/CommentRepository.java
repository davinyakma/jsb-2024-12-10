package com.mysite.sbb.comment;

import com.mysite.sbb.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByQuestionId(Integer questionId, Pageable pageable);

    // 사용자별 댓글을 가져오는 쿼리 메소드
    List<Comment> findByAuthor(SiteUser author);
}
