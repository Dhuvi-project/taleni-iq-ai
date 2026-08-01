import { useState } from 'react';
import { MessagesSquare, ArrowRight, RotateCcw } from 'lucide-react';
import { useToast } from '../context/ToastContext';
import { generateInterviewQuestions } from '../api/interview';
import GlassCard from '../components/GlassCard';
import EmptyState from '../components/EmptyState';
import { Spinner } from '../components/Spinner';
import './shared.css';

const QUESTION_TYPES = [
  { label: 'All Types', value: 'ALL' },
  { label: 'Technical', value: 'TECHNICAL' },
  { label: 'Behavioral', value: 'BEHAVIORAL' },
  { label: 'Coding', value: 'CODING' },
];

export default function Interview() {
  const toast = useToast();
  const [role, setRole] = useState('');
  const [type, setType] = useState(QUESTION_TYPES[0].value);
  const [loading, setLoading] = useState(false);
  const [questions, setQuestions] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [revealed, setRevealed] = useState([]);

  const handleGenerate = async (e) => {
    e.preventDefault();
    if (!role.trim()) {
      toast.error('Enter a target role first.');
      return;
    }
    setLoading(true);
    try {
      const result = await generateInterviewQuestions({ role: role.trim(), type });
      const qs = result.questions || [];
      setQuestions(qs);
      setCurrentIndex(0);
      setRevealed(qs.length ? [0] : []);
      if (!qs.length) toast.info('No questions were generated. Try a different role or type.');
    } catch (err) {
      toast.error(err.friendlyMessage || 'Could not generate questions.');
    } finally {
      setLoading(false);
    }
  };

  const nextQuestion = () => {
    if (currentIndex < questions.length - 1) {
      const next = currentIndex + 1;
      setCurrentIndex(next);
      setRevealed((prev) => [...prev, next]);
    }
  };

  const reset = () => {
    setQuestions([]);
    setCurrentIndex(0);
    setRevealed([]);
  };

  return (
    <div className="fade-in">
      <div className="page-header">
        <div>
          <h1>Interview Question Generator</h1>
          <p className="text-secondary">Generate role-specific interview questions and practice sequentially.</p>
        </div>
      </div>

      <GlassCard style={{ marginBottom: 22 }}>
        <form onSubmit={handleGenerate} className="grid-cols-3" style={{ alignItems: 'end' }}>
          <div className="field" style={{ marginBottom: 0 }}>
            <label>Target role</label>
            <input
              className="input"
              placeholder="e.g. Senior Backend Engineer"
              value={role}
              onChange={(e) => setRole(e.target.value)}
            />
          </div>
          <div className="field" style={{ marginBottom: 0 }}>
            <label>Question type</label>
            <select className="select" value={type} onChange={(e) => setType(e.target.value)}>
              {QUESTION_TYPES.map((t) => (
                <option key={t.value} value={t.value}>{t.label}</option>
              ))}
            </select>
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? <Spinner size={17} /> : <><MessagesSquare size={16} /> Generate Questions</>}
          </button>
        </form>
      </GlassCard>

      <GlassCard>
        <div className="flex justify-between items-center" style={{ marginBottom: 16 }}>
          <h3 className="section-title" style={{ marginBottom: 0 }}>Practice Session</h3>
          {questions.length > 0 && (
            <button className="btn btn-ghost btn-sm" onClick={reset}>
              <RotateCcw size={14} /> Reset
            </button>
          )}
        </div>
        {questions.length === 0 ? (
          <EmptyState
            icon={MessagesSquare}
            title="No questions yet"
            message="Enter a role and question type above to generate your first set of interview questions."
          />
        ) : (
          <>
            <div className="chat-bubble-wrap" style={{ marginBottom: 20 }}>
              {revealed.map((idx) => (
                <div className="chat-bubble ai" key={idx}>
                  <span className="badge badge-primary" style={{ marginBottom: 8 }}>
                    {questions[idx].type || type} · Q{idx + 1}
                  </span>
                  <div>{questions[idx].questionText}</div>
                </div>
              ))}
            </div>
            {currentIndex < questions.length - 1 ? (
              <button className="btn btn-primary" onClick={nextQuestion}>
                Next Question <ArrowRight size={16} />
              </button>
            ) : (
              <span className="badge badge-success">All {questions.length} questions revealed</span>
            )}
          </>
        )}
      </GlassCard>
    </div>
  );
}
