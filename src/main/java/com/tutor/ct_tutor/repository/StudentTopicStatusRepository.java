package com.tutor.ct_tutor.repository;

import com.tutor.ct_tutor.entity.StudentTopicStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentTopicStatusRepository extends JpaRepository<StudentTopicStatus, UUID> {

    // Получить статусы ученика по конкретному предмету (через связанную сущность Topic -> Subject)
    List<StudentTopicStatus> findByStudentIdAndTopic_Subject_IdOrderByTopic_OrderIndexAsc(UUID studentId, UUID subjectId);

    // Найти статус конкретной темы для ученика
    Optional<StudentTopicStatus> findByStudentIdAndTopic_Id(UUID studentId, UUID topicId);
}