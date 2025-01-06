package com.ann.annovation.question;

import com.ann.annovation.category.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    Question findBySubject(String subject);
    Question findBySubjectAndContent(String subject, String content);
    List<Question> findBySubjectLike(String subject);
    Page<Question> findAll(Pageable pageable);
    // Specification을 사용하여 동적 쿼리를 실행하고, 페이징과 정렬을 결합하여 결과를 반환
    Page<Question> findAll(Specification<Question> spec, Pageable pageable);
    Page<Question> findByCategory(Category category, Pageable pageable);
}

// 페이징 (paging) : 입력된 정보나 데이터를 여러 페이지에 나눠 표시하고, 사용자가 페이지를 이동할 수 있게 하는 기능
// Page : 페이지 번호, 페이지 크기, 전체 페이지 수 등의 정보를 포함한 특별한 반환 타입
// Pageable pageable : 페이징과 정렬 정보를 담은 객체, 이 객체를 통해 몇 페이지를 가져올지, 한 페이지에 몇 개의 항목을 가져올지, 어떤 기준으로 정렬할지를 지정