package com.ann.annovation.user;

import com.ann.annovation.DataNotFoundException;
import com.ann.annovation.answer.Answer;
import com.ann.annovation.answer.AnswerService;
import com.ann.annovation.comment.Comment;
import com.ann.annovation.comment.CommentService;
import com.ann.annovation.question.Question;
import com.ann.annovation.question.QuestionService;
import com.ann.annovation.util.PasswordUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final JavaMailSender mailSender;
    private final CommentService commentService;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            // bindingResult.rejectValue(필드명, 오류 코드, 오류 메시지)
            // 여기서 오류 코드는 임의로 passwordInCorrect로 정의했다. 하지만 대형 프로젝트에서는 번역과 관리를 위해 오류 코드를 잘 정의하여 사용해야 한다.
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }

        try {
            userService.create(userCreateForm.getUsername(),
                    userCreateForm.getEmail(), userCreateForm.getPassword1());
        } catch (DataIntegrityViolationException e) { // 사용자 ID 또는 이메일 주소가 이미 존재할 경우
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup_form";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage()); // bindingResult.reject(오류 코드, 오류 메시지)는 UserCreateForm의 검증에 의한 오류 외에 일반적인 오류를 발생시킬 때 사용
            return "signup_form";
        }

        return "redirect:/";
    }

    // 실제 로그인을 진행하는 @PostMapping 방식의 메서드는 스프링 시큐리티가 대신 처리
    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    @GetMapping("/find-account")
    public String findAccount(Model model) {
        model.addAttribute("sendConfirm", false);
        model.addAttribute("error", false);
        return "find_account";
    }

    @PostMapping("/find-account")
    public String findAccount(Model model, @RequestParam(value = "email") String email) {
        try {

            // 1. 사용자 조회
            SiteUser user = this.userService.getUserByEmail(email);

            // 2. 새 비밀번호 생성
            String newPassword = PasswordUtil.getRandomPassword(12);

            // 3. 비밀번호 업데이트
            this.userService.update(user, newPassword);

            // 4. 이메일 준비
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(email);
            simpleMailMessage.setSubject("계정 정보입니다.");
            simpleMailMessage.setText(String.format(
                    "%s계정의 비밀번호를 새롭게 초기화 했습니다.\n새 비밀번호는 %s입니다.\n로그인 후 내 정보에서 새로 비밀번호를 지정해주세요.",
                    user.getUsername(),
                    newPassword
            ));

            // 5. 이메일 비동기 발송
            new Thread(() -> mailSender.send(simpleMailMessage)).start();

            // 6. 성공 상태 설정
            model.addAttribute("sendConfirm", true);
            model.addAttribute("userEmail", email);
            model.addAttribute("error", false);

        } catch (DataNotFoundException e) {
            model.addAttribute("sendConfirm", false);
            model.addAttribute("error", true);
        }
        return "find_account";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String profile(
            Model model, Principal principal,
            @RequestParam(value="question-page", defaultValue="0") int questionPage,
            @RequestParam(value="ans-page", defaultValue="0") int ansPage,
            @RequestParam(value="question-vote-page", defaultValue="0") int questionVoterPage,
            @RequestParam(value="ans-vote-page", defaultValue="0") int ansVoterPage,
            @RequestParam(value="comment-page", defaultValue="0") int commentPage
    ) {
        SiteUser user = this.userService.getUser(principal.getName());
        Page<Question> wroteQuestions = this.questionService.getListByAuthor(questionPage, user);
        Page<Answer> wroteAnswers = this.answerService.getListByAuthor(ansPage, user);
        Page<Question> votedQuestions = this.questionService.getListByVoter(questionVoterPage, user);
        Page<Answer> votedAnswers = this.answerService.getListByVoter(ansVoterPage, user);
        Page<Comment> wroteComments = this.commentService.getListByAuthor(commentPage, user);

        model.addAttribute("wrote_question_paging", wroteQuestions);
        model.addAttribute("wrote_answer_paging", wroteAnswers);
        model.addAttribute("voted_question_paging", votedQuestions);
        model.addAttribute("voted_answer_paging", votedAnswers);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("wrote_comment_paging", wroteComments);
        return "profile";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/change-password")
    public String changePassword(UserChangePasswordForm userChangePasswordForm) {
        return "change_password_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change-password")
    public String changePassword(
            @Valid UserChangePasswordForm userChangePasswordForm,
            BindingResult bindingResult,
            Principal principal
    ) {

        if (bindingResult.hasErrors()) {
            return "change_password_form";
        }

        String username = principal.getName();
        SiteUser user = userService.getUser(username);

        if (!userService.validatePassword(user, userChangePasswordForm.getOriginPassword())) {
            bindingResult.reject("invalidPassword", "유효하지 않은 비밀번호입니다.");
            return "change_password_form";
        }

        if (!userChangePasswordForm.getNewPassword1().equals(userChangePasswordForm.getNewPassword2())) {
            bindingResult.reject("passwordCheckFailed", "비밀번호 확인 실패하였습니다.");
            return "change_password_form";
        }

        userService.changePassword(user, userChangePasswordForm.getNewPassword1());

        return "redirect:/user/logout";
    }
}
