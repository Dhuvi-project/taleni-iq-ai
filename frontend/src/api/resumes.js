import client from './client';

export const uploadResume = (file, onUploadProgress) => {
  const formData = new FormData();
  formData.append('file', file);
  return client
    .post('/resumes/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress,
    })
    .then((r) => r.data);
};

export const getResumesByUser = (userId) => client.get(`/resumes/${userId}`).then((r) => r.data);

export const getResumeAnalysis = (resumeId) =>
  client.get(`/resumes/${resumeId}/analysis`).then((r) => r.data);
