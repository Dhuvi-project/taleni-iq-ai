import client from './client';

export const getCareerRecommendations = (resumeId) =>
  client.get(`/careers/recommendations/${resumeId}`).then((r) => r.data);
