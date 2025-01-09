package com.ann.annovation.comment;

import com.ann.annovation.DataNotFoundException;
import com.ann.annovation.answer.Answer;
import com.ann.annovation.question.Question;
import com.ann.annovation.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public Comment getComment(Integer id) {
        Optional<Comment> comment = this.commentRepository.findById(id);
        if (comment.isPresent()) {
            return comment.get();
        } else {
            throw new DataNotFoundException("comment not found");
        }
    }

    public Comment create(String content, Question question, Answer answer,
                          SiteUser siteUser) {
        Comment c = new Comment();
        c.setContent(content);
        c.setQuestion(question);
        c.setAnswer(answer);
        c.setAuthor(siteUser);
        c.setCreateDate(LocalDateTime.now());
        this.commentRepository.save(c);
        return c;
    }

    public List<Comment> getCommentList(Question question) {
        return this.commentRepository.findByQuestionAndAnswerIsNull(question);
    }

    public void delete(Comment comment) {
        this.commentRepository.delete(comment);
    }

    public List<Comment> getAnswerCommentList(Answer answer) {
        return this.commentRepository.findByAnswer(answer);
    }

    public Page<Comment> getListByAuthor(int page, SiteUser siteUser) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));
        return this.commentRepository.findByAuthor(siteUser, pageable);
    }
}