package com.union.brainrush.repository;

import com.union.brainrush.model.QuestionFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionFormat, Long> {
    @Query(value = "SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<QuestionFormat> findRandomQuestions(@Param("limit") int limit);
}
