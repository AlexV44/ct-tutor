--liquibase formatted sql

--changeset developer:1-create-full-tutor-schema

-- Включаем расширение для генерации UUID (если еще не включено)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Таблица предметов
CREATE TABLE subjects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    system_prompt TEXT,
    icon VARCHAR(50)
);

-- 2. Вставка дефолтных предметов для подготовки к ЦТ/ЦЭ
INSERT INTO subjects (slug, name, system_prompt, icon)
VALUES
    ('math', 'Математика', 'Ты сократический ИИ-тьютор по математике для подготовки к ЦТ/ЦЭ в Беларуси.', 'Calculator'),
    ('russian', 'Русский язык', 'Ты сократический ИИ-тьютор по русскому языку для подготовки к ЦТ/ЦЭ в Беларуси.', 'BookOpen'),
    ('physics', 'Физика', 'Ты сократический ИИ-тьютор по физике для подготовки к ЦТ/ЦЭ в Беларуси.', 'Zap'),
    ('belarusian', 'Белорусский язык', 'Ты сократический ИИ-тьютор по белорусскому языку для подготовки к ЦТ/ЦЭ в Беларуси.', 'BookOpenText'),
    ('chemistry', 'Химия', 'Ты сократический ИИ-тьютор по химии для подготовки к ЦТ/ЦЭ в Беларуси.', 'FlaskConical'),
    ('biology', 'Биология', 'Ты сократический ИИ-тьютор по биологии для подготовки к ЦТ/ЦЭ в Беларуси.', 'Dna'),
    ('history-belarus', 'История Беларуси', 'Ты сократический ИИ-тьютор по истории Беларуси для подготовки к ЦТ/ЦЭ в Беларуси.', 'Landmark'),
    ('social-studies', 'Обществоведение', 'Ты сократический ИИ-тьютор по обществоведению для подготовки к ЦТ/ЦЭ в Беларуси.', 'Users'),
    ('english', 'Английский язык', 'Ты сократический ИИ-тьютор по английскому языку для подготовки к ЦТ/ЦЭ в Беларуси.', 'Languages'),
    ('informatics', 'Информатика', 'Ты сократический ИИ-тьютор по информатике для подготовки к ЦТ/ЦЭ в Беларуси.', 'Terminal')
ON CONFLICT (slug) DO NOTHING;

-- 3. Таблица тем по предметам
CREATE TABLE topics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    order_index INT NOT NULL,
    CONSTRAINT fk_topics_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

-- 4. Статусы тем для каждого ученика
CREATE TABLE student_topic_statuses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    topic_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'not_started',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sts_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_sts_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE,
    CONSTRAINT uk_student_topic UNIQUE (student_id, topic_id)
);

-- 5. Профиль ученика (цели, дни до экзамена и ИИ-портрет)
CREATE TABLE student_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    subject_slug VARCHAR(50) NOT NULL,
    target_score INT,
    exam_days_left INT,
    ai_profile TEXT, -- Динамический портрет, формируемый ИИ
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sp_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_student_subject_profile UNIQUE (student_id, subject_slug)
);

-- Индекс для быстрого поиска профиля по ученику и предмету (через slug)
CREATE INDEX idx_student_profiles_lookup
ON student_profiles(student_id, subject_slug);

-- 6. Банк ошибок нового поколения
CREATE TABLE student_error_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    subject_id UUID NOT NULL,
    question_text TEXT NOT NULL,
    user_answer VARCHAR(255),
    correct_answer VARCHAR(255),
    error_reason VARCHAR(50) NOT NULL, -- 'theory', 'carelessness', 'trap'
    explanation TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sel_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_sel_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);