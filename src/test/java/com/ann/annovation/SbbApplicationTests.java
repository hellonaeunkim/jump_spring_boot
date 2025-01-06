package com.ann.annovation;

import com.ann.annovation.answer.AnswerService;
import com.ann.annovation.question.Question;
import com.ann.annovation.question.QuestionService;
import com.ann.annovation.user.SiteUser;
import com.ann.annovation.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SpringBootTest
class SbbApplicationTests {

    @Autowired
    private QuestionService questionService;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private UserService userService;

    @Test
    void testJpa1() {
        for (int i = 1; i <= 300; i++) {
            String subject = String.format("테스트 데이터입니다:[%03d]", i);
            String content = "내용무";
            this.questionService.create(subject, content, null, null);
        }
    }

    @Test
    void testJpa2() {
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("createDate")));
        Page<Question> lastQuestionPage = this.questionService.getList(pageable.getPageNumber(), "");

        if (lastQuestionPage.isEmpty()) {
            throw new IllegalStateException("No questions found to add answers.");
        }

        Question question = lastQuestionPage.getContent().get(0);
        SiteUser user = userService.create("temp", "temp@temp.com", "1234");

        for (int i = 1; i <= 300; i++) {
            this.answerService.create(question, String.format("테스트 답변 %03d!!", i), user);
        }
    }
}
