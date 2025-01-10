package com.ann.annovation.question;

import com.ann.annovation.DataNotFoundException;
import com.ann.annovation.answer.Answer;
import com.ann.annovation.category.Category;
import com.ann.annovation.user.SiteUser;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public Page<Question> getList(int page, String kw, String sort) {
        List<Sort.Order> sorts = new ArrayList<>();

        // 정렬 조건 설정
        if ("latest".equals(sort)) {
            // 답변의 최신 작성일시를 기준으로 정렬
            sorts.add(Sort.Order.desc("answerList.createDate"));
        } else if ("comment".equals(sort)) {
            // 댓글의 최신 작성일시를 기준으로 정렬
            sorts.add(Sort.Order.desc("commentList.createDate"));
        } else {
            // 기본 정렬 (질문 작성일시 기준)
            sorts.add(Sort.Order.desc("createDate"));
        }

        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        // 검색어를 의미하는 매개변수 kw를 getList 메서드에 추가하고 kw값으로 Specification 객체를 생성하여 findAll 메서드 호출 시 전달했다.
        Specification<Question> spec = search(kw);
        return this.questionRepository.findAll(spec, pageable);
    }

    public Question getQuestion(Integer id) {
        Optional<Question> question = this.questionRepository.findById(id);
        if (question.isPresent()) {
            return question.get();
        } else {
            throw new DataNotFoundException("question not found");
        }
    }

    public void create(String subject, String content, Category category, SiteUser siteuser) {
        Question q = new Question();
        q.setSubject(subject);
        q.setContent(content);
        q.setAuthor(siteuser);
        q.setCategory(category);
        q.setCreateDate(LocalDateTime.now());
        this.questionRepository.save(q);
    }

    public void modify(Question question, String subject, String content) {
        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        this.questionRepository.save(question);
    }

    public void delete(Question question) {
        this.questionRepository.delete(question);
    }

    public void vote(Question question, SiteUser siteUser) {
        question.getVoter().add(siteUser);
        this.questionRepository.save(question);
    }

    public Page<Question> getCategoryQuestionList(Category category, int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return this.questionRepository.findByCategory(category, pageable);
    }

    public Page<Question> getListByAuthor(int page, SiteUser siteUser) {
        List<Sort.Order> sorts = new ArrayList();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));
        return this.questionRepository.findByAuthor(siteUser, pageable);
    }

    // 특정 사용자가 추천한 질문들을 찾기 위한 Method
    public Specification<Question> hasVoter(SiteUser siteUser) {
        // Specification 인터페이스를 구현하는 익명 클래스 생성
        return new Specification<Question>() {
            private static final long serialVersionUID = 1L;

            // 실제 검색 조건을 만드는 Method
            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);
                // voter 목록에 특정 사용자(siteUser)가 포함되어 있는지 확인
                return cb.isMember(siteUser, q.get("voter"));
            }
        };
    }

    public Page<Question> getListByVoter(int page, SiteUser siteUser) {
        List<Sort.Order> sorts = new ArrayList();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));
        Specification<Question> spec = this.hasVoter(siteUser);
        return this.questionRepository.findAll(spec, pageable);
    }
}
