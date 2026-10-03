import axios from 'axios';
import { supabase } from './supabaseClient';

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
});

client.interceptors.request.use(async (config) => {
  const { data } = await supabase.auth.getSession();
  const token = data?.session?.access_token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let onUnauthorized = null;
export const setUnauthorizedHandler = (fn) => {
  onUnauthorized = fn;
};

// The Render free-tier backend spins down after idle and can take over a minute to cold-start;
// during that window its edge proxy returns 429 for requests that arrive before it's ready.
let onWakingUp = null;
export const setWakingUpHandler = (fn) => {
  onWakingUp = fn;
};

const COLD_START_RETRY_DELAYS_MS = [2000, 4000, 8000, 15000, 20000, 20000];

const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

client.interceptors.response.use(
  (response) => {
    if (onWakingUp) onWakingUp(0, 0);
    return response;
  },
  async (error) => {
    if (error.response && error.response.status === 429) {
      const config = error.config;
      const attempt = config._coldStartRetryCount || 0;
      if (attempt < COLD_START_RETRY_DELAYS_MS.length) {
        config._coldStartRetryCount = attempt + 1;
        if (onWakingUp) onWakingUp(attempt + 1, COLD_START_RETRY_DELAYS_MS.length);
        await wait(COLD_START_RETRY_DELAYS_MS[attempt]);
        return client(config);
      }
    }
    if (error.response && error.response.status === 401) {
      if (onUnauthorized) onUnauthorized();
    }
    if (onWakingUp) onWakingUp(0, 0);
    const message =
      error.response?.status === 429
        ? 'The server is taking longer than usual to wake up. Please try again in a moment.'
        : error.response?.data?.message ||
          error.response?.data?.error ||
          error.message ||
          'Something went wrong. Please try again.';
    return Promise.reject({ ...error, friendlyMessage: message });
  }
);

export default client;
