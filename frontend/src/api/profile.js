import client from './client';

export const getProfile = () => client.get('/profile').then((r) => r.data);

export const updateProfile = (payload) => client.put('/profile', payload).then((r) => r.data);
