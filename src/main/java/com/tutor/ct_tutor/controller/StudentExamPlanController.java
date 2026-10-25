package com.tutor.ct_tutor.controller;

import com.tutor.ct_tutor.dto.request.UpdateExamPlanRequest;
import com.tutor.ct_tutor.dto.response.ExamPlanResponse;
import com.tutor.ct_tutor.service.StudentExamPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/exam-plan")
@RequiredArgsConstructor
public class StudentExamPlanController {

    private final StudentExamPlanService planService;

    @GetMapping
    public ResponseEntity<ExamPlanResponse> getPlan(
            @RequestParam UUID studentId,
            @RequestParam String subject) {
        return ResponseEntity.ok(planService.getPlan(studentId, subject));
    }

    @PostMapping
    public ResponseEntity<?> updatePlan(@RequestBody UpdateExamPlanRequest request) {
        planService.updatePlan(request);
        return ResponseEntity.ok(Map.of("status", "success", "message", "План успешно обновлен"));
    }
}
