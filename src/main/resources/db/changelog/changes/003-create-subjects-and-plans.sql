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
INSERT INTO subjects (id, slug, name, system_prompt, icon)
VALUES
    (gen_random_uuid(), 'math', 'Математика', 'Ты сократический ИИ-тьютор по математике для подготовки к ЦТ/ЦЭ в Беларуси.', 'Calculator'),
    (gen_random_uuid(), 'russian', 'Русский язык', 'Ты ИИ-тьютор по русскому языку для подготовки к ЦТ/ЦЭ в Беларуси.', 'BookOpen')
ON CONFLICT (slug) DO NOTHING;

-- 3. Таблица планов подготовки учеников
CREATE TABLE student_exam_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    subject_id UUID NOT NULL,
    days_until_exam INT,
    target_score INT,

    -- Внешний ключ на таблицу предметов
    CONSTRAINT fk_plan_subject
        FOREIGN KEY (subject_id)
        REFERENCES subjects(id)
        ON DELETE CASCADE,

    -- Уникальное ограничение: один план по конкретному предмету для одного ученика
    CONSTRAINT uk_student_subject_plan
        UNIQUE (student_id, subject_id)
);

-- Индекс для быстрого поиска планов по ученику и предмету
CREATE INDEX idx_student_exam_plans_lookup
ON student_exam_plans(student_id, subject_id);