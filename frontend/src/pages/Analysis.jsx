import { useEffect, useMemo, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar, ResponsiveContainer, Tooltip } from 'recharts';
import { ThumbsUp, ThumbsDown, ArrowLeft, Sparkles } from 'lucide-react';
import { getResumeAnalysis } from '../api/resumes';
import GlassCard from '../components/GlassCard';
import ScoreGauge from '../components/ScoreGauge';
import SkillBar from '../components/SkillBar';
import EmptyState from '../components/EmptyState';
import { PageLoader } from '../components/Spinner';
import './shared.css';

const SECTION_LABELS = {
  contact: 'Contact Info',
  summary: 'Summary',
  experience: 'Experience',
  education: 'Education',
  skills: 'Skills',
};

const CLOUD_COLORS = ['var(--color-primary)', 'var(--color-secondary)', 'var(--color-accent)', 'var(--color-warning)'];

export default function Analysis() {
  const { resumeId } = useParams();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let mounted = true;
    setLoading(true);
    setError(null);
    getResumeAnalysis(resumeId)
      .then((d) => mounted && setData(d))
      .catch((err) => mounted && setError(err.friendlyMessage))
      .finally(() => mounted && setLoading(false));
    return () => {
      mounted = false;
    };
  }, [resumeId]);

  const radarData = useMemo(() => {
    if (!data?.extractedSkills) return [];
    return data.extractedSkills.slice(0, 8).map((s) => {
      if (typeof s === 'string') return { skill: s, value: 70 };
      return { skill: s.name || s.skill, value: s.level ?? s.value ?? 70 };
    });
  }, [data]);

  const keywordCloud = useMemo(() => {
    const kd = data?.keywordDensity;
    if (!kd) return [];
    if (Array.isArray(kd)) return kd;
    return Object.entries(kd).map(([word, freq]) => ({ word, freq }));
  }, [data]);

  const maxFreq = Math.max(1, ...keywordCloud.map((k) => k.freq || k.count || 1));

  if (loading) return <PageLoader />;
  if (error)
    return (
      <GlassCard>
        <EmptyState title="Couldn't load analysis" message={error} />
      </GlassCard>
    );
  if (!data)
    return (
      <GlassCard>
        <EmptyState title="No analysis available" message="This resume hasn't been analyzed yet." />
      </GlassCard>
    );

  return (
    <div className="fade-in">
      <div className="page-header">
        <div>
          <Link to="/upload" className="btn btn-ghost btn-sm" style={{ marginBottom: 10 }}>
            <ArrowLeft size={15} /> Back
          </Link>
          <h1>AI Resume Analysis</h1>
          <p className="text-secondary">Detailed breakdown of resume #{data.resumeId ?? resumeId}</p>
        </div>
      </div>

      <div className="grid-cols-2" style={{ marginBottom: 22 }}>
        <GlassCard style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
          <ScoreGauge score={data.atsScore} size={170} label="Overall ATS Score" />
          <div className="flex gap-2" style={{ marginTop: 18 }}>
            <span className="chip">Keyword density: {typeof data.keywordDensity === 'number' ? `${data.keywordDensity}%` : keywordCloud.length}</span>
            <span className="chip">Action verbs: {data.actionVerbCount ?? 0}</span>
          </div>
        </GlassCard>

        <GlassCard>
          <h3 className="section-title">Section Score Breakdown</h3>
          {data.sectionScores &&
            Object.entries(data.sectionScores).map(([key, val]) => (
              <SkillBar key={key} label={SECTION_LABELS[key] || key} value={val} rightLabel={`${val}/100`} />
            ))}
        </GlassCard>
      </div>

      <div className="grid-cols-2" style={{ marginBottom: 22 }}>
        <GlassCard>
          <h3 className="section-title">
            <Sparkles size={17} /> Skill Radar
          </h3>
          {radarData.length === 0 ? (
            <EmptyState title="No skills extracted" message="Extracted skills will appear here." />
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <RadarChart data={radarData} outerRadius="75%">
                <PolarGrid stroke="var(--border-subtle)" />
                <PolarAngleAxis dataKey="skill" tick={{ fill: 'var(--text-secondary)', fontSize: 12 }} />
                <PolarRadiusAxis angle={30} domain={[0, 100]} tick={{ fill: 'var(--text-muted)', fontSize: 10 }} />
                <Radar name="Proficiency" dataKey="value" stroke="var(--color-primary)" fill="var(--color-primary)" fillOpacity={0.35} />
                <Tooltip contentStyle={{ background: 'var(--bg-elevated)', border: '1px solid var(--border-subtle)', borderRadius: 10 }} />
              </RadarChart>
            </ResponsiveContainer>
          )}
        </GlassCard>

        <GlassCard>
          <h3 className="section-title">Keyword Cloud</h3>
          {keywordCloud.length === 0 ? (
            <EmptyState title="No keyword data" message="Keyword frequency will appear once available." />
          ) : (
            <div className="tag-cloud">
              {keywordCloud.map((k, i) => {
                const freq = k.freq || k.count || 1;
                const size = 12 + (freq / maxFreq) * 14;
                return (
                  <span
                    key={k.word || k.keyword || i}
                    className="tag-cloud-item"
                    style={{ fontSize: size, color: CLOUD_COLORS[i % CLOUD_COLORS.length] }}
                  >
                    {k.word || k.keyword}
                  </span>
                );
              })}
            </div>
          )}
        </GlassCard>
      </div>

      <div className="grid-cols-2">
        <GlassCard>
          <h3 className="section-title">
            <ThumbsUp size={17} className="text-success" /> Strengths
          </h3>
          {data.strengths?.length ? (
            <ul className="list-reset check-list">
              {data.strengths.map((s, i) => (
                <li key={i}>
                  <ThumbsUp size={14} className="text-success" style={{ marginTop: 3, flexShrink: 0 }} /> {s}
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-muted">No strengths identified yet.</p>
          )}
        </GlassCard>

        <GlassCard>
          <h3 className="section-title">
            <ThumbsDown size={17} className="text-danger" /> Weaknesses
          </h3>
          {data.weaknesses?.length ? (
            <ul className="list-reset check-list">
              {data.weaknesses.map((w, i) => (
                <li key={i}>
                  <ThumbsDown size={14} className="text-danger" style={{ marginTop: 3, flexShrink: 0 }} /> {w}
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-muted">No weaknesses identified yet.</p>
          )}
        </GlassCard>
      </div>
    </div>
  );
}
