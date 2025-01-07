package com.ann.annovation.category;

import com.ann.annovation.question.Question;
import com.ann.annovation.question.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/category")
@RequiredArgsConstructor
@Controller
public class CategoryController {

    private final CategoryService categoryService;
    private final QuestionService questionService;

    @GetMapping("/{category}")
    public String contentList(
            Model model,
            @PathVariable("category") String categoryName,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "kw", defaultValue = "") String kw
    ) {

        Category category = this.categoryService.getCategoryByName(categoryName);
        List<Category> categoryList = this.categoryService.getAll();
        Page<Question> paging = this.questionService.getCategoryQuestionList(category, page);

        model.addAttribute("category_name", categoryName);
        model.addAttribute("category_list", categoryList);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "category_question_list";
    }
}
