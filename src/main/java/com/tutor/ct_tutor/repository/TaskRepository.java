package com.tutor.ct_tutor.repository;

import com.tutor.ct_tutor.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query(value = "SELECT * FROM tasks WHERE subject_slug = :subjectSlug " +
            "ORDER BY embedding <=> CAST(:queryEmbedding AS vector) LIMIT 2",
            nativeQuery = true)
    List<Task> findSimilarTasks(@Param("subjectSlug") String subjectSlug,
                                @Param("queryEmbedding") String queryEmbedding);
}