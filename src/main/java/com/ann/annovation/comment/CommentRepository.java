package com.ann.annovation.comment;

import com.ann.annovation.answer.Answer;
import com.ann.annovation.question.Question;
import com.ann.annovation.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByQuestionAndAnswerIsNull(Question question);
    List<Comment> findByAnswer(Answer answer);
    Page<Comment> findByAuthor(SiteUser siteUser, Pageable pageable);
}
