import axiosClient from './axiosClient';
import { API_ROUTES } from '../constants/app';

// Mirrors ai-service's AI controllers under /api/v1/ai/*, plus the closely
// related /analytics/alerts and /audit surfaces. Every call here returns a
// real backend response — the UI never fabricates a recommendation or score.
const ai = API_ROUTES.ai;

export const recommendationHistoryService = {
  async list({ page = 0, size = 20, sort } = {}) {
    const { data } = await axiosClient.get(`${ai}/recommendation-history`, { params: { page, size, sort } });
    return data;
  },
  async getForTask(taskId) {
    const { data } = await axiosClient.get(`${ai}/recommendation-history/task/${taskId}`);
    return data;
  },
  async recordDecision(id, { accepted, actualHours }) {
    const { data } = await axiosClient.patch(`${ai}/recommendation-history/${id}/decision`, { accepted, actualHours });
    return data;
  },
};

export const aiService = {
  // Task Estimation (Feature 2)
  async estimateTask({ description, priority, complexity, technology }) {
    const { data } = await axiosClient.post(`${ai}/estimation`, { description, priority, complexity, technology });
    return data; // TaskEstimationResponse
  },

  // Task Risk Prediction
  async getTaskRisk(taskId) {
    const { data } = await axiosClient.get(`${ai}/tasks/${taskId}/risk`);
    return data;
  },

  // Smart Task Assignment (Feature 1)
  async recommendAssignment({ taskId, projectId, requiredSkills, priority, estimatedHours, dueDate }) {
    const { data } = await axiosClient.post(`${ai}/recommendations/task-assignment`, {
      taskId,
      projectId,
      requiredSkills,
      priority,
      estimatedHours,
      dueDate,
    });
    return data; // TaskAssignmentRecommendationResponse[]
  },
  async suggestReassignments(employeeId) {
    const { data } = await axiosClient.get(`${ai}/recommendations/reassignment/${employeeId}`);
    return data; // ReassignmentSuggestionResponse[]
  },

  // Recommendation History (Feature 8)
  async recommendationHistoryForTask(taskId) {
    return recommendationHistoryService.getForTask(taskId);
  },
  async recommendationHistory(params) {
    return recommendationHistoryService.list(params);
  },
  async recordRecommendationDecision(id, { accepted, actualHours }) {
    return recommendationHistoryService.recordDecision(id, { accepted, actualHours });
  },

  // HR Policy Chatbot (RAG Prototype)
  async askPolicyQuestion(query) {
    const { data } = await axiosClient.post(`${ai}/policy/chat`, { query });
    return data;
  },
  
  // Daily Manager Brief (Feature 4)
  async dailyBrief() {
    const { data } = await axiosClient.get(`${ai}/daily-brief`);
    return data;
  },

  // Burnout Detection (Feature 6)
  async burnoutAssess(employeeId) {
    const { data } = await axiosClient.get(`${ai}/burnout/${employeeId}`);
    return data;
  },

  // Live alerts
  async alerts() {
    const { data } = await axiosClient.get(`${API_ROUTES.analytics}/alerts`);
    return data;
  },

  // Audit trail
  async auditForEntity(entityType, entityId, { page = 0, size = 20 } = {}) {
    const { data } = await axiosClient.get(`${API_ROUTES.audit}/entity/${entityType}/${entityId}`, {
      params: { page, size },
    });
    return data;
  },
  async auditForProject(projectId, { page = 0, size = 20 } = {}) {
    const { data } = await axiosClient.get(`${API_ROUTES.audit}/project/${projectId}`, { params: { page, size } });
    return data;
  },
  async auditAll({ page = 0, size = 20 } = {}) {
    const { data } = await axiosClient.get(API_ROUTES.audit, { params: { page, size } });
    return data;
  },
};
