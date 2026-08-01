import { useEffect, useState } from 'react';
import { Pencil, Save, X, Briefcase, GraduationCap, Plus, Trash2 } from 'lucide-react';
import { useToast } from '../context/ToastContext';
import { getProfile, updateProfile } from '../api/profile';
import GlassCard from '../components/GlassCard';
import EmptyState from '../components/EmptyState';
import { PageLoader, Spinner } from '../components/Spinner';
import './shared.css';

function emptyExp() {
  return { title: '', company: '', startDate: '', endDate: '', current: false };
}

function formatExpDuration(exp) {
  const start = exp.startDate || '?';
  const end = exp.current ? 'Present' : exp.endDate || '?';
  return `${start} - ${end}`;
}
function emptyEdu() {
  return { degree: '', institution: '', year: '' };
}

export default function Profile() {
  const toast = useToast();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editMode, setEditMode] = useState(false);
  const [form, setForm] = useState(null);
  const [saving, setSaving] = useState(false);
  const [skillInput, setSkillInput] = useState('');

  useEffect(() => {
    getProfile()
      .then((data) => {
        setProfile(data);
        setForm({
          headline: data.headline || '',
          experience: data.experience || [],
          education: data.education || [],
          skills: data.skills || [],
        });
      })
      .catch((err) => setError(err.friendlyMessage))
      .finally(() => setLoading(false));
  }, []);

  const startEdit = () => setEditMode(true);
  const cancelEdit = () => {
    setForm({
      headline: profile.headline || '',
      experience: profile.experience || [],
      education: profile.education || [],
      skills: profile.skills || [],
    });
    setEditMode(false);
  };

  const isBlank = (v) => !v || !String(v).trim();

  const isExpEmpty = (exp) => isBlank(exp.title) && isBlank(exp.company) && isBlank(exp.startDate) && isBlank(exp.endDate);
  const isEduEmpty = (edu) => isBlank(edu.degree) && isBlank(edu.institution) && isBlank(edu.year);

  const validateAndCleanForm = () => {
    for (let i = 0; i < form.experience.length; i++) {
      const exp = form.experience[i];
      if (isExpEmpty(exp)) continue;
      if (isBlank(exp.title)) return { error: `Experience #${i + 1}: title is required.` };
      if (isBlank(exp.company)) return { error: `Experience #${i + 1}: company is required.` };
      if (isBlank(exp.startDate)) return { error: `Experience #${i + 1}: start date is required.` };
      if (!exp.current && isBlank(exp.endDate)) {
        return { error: `Experience #${i + 1}: end date is required unless you're currently working here.` };
      }
    }
    for (let i = 0; i < form.education.length; i++) {
      const edu = form.education[i];
      if (isEduEmpty(edu)) continue;
      if (isBlank(edu.degree)) return { error: `Education #${i + 1}: degree is required.` };
      if (isBlank(edu.institution)) return { error: `Education #${i + 1}: institution is required.` };
      if (isBlank(edu.year)) return { error: `Education #${i + 1}: year is required.` };
    }
    return {
      cleaned: {
        ...form,
        experience: form.experience.filter((exp) => !isExpEmpty(exp)),
        education: form.education.filter((edu) => !isEduEmpty(edu)),
      },
    };
  };

  const handleSave = async () => {
    const { error: validationError, cleaned } = validateAndCleanForm();
    if (validationError) {
      toast.error(validationError);
      return;
    }
    setSaving(true);
    try {
      const updated = await updateProfile({ ...profile, ...cleaned });
      setProfile(updated);
      setForm({
        headline: updated.headline || '',
        experience: updated.experience || [],
        education: updated.education || [],
        skills: updated.skills || [],
      });
      setEditMode(false);
      toast.success('Profile updated successfully.');
    } catch (err) {
      toast.error(err.friendlyMessage || 'Failed to update profile.');
    } finally {
      setSaving(false);
    }
  };

  const addSkill = () => {
    if (skillInput.trim() && !form.skills.includes(skillInput.trim())) {
      setForm({ ...form, skills: [...form.skills, skillInput.trim()] });
      setSkillInput('');
    }
  };

  const removeSkill = (s) => setForm({ ...form, skills: form.skills.filter((x) => x !== s) });

  if (loading) return <PageLoader />;
  if (error) return <GlassCard><EmptyState title="Couldn't load profile" message={error} /></GlassCard>;
  if (!profile || !form) return null;

  const initials = (profile.name || '?')
    .split(' ')
    .map((p) => p[0])
    .slice(0, 2)
    .join('')
    .toUpperCase();

  return (
    <div className="fade-in">
      <div className="page-header">
        <div>
          <h1>Profile</h1>
          <p className="text-secondary">Manage your public profile and career details.</p>
        </div>
        {!editMode ? (
          <button className="btn btn-primary" onClick={startEdit}>
            <Pencil size={16} /> Edit Profile
          </button>
        ) : (
          <div className="flex gap-1">
            <button className="btn btn-secondary" onClick={cancelEdit} disabled={saving}>
              <X size={16} /> Cancel
            </button>
            <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
              {saving ? <Spinner size={16} /> : <><Save size={16} /> Save</>}
            </button>
          </div>
        )}
      </div>

      <GlassCard style={{ marginBottom: 22 }}>
        <div className="flex items-center gap-2">
          <span className="avatar-circle" style={{ width: 68, height: 68, fontSize: 24 }}>{initials}</span>
          <div style={{ flex: 1 }}>
            <h2 style={{ marginBottom: 2 }}>{profile.name}</h2>
            <p className="text-muted" style={{ marginBottom: 8 }}>{profile.email}</p>
            {editMode ? (
              <input
                className="input"
                placeholder="Your headline (e.g. Senior Frontend Engineer)"
                value={form.headline}
                onChange={(e) => setForm({ ...form, headline: e.target.value })}
              />
            ) : (
              <p style={{ marginBottom: 0 }}>{profile.headline || 'No headline set yet.'}</p>
            )}
          </div>
          <span className="badge badge-primary">{profile.role}</span>
        </div>
      </GlassCard>

      <div className="grid-cols-2" style={{ marginBottom: 22 }}>
        <GlassCard>
          <h3 className="section-title">
            <Briefcase size={17} /> Experience
          </h3>
          {(editMode ? form.experience : profile.experience || []).length === 0 && !editMode && (
            <p className="text-muted">No experience added yet.</p>
          )}
          <div className="timeline">
            {(editMode ? form.experience : profile.experience || []).map((exp, i) => (
              <div className="timeline-item" key={i}>
                <div className="timeline-dot-wrap">
                  <div className="timeline-dot"><Briefcase size={14} /></div>
                  {i < (editMode ? form.experience : profile.experience).length - 1 && <div className="timeline-line" />}
                </div>
                <div className="timeline-content" style={{ width: '100%' }}>
                  {editMode ? (
                    <div className="flex-col gap-1">
                      <input className="input" placeholder="Title" value={exp.title} onChange={(e) => {
                        const next = [...form.experience];
                        next[i] = { ...next[i], title: e.target.value };
                        setForm({ ...form, experience: next });
                      }} />
                      <input className="input" placeholder="Company" value={exp.company} onChange={(e) => {
                        const next = [...form.experience];
                        next[i] = { ...next[i], company: e.target.value };
                        setForm({ ...form, experience: next });
                      }} />
                      <div className="flex gap-1">
                        <input className="input" placeholder="Start date (e.g. Jan 2021)" value={exp.startDate} onChange={(e) => {
                          const next = [...form.experience];
                          next[i] = { ...next[i], startDate: e.target.value };
                          setForm({ ...form, experience: next });
                        }} />
                        <input
                          className="input"
                          placeholder="End date (e.g. Dec 2023)"
                          value={exp.current ? '' : exp.endDate}
                          disabled={exp.current}
                          onChange={(e) => {
                            const next = [...form.experience];
                            next[i] = { ...next[i], endDate: e.target.value };
                            setForm({ ...form, experience: next });
                          }}
                        />
                      </div>
                      <label className="flex items-center gap-1" style={{ fontSize: 13, cursor: 'pointer' }}>
                        <input
                          type="checkbox"
                          checked={exp.current}
                          onChange={(e) => {
                            const next = [...form.experience];
                            next[i] = { ...next[i], current: e.target.checked, endDate: e.target.checked ? '' : next[i].endDate };
                            setForm({ ...form, experience: next });
                          }}
                        />
                        Currently working here
                      </label>
                      <button className="btn btn-ghost btn-sm" onClick={() => setForm({ ...form, experience: form.experience.filter((_, idx) => idx !== i) })}>
                        <Trash2 size={14} /> Remove
                      </button>
                    </div>
                  ) : (
                    <>
                      <h4>{exp.title}</h4>
                      <div className="timeline-date">{exp.company} · {formatExpDuration(exp)}</div>
                    </>
                  )}
                </div>
              </div>
            ))}
          </div>
          {editMode && (
            <button className="btn btn-outline btn-sm" onClick={() => setForm({ ...form, experience: [...form.experience, emptyExp()] })}>
              <Plus size={14} /> Add Experience
            </button>
          )}
        </GlassCard>

        <GlassCard>
          <h3 className="section-title">
            <GraduationCap size={17} /> Education
          </h3>
          {(editMode ? form.education : profile.education || []).length === 0 && !editMode && (
            <p className="text-muted">No education added yet.</p>
          )}
          <div className="timeline">
            {(editMode ? form.education : profile.education || []).map((edu, i) => (
              <div className="timeline-item" key={i}>
                <div className="timeline-dot-wrap">
                  <div className="timeline-dot"><GraduationCap size={14} /></div>
                  {i < (editMode ? form.education : profile.education).length - 1 && <div className="timeline-line" />}
                </div>
                <div className="timeline-content" style={{ width: '100%' }}>
                  {editMode ? (
                    <div className="flex-col gap-1">
                      <input className="input" placeholder="Degree" value={edu.degree} onChange={(e) => {
                        const next = [...form.education];
                        next[i] = { ...next[i], degree: e.target.value };
                        setForm({ ...form, education: next });
                      }} />
                      <input className="input" placeholder="Institution" value={edu.institution} onChange={(e) => {
                        const next = [...form.education];
                        next[i] = { ...next[i], institution: e.target.value };
                        setForm({ ...form, education: next });
                      }} />
                      <input className="input" placeholder="Year" value={edu.year} onChange={(e) => {
                        const next = [...form.education];
                        next[i] = { ...next[i], year: e.target.value };
                        setForm({ ...form, education: next });
                      }} />
                      <button className="btn btn-ghost btn-sm" onClick={() => setForm({ ...form, education: form.education.filter((_, idx) => idx !== i) })}>
                        <Trash2 size={14} /> Remove
                      </button>
                    </div>
                  ) : (
                    <>
                      <h4>{edu.degree}</h4>
                      <div className="timeline-date">{edu.institution} · {edu.year}</div>
                    </>
                  )}
                </div>
              </div>
            ))}
          </div>
          {editMode && (
            <button className="btn btn-outline btn-sm" onClick={() => setForm({ ...form, education: [...form.education, emptyEdu()] })}>
              <Plus size={14} /> Add Education
            </button>
          )}
        </GlassCard>
      </div>

      <GlassCard>
        <h3 className="section-title">Skills</h3>
        <div className="tag-cloud" style={{ marginBottom: editMode ? 16 : 0 }}>
          {(editMode ? form.skills : profile.skills || []).length === 0 ? (
            <p className="text-muted">No skills added yet.</p>
          ) : (
            (editMode ? form.skills : profile.skills).map((s) => (
              <span key={s} className="chip chip-primary">
                {s}
                {editMode && (
                  <X size={12} style={{ cursor: 'pointer' }} onClick={() => removeSkill(s)} />
                )}
              </span>
            ))
          )}
        </div>
        {editMode && (
          <div className="flex gap-1">
            <input
              className="input"
              placeholder="Add a skill and press Enter"
              value={skillInput}
              onChange={(e) => setSkillInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), addSkill())}
            />
            <button className="btn btn-secondary" onClick={addSkill}>
              <Plus size={16} />
            </button>
          </div>
        )}
      </GlassCard>
    </div>
  );
}
