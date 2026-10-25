package com.tutor.ct_tutor.repository;

import com.tutor.ct_tutor.entity.TutorRequestEntity;
import com.tutor.ct_tutor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TutorRepository extends JpaRepository<TutorRequestEntity, UUID> {
    List<TutorRequestEntity> findByUserOrderByCreatedAtDesc(User user);
    List<TutorRequestEntity> findTop5ByUserOrderByCreatedAtDesc(User user);
}
