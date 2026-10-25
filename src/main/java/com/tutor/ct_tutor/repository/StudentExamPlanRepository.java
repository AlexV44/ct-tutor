package com.tutor.ct_tutor.repository;

import com.tutor.ct_tutor.entity.StudentExamPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface StudentExamPlanRepository extends JpaRepository<StudentExamPlan, UUID> {
    Optional<StudentExamPlan> findByStudentIdAndSubject_Slug(UUID studentId, String subjectSlug);
}
