import api from './api';

const authService = {
    /**
     * Login with credentials
     * @param {string} tenantId 
     * @param {string} email 
     * @param {string} password 
     * @returns {Promise<{token: string, user: object}>}
     */
    async login(tenantId, email, password) {
        const response = await api.post('/identity/auth/login', {
            tenant_id: tenantId,
            email,
            password,
        });

        const { token, user } = response.data;

        // Store token and user in localStorage
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));

        return { token, user };
    },

    /**
     * Logout user
     */
    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    },

    /**
     * Get current user from localStorage
     * @returns {object|null}
     */
    getCurrentUser() {
        const userStr = localStorage.getItem('user');
        return userStr ? JSON.parse(userStr) : null;
    },

    /**
     * Get current token from localStorage
     * @returns {string|null}
     */
    getToken() {
        return localStorage.getItem('token');
    },

    /**
     * Check if user is authenticated
     * @returns {boolean}
     */
    isAuthenticated() {
        return !!this.getToken();
    },

    /**
     * Get user stores for POS
     * @returns {Promise<Array>}
     */
    async getUserStores() {
        const response = await api.get('/identity/users/stores');
        return response.data;
    },
};

export default authService;
