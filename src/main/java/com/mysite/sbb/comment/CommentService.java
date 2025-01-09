package com.mysite.sbb.comment;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentService {

    @Autowired
    private final CommentRepository commentRepository;

    public Page<Comment> getCommentsByQuestionId(Integer questionId, int page, String sortBy) {
        int pageSize = 10; // 기본 페이지 크기 설정
        Pageable pageable;

        if ("recommend".equals(sortBy)) {
            pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("voter"))); // 추천순
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("createDate"))); // 최신순
        }

        return commentRepository.findByQuestionId(questionId, pageable);
    }

    public Comment create(Question question, Answer answer, String content, SiteUser author) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreateDate(LocalDateTime.now());
        comment.setAuthor(author);
        comment.setQuestion(question);
        comment.setAnswer(answer);
        return this.commentRepository.save(comment);
    }

    public Comment getComment(Integer id) {
        Optional<Comment> comment = this.commentRepository.findById(Long.valueOf(id));
        if (comment.isPresent()) {
            return comment.get();
        } else {
            throw new DataNotFoundException("answer not found");
        }
    }

    public void modify(Comment comment, String content) {
        comment.setContent(content);
        comment.setModifyDate(LocalDateTime.now());
        this.commentRepository.save(comment);
    }

    public void delete(Comment comment) {
        this.commentRepository.delete(comment);
    }

    @Transactional
    public void vote(Comment comment, SiteUser siteUser) {
        comment.getVoter().add(siteUser);
        this.commentRepository.save(comment);
    }

    // 사용자가 작성한 댓글 목록을 가져오는 메소드
    public List<Comment> getCommentsByAuthor(SiteUser user) {
        return commentRepository.findByAuthor(user);
    }

    // 최근 5개 댓글 가져오기
    public List<Comment> getRecentComments() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Order.desc("createDate")));
        return commentRepository.findRecentComments(pageable);
    }
}
