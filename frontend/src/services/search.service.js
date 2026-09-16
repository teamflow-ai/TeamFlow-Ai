import axiosClient from './axiosClient';
import { API_ROUTES } from '../constants/app';

// Mirrors project-service GlobalSearchController (/api/v1/search).
export const searchService = {
  async search(query) {
    if (!query || !query.trim()) return null;
    const { data } = await axiosClient.get(API_ROUTES.search, { params: { query } });
    return data; // GlobalSearchResponse
  },
};
