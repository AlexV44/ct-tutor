package com.tutor.ct_tutor.repository;

import com.tutor.ct_tutor.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {

    // Найти профиль ученика по его ID и слагу предмета (например, 'math')
    Optional<StudentProfile> findByStudentIdAndSubjectSlug(UUID studentId, String subjectSlug);
}