package com.ann.annovation.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // category_list 데이터는 h2 DB에 직접 INSERT함
    Optional<Category> findByName(String subject);
}
