import { useEffect, useState } from 'react';
import { Award, Map as MapIcon } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { getResumesByUser } from '../api/resumes';
import { getSkillGap } from '../api/skills';
import GlassCard from '../components/GlassCard';
import SkillBar from '../components/SkillBar';
import ScoreGauge from '../components/ScoreGauge';
import EmptyState from '../components/EmptyState';
import { Spinner } from '../components/Spinner';
import './shared.css';

const LEVEL_PERCENT = {
  beginner: 33,
  intermediate: 66,
  advanced: 100,
  required: 100,
};

function levelToPercent(level) {
  return LEVEL_PERCENT[String(level || '').toLowerCase()] ?? 50;
}

export default function SkillGap() {
  const { user } = useAuth();
  const [resumes, setResumes] = useState([]);
  const [selectedResume, setSelectedResume] = useState('');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    getResumesByUser(user.id)
      .then((list) => {
        const arr = Array.isArray(list) ? list : [];
        setResumes(arr);
        if (arr.length) setSelectedResume(String(arr[0].id));
      })
      .catch(() => {});
  }, [user.id]);

  useEffect(() => {
    if (!selectedResume) return;
    setLoading(true);
    setError(null);
    getSkillGap(selectedResume)
      .then(setData)
      .catch((err) => setError(err.friendlyMessage))
      .finally(() => setLoading(false));
  }, [selectedResume]);

  return (
    <div className="fade-in">
      <div className="page-header">
        <div>
          <h1>Skill Gap Analysis</h1>
          <p className="text-secondary">See where you stand against the skills required for your target roles.</p>
        </div>
        {resumes.length > 0 && (
          <select className="select" style={{ maxWidth: 260 }} value={selectedResume} onChange={(e) => setSelectedResume(e.target.value)}>
            {resumes.map((r) => (
              <option key={r.id} value={r.id}>{r.fileName} (v{r.version})</option>
            ))}
          </select>
        )}
      </div>

      {resumes.length === 0 ? (
        <GlassCard>
          <EmptyState title="No resumes found" message="Upload a resume first to analyze your skill gaps." />
        </GlassCard>
      ) : loading ? (
        <div className="flex items-center gap-1"><Spinner /> Analyzing skill gaps...</div>
      ) : error ? (
        <GlassCard><EmptyState title="Couldn't load skill gap" message={error} /></GlassCard>
      ) : !data ? (
        <GlassCard><EmptyState title="No data" message="No skill gap data available for this resume yet." /></GlassCard>
      ) : (
        <>
          <div className="grid-cols-2" style={{ marginBottom: 22 }}>
            <GlassCard>
              <h3 className="section-title">Current Skills</h3>
              {(data.currentSkills || []).length === 0 ? (
                <p className="text-muted">No current skills recorded.</p>
              ) : (
                data.currentSkills.map((s) => <SkillBar key={s.name} label={s.name} value={levelToPercent(s.level)} rightLabel={s.level} />)
              )}
            </GlassCard>
            <GlassCard>
              <h3 className="section-title">Required Skills</h3>
              {(data.requiredSkills || []).length === 0 ? (
                <p className="text-muted">No required skills recorded.</p>
              ) : (
                data.requiredSkills.map((s) => <SkillBar key={s.name} label={s.name} value={levelToPercent(s.level)} rightLabel={s.level} />)
              )}
            </GlassCard>
          </div>

          <div className="grid-cols-2" style={{ marginBottom: 22, alignItems: 'start' }}>
            <GlassCard style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <h3 className="section-title" style={{ alignSelf: 'flex-start' }}>Gap Score</h3>
              <ScoreGauge score={data.gapScore} label="Readiness" size={160} />
            </GlassCard>
            <GlassCard>
              <h3 className="section-title">
                <Award size={17} /> Recommended Certifications
              </h3>
              {(data.certifications || []).length === 0 ? (
                <p className="text-muted">No certifications recommended right now.</p>
              ) : (
                <div className="tag-cloud">
                  {data.certifications.map((c) => (
                    <span key={c} className="chip chip-primary">{c}</span>
                  ))}
                </div>
              )}
            </GlassCard>
          </div>

          <GlassCard>
            <h3 className="section-title">
              <MapIcon size={17} /> 8-Week Upskilling Roadmap
            </h3>
            {(data.roadmap || []).length === 0 ? (
              <EmptyState title="No roadmap yet" message="A personalized roadmap will appear here." />
            ) : (
              <div className="roadmap-track">
                {data.roadmap.map((step) => (
                  <div className="roadmap-week" key={step.week}>
                    <span className="roadmap-week-num">{step.week}</span>
                    <h4>{step.focus}</h4>
                    <ul className="list-reset" style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                      {(step.resources || []).map((r) => (
                        <li key={r} className="text-muted" style={{ fontSize: 12.5 }}>• {r}</li>
                      ))}
                    </ul>
                  </div>
                ))}
              </div>
            )}
          </GlassCard>
        </>
      )}
    </div>
  );
}
