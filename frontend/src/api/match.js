import client from './client';

export const matchResumeToJob = (payload) => client.post('/match', payload).then((r) => r.data);
