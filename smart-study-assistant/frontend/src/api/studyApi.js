import apiClient from './client';

export const studyApi = {
  uploadMaterial: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post('/api/v1/study/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  generateQuiz: async (targetTopic, numberOfQuestions) => {
    const response = await apiClient.post('/api/v1/study/generate-quiz', { targetTopic, numberOfQuestions });
    return response.data;
  },

  askQuestion: async (question) => {
    const response = await apiClient.post('/api/v1/study/ask', { question });
    return response.data;
  },

  listDocuments: async () => {
    const response = await apiClient.get('/api/v1/study/documents');
    return response.data;
  },

  deleteDocument: async (docId) => {
    const response = await apiClient.delete(`/api/v1/study/documents/${docId}`);
    return response.data;
  },
};
