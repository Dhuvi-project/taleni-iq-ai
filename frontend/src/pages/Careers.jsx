import { useEffect, useState } from 'react';
import { Compass, TrendingUp, MapPin, Building2, IndianRupee, GraduationCap } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { getResumesByUser } from '../api/resumes';
import { getCareerRecommendations } from '../api/careers';
import GlassCard from '../components/GlassCard';
import EmptyState from '../components/EmptyState';
import { PageLoader, Spinner } from '../components/Spinner';
import './shared.css';

function JobCard({ job, delay }) {
  const {
    jobTitle,
    company,
    location,
    matchScore = 0,
    salaryMinLPA,
    salaryMaxLPA,
    marketDemand,
    growthPercent,
    requiredUpskilling,
  } = job;
  const clampedScore = Math.max(0, Math.min(100, Math.round(matchScore)));

  return (
    <GlassCard className="fade-in-up job-rec-card" style={{ animationDelay: `${delay}s` }}>
      <div className="flex justify-between items-start" style={{ gap: 12, marginBottom: 6 }}>
        <div>
          <h3 style={{ marginBottom: 4 }}>{jobTitle}</h3>
          <div className="flex items-center gap-1 text-secondary" style={{ fontSize: 13.5 }}>
            <Building2 size={14} />
            <span>{company}</span>
          </div>
          <div className="flex items-center gap-1 text-muted" style={{ fontSize: 13, marginTop: 2 }}>
            <MapPin size={13} />
            <span>{location}</span>
          </div>
        </div>
        <div className="job-rec-score" title="Match score">
          <svg width="56" height="56" viewBox="0 0 56 56">
            <circle cx="28" cy="28" r="24" fill="none" stroke="var(--surface-200)" strokeWidth="6" />
            <circle
              cx="28"
              cy="28"
              r="24"
              fill="none"
              stroke="var(--color-primary)"
              strokeWidth="6"
              strokeLinecap="round"
              strokeDasharray={2 * Math.PI * 24}
              strokeDashoffset={2 * Math.PI * 24 * (1 - clampedScore / 100)}
              transform="rotate(-90 28 28)"
            />
          </svg>
          <span className="job-rec-score-value">{clampedScore}%</span>
        </div>
      </div>

      <div className="flex items-center gap-1" style={{ margin: '14px 0' }}>
        <span className="badge badge-primary">
          <IndianRupee size={12} />
          {salaryMinLPA} - {salaryMaxLPA} LPA
        </span>
        {typeof growthPercent === 'number' && (
          <span className="badge badge-success">
            <TrendingUp size={12} /> +{growthPercent}%
          </span>
        )}
      </div>

      {marketDemand && (
        <p className="text-secondary job-rec-demand" style={{ fontSize: 13.5, marginBottom: 14 }}>
          <TrendingUp size={14} className="text-gradient" style={{ marginRight: 6, verticalAlign: '-2px' }} />
          {marketDemand}
        </p>
      )}

      {requiredUpskilling?.length > 0 && (
        <>
          <h4 style={{ fontSize: 13, marginBottom: 10 }} className="text-muted">
            <GraduationCap size={14} style={{ marginRight: 4, verticalAlign: '-2px' }} />
            Recommended Upskilling
          </h4>
          <div className="tag-cloud">
            {requiredUpskilling.map((s) => (
              <span key={s} className="chip chip-primary">{s}</span>
            ))}
          </div>
        </>
      )}
    </GlassCard>
  );
}

export default function Careers() {
  const { user } = useAuth();
  const [resumes, setResumes] = useState([]);
  const [selectedResume, setSelectedResume] = useState('');
  const [resumesLoading, setResumesLoading] = useState(true);
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    getResumesByUser(user.id)
      .then((list) => {
        const arr = Array.isArray(list) ? list : [];
        setResumes(arr);
        if (arr.length) setSelectedResume(String(arr[0].id));
      })
      .catch(() => {})
      .finally(() => setResumesLoading(false));
  }, [user.id]);

  useEffect(() => {
    if (!selectedResume) return;
    let mounted = true;
    setLoading(true);
    setError(null);
    getCareerRecommendations(selectedResume)
      .then((data) => mounted && setRecommendations(Array.isArray(data) ? data : []))
      .catch((err) => mounted && setError(err.friendlyMessage))
      .finally(() => mounted && setLoading(false));
    return () => {
      mounted = false;
    };
  }, [selectedResume]);

  return (
    <div className="fade-in">
      <div className="page-header">
        <div>
          <h1>Recommended Jobs for You</h1>
          <p className="text-secondary">
            AI-matched job openings based on your resume, with realistic Indian IT market salary bands.
          </p>
        </div>
        {resumes.length > 0 && (
          <select className="select" style={{ maxWidth: 260 }} value={selectedResume} onChange={(e) => setSelectedResume(e.target.value)}>
            {resumes.map((r) => (
              <option key={r.id} value={r.id}>{r.fileName} (v{r.version})</option>
            ))}
          </select>
        )}
      </div>

      {resumesLoading ? (
        <PageLoader />
      ) : resumes.length === 0 ? (
        <GlassCard>
          <EmptyState
            icon={Compass}
            title="No resume found"
            message="Upload a resume first to get personalized job recommendations."
          />
        </GlassCard>
      ) : loading ? (
        <div className="flex items-center gap-1"><Spinner /> Finding recommended jobs...</div>
      ) : error ? (
        <GlassCard><EmptyState title="Couldn't load recommendations" message={error} /></GlassCard>
      ) : recommendations.length === 0 ? (
        <GlassCard>
          <EmptyState
            icon={Compass}
            title="No recommendations yet"
            message="Complete your profile skills for more personalized job recommendations."
          />
        </GlassCard>
      ) : (
        <div className="grid-cols-2">
          {recommendations.map((job, i) => (
            <JobCard key={`${job.jobTitle}-${job.company}-${i}`} job={job} delay={i * 0.06} />
          ))}
        </div>
      )}
    </div>
  );
}
