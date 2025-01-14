package com.mysite.sbb.answer;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AnswerService {

    private final AnswerRepository answerRepository;

    public Page<Answer> getList(int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return this.answerRepository.findAll(pageable);
    }

    public Page<Answer> getAnswersByQuestionId(Integer questionId, int page, String sortBy) {
        int pageSize = 10; // 기본 페이지 크기 설정
        Pageable pageable;

        if ("recommend".equals(sortBy)) {
            pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("voter"))); // 추천순
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("createDate"))); // 최신순
        }

        return answerRepository.findByQuestionId(questionId, pageable);
    }

    public Answer create(Question question, String content, SiteUser author) {
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setCreateDate(LocalDateTime.now());
        answer.setQuestion(question);
        answer.setAuthor(author);
        this.answerRepository.save(answer);
        return answer;
    }

    public Answer getAnswer(Integer id) {
        Optional<Answer> answer = this.answerRepository.findById(id);
        if (answer.isPresent()) {
            return answer.get();
        } else {
            throw new DataNotFoundException("answer not found");
        }
    }

    public void modify(Answer answer, String content) {
        answer.setContent(content);
        answer.setModifyDate(LocalDateTime.now());
        this.answerRepository.save(answer);
    }

    public void delete(Answer answer) {
        this.answerRepository.delete(answer);
    }

    @Transactional
    public void vote(Answer answer, SiteUser siteUser) {
        answer.getVoter().add(siteUser);
        this.answerRepository.save(answer);
    }

    // 사용자가 작성한 답변 목록을 가져오는 메소드
    public List<Answer> getAnswersByAuthor(SiteUser user) {
        return answerRepository.findByAuthor(user);
    }

    // 최근 5개 답변 가져오기
    public List<Answer> getRecentAnswers() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Order.desc("createDate")));
        return answerRepository.findRecentAnswers(pageable);
    }
}
