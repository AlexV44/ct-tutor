package com.tutor.ct_tutor.repository;

import com.tutor.ct_tutor.entity.StudentErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentErrorLogRepository extends JpaRepository<StudentErrorLog, UUID> {

    // Получить все ошибки ученика по конкретному предмету
    List<StudentErrorLog> findByStudentIdAndSubjectId(UUID studentId, UUID subjectId);

    // Получить вообще все ошибки ученика
    List<StudentErrorLog> findByStudentId(UUID studentId);
}