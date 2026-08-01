import { useCallback, useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { UploadCloud, FileText, CheckCircle2, Loader2, ScanSearch, Gauge, PartyPopper } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { uploadResume, getResumesByUser } from '../api/resumes';
import GlassCard from '../components/GlassCard';
import EmptyState from '../components/EmptyState';
import { Spinner } from '../components/Spinner';
import './shared.css';

const MAX_SIZE = 10 * 1024 * 1024;
const ACCEPTED = ['.pdf', '.doc', '.docx'];

const STAGES = [
  { key: 'uploading', label: 'Uploading', icon: UploadCloud },
  { key: 'extracting', label: 'Extracting', icon: ScanSearch },
  { key: 'scoring', label: 'Scoring', icon: Gauge },
  { key: 'done', label: 'Done', icon: PartyPopper },
];

export default function UploadResume() {
  const { user } = useAuth();
  const toast = useToast();
  const [dragging, setDragging] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [stageIndex, setStageIndex] = useState(-1);
  const [resumes, setResumes] = useState([]);
  const [loadingList, setLoadingList] = useState(true);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

  const loadResumes = useCallback(() => {
    setLoadingList(true);
    getResumesByUser(user.id)
      .then((data) => setResumes(Array.isArray(data) ? data : []))
      .catch((err) => setError(err.friendlyMessage))
      .finally(() => setLoadingList(false));
  }, [user.id]);

  useEffect(() => {
    loadResumes();
  }, [loadResumes]);

  const validateFile = (file) => {
    const ext = '.' + file.name.split('.').pop().toLowerCase();
    if (!ACCEPTED.includes(ext)) {
      toast.error('Only PDF and DOCX files are supported.');
      return false;
    }
    if (file.size > MAX_SIZE) {
      toast.error('File must be 10MB or smaller.');
      return false;
    }
    return true;
  };

  const handleFile = async (file) => {
    if (!validateFile(file)) return;
    setUploading(true);
    setProgress(0);
    setStageIndex(0);
    try {
      const result = await uploadResume(file, (evt) => {
        if (evt.total) {
          const pct = Math.round((evt.loaded * 100) / evt.total);
          setProgress(pct);
          if (pct >= 100) setStageIndex(1);
        }
      });
      setStageIndex(2);
      await new Promise((r) => setTimeout(r, 700));
      setStageIndex(3);
      toast.success(`Resume uploaded! ATS score: ${result.atsScore}`);
      loadResumes();
      await new Promise((r) => setTimeout(r, 900));
    } catch (err) {
      toast.error(err.friendlyMessage || 'Upload failed. Please try again.');
    } finally {
      setUploading(false);
      setStageIndex(-1);
      setProgress(0);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const onDrop = (e) => {
    e.preventDefault();
    setDragging(false);
    if (uploading) return;
    const file = e.dataTransfer.files?.[0];
    if (file) handleFile(file);
  };

  return (
    <div className="fade-in">
      <div className="page-header">
        <div>
          <h1>Upload Resume</h1>
          <p className="text-secondary">Drag and drop your resume — PDF or DOCX, up to 10MB.</p>
        </div>
      </div>

      <GlassCard style={{ marginBottom: 24 }}>
        <div
          className={`upload-dropzone ${dragging ? 'dragging' : ''}`}
          onDragOver={(e) => {
            e.preventDefault();
            setDragging(true);
          }}
          onDragLeave={() => setDragging(false)}
          onDrop={onDrop}
          onClick={() => !uploading && fileInputRef.current?.click()}
        >
          <div className="upload-dropzone-icon">
            <UploadCloud size={28} />
          </div>
          <h3>Drag & drop your resume here</h3>
          <p className="text-muted">or click to browse · PDF, DOC, DOCX · max 10MB</p>
          <input
            ref={fileInputRef}
            type="file"
            accept=".pdf,.doc,.docx"
            hidden
            onChange={(e) => e.target.files?.[0] && handleFile(e.target.files[0])}
          />
        </div>

        {uploading && (
          <>
            <div className="progress-bar-track">
              <div className="progress-bar-fill" style={{ width: `${progress}%` }} />
            </div>
            <div className="parsing-stages">
              {STAGES.map((stage, i) => {
                const isActive = i === stageIndex;
                const isDone = i < stageIndex;
                const Icon = stage.icon;
                return (
                  <div key={stage.key} className={`parsing-stage ${isActive ? 'active' : ''} ${isDone ? 'done' : ''}`}>
                    <div className="parsing-stage-icon">
                      {isActive ? <Loader2 size={16} className="spin" /> : isDone ? <CheckCircle2 size={16} /> : <Icon size={16} />}
                    </div>
                    <div className="parsing-stage-label">{stage.label}</div>
                  </div>
                );
              })}
            </div>
          </>
        )}
      </GlassCard>

      <GlassCard>
        <h3 className="section-title">
          <FileText size={18} /> Your Current Resume
        </h3>
        {loadingList ? (
          <div className="flex items-center gap-1"><Spinner /> Loading...</div>
        ) : error ? (
          <p className="text-danger">{error}</p>
        ) : resumes.length === 0 ? (
          <EmptyState icon={FileText} title="No resume yet" message="Upload your resume above to get started." />
        ) : (
          (() => {
            const current = resumes[0];
            return (
              <div className="flex items-center justify-between" style={{ flexWrap: 'wrap', gap: 12 }}>
                <div>
                  <h4 style={{ marginBottom: 4 }}>{current.fileName}</h4>
                  <p className="text-muted" style={{ marginBottom: 0, fontSize: 13.5 }}>
                    Uploaded {new Date(current.createdAt).toLocaleDateString()}
                  </p>
                </div>
                <div className="flex items-center gap-1">
                  <span className={`badge ${current.atsScore >= 80 ? 'badge-success' : current.atsScore >= 50 ? 'badge-warning' : 'badge-danger'}`}>
                    ATS Score: {current.atsScore}
                  </span>
                  <Link to={`/analysis/${current.id}`} className="btn btn-sm btn-outline">
                    View Analysis
                  </Link>
                </div>
              </div>
            );
          })()
        )}
      </GlassCard>
    </div>
  );
}
