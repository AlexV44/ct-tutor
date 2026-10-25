package com.tutor.ct_tutor.service;

import com.tutor.ct_tutor.dto.request.UpdateExamPlanRequest;
import com.tutor.ct_tutor.dto.response.ExamPlanResponse;
import com.tutor.ct_tutor.entity.StudentExamPlan;
import com.tutor.ct_tutor.repository.StudentExamPlanRepository;
import com.tutor.ct_tutor.entity.Subject;
import com.tutor.ct_tutor.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentExamPlanService {

    private final StudentExamPlanRepository planRepository;
    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public ExamPlanResponse getPlan(UUID studentId, String subjectSlug) {
        Subject subject = subjectRepository.findBySlug(subjectSlug)
                .orElseThrow(() -> new IllegalArgumentException("Предмет не найден: " + subjectSlug));

        StudentExamPlan plan = planRepository.findByStudentIdAndSubject_Slug(studentId, subjectSlug)
                .orElse(null);

        return new ExamPlanResponse(
                studentId,
                subject.getSlug(),
                subject.getName(),
                plan != null ? plan.getDaysUntilExam() : null,
                plan != null ? plan.getTargetScore() : null
        );
    }

    @Transactional
    public void updatePlan(UpdateExamPlanRequest dto) {
        Subject subject = subjectRepository.findBySlug(dto.subjectSlug())
                .orElseThrow(() -> new IllegalArgumentException("Предмет не найден: " + dto.subjectSlug()));

        StudentExamPlan plan = planRepository.findByStudentIdAndSubject_Slug(dto.studentId(), dto.subjectSlug())
                .orElseGet(() -> StudentExamPlan.builder()
                        .studentId(dto.studentId())
                        .subject(subject)
                        .build());

        if (dto.days() != null) {
            plan.setDaysUntilExam(dto.days());
        }
        if (dto.score() != null) {
            plan.setTargetScore(dto.score());
        }

        planRepository.save(plan);
    }
}
