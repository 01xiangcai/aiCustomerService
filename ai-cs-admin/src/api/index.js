import request from '@/utils/request'

/** 认证相关 API */
export const authApi = {
    register: (data) => request.post('/api/auth/register', data),
    login: (data) => request.post('/api/auth/login', data)
}

/** 应用管理 API */
export const appApi = {
    create: (data) => request.post('/api/apps', data),
    list: (params) => request.get('/api/apps', { params }),
    detail: (id) => request.get(`/api/apps/${id}`),
    update: (id, data) => request.put(`/api/apps/${id}`, data),
    resetSecret: (id) => request.post(`/api/apps/${id}/reset-secret`)
}

/** 知识库 API */
export const knowledgeApi = {
    upload: (appId, formData) => request.post(`/api/knowledge/${appId}/upload`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    }),
    listDocuments: (appId, params) => request.get(`/api/knowledge/${appId}/documents`, { params }),
    deleteDocument: (appId, docId) => request.delete(`/api/knowledge/${appId}/documents/${docId}`)
}

/** 统计 API */
export const statsApi = {
    overview: (appId) => request.get(`/api/stats/${appId}/overview`),
    trend: (appId, days = 7) => request.get(`/api/stats/${appId}/trend`, { params: { days } })
}
