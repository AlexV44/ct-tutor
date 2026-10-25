import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Sparkles, Trophy, Rocket, MessageSquare,
  Flame, Star, ChevronRight, Send, CheckCircle2, ArrowLeft, Search, Award,
  Plus, Trash2, Edit2, BookOpen, Check, Circle, Clock
} from 'lucide-react';

type Role = 'select' | 'student' | 'parent' | 'teacher';
type StudentTab = 'main' | 'catalog' | 'chat' | 'tests' | 'progress';

interface SubjectItem {
  id: string;
  slug: string;
  name: string;
  icon: string;
  description: string;
}

interface ExamPlan {
  subjectSlug: string;
  daysUntilExam: number | null;
  targetScore: number | null;
  calculatedScore?: number;
}

interface Mission {
  id: string;
  subjectSlug: string;
  text: string;
  completed: boolean;
}

interface Topic {
  title: string;
  status: 'done' | 'in_progress' | 'locked';
}

const STUDENT_ID = "93cafd4b-15ef-4a16-8092-28ee87100b46";

// Список доступных предметов
const ALL_SUBJECTS: SubjectItem[] = [
  { id: '1', slug: 'math', name: 'Математика', icon: '📐', description: 'Алгебра, геометрия, тригонометрия' },
  { id: '2', slug: 'physics', name: 'Физика', icon: '⚡', description: 'Механика, электричество, оптика' },
  { id: '3', slug: 'russian', name: 'Русский язык', icon: '📚', description: 'Орфография, пунктуация, синтаксис' },
  { id: '4', slug: 'english', name: 'Английский язык', icon: '🇬🇧', description: 'Грамматика, лексика, чтение' },
  { id: '5', slug: 'history', name: 'История Беларуси', icon: '🏛️', description: 'Даты, события, исторические личности' },
  { id: '6', slug: 'chemistry', name: 'Химия', icon: '🧪', description: 'Органическая и неорганическая химия' },
];

// ИИ-планы тем для предметов (генерируются под профиль)
const SUBJECT_TOPICS: Record<string, Topic[]> = {
  math: [
    { title: 'Тригонометрические уравнения и тождества', status: 'done' },
    { title: 'Производная функции и касательные', status: 'in_progress' },
    { title: 'тереометрия: сечения многогранников', status: 'locked' },
    { title: 'Параметры в уравнениях и неравенствах', status: 'locked' }
  ],
  russian: [
    { title: 'Н и НН в суффиксах различных частей речи', status: 'done' },
    { title: 'Правописание НЕ и НИ с разными частями речи', status: 'in_progress' },
    { title: 'Пунктуация в сложных предложениях', status: 'locked' },
    { title: 'Лексические нормы и паронимы', status: 'locked' }
  ],
  physics: [
    { title: 'Кинематика материальной точки', status: 'done' },
    { title: 'Законы сохранения в механике', status: 'in_progress' },
    { title: 'Электростатика и постоянный ток', status: 'locked' }
  ],
  english: [
    { title: 'Present & Past Tenses Active/Passive', status: 'done' },
    { title: 'Conditionals (0, 1, 2, 3) & Wishes', status: 'in_progress' },
    { title: 'Phrasal Verbs for CT', status: 'locked' }
  ],
  history: [
    { title: 'Беларусь в составе Полоцкого княжества', status: 'done' },
    { title: 'ВКЛ в XIV–XVI веках', status: 'in_progress' },
    { title: 'Культура Беларуси в XIX веке', status: 'locked' }
  ],
  chemistry: [
    { title: 'Строение атома и периодический закон', status: 'done' },
    { title: 'Классы неорганических соединений', status: 'in_progress' },
    { title: 'Органический синтез', status: 'locked' }
  ]
};

export default function App() {
  const [role, setRole] = useState<Role>('select');
  const [studentTab, setStudentTab] = useState<StudentTab>('main');

  return (
    <div className="min-h-screen w-screen bg-gradient-to-b from-indigo-50 via-purple-50 to-pink-50 text-slate-800 font-sans pb-24 selection:bg-amber-400 selection:text-slate-900 overflow-x-hidden flex flex-col items-center justify-start">
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-lg h-[300px] bg-gradient-to-r from-amber-300/30 to-pink-400/30 blur-[100px] rounded-full pointer-events-none" />

      <div className="w-full max-w-md flex flex-col min-h-screen relative z-10">
        <AnimatePresence mode="wait">
          {role === 'select' && (
            <RoleSelectionScreen key="select" onSelect={setRole} />
          )}
          {role === 'student' && (
            <StudentView key="student" activeTab={studentTab} setActiveTab={setStudentTab} onBack={() => setRole('select')} />
          )}
          {role === 'parent' && (
            <ParentView key="parent" onBack={() => setRole('select')} />
          )}
          {role === 'teacher' && (
            <TeacherView key="teacher" onBack={() => setRole('select')} />
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}

/* ==========================================
   1. ВЫБОР РОЛИ
========================================== */
function RoleSelectionScreen({ onSelect }: { onSelect: (r: Role) => void }) {
  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.96 }}
      animate={{ opacity: 1, scale: 1 }}
      exit={{ opacity: 0, scale: 0.96 }}
      className="w-full flex-1 flex flex-col justify-center px-5 py-6"
    >
      <div className="text-center mb-8">
        <div className="inline-flex items-center gap-1.5 bg-amber-400/20 text-amber-700 text-xs font-extrabold px-3.5 py-1.5 rounded-full uppercase tracking-wider mb-3 border border-amber-400/30 shadow-sm">
          <Sparkles className="w-3.5 h-3.5 text-amber-500" /> Твой чит-код к ЦТ и ЦЭ
        </div>
        <h1 className="text-3xl font-black tracking-tight mb-2 text-indigo-950">Кто ты сегодня? 🚀</h1>
        <p className="text-slate-600 text-sm font-medium">Выбери свой режим и давай порвем экзамены!</p>
      </div>

      <div className="space-y-3.5">
        <RoleCard
          emoji="💻"
          bg="bg-gradient-to-r from-blue-500 to-indigo-600"
          title="Я — Ученик"
          desc="Игры с ИИ-тьютором, квесты и подготовка на 100 баллов"
          onClick={() => onSelect('student')}
        />
        <RoleCard
          emoji="🦸‍♂️"
          bg="bg-gradient-to-r from-emerald-500 to-teal-600"
          title="Я — Родитель"
          desc="Сводки успехов, забота и ИИ-советник по мотивации"
          onClick={() => onSelect('parent')}
        />
        <RoleCard
          emoji="🧙‍♂️"
          bg="bg-gradient-to-r from-purple-500 to-pink-600"
          title="Я — Учитель / Ментор"
          desc="Магия управления классом и крутые инструменты"
          onClick={() => onSelect('teacher')}
        />
      </div>
    </motion.div>
  );
}

function RoleCard({ emoji, bg, title, desc, onClick }: { emoji: string; bg: string; title: string; desc: string; onClick: () => void }) {
  return (
    <motion.div
      whileHover={{ scale: 1.02, y: -2 }}
      whileTap={{ scale: 0.98 }}
      onClick={onClick}
      className="bg-white/90 backdrop-blur-xl border border-white p-4 rounded-3xl flex items-center gap-4 cursor-pointer shadow-xl shadow-indigo-100/60 hover:shadow-2xl transition-all"
    >
      <div className={`w-14 h-14 rounded-2xl ${bg} flex items-center justify-center text-2xl shadow-md shadow-indigo-200/50 flex-shrink-0`}>
        {emoji}
      </div>
      <div className="flex-1 min-w-0">
        <h3 className="font-extrabold text-base text-indigo-950 truncate">{title}</h3>
        <p className="text-xs text-slate-500 mt-0.5 font-medium leading-relaxed">{desc}</p>
      </div>
      <div className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-400 flex-shrink-0">
        <ChevronRight className="w-4 h-4" />
      </div>
    </motion.div>
  );
}

/* ==========================================
   2. ИНТЕРФЕЙС УЧЕНИКА
========================================== */
function StudentView({ activeTab, setActiveTab, onBack }: { activeTab: StudentTab; setActiveTab: (t: StudentTab) => void; onBack: () => void }) {
  // Избранные предметы
  const [favoriteSlugs, setFavoriteSlugs] = useState<string[]>(() => {
    const saved = localStorage.getItem('favorite_subjects');
    return saved ? JSON.parse(saved) : ['math', 'russian'];
  });

  // Активный предмет для чата
  const [activeSubject, setActiveSubject] = useState<SubjectItem>(ALL_SUBJECTS[0]);

  // Планы подготовки по предметам
  const [plans, setPlans] = useState<Record<string, ExamPlan>>({});

  // Прогресс по каждому предмету (в процентах от 0 до 100)
  const [subjectProgress, setSubjectProgress] = useState<Record<string, number>>(() => {
    const saved = localStorage.getItem('subject_progress');
    return saved ? JSON.parse(saved) : { math: 35, russian: 50, physics: 20, english: 40, history: 30, chemistry: 15 };
  });

  // Миссии для предметов
  const [missions, setMissions] = useState<Mission[]>([
    { id: '1', subjectSlug: 'math', text: 'Разобрать 3 задачи с параметром', completed: false },
    { id: '2', subjectSlug: 'math', text: 'Пройти тест по производным', completed: true },
    { id: '3', subjectSlug: 'russian', text: 'Повторить правила правописания НЕ', completed: false },
    { id: '4', subjectSlug: 'physics', text: 'Решить тест по кинематике', completed: false },
  ]);

  useEffect(() => {
    localStorage.setItem('favorite_subjects', JSON.stringify(favoriteSlugs));
  }, [favoriteSlugs]);

  useEffect(() => {
    localStorage.setItem('subject_progress', JSON.stringify(subjectProgress));
  }, [subjectProgress]);

  // Загружаем данные по избранным предметам с бэкенда
  useEffect(() => {
    favoriteSlugs.forEach(slug => {
      fetch(`/api/exam-plan?studentId=${STUDENT_ID}&subject=${slug}`)
        .then(res => res.ok ? res.json() : null)
        .then(data => {
          if (data) {
            setPlans(prev => ({
              ...prev,
              [slug]: {
                subjectSlug: slug,
                daysUntilExam: data.daysUntilExam,
                targetScore: data.targetScore,
                calculatedScore: data.calculatedScore || Math.min(100, (data.targetScore || 60) - 10)
              }
            }));
          }
        })
        .catch(err => console.error(`Ошибка загрузки плана для ${slug}:`, err));
    });
  }, [favoriteSlugs]);

  const toggleFavorite = (slug: string) => {
    setFavoriteSlugs(prev =>
      prev.includes(slug) ? prev.filter(s => s !== slug) : [...prev, slug]
    );
  };

  const handleSavePlan = async (slug: string, days: number, score: number) => {
    setPlans(prev => ({
      ...prev,
      [slug]: {
        ...prev[slug],
        subjectSlug: slug,
        daysUntilExam: days,
        targetScore: score,
        calculatedScore: Math.min(100, Math.max(0, score - 12))
      }
    }));

    try {
      await fetch('/api/exam-plan', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          studentId: STUDENT_ID,
          subjectSlug: slug,
          days: days,
          score: score
        })
      });
    } catch (e) {
      console.error("Ошибка сохранения плана:", e);
    }
  };

  // Переключение статуса миссии и увеличение прогресса предмета
  const toggleMission = (missionId: string) => {
    setMissions(prev => prev.map(m => {
      if (m.id === missionId) {
        const nextCompleted = !m.completed;
        // Изменяем прогресс предмета на +/- 10%
        setSubjectProgress(sp => {
          const current = sp[m.subjectSlug] || 30;
          const updated = nextCompleted ? Math.min(100, current + 15) : Math.max(0, current - 15);
          return { ...sp, [m.subjectSlug]: updated };
        });
        return { ...m, completed: nextCompleted };
      }
      return m;
    }));
  };

  const startStudying = (subject: SubjectItem) => {
    setActiveSubject(subject);
    setActiveTab('chat');
  };

  // Расчет общего прогресса по всем избранным предметам
  const overallProgress = favoriteSlugs.length > 0
    ? Math.round(favoriteSlugs.reduce((acc, slug) => acc + (subjectProgress[slug] || 30), 0) / favoriteSlugs.length)
    : 0;

  return (
    <div className="w-full flex-1 flex flex-col pb-20">
      <Header title="⚡ ЦТ-Тьютор • Кабинет" onBack={onBack} />

      <div className="flex-1 px-4 py-3">
        <div className={activeTab === 'main' ? 'block' : 'hidden'}>
          <StudentMainTab
            favoriteSlugs={favoriteSlugs}
            onRemoveFavorite={toggleFavorite}
            onStartStudy={startStudying}
            onGoCatalog={() => setActiveTab('catalog')}
            missions={missions}
            onToggleMission={toggleMission}
          />
        </div>

        <div className={activeTab === 'catalog' ? 'block' : 'hidden'}>
          <StudentCatalogTab
            favoriteSlugs={favoriteSlugs}
            onToggleFavorite={toggleFavorite}
            onBackToMain={() => setActiveTab('main')}
          />
        </div>

        <div className={activeTab === 'chat' ? 'block' : 'hidden'}>
          <AiChatBox subject={activeSubject} />
        </div>

        <div className={activeTab === 'tests' ? 'block' : 'hidden'}>
          <StudentTestsTab />
        </div>

        <div className={activeTab === 'progress' ? 'block' : 'hidden'}>
          <StudentProgressTab
            favoriteSlugs={favoriteSlugs}
            plans={plans}
            subjectProgress={subjectProgress}
            overallProgress={overallProgress}
            onSavePlan={handleSavePlan}
          />
        </div>
      </div>

      <div className="fixed bottom-0 left-0 right-0 w-full flex justify-center p-3 z-50 pointer-events-none">
        <div className="w-full max-w-md bg-white/80 backdrop-blur-md border border-indigo-100 p-2 rounded-2xl shadow-2xl pointer-events-auto">
          <div className="flex justify-around items-center bg-indigo-950 text-white rounded-xl p-2">
            <TabBtn icon={<Rocket className="w-5 h-5" />} label="Главная" active={activeTab === 'main' || activeTab === 'catalog'} onClick={() => setActiveTab('main')} />
            <TabBtn icon={<MessageSquare className="w-5 h-5" />} label="Тьютор" active={activeTab === 'chat'} onClick={() => setActiveTab('chat')} />
            <TabBtn icon={<Trophy className="w-5 h-5" />} label="Тесты" active={activeTab === 'tests'} onClick={() => setActiveTab('tests')} />
            <TabBtn icon={<Star className="w-5 h-5" />} label="Прогресс" active={activeTab === 'progress'} onClick={() => setActiveTab('progress')} />
          </div>
        </div>
      </div>
    </div>
  );
}

/* Главная вкладка: Избранные предметы + План тем ИИ + Миссии на сегодня */
function StudentMainTab({
  favoriteSlugs,
  onRemoveFavorite,
  onStartStudy,
  onGoCatalog,
  missions,
  onToggleMission
}: {
  favoriteSlugs: string[];
  onRemoveFavorite: (slug: string) => void;
  onStartStudy: (subject: SubjectItem) => void;
  onGoCatalog: () => void;
  missions: Mission[];
  onToggleMission: (id: string) => void;
}) {
  const favoriteSubjects = ALL_SUBJECTS.filter(s => favoriteSlugs.includes(s.slug));
  const [selectedPlanSlug, setSelectedPlanSlug] = useState<string>(favoriteSlugs[0] || 'math');

  // Обновляем выбранный предмет для плана, если избранные изменились
  useEffect(() => {
    if (favoriteSlugs.length > 0 && !favoriteSlugs.includes(selectedPlanSlug)) {
      setSelectedPlanSlug(favoriteSlugs[0]);
    }
  }, [favoriteSlugs, selectedPlanSlug]);

  const currentTopics = SUBJECT_TOPICS[selectedPlanSlug] || [];
  const currentSubjectObj = ALL_SUBJECTS.find(s => s.slug === selectedPlanSlug);
  const activeMissions = missions.filter(m => favoriteSlugs.includes(m.subjectSlug));

  return (
    <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="space-y-4">
      {/* Баннер быстрых предметов */}
      <div className="bg-gradient-to-br from-indigo-600 via-blue-600 to-violet-600 text-white p-5 rounded-3xl shadow-xl shadow-blue-500/20">
        <div className="flex justify-between items-center mb-3">
          <h2 className="text-xl font-black">Мои предметы 🎯</h2>
          <button
            onClick={onGoCatalog}
            className="bg-amber-400 hover:bg-amber-300 text-slate-900 font-extrabold px-3 py-1.5 rounded-2xl text-xs flex items-center gap-1 shadow-md transition-all flex-shrink-0"
          >
            <Plus className="w-3.5 h-3.5" /> Каталог
          </button>
        </div>

        {favoriteSubjects.length === 0 ? (
          <div className="text-center py-4 bg-white/10 rounded-2xl">
            <p className="text-xs text-indigo-100 font-medium">Добавьте предметы для подготовки!</p>
          </div>
        ) : (
          <div className="space-y-2.5">
            {favoriteSubjects.map(subject => (
              <div key={subject.slug} className="bg-white/10 backdrop-blur-md border border-white/20 p-3 rounded-2xl flex items-center justify-between gap-2">
                <div className="flex items-center gap-2.5 min-w-0">
                  <span className="text-2xl">{subject.icon}</span>
                  <div className="min-w-0">
                    <h3 className="font-extrabold text-xs text-white truncate">{subject.name}</h3>
                  </div>
                </div>

                <div className="flex items-center gap-1.5 flex-shrink-0">
                  <button
                    onClick={() => onStartStudy(subject)}
                    className="bg-white text-indigo-900 font-extrabold text-[11px] px-3 py-1.5 rounded-xl shadow-sm flex items-center gap-1 hover:bg-amber-300 transition-all"
                  >
                    <span>Изучать</span>
                    <BookOpen className="w-3 h-3" />
                  </button>
                  <button
                    onClick={() => onRemoveFavorite(subject.slug)}
                    className="p-1.5 text-indigo-200 hover:text-red-300 rounded-lg transition-colors"
                    title="Убрать с главной"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Блок: План тем от ИИ с переключателем предметов */}
      <div className="bg-white/90 backdrop-blur-xl border border-white p-5 rounded-3xl shadow-xl shadow-indigo-100 space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="font-extrabold text-sm text-indigo-950 flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-amber-500" /> ИИ-план тем подготовки
          </h3>
        </div>

        {favoriteSlugs.length > 0 ? (
          <>
            {/* Переключатель предметов для плана */}
            <div className="flex gap-1.5 overflow-x-auto pb-1 no-scrollbar">
              {favoriteSubjects.map(sub => (
                <button
                  key={sub.slug}
                  onClick={() => setSelectedPlanSlug(sub.slug)}
                  className={`px-3 py-1.5 rounded-xl text-xs font-extrabold whitespace-nowrap transition-all ${selectedPlanSlug === sub.slug ? 'bg-indigo-600 text-white shadow-md shadow-indigo-500/20' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'}`}
                >
                  {sub.icon} {sub.name}
                </button>
              ))}
            </div>

            {/* Содержимое плана тем */}
            <div className="space-y-2 pt-1">
              {currentTopics.map((topic, idx) => (
                <div key={idx} className="bg-indigo-50/50 border border-indigo-100/70 p-3 rounded-2xl flex items-center justify-between gap-2">
                  <div className="flex items-center gap-2.5 min-w-0">
                    {topic.status === 'done' && <CheckCircle2 className="w-4 h-4 text-emerald-500 flex-shrink-0" />}
                    {topic.status === 'in_progress' && <Clock className="w-4 h-4 text-amber-500 flex-shrink-0" />}
                    {topic.status === 'locked' && <div className="w-4 h-4 rounded-full border-2 border-slate-300 flex-shrink-0" />}
                    <span className={`text-xs font-bold truncate ${topic.status === 'done' ? 'line-through text-slate-400' : 'text-indigo-950'}`}>
                      {topic.title}
                    </span>
                  </div>
                  <span className={`text-[10px] font-extrabold px-2 py-0.5 rounded-lg flex-shrink-0 ${topic.status === 'done' ? 'bg-emerald-100 text-emerald-700' : topic.status === 'in_progress' ? 'bg-amber-100 text-amber-700' : 'bg-slate-200 text-slate-600'}`}>
                    {topic.status === 'done' ? 'Изучено' : topic.status === 'in_progress' ? 'В процессе' : 'Предстоит'}
                  </span>
                </div>
              ))}
            </div>
          </>
        ) : (
          <p className="text-xs text-slate-500 text-center py-2">Добавьте предметы выше, чтобы увидеть персональный план тем.</p>
        )}
      </div>

      {/* Блок: Миссии на сегодня */}
      <div className="bg-white/90 backdrop-blur-xl border border-white p-5 rounded-3xl shadow-xl shadow-indigo-100 space-y-3">
        <h3 className="font-extrabold text-sm text-indigo-950 flex items-center gap-2">
          <Trophy className="w-4 h-4 text-amber-500" /> Миссии на сегодня
        </h3>
        {activeMissions.length === 0 ? (
          <p className="text-xs text-slate-500 text-center py-2">Нет активных миссий для выбранных предметов.</p>
        ) : (
          <div className="space-y-2.5">
            {activeMissions.map(mission => {
              const subObj = ALL_SUBJECTS.find(s => s.slug === mission.subjectSlug);
              return (
                <div
                  key={mission.id}
                  onClick={() => onToggleMission(mission.id)}
                  className={`p-3 rounded-2xl border transition-all cursor-pointer flex items-center justify-between gap-3 ${mission.completed ? 'bg-emerald-50/60 border-emerald-200' : 'bg-slate-50 border-slate-200 hover:border-indigo-300'}`}
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <div className={`w-6 h-6 rounded-lg flex items-center justify-center flex-shrink-0 transition-all ${mission.completed ? 'bg-emerald-500 text-white' : 'border-2 border-slate-300'}`}>
                      {mission.completed && <Check className="w-3.5 h-3.5" />}
                    </div>
                    <div className="min-w-0">
                      <p className={`text-xs font-bold truncate ${mission.completed ? 'line-through text-slate-400' : 'text-slate-800'}`}>
                        {mission.text}
                      </p>
                      <span className="text-[10px] text-indigo-600 font-extrabold">
                        {subObj?.icon} {subObj?.name}
                      </span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </motion.div>
  );
}

/* Каталог предметов */
function StudentCatalogTab({
  favoriteSlugs,
  onToggleFavorite,
  onBackToMain
}: {
  favoriteSlugs: string[];
  onToggleFavorite: (slug: string) => void;
  onBackToMain: () => void;
}) {
  return (
    <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="space-y-4">
      <div className="flex items-center justify-between bg-white/80 p-4 rounded-3xl border border-indigo-100">
        <div>
          <h2 className="font-extrabold text-indigo-950 text-base">Каталог предметов</h2>
          <p className="text-xs text-slate-500 font-medium">Выберите предметы для подготовки к ЦТ/ЦЭ</p>
        </div>
        <button
          onClick={onBackToMain}
          className="text-xs font-bold text-indigo-600 bg-indigo-50 px-3 py-2 rounded-xl"
        >
          Готово
        </button>
      </div>

      <div className="grid grid-cols-1 gap-3">
        {ALL_SUBJECTS.map(subject => {
          const isFav = favoriteSlugs.includes(subject.slug);
          return (
            <div
              key={subject.slug}
              onClick={() => onToggleFavorite(subject.slug)}
              className={`p-4 rounded-3xl border transition-all cursor-pointer flex items-center justify-between gap-3 ${isFav ? 'bg-indigo-50/80 border-indigo-300 shadow-md' : 'bg-white/80 border-white hover:bg-white'}`}
            >
              <div className="flex items-center gap-3">
                <span className="text-3xl">{subject.icon}</span>
                <div>
                  <h3 className="font-extrabold text-sm text-indigo-950">{subject.name}</h3>
                  <p className="text-xs text-slate-500 font-medium mt-0.5">{subject.description}</p>
                </div>
              </div>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center transition-all ${isFav ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-400'}`}>
                {isFav ? <Check className="w-4 h-4" /> : <Plus className="w-4 h-4" />}
              </div>
            </div>
          );
        })}
      </div>
    </motion.div>
  );
}

/* Вкладка Прогресс: Общая шкала + Индивидуальные шкалы по предметам */
function StudentProgressTab({
  favoriteSlugs,
  plans,
  subjectProgress,
  overallProgress,
  onSavePlan
}: {
  favoriteSlugs: string[];
  plans: Record<string, ExamPlan>;
  subjectProgress: Record<string, number>;
  overallProgress: number;
  onSavePlan: (slug: string, days: number, score: number) => void;
}) {
  const favoriteSubjects = ALL_SUBJECTS.filter(s => favoriteSlugs.includes(s.slug));

  return (
    <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="space-y-4">
      {/* Общая шкала подготовки ко всем экзаменам */}
      <div className="bg-gradient-to-br from-indigo-600 via-blue-600 to-violet-600 text-white p-5 rounded-3xl shadow-xl shadow-blue-500/20 space-y-3">
        <div className="flex justify-between items-center">
          <div className="flex items-center gap-2">
            <Award className="w-5 h-5 text-amber-300" />
            <h3 className="font-black text-base">Общая готовность к ЦТ</h3>
          </div>
          <span className="bg-white/20 px-3 py-1 rounded-full text-xs font-black text-amber-300 backdrop-blur-sm">
            {overallProgress}%
          </span>
        </div>
        <div className="w-full bg-black/20 h-3.5 rounded-full overflow-hidden p-0.5">
          <div
            className="bg-gradient-to-r from-amber-300 to-yellow-400 h-full rounded-full transition-all duration-500 shadow-sm"
            style={{ width: `${overallProgress}%` }}
          />
        </div>
        <p className="text-[11px] text-indigo-100 font-medium">
          Уровень рассчитан на основе выполненных миссий, решенных тестов и тем по всем выбранным предметам.
        </p>
      </div>

      {/* Шкалы по каждому предмету */}
      <div className="bg-white/90 backdrop-blur-xl border border-white p-5 rounded-3xl shadow-xl shadow-indigo-100 space-y-4">
        <h3 className="font-extrabold text-base text-indigo-950 flex items-center gap-2">
          <Trophy className="w-5 h-5 text-amber-500" /> Прогресс по предметам
        </h3>

        {favoriteSubjects.length === 0 ? (
          <p className="text-xs text-slate-500 text-center py-4">Добавьте предметы на главной, чтобы отслеживать прогресс.</p>
        ) : (
          <div className="space-y-4">
            {favoriteSubjects.map(subject => (
              <SubjectProgressCard
                key={subject.slug}
                subject={subject}
                plan={plans[subject.slug]}
                progress={subjectProgress[subject.slug] || 30}
                onSave={(days, score) => onSavePlan(subject.slug, days, score)}
              />
            ))}
          </div>
        )}
      </div>
    </motion.div>
  );
}

function SubjectProgressCard({
  subject,
  plan,
  progress,
  onSave
}: {
  subject: SubjectItem;
  plan?: ExamPlan;
  progress: number;
  onSave: (days: number, score: number) => void;
}) {
  const [isEditing, setIsEditing] = useState(false);
  const [daysInput, setDaysInput] = useState(plan?.daysUntilExam?.toString() || '60');
  const [scoreInput, setScoreInput] = useState(plan?.targetScore?.toString() || '80');

  const handleSave = () => {
    onSave(Number(daysInput) || 0, Number(scoreInput) || 0);
    setIsEditing(false);
  };

  return (
    <div className="bg-indigo-50/60 border border-indigo-100/80 p-4 rounded-2xl space-y-3">
      <div className="flex justify-between items-center">
        <div className="flex items-center gap-2">
          <span className="text-xl">{subject.icon}</span>
          <span className="font-extrabold text-sm text-indigo-950">{subject.name}</span>
        </div>
        <button
          onClick={() => setIsEditing(!isEditing)}
          className="text-indigo-600 hover:text-indigo-800 p-1 rounded-lg text-xs font-bold flex items-center gap-1"
        >
          <Edit2 className="w-3.5 h-3.5" /> {isEditing ? 'Отмена' : 'Изменить цели'}
        </button>
      </div>

      {/* Шкала прогресса по предмету */}
      <div className="space-y-1.5 bg-white p-3 rounded-xl border border-indigo-100/60">
        <div className="flex justify-between text-xs font-bold">
          <span className="text-slate-600">Шкала освоения предмета</span>
          <span className="text-indigo-600 font-black">{progress}%</span>
        </div>
        <div className="w-full bg-slate-100 h-2.5 rounded-full overflow-hidden p-0.5">
          <div
            className="bg-gradient-to-r from-blue-600 to-indigo-600 h-full rounded-full transition-all duration-500"
            style={{ width: `${progress}%` }}
          />
        </div>
      </div>

      {isEditing ? (
        <div className="space-y-3 bg-white p-3.5 rounded-xl border border-indigo-100">
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="text-[10px] font-bold text-slate-500 uppercase">Дней до ЦТ</label>
              <input
                type="number"
                value={daysInput}
                onChange={e => setDaysInput(e.target.value)}
                className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-1.5 text-xs font-bold text-indigo-950 mt-1"
              />
            </div>
            <div>
              <label className="text-[10px] font-bold text-slate-500 uppercase">Целевой балл</label>
              <input
                type="number"
                value={scoreInput}
                onChange={e => setScoreInput(e.target.value)}
                className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-1.5 text-xs font-bold text-indigo-950 mt-1"
              />
            </div>
          </div>
          <button
            onClick={handleSave}
            className="w-full bg-indigo-600 text-white font-bold py-2 rounded-xl text-xs shadow-md shadow-indigo-500/20"
          >
            Сохранить цели
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-3 gap-2 text-center">
          <div className="bg-white p-2.5 rounded-xl border border-indigo-100/60">
            <div className="text-base font-black text-blue-600">{plan?.daysUntilExam ?? '—'}</div>
            <div className="text-[10px] text-slate-500 font-bold">Дней осталось</div>
          </div>
          <div className="bg-white p-2.5 rounded-xl border border-indigo-100/60">
            <div className="text-base font-black text-amber-500">{plan?.targetScore ?? '—'}</div>
            <div className="text-[10px] text-slate-500 font-bold">Цель (балл)</div>
          </div>
          <div className="bg-white p-2.5 rounded-xl border border-indigo-100/60">
            <div className="text-base font-black text-emerald-600">{plan?.calculatedScore ?? '—'}</div>
            <div className="text-[10px] text-slate-500 font-bold">Расчетный балл</div>
          </div>
        </div>
      )}
    </div>
  );
}

/* Изолированный ИИ-чат по конкретному предмету */
function AiChatBox({ subject }: { subject: SubjectItem }) {
  const storageKey = `ct_tutor_chat_${subject.slug}`;

  const [messages, setMessages] = useState(() => {
    try {
      const saved = localStorage.getItem(storageKey);
      if (saved) return JSON.parse(saved);
    } catch (e) {
      console.error("Ошибка загрузки истории чата:", e);
    }
    return [
      {
        id: '1',
        text: `Привет! Я твой ИИ-тьютор по предмету «${subject.name}». С чего начнем подготовку сегодня? ✨`,
        isAi: true
      }
    ];
  });

  const [input, setInput] = useState('');

  useEffect(() => {
    try {
      localStorage.setItem(storageKey, JSON.stringify(messages));
    } catch (e) {
      console.error("Ошибка сохранения истории чата:", e);
    }
  }, [messages, storageKey]);

  const send = async () => {
    if (!input.trim()) return;
    const userText = input;
    const userMsg = { id: Date.now().toString(), text: userText, isAi: false };
    setMessages(prev => [...prev, userMsg]);
    setInput('');

    const loadingId = (Date.now() + 1).toString();
    setMessages(prev => [...prev, { id: loadingId, text: '⏳ Тьютор размышляет...', isAi: true }]);

    try {
      const initData = window.Telegram?.WebApp?.initData || "mock_init_data";

      const res = await fetch('/api/tutor', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          subject: subject.slug,
          message: userText,
          initData: initData
        })
      });

      const data = await res.json();
      const cleanReply = (data.reply || '').replace(/\*|_/g, '');

      setMessages(prev => prev.filter(m => m.id !== loadingId).concat({
        id: Date.now().toString(),
        text: res.ok ? cleanReply : `❌ Ошибка сервера: ${data.error || 'Не удалось получить ответ'}`,
        isAi: true
      }));

    } catch (e) {
      setMessages(prev => prev.filter(m => m.id !== loadingId).concat({
        id: Date.now().toString(),
        text: '❌ Ошибка сети: не удалось связаться с сервером.',
        isAi: true
      }));
    }
  };

  return (
    <div className="bg-white/90 backdrop-blur-xl border border-white rounded-3xl shadow-xl shadow-indigo-100 flex flex-col h-[450px] overflow-hidden">
      <div className="bg-indigo-50 border-b border-indigo-100 px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="text-lg">{subject.icon}</span>
          <span className="font-extrabold text-xs text-indigo-950">Тьютор • {subject.name}</span>
        </div>
        <button
          onClick={() => {
            localStorage.removeItem(storageKey);
            setMessages([{ id: '1', text: `История очищена. О чем спросишь по предмету «${subject.name}»? ✨`, isAi: true }]);
          }}
          className="text-[10px] text-slate-400 hover:text-slate-600 font-bold transition-colors"
        >
          Очистить
        </button>
      </div>

      <div className="flex-1 p-3.5 overflow-y-auto space-y-3">
        {messages.map(m => (
          <motion.div
            initial={{ opacity: 0, y: 5 }}
            animate={{ opacity: 1, y: 0 }}
            key={m.id}
            className={`max-w-[85%] p-3.5 rounded-2xl text-xs leading-relaxed break-words whitespace-pre-wrap font-medium ${m.isAi ? 'bg-indigo-50/80 border border-indigo-100/60 text-indigo-950 self-start' : 'bg-gradient-to-r from-blue-600 to-indigo-600 text-white self-end ml-auto shadow-md shadow-blue-500/20'}`}
          >
            {m.text}
          </motion.div>
        ))}
      </div>

      <div className="p-3 border-t border-indigo-100 bg-white flex gap-2">
        <input
          type="text"
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && send()}
          placeholder={`Задать вопрос по предметам (${subject.name})...`}
          className="flex-1 bg-slate-50 border border-slate-200 rounded-2xl px-4 py-2.5 text-xs text-slate-800 font-medium focus:outline-none focus:border-indigo-500 transition-colors"
        />
        <button onClick={send} className="bg-indigo-600 hover:bg-indigo-500 text-white p-2.5 rounded-2xl transition-all shadow-md shadow-indigo-500/25 flex items-center justify-center">
          <Send className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}

function StudentTestsTab() {
  return (
    <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="space-y-4">
      <div className="bg-white/90 backdrop-blur-xl border border-white p-5 rounded-3xl shadow-xl shadow-indigo-100">
        <h3 className="font-extrabold text-base mb-3 text-indigo-950 flex items-center gap-2">
          <Trophy className="w-5 h-5 text-amber-500" /> Тренажер тестов РИКЗ
        </h3>
        <div className="space-y-3">
          <div className="bg-indigo-50/50 border border-indigo-100 p-4 rounded-2xl flex justify-between items-center gap-2">
            <div>
              <h4 className="font-extrabold text-sm text-indigo-950">Вариант №1 (Стандарт ЦТ)</h4>
              <p className="text-xs text-slate-500 mt-0.5 font-medium">30 заданий • 120 минут</p>
            </div>
            <button className="bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold px-4 py-2.5 rounded-xl shadow-md shadow-indigo-500/20 transition-all flex-shrink-0">В бой!</button>
          </div>
        </div>
      </div>
    </motion.div>
  );
}

/* ==========================================
   3. ИНТЕРФЕЙС РОДИТЕЛЯ
========================================== */
function ParentView({ onBack }: { onBack: () => void }) {
  const [searchQuery, setSearchQuery] = useState('');
  const [foundStudent, setFoundStudent] = useState<{
    name: string;
    username: string;
    studyTime: string;
    forecastScore: string;
  } | null>(null);

  const handleSearch = () => {
    if (!searchQuery.trim()) return;
    setFoundStudent({
      name: 'Алексей Видилин',
      username: searchQuery.startsWith('@') ? searchQuery : `@${searchQuery}`,
      studyTime: '5.4 ч / нед',
      forecastScore: '78 б.'
    });
  };

  return (
    <div className="w-full flex-1 flex flex-col pb-6">
      <Header title="Кабинет родителя" onBack={onBack} />
      <div className="flex-1 px-4 py-3 space-y-4">

        <div className="bg-white/90 backdrop-blur-xl border border-white p-5 rounded-3xl shadow-xl shadow-indigo-100">
          <h3 className="font-extrabold text-sm mb-3 text-indigo-950">Привязать аккаунт ребенка</h3>
          <div className="flex gap-2">
            <div className="relative flex-1">
              <Search className="absolute left-3.5 top-3.5 w-4 h-4 text-slate-400" />
              <input
                type="text"
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleSearch()}
                placeholder="ID ученика или @username"
                className="w-full bg-slate-50 border border-slate-200 rounded-2xl pl-10 pr-4 py-3 text-xs text-slate-800 focus:outline-none focus:border-indigo-500 font-medium transition-colors"
              />
            </div>
            <button
              onClick={handleSearch}
              className="bg-indigo-600 hover:bg-indigo-500 text-white font-bold px-4 py-3 rounded-2xl text-xs transition-all shadow-md shadow-indigo-500/20"
            >
              Найти
            </button>
          </div>
        </div>

        {foundStudent && (
          <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="bg-white/90 backdrop-blur-xl border border-white p-5 rounded-3xl shadow-xl shadow-indigo-100">
            <div className="flex justify-between items-center mb-3">
              <div>
                <h3 className="font-extrabold text-sm text-indigo-950">👤 {foundStudent.name}</h3>
                <p className="text-[11px] text-slate-400 font-medium">{foundStudent.username}</p>
              </div>
              <span className="bg-emerald-100 text-emerald-700 text-[10px] font-black px-2.5 py-1 rounded-full uppercase">Подключен</span>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="bg-indigo-50/60 p-3.5 rounded-2xl border border-indigo-100 text-center">
                <div className="text-lg font-black text-indigo-600">{foundStudent.studyTime}</div>
                <div className="text-[11px] text-slate-500 font-bold">Время занятий</div>
              </div>
              <div className="bg-emerald-50/60 p-3.5 rounded-2xl border border-indigo-100 text-center">
                <div className="text-lg font-black text-emerald-600">{foundStudent.forecastScore}</div>
                <div className="text-[11px] text-slate-500 font-bold">Прогноз ЦТ</div>
              </div>
            </div>
          </motion.div>
        )}

        <SimpleConsultantBox roleName="ИИ-Консультант родителя" />
      </div>
    </div>
  );
}

/* ==========================================
   4. ИНТЕРФЕЙС УЧИТЕЛЯ
========================================== */
function TeacherView({ onBack }: { onBack: () => void }) {
  const [students, setStudents] = useState([
    { id: '1', name: 'Алексей Видилин', progress: '68%', status: 'Отлично', isWarn: false },
    { id: '2', name: 'Иван Иванов', progress: '45%', status: 'Внимание', isWarn: true },
    { id: '3', name: 'Анна Смирнова', progress: '82%', status: 'Лидер', isWarn: false },
  ]);

  const [newStudentName, setNewStudentName] = useState('');
  const [isAdding, setIsAdding] = useState(false);

  const handleAddStudent = () => {
    if (!newStudentName.trim()) return;
    setStudents(prev => [
      ...prev,
      {
        id: Date.now().toString(),
        name: newStudentName,
        progress: '0%',
        status: 'Новый',
        isWarn: false
      }
    ]);
    setNewStudentName('');
    setIsAdding(false);
  };

  return (
    <div className="w-full flex-1 flex flex-col pb-6">
      <Header title="Кабинет преподавателя" onBack={onBack} />
      <div className="flex-1 px-4 py-3 space-y-4">

        <div className="bg-white/90 backdrop-blur-xl border border-white p-5 rounded-3xl shadow-xl shadow-indigo-100">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h3 className="font-extrabold text-sm text-indigo-950">🎓 Группа ЦТ-11А</h3>
              <p className="text-[11px] text-slate-500 font-medium mt-0.5">Всего учащихся: {students.length}</p>
            </div>
            <button
              onClick={() => setIsAdding(!isAdding)}
              className="bg-indigo-600 hover:bg-indigo-500 text-white font-bold px-3 py-2 rounded-xl text-xs flex items-center gap-1 shadow-md shadow-indigo-500/20 transition-all"
            >
              <Plus className="w-3.5 h-3.5" /> {isAdding ? 'Отмена' : 'Добавить'}
            </button>
          </div>

          {isAdding && (
            <motion.div initial={{ opacity: 0, y: -5 }} animate={{ opacity: 1, y: 0 }} className="mb-4 p-3 bg-indigo-50/70 rounded-2xl border border-indigo-100 flex gap-2">
              <input
                type="text"
                value={newStudentName}
                onChange={e => setNewStudentName(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleAddStudent()}
                placeholder="Имя ученика или @username"
                className="flex-1 bg-white border border-slate-200 rounded-xl px-3 py-2 text-xs text-slate-800 font-medium focus:outline-none focus:border-indigo-500"
              />
              <button
                onClick={handleAddStudent}
                className="bg-emerald-600 hover:bg-emerald-500 text-white font-bold px-3 py-2 rounded-xl text-xs transition-all"
              >
                Сохранить
              </button>
            </motion.div>
          )}

          <div className="space-y-2 text-xs">
            {students.map(student => (
              <TeacherRow
                key={student.id}
                name={student.name}
                progress={student.progress}
                status={student.status}
                isWarn={student.isWarn}
              />
            ))}
          </div>
        </div>

        <SimpleConsultantBox roleName="ИИ-Ассистент преподавателя" />
      </div>
    </div>
  );
}

function TeacherRow({ name, progress, status, isWarn }: { name: string; progress: string; status: string; isWarn?: boolean }) {
  return (
    <div className="flex justify-between items-center bg-indigo-50/50 p-3 rounded-2xl border border-indigo-100 font-medium">
      <span className="font-bold text-indigo-950">{name}</span>
      <div className="flex items-center gap-2">
        <span className="text-slate-500">{progress}</span>
        <span className={`px-2.5 py-1 rounded-xl text-[10px] font-black ${isWarn ? 'bg-amber-100 text-amber-700' : 'bg-emerald-100 text-emerald-700'}`}>
          {status}
        </span>
      </div>
    </div>
  );
}

/* Общий чат для родителей и учителей */
function SimpleConsultantBox({ roleName }: { roleName: string }) {
  const storageKey = `ct_tutor_chat_${roleName.replace(/\s+/g, '_')}`;
  const [messages, setMessages] = useState(() => {
    try {
      const saved = localStorage.getItem(storageKey);
      if (saved) return JSON.parse(saved);
    } catch (e) {}
    return [{ id: '1', text: `Привет! Я ${roleName}. Чем могу помочь вам сегодня? ✨`, isAi: true }];
  });
  const [input, setInput] = useState('');

  useEffect(() => {
    localStorage.setItem(storageKey, JSON.stringify(messages));
  }, [messages, storageKey]);

  const send = () => {
    if (!input.trim()) return;
    const userMsg = { id: Date.now().toString(), text: input, isAi: false };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setTimeout(() => {
      setMessages(prev => [...prev, { id: (Date.now() + 1).toString(), text: 'Это тестовый ответ ИИ-ассистента в демо-режиме.', isAi: true }]);
    }, 600);
  };

  return (
    <div className="bg-white/90 backdrop-blur-xl border border-white rounded-3xl shadow-xl shadow-indigo-100 flex flex-col h-[350px] overflow-hidden">
      <div className="bg-indigo-50 border-b border-indigo-100 px-4 py-3 flex items-center justify-between">
        <span className="font-extrabold text-xs text-indigo-950">{roleName}</span>
      </div>
      <div className="flex-1 p-3.5 overflow-y-auto space-y-3">
        {messages.map(m => (
          <div key={m.id} className={`max-w-[85%] p-3.5 rounded-2xl text-xs font-medium ${m.isAi ? 'bg-indigo-50/80 text-indigo-950 self-start' : 'bg-blue-600 text-white self-end ml-auto'}`}>
            {m.text}
          </div>
        ))}
      </div>
      <div className="p-3 border-t border-indigo-100 bg-white flex gap-2">
        <input
          type="text"
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && send()}
          placeholder="Введите сообщение..."
          className="flex-1 bg-slate-50 border border-slate-200 rounded-2xl px-4 py-2 text-xs focus:outline-none"
        />
        <button onClick={send} className="bg-indigo-600 text-white p-2.5 rounded-2xl">
          <Send className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}

/* Вспомогательные компоненты */
function Header({ title, onBack }: { title: string; onBack: () => void }) {
  return (
    <div className="flex items-center justify-between px-4 py-3 border-b border-indigo-100/60 bg-white/80 backdrop-blur-md sticky top-0 z-40 w-full">
      <button onClick={onBack} className="p-2.5 rounded-2xl bg-indigo-50 border border-indigo-100 hover:bg-indigo-100 transition-colors">
        <ArrowLeft className="w-4 h-4 text-indigo-950" />
      </button>
      <h2 className="font-extrabold text-sm tracking-wide text-indigo-950">{title}</h2>
      <div className="w-9" />
    </div>
  );
}

function TabBtn({ icon, label, active, onClick }: { icon: React.ReactNode; label: string; active: boolean; onClick: () => void }) {
  return (
    <motion.button
      whileTap={{ scale: 0.95 }}
      onClick={onClick}
      className={`flex flex-col items-center gap-1 py-1.5 px-3 rounded-xl transition-all ${active ? 'text-amber-300 font-black scale-105' : 'text-slate-400 hover:text-white'}`}
    >
      {icon}
      <span className="text-[10px]">{label}</span>
    </motion.button>
  );
}