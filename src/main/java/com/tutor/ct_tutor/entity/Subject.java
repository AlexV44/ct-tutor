package com.tutor.ct_tutor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "subjects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String slug; // например: "math", "russian", "physics"

    @Column(nullable = false, length = 100)
    private String name; // например: "Математика", "Русский язык"

    @Column(columnDefinition = "TEXT")
    private String systemPrompt; // Персональная инструкция для ИИ под этот предмет

    @Column(length = 50)
    private String icon; // Название иконки для UI (например, "Calculator", "BookOpen")
}