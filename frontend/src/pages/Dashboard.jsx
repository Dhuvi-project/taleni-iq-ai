import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { FileText, Target, Sparkles, TrendingUp, UploadCloud, ArrowRight } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { getResumesByUser } from '../api/resumes';
import GlassCard from '../components/GlassCard';
import StatCard from '../components/StatCard';
import ScoreGauge from '../components/ScoreGauge';
import EmptyState from '../components/EmptyState';
import { PageLoader } from '../components/Spinner';
import './shared.css';

export default function Dashboard() {
  const { user } = useAuth();
  const [resumes, setResumes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let mounted = true;
    setLoading(true);
    getResumesByUser(user.id)
      .then((data) => mounted && setResumes(Array.isArray(data) ? data : []))
      .catch((err) => mounted && setError(err.friendlyMessage))
      .finally(() => mounted && setLoading(false));
    return () => {
      mounted = false;
    };
  }, [user.id]);

  if (loading) return <PageLoader />;

  const latest = resumes[0];
  const atsScore = latest?.atsScore ?? 0;
  const avgScore = resumes.length
    ? Math.round(resumes.reduce((s, r) => s + (r.atsScore || 0), 0) / resumes.length)
    : 0;

  return (
    <div className="fade-in">
      <div className="page-header">
        <div>
          <h1>Welcome back, {user.name.split(' ')[0]}</h1>
          <p className="text-secondary">Here's how your candidacy is shaping up.</p>
        </div>
        <Link to="/upload" className="btn btn-primary">
          <UploadCloud size={17} /> Upload Resume
        </Link>
      </div>

      <div className="insight-banner glass">
        <div className="insight-banner-icon">
          <Sparkles size={22} />
        </div>
        <div>
          <h3>AI Insight</h3>
          <p>
            {latest
              ? `Your latest resume "${latest.fileName}" scores ${atsScore}/100 on ATS compatibility. ${
                  atsScore >= 80
                    ? 'Excellent — keep it updated with your latest achievements.'
                    : 'Consider running an AI Analysis to close the gaps and boost your score.'
                }`
              : 'Upload your first resume to unlock personalized AI insights, ATS scoring, and job matches.'}
          </p>
        </div>
      </div>

      {error && <p className="text-danger">{error}</p>}

      {!latest ? (
        <GlassCard>
          <EmptyState
            icon={FileText}
            title="No resumes yet"
            message="Upload a resume to see your health score, ATS breakdown, and tailored job matches."
            action={
              <Link to="/upload" className="btn btn-primary">
                Upload your first resume
              </Link>
            }
          />
        </GlassCard>
      ) : (
        <>
          <div className="grid-cols-3" style={{ marginBottom: 22 }}>
            <GlassCard className="flex-col items-center" style={{ display: 'flex', justifyContent: 'center' }}>
              <ScoreGauge score={atsScore} label="Latest ATS Score" />
            </GlassCard>
            <StatCard icon={FileText} value={resumes.length} label="Resume Versions" />
            <StatCard icon={TrendingUp} value={`${avgScore}`} label="Average Score" />
          </div>

          <div className="grid-cols-2">
            <GlassCard>
              <h3 className="section-title">
                <Target size={18} /> Quick Actions
              </h3>
              <div className="flex-col gap-2">
                <Link to={`/analysis/${latest.id}`} className="btn btn-secondary btn-block">
                  View AI Analysis <ArrowRight size={15} />
                </Link>
                <Link to="/matching" className="btn btn-secondary btn-block">
                  Match Against a Job <ArrowRight size={15} />
                </Link>
                <Link to="/skill-gap" className="btn btn-secondary btn-block">
                  Analyze Skill Gaps <ArrowRight size={15} />
                </Link>
                <Link to="/interview" className="btn btn-secondary btn-block">
                  Practice Interview Questions <ArrowRight size={15} />
                </Link>
              </div>
            </GlassCard>

            <GlassCard>
              <h3 className="section-title">Activity Timeline</h3>
              <div className="timeline">
                {resumes.slice(0, 5).map((r, i) => (
                  <div className="timeline-item" key={r.id}>
                    <div className="timeline-dot-wrap">
                      <div className="timeline-dot">
                        <FileText size={15} />
                      </div>
                      {i < Math.min(resumes.length, 5) - 1 && <div className="timeline-line" />}
                    </div>
                    <div className="timeline-content">
                      <h4>{r.fileName} · v{r.version}</h4>
                      <div className="timeline-date">
                        {new Date(r.createdAt).toLocaleString()}
                      </div>
                      <span className="badge badge-primary">ATS {r.atsScore}</span>
                    </div>
                  </div>
                ))}
              </div>
            </GlassCard>
          </div>
        </>
      )}
    </div>
  );
}
