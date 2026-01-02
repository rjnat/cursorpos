import api from './api';
import indexedDBService from './indexedDB';

/**
 * Transaction Service - API calls for transaction/order management with offline support
 */

/**
 * Create a new transaction (with offline queue support)
 * @param {Object} transactionData - Transaction details
 * @returns {Promise} - Created transaction response or queued order
 */
export const createTransaction = async (transactionData) => {
    try {
        // If online, try to create transaction immediately
        if (navigator.onLine) {
            const response = await api.post('/transactions', transactionData);
            return response.data;
        }

        // If offline, queue the order
        console.log('Offline - queueing order for later sync');
        const queuedOrder = await indexedDBService.queueOrder({
            orderData: transactionData,
            tenantId: transactionData.tenantId,
            storeId: transactionData.storeId
        });

        // Return a mock response for offline order
        return {
            id: queuedOrder.clientOrderId,
            transactionNumber: queuedOrder.clientOrderId,
            status: 'PENDING_SYNC',
            offline: true,
            queuedAt: queuedOrder.createdAt,
            ...transactionData
        };
    } catch (error) {
        console.error('Error creating transaction:', error);

        // If the request failed due to network error, queue it
        if (!navigator.onLine || error.code === 'ERR_NETWORK') {
            console.log('Network error - queueing order for later sync');
            try {
                const queuedOrder = await indexedDBService.queueOrder({
                    orderData: transactionData,
                    tenantId: transactionData.tenantId,
                    storeId: transactionData.storeId
                });

                return {
                    id: queuedOrder.clientOrderId,
                    transactionNumber: queuedOrder.clientOrderId,
                    status: 'PENDING_SYNC',
                    offline: true,
                    queuedAt: queuedOrder.createdAt,
                    ...transactionData
                };
            } catch (queueError) {
                console.error('Failed to queue order:', queueError);
                throw queueError;
            }
        }

        throw error;
    }
};

/**
 * Get transaction by ID
 * @param {string} transactionId - Transaction UUID
 * @returns {Promise} - Transaction details
 */
export const getTransactionById = async (transactionId) => {
    try {
        const response = await api.get(`/transactions/${transactionId}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching transaction:', error);
        throw error;
    }
};

/**
 * Get transaction by number
 * @param {string} transactionNumber - Transaction number
 * @returns {Promise} - Transaction details
 */
export const getTransactionByNumber = async (transactionNumber) => {
    try {
        const response = await api.get(`/transactions/number/${transactionNumber}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching transaction by number:', error);
        throw error;
    }
};

/**
 * Get all transactions (paginated)
 * @param {Object} params - Query parameters (page, size, etc.)
 * @returns {Promise} - Transaction list with pagination
 */
export const getAllTransactions = async (params = {}) => {
    try {
        const response = await api.get('/transactions', { params });
        return response.data;
    } catch (error) {
        console.error('Error fetching transactions:', error);
        throw error;
    }
};

/**
 * Get transactions by branch
 * @param {string} branchId - Branch UUID
 * @param {Object} params - Query parameters (page, size)
 * @returns {Promise} - Transaction list
 */
export const getTransactionsByBranch = async (branchId, params = {}) => {
    try {
        const response = await api.get(`/transactions/branch/${branchId}`, { params });
        return response.data;
    } catch (error) {
        console.error('Error fetching transactions by branch:', error);
        throw error;
    }
};

/**
 * Get transactions by status
 * @param {string} status - Transaction status (PENDING, COMPLETED, CANCELLED)
 * @param {Object} params - Query parameters (page, size)
 * @returns {Promise} - Transaction list
 */
export const getTransactionsByStatus = async (status, params = {}) => {
    try {
        const response = await api.get(`/transactions/status/${status}`, { params });
        return response.data;
    } catch (error) {
        console.error('Error fetching transactions by status:', error);
        throw error;
    }
};

/**
 * Get transactions by date range
 * @param {string} startDate - Start date (ISO format)
 * @param {string} endDate - End date (ISO format)
 * @param {Object} params - Query parameters (page, size)
 * @returns {Promise} - Transaction list
 */
export const getTransactionsByDateRange = async (startDate, endDate, params = {}) => {
    try {
        const response = await api.get('/transactions/date-range', {
            params: { startDate, endDate, ...params }
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching transactions by date range:', error);
        throw error;
    }
};

/**
 * Cancel a transaction
 * @param {string} transactionId - Transaction UUID
 * @returns {Promise} - Cancelled transaction response
 */
export const cancelTransaction = async (transactionId) => {
    try {
        const response = await api.put(`/transactions/${transactionId}/cancel`);
        return response.data;
    } catch (error) {
        console.error('Error cancelling transaction:', error);
        throw error;
    }
};

/**
 * Generate receipt for a transaction
 * @param {string} transactionId - Transaction UUID
 * @returns {Promise} - Receipt details
 */
export const generateReceipt = async (transactionId) => {
    try {
        const response = await api.post(`/receipts/transaction/${transactionId}`);
        return response.data;
    } catch (error) {
        console.error('Error generating receipt:', error);
        throw error;
    }
};

/**
 * Get receipt by ID
 * @param {string} receiptId - Receipt UUID
 * @returns {Promise} - Receipt details
 */
export const getReceiptById = async (receiptId) => {
    try {
        const response = await api.get(`/receipts/${receiptId}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching receipt:', error);
        throw error;
    }
};

/**
 * Get receipt by transaction ID
 * @param {string} transactionId - Transaction UUID
 * @returns {Promise} - Receipt details
 */
export const getReceiptByTransactionId = async (transactionId) => {
    try {
        const response = await api.get(`/receipts/transaction/${transactionId}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching receipt by transaction:', error);
        throw error;
    }
};

/**
 * Mark receipt as printed
 * @param {string} receiptId - Receipt UUID
 * @returns {Promise} - Updated receipt
 */
export const printReceipt = async (receiptId) => {
    try {
        const response = await api.put(`/receipts/${receiptId}/print`);
        return response.data;
    } catch (error) {
        console.error('Error marking receipt as printed:', error);
        throw error;
    }
};
