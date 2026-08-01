import { useEffect, useState } from 'react';
import { Target, Sparkles } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { getResumesByUser } from '../api/resumes';
import { matchResumeToJob } from '../api/match';
import GlassCard from '../components/GlassCard';
import ScoreGauge from '../components/ScoreGauge';
import EmptyState from '../components/EmptyState';
import { Spinner } from '../components/Spinner';
import './shared.css';

export default function Matching() {
  const { user } = useAuth();
  const toast = useToast();
  const [resumes, setResumes] = useState([]);
  const [selectedResume, setSelectedResume] = useState('');
  const [jobDescription, setJobDescription] = useState('');
  const [matchResult, setMatchResult] = useState(null);
  const [matching, setMatching] = useState(false);

  useEffect(() => {
    getResumesByUser(user.id)
      .then((data) => {
        const list = Array.isArray(data) ? data : [];
        setResumes(list);
        if (list.length) setSelectedResume(String(list[0].id));
      })
      .catch(() => {});
  }, [user.id]);

  const handleMatch = async (e) => {
    e.preventDefault();
    if (!selectedResume) {
      toast.error('Select a resume first.');
      return;
    }
    if (!jobDescription.trim()) {
      toast.error('Paste a job description to match against.');
      return;
    }
    setMatching(true);
    setMatchResult(null);
    try {
      const result = await matchResumeToJob({ resumeId: Number(selectedResume), jobDescription });
      setMatchResult(result);
    } catch (err) {
      toast.error(err.friendlyMessage || 'Matching failed. Please try again.');
    } finally {
      setMatching(false);
    }
  };

  return (
    <div className="fade-in">
      <div className="page-header">
        <div>
          <h1>Job Matching</h1>
          <p className="text-secondary">Paste a job description to see how well your resume matches.</p>
        </div>
      </div>

      <div className="grid-cols-2" style={{ alignItems: 'start' }}>
        <GlassCard>
          <h3 className="section-title">
            <Target size={18} /> Match Your Resume
          </h3>
          <form onSubmit={handleMatch}>
            <div className="field">
              <label>Select resume</label>
              <select className="select" value={selectedResume} onChange={(e) => setSelectedResume(e.target.value)}>
                {resumes.length === 0 && <option value="">No resumes uploaded</option>}
                {resumes.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.fileName} (v{r.version})
                  </option>
                ))}
              </select>
            </div>
            <div className="field">
              <label>Job description</label>
              <textarea
                className="textarea"
                placeholder="Paste the full job description here..."
                value={jobDescription}
                onChange={(e) => setJobDescription(e.target.value)}
              />
            </div>
            <button type="submit" className="btn btn-primary btn-block" disabled={matching}>
              {matching ? <Spinner size={17} /> : <><Sparkles size={16} /> Analyze Match</>}
            </button>
          </form>
        </GlassCard>

        <GlassCard style={{ minHeight: 320 }}>
          <h3 className="section-title">Match Results</h3>
          {!matchResult ? (
            <EmptyState title="No match yet" message="Submit a job description to see your compatibility score." />
          ) : (
            <div className="fade-in">
              <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 20 }}>
                <ScoreGauge score={matchResult.matchScore} size={150} label="Match Score" />
              </div>
              <div className="grid-cols-2">
                <div>
                  <h4 style={{ fontSize: 13.5, marginBottom: 10 }} className="text-success">Matched Keywords</h4>
                  <div className="tag-cloud">
                    {matchResult.matchedKeywords?.length ? (
                      matchResult.matchedKeywords.map((k) => (
                        <span key={k} className="chip chip-success">{k}</span>
                      ))
                    ) : (
                      <span className="text-muted">None</span>
                    )}
                  </div>
                </div>
                <div>
                  <h4 style={{ fontSize: 13.5, marginBottom: 10 }} className="text-danger">Missing Keywords</h4>
                  <div className="tag-cloud">
                    {matchResult.missingKeywords?.length ? (
                      matchResult.missingKeywords.map((k) => (
                        <span key={k} className="chip chip-danger">{k}</span>
                      ))
                    ) : (
                      <span className="text-muted">None</span>
                    )}
                  </div>
                </div>
              </div>
            </div>
          )}
        </GlassCard>
      </div>
    </div>
  );
}
