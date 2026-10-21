import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
  // Local LLMs can be slow, especially on the first request.
  timeout: 180000,
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message ||
      (typeof error.response?.data === 'string' ? error.response.data : null) ||
      error.message ||
      'An unexpected error occurred';
    return Promise.reject({ message, status: error.response?.status });
  }
);

export default apiClient;
