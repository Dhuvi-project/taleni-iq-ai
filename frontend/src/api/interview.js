import client from './client';

export const generateInterviewQuestions = (payload) =>
  client.post('/interview/generate', payload).then((r) => r.data);
