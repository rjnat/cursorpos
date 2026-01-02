import api from './api';

/**
 * Approval Service
 * Handles manager approval requests for discounts, refunds, etc.
 */
const approvalService = {
    /**
     * Get list of approval requests with filters
     * @param {Object} filters - Filter options (status, dateRange, search, page, limit)
     * @returns {Promise<{data: Array, total: number}>}
     */
    async getApprovals(filters = {}) {
        const params = new URLSearchParams();

        if (filters.status) params.append('status', filters.status);
        if (filters.dateRange) params.append('dateRange', filters.dateRange);
        if (filters.search) params.append('search', filters.search);
        if (filters.page) params.append('page', filters.page);
        if (filters.limit) params.append('limit', filters.limit);

        const response = await api.get(`/approvals?${params.toString()}`);
        return response.data;
    },

    /**
     * Approve a request
     * @param {string} approvalId - The ID of the approval request
     * @param {string} managerId - The ID of the manager approving
     * @returns {Promise<Object>}
     */
    async approveRequest(approvalId, managerId) {
        const response = await api.post(`/approvals/${approvalId}/approve`, {
            managerId,
            approvedAt: new Date().toISOString()
        });
        return response.data;
    },

    /**
     * Reject a request
     * @param {string} approvalId - The ID of the approval request
     * @param {string} managerId - The ID of the manager rejecting
     * @returns {Promise<Object>}
     */
    async rejectRequest(approvalId, managerId) {
        const response = await api.post(`/approvals/${approvalId}/reject`, {
            managerId,
            rejectedAt: new Date().toISOString()
        });
        return response.data;
    },

    /**
     * Export approval history as CSV
     * @param {Object} filters - Filter options (status, dateRange)
     * @returns {Promise<Blob>}
     */
    async exportApprovals(filters = {}) {
        const params = new URLSearchParams();

        if (filters.status) params.append('status', filters.status);
        if (filters.dateRange) params.append('dateRange', filters.dateRange);

        const response = await api.get(`/approvals/export?${params.toString()}`, {
            responseType: 'blob'
        });
        return response.data;
    },

    /**
     * Create a new approval request
     * @param {Object} request - Request details
     * @returns {Promise<Object>}
     */
    async createRequest(request) {
        const response = await api.post('/approvals', {
            ...request,
            status: 'PENDING',
            createdAt: new Date().toISOString()
        });
        return response.data;
    },

    /**
     * Get approval request by ID
     * @param {string} approvalId - The ID of the approval request
     * @returns {Promise<Object>}
     */
    async getApprovalById(approvalId) {
        const response = await api.get(`/approvals/${approvalId}`);
        return response.data;
    },

    /**
     * Get approval statistics for dashboard
     * @param {Object} filters - Filter options (dateRange)
     * @returns {Promise<Object>}
     */
    async getApprovalStats(filters = {}) {
        const params = new URLSearchParams();

        if (filters.dateRange) params.append('dateRange', filters.dateRange);

        const response = await api.get(`/approvals/stats?${params.toString()}`);
        return response.data;
    }
};

export { approvalService };
