package com.ann.annovation.question;

import com.ann.annovation.answer.Answer;
import com.ann.annovation.answer.AnswerForm;
import com.ann.annovation.answer.AnswerService;
import com.ann.annovation.category.Category;
import com.ann.annovation.category.CategoryService;
import com.ann.annovation.comment.CommentForm;
import com.ann.annovation.comment.CommentService;
import com.ann.annovation.user.SiteUser;
import com.ann.annovation.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RequestMapping("/question")
@RequiredArgsConstructor
@Controller
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;
    private final AnswerService answerService;
    private final CommentService commentService;
    private final CategoryService categoryService;


    @GetMapping("/list")
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "kw", defaultValue = "") String kw,
            @RequestParam(value = "sort", defaultValue = "") String sort
    ) {
        Page<Question> paging = this.questionService.getList(page, kw, sort);
        List<Category> categoryList = this.categoryService.getAll();

        model.addAttribute("paging", paging);
        // 화면에서 입력한 검색어를 화면에 그대로 유지하기 위해 kw 값 저장
        model.addAttribute("kw", kw);

        model.addAttribute("category_list", categoryList);
        model.addAttribute("sort", sort);

        // 반환 값으로 "question_list"라는 이름의 뷰를 렌더링하도록 지시
        // 렌더링(Rendering)은 데이터를 사용자가 볼 수 있는 화면으로 변환하는 과정
        return "question_list";
    }

    @GetMapping(value = "/detail/{id}")
    public String detail(
            Model model,
            @PathVariable("id") Integer id,
            AnswerForm answerForm,
            @RequestParam(value = "ans-page", defaultValue = "0") int answerPage,
            @RequestParam(value = "ans-ordering", defaultValue = "time") String answerOrderMethod) {

        this.questionService.viewUp(id);

        // AnswerForm 객체를 템플릿에 전달하여 폼 데이터와 연결합니다.
        // QuestionService의 getQuestion 메서드를 호출하여 특정 id에 해당하는 질문 데이터를 가져옴
        Question question = this.questionService.getQuestion(id);

        // answerPaging : 특정 question에 달린 answer들을 paging
        Page<Answer> answerPaging = this.answerService.getAnswerList(question, answerPage, answerOrderMethod);

        List<Category> categoryList = this.categoryService.getAll();

        // 가져온 질문 데이터를 모델(Model)에 추가
        // "question"이라는 이름으로 뷰에 전달하여 화면에서 사용 가능
        // model.addAttribute("key", value)를 통해 데이터를 추가하며, 뷰에서는 key 이름으로 접근합니다.
        model.addAttribute("question", question);

        model.addAttribute("ans_paging", answerPaging);
        model.addAttribute("commentForm", new CommentForm());
        // 질문 댓글 목록
        model.addAttribute("comment_list", this.commentService.getCommentList(question));

        // 답변별 댓글 목록
        for (Answer answer : answerPaging) {
            model.addAttribute("answer_comments_" + answer.getId(),
                    this.commentService.getAnswerCommentList(answer));
        }

        model.addAttribute("category_list", categoryList);
        return "question_detail";
    }

    // @PreAuthorize("isAuthenticated()") : 메서드를 로그인한 경우에만 실행,  로그아웃 상태에서 호출되면 로그인 페이지로 강제 이동
    // principal 객체가 로그인을 해야만 생성되는 객체, 로그아웃 상태에서는 객체 값이 없어 오류를 발생시킴
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(Model model, QuestionForm questionForm) {
        List<Category> categoryList = this.categoryService.getAll();
        model.addAttribute("category_list", categoryList);
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(
            @Valid QuestionForm questionForm,
            BindingResult bindingResult,
            Principal principal
    ) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        SiteUser siteUser = this.userService.getUser(principal.getName());
        Category category = this.categoryService.getCategoryByName(questionForm.getCategory());

        this.questionService.create(questionForm.getSubject(), questionForm.getContent(), category, siteUser);
        
        return "redirect:/question/list"; // 질문 저장후 질문목록으로 이동
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(
            Model model,
            QuestionForm questionForm,
            @PathVariable("id") Integer id,
            Principal principal) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        List<Category> categoryList = this.categoryService.getAll();
        model.addAttribute("category_list", categoryList);
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(
            @Valid QuestionForm questionForm,
            BindingResult bindingResult,
            Principal principal,
            @PathVariable("id") Integer id) {

        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        return String.format("redirect:/question/detail/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(
            Principal principal,
            @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }
}