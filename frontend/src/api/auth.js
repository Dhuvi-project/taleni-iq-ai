import client from './client';

export const login = (payload) => client.post('/auth/login', payload).then((r) => r.data);

export const getMe = () => client.get('/auth/me').then((r) => r.data);
