package com.tutor.ct_tutor.repository;

import com.tutor.ct_tutor.entity.TutorRequestEntity;
import com.tutor.ct_tutor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TutorRepository extends JpaRepository<TutorRequestEntity, UUID> {
    List<TutorRequestEntity> findByUserOrderByCreatedAtDesc(User user);
    @Query("SELECT t FROM TutorRequestEntity t WHERE t.user = :user AND t.subject = :subject ORDER BY t.createdAt DESC")
    List<TutorRequestEntity> findTop5ByUserAndSubject(@Param("user") User user, @Param("subject") String subject);
    long countByUserAndSubject(User user, String subject);
}
