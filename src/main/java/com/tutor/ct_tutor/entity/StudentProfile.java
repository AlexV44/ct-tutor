package com.tutor.ct_tutor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "subject_slug", nullable = false)
    private String subjectSlug;

    @Column(name = "target_score")
    private Integer targetScore;

    @Column(name = "exam_days_left")
    private Integer examDaysLeft;

    @Column(name = "ai_profile", columnDefinition = "TEXT")
    private String aiProfile; // Скрытый портрет ученика от ИИ

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}