import client from './client';

export const sendAssistantMessage = (message, history = []) =>
  client.post('/assistant/chat', { message, history }).then((r) => r.data);
