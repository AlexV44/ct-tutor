package com.tutor.ct_tutor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tasks")
@Getter
@Setter
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_number", nullable = false)
    private Integer taskNumber;

    @Column(name = "subject_slug", nullable = false)
    private String subjectSlug;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "options", columnDefinition = "TEXT")
    private String options;

    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;

    @Column(name = "solution_explanation", columnDefinition = "TEXT")
    private String solutionExplanation;

    @Column(name = "image_path")
    private String imagePath;

    // Маппинг для векторного поля через библиотеку pgvector
    @Column(name = "embedding")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536) // Указывает размерность вектора (соответствует vector(1536) в БД)
    private float[] embedding;
}
