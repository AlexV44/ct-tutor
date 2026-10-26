package com.tutor.ct_tutor.service;

import com.tutor.ct_tutor.dto.request.TopicProgressDto;
import com.tutor.ct_tutor.entity.StudentTopicStatus;
import com.tutor.ct_tutor.entity.Subject;
import com.tutor.ct_tutor.entity.Topic;
import com.tutor.ct_tutor.entity.User;
import com.tutor.ct_tutor.repository.StudentTopicStatusRepository;
import com.tutor.ct_tutor.repository.SubjectRepository;
import com.tutor.ct_tutor.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final StudentTopicStatusRepository statusRepository;

    /**
     * Получение списка тем предмета со статусами ученика.
     * Если статусов еще нет, первая тема автоматически получает статус 'in_progress', остальные 'not_started'.
     */
    @Transactional
    public List<TopicProgressDto> getStudentTopics(User user, String subjectSlug) {
        Subject subject = subjectRepository.findBySlug(subjectSlug)
                .orElseThrow(() -> new RuntimeException("Предмет не найден: " + subjectSlug));

        List<Topic> allTopics = topicRepository.findBySubjectIdOrderByOrderIndexAsc(subject.getId());

        List<StudentTopicStatus> userStatuses = statusRepository.findByStudentIdAndTopic_Subject_IdOrderByTopic_OrderIndexAsc(user.getId(), subject.getId());
        Map<UUID, String> statusMap = userStatuses.stream()
                .collect(Collectors.toMap(st -> st.getTopic().getId(), StudentTopicStatus::getStatus));

        // Инициализация при первом открытии предмета
        if (statusMap.isEmpty() && !allTopics.isEmpty()) {
            for (int i = 0; i < allTopics.size(); i++) {
                Topic topic = allTopics.get(i);
                StudentTopicStatus st = new StudentTopicStatus();
                st.setStudentId(user.getId());
                st.setTopic(topic);
                // Первая тема автоматически 'in_progress', остальные 'not_started'
                st.setStatus(i == 0 ? "in_progress" : "not_started");
                statusRepository.save(st);
                statusMap.put(topic.getId(), st.getStatus());
            }
        }

        List<TopicProgressDto> dtos = new ArrayList<>();
        for (int i = 0; i < allTopics.size(); i++) {
            Topic topic = allTopics.get(i);
            String status = statusMap.get(topic.getId());

            if (status == null) {
                status = "not_started";
                StudentTopicStatus st = new StudentTopicStatus();
                st.setStudentId(user.getId());
                st.setTopic(topic);
                st.setStatus(status);
                statusRepository.save(st);
                statusMap.put(topic.getId(), status);
            }

            dtos.add(new TopicProgressDto(
                    topic.getId(),
                    topic.getTitle(),
                    topic.getOrderIndex(),
                    status
            ));
        }
        return dtos;
    }

    /**
     * Автоматический перевод темы в статус "в процессе" (когда ученик выбирает её для работы)
     */
    @Transactional
    public List<TopicProgressDto> startTopic(User user, String subjectSlug, UUID topicId) {
        StudentTopicStatus statusRecord = statusRepository.findByStudentIdAndTopic_Id(user.getId(), topicId)
                .orElseGet(() -> {
                    StudentTopicStatus st = new StudentTopicStatus();
                    st.setStudentId(user.getId());
                    Topic topic = topicRepository.findById(topicId)
                            .orElseThrow(() -> new RuntimeException("Тема не найдена"));
                    st.setTopic(topic);
                    return st;
                });

        // Меняем на in_progress только если она еще не была пройдена
        if (!"done".equals(statusRecord.getStatus())) {
            statusRecord.setStatus("in_progress");
            statusRecord.setUpdatedAt(LocalDateTime.now());
            statusRepository.save(statusRecord);
        }

        return getStudentTopics(user, subjectSlug);
    }

    /**
     * Автоматическое завершение текущей темы и разблокировка (перевод в in_progress) следующей
     */
    @Transactional
    public List<TopicProgressDto> completeTopicAutomatically(User user, String subjectSlug, UUID topicId) {
        StudentTopicStatus current = statusRepository.findByStudentIdAndTopic_Id(user.getId(), topicId)
                .orElseThrow(() -> new RuntimeException("Статус темы не найден"));

        // 1. Текущая тема автоматически становится выполненной
        current.setStatus("done");
        current.setUpdatedAt(LocalDateTime.now());
        statusRepository.save(current);

        // 2. Находим предмет и все его темы для определения следующей по порядку
        Subject subject = subjectRepository.findBySlug(subjectSlug)
                .orElseThrow(() -> new RuntimeException("Предмет не найден: " + subjectSlug));

        List<Topic> allTopics = topicRepository.findBySubjectIdOrderByOrderIndexAsc(subject.getId());
        int currentOrder = current.getTopic().getOrderIndex();

        // 3. Автоматически переводим следующую по порядку тему в статус "in_progress" (если она была "not_started")
        allTopics.stream()
                .filter(t -> t.getOrderIndex() > currentOrder)
                .findFirst()
                .ifPresent(nextTopic -> {
                    StudentTopicStatus nextStatus = statusRepository.findByStudentIdAndTopic_Id(user.getId(), nextTopic.getId())
                            .orElseGet(() -> {
                                StudentTopicStatus st = new StudentTopicStatus();
                                st.setStudentId(user.getId());
                                st.setTopic(nextTopic);
                                return st;
                            });

                    if ("not_started".equals(nextStatus.getStatus()) || nextStatus.getStatus() == null) {
                        nextStatus.setStatus("in_progress");
                        nextStatus.setUpdatedAt(LocalDateTime.now());
                        statusRepository.save(nextStatus);
                    }
                });

        return getStudentTopics(user, subjectSlug);
    }
}