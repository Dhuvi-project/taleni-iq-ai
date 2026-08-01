import client from './client';

export const getSkillGap = (resumeId) => client.get(`/skills/gap/${resumeId}`).then((r) => r.data);
