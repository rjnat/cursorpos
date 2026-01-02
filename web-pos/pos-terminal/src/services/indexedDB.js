/**
 * IndexedDB Service for Offline Support
 * Manages local storage of products and offline order queue
 */

const DB_NAME = 'cursorpos_db';
const DB_VERSION = 1;
const PRODUCTS_STORE = 'products_cache';
const ORDERS_STORE = 'orders_queue';

class IndexedDBService {
    constructor() {
        this.db = null;
        this.initPromise = null;
    }

    /**
     * Initialize IndexedDB database
     * @returns {Promise<IDBDatabase>}
     */
    async init() {
        if (this.db) {
            return this.db;
        }

        if (this.initPromise) {
            return this.initPromise;
        }

        this.initPromise = new Promise((resolve, reject) => {
            const request = indexedDB.open(DB_NAME, DB_VERSION);

            request.onerror = () => {
                reject(new Error(`Failed to open IndexedDB: ${request.error}`));
            };

            request.onsuccess = () => {
                this.db = request.result;
                resolve(this.db);
            };

            request.onupgradeneeded = (event) => {
                const db = event.target.result;

                // Create products cache store
                if (!db.objectStoreNames.contains(PRODUCTS_STORE)) {
                    const productsStore = db.createObjectStore(PRODUCTS_STORE, { keyPath: 'id' });
                    productsStore.createIndex('tenantId', 'tenantId', { unique: false });
                    productsStore.createIndex('sku', 'sku', { unique: false });
                    productsStore.createIndex('cachedAt', 'cachedAt', { unique: false });
                }

                // Create orders queue store
                if (!db.objectStoreNames.contains(ORDERS_STORE)) {
                    const ordersStore = db.createObjectStore(ORDERS_STORE, {
                        keyPath: 'clientOrderId'
                    });
                    ordersStore.createIndex('tenantId', 'tenantId', { unique: false });
                    ordersStore.createIndex('storeId', 'storeId', { unique: false });
                    ordersStore.createIndex('status', 'status', { unique: false });
                    ordersStore.createIndex('createdAt', 'createdAt', { unique: false });
                }
            };
        });

        return this.initPromise;
    }

    /**
     * Save products to cache
     * @param {Array} products - Array of product objects
     * @returns {Promise<void>}
     */
    async cacheProducts(products) {
        await this.init();
        const transaction = this.db.transaction([PRODUCTS_STORE], 'readwrite');
        const store = transaction.objectStore(PRODUCTS_STORE);
        const cachedAt = new Date().toISOString();

        const promises = products.map((product) => {
            return new Promise((resolve, reject) => {
                const request = store.put({ ...product, cachedAt });
                request.onsuccess = () => resolve();
                request.onerror = () => reject(request.error);
            });
        });

        await Promise.all(promises);
    }

    /**
     * Get cached products
     * @param {string} tenantId - Tenant ID
     * @returns {Promise<Array>}
     */
    async getCachedProducts(tenantId) {
        await this.init();
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([PRODUCTS_STORE], 'readonly');
            const store = transaction.objectStore(PRODUCTS_STORE);
            const index = store.index('tenantId');
            const request = index.getAll(tenantId);

            request.onsuccess = () => {
                resolve(request.result || []);
            };
            request.onerror = () => reject(request.error);
        });
    }

    /**
     * Search cached products
     * @param {string} tenantId - Tenant ID
     * @param {string} query - Search query
     * @returns {Promise<Array>}
     */
    async searchCachedProducts(tenantId, query) {
        const products = await this.getCachedProducts(tenantId);

        if (!query || query.trim() === '') {
            return products;
        }

        const lowerQuery = query.toLowerCase();
        return products.filter((product) => {
            return (
                product.name?.toLowerCase().includes(lowerQuery) ||
                product.sku?.toLowerCase().includes(lowerQuery) ||
                product.category?.toLowerCase().includes(lowerQuery)
            );
        });
    }

    /**
     * Clear product cache
     * @returns {Promise<void>}
     */
    async clearProductCache() {
        await this.init();
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([PRODUCTS_STORE], 'readwrite');
            const store = transaction.objectStore(PRODUCTS_STORE);
            const request = store.clear();

            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }

    /**
     * Add order to queue
     * @param {Object} order - Order object
     * @returns {Promise<void>}
     */
    async queueOrder(order) {
        await this.init();
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([ORDERS_STORE], 'readwrite');
            const store = transaction.objectStore(ORDERS_STORE);

            const queuedOrder = {
                ...order,
                clientOrderId: order.clientOrderId || `offline_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                status: 'PENDING',
                createdAt: new Date().toISOString(),
                syncAttempts: 0,
                errorMessage: null
            };

            const request = store.add(queuedOrder);

            request.onsuccess = () => resolve(queuedOrder);
            request.onerror = () => reject(request.error);
        });
    }

    /**
     * Get all queued orders
     * @param {string} status - Filter by status (optional)
     * @returns {Promise<Array>}
     */
    async getQueuedOrders(status = null) {
        await this.init();
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([ORDERS_STORE], 'readonly');
            const store = transaction.objectStore(ORDERS_STORE);

            let request;
            if (status) {
                const index = store.index('status');
                request = index.getAll(status);
            } else {
                request = store.getAll();
            }

            request.onsuccess = () => {
                resolve(request.result || []);
            };
            request.onerror = () => reject(request.error);
        });
    }

    /**
     * Update order status in queue
     * @param {string} clientOrderId - Client order ID
     * @param {string} status - New status
     * @param {string} errorMessage - Error message (optional)
     * @returns {Promise<void>}
     */
    async updateOrderStatus(clientOrderId, status, errorMessage = null) {
        await this.init();
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([ORDERS_STORE], 'readwrite');
            const store = transaction.objectStore(ORDERS_STORE);
            const getRequest = store.get(clientOrderId);

            getRequest.onsuccess = () => {
                const order = getRequest.result;
                if (!order) {
                    reject(new Error(`Order not found: ${clientOrderId}`));
                    return;
                }

                order.status = status;
                order.syncAttempts = (order.syncAttempts || 0) + 1;
                order.lastAttemptAt = new Date().toISOString();
                if (errorMessage) {
                    order.errorMessage = errorMessage;
                }

                const putRequest = store.put(order);
                putRequest.onsuccess = () => resolve();
                putRequest.onerror = () => reject(putRequest.error);
            };

            getRequest.onerror = () => reject(getRequest.error);
        });
    }

    /**
     * Remove order from queue
     * @param {string} clientOrderId - Client order ID
     * @returns {Promise<void>}
     */
    async removeOrder(clientOrderId) {
        await this.init();
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([ORDERS_STORE], 'readwrite');
            const store = transaction.objectStore(ORDERS_STORE);
            const request = store.delete(clientOrderId);

            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }

    /**
     * Clear all synced orders from queue
     * @returns {Promise<number>} Number of orders removed
     */
    async clearSyncedOrders() {
        const syncedOrders = await this.getQueuedOrders('SYNCED');
        const promises = syncedOrders.map((order) => this.removeOrder(order.clientOrderId));
        await Promise.all(promises);
        return syncedOrders.length;
    }

    /**
     * Get queue statistics
     * @returns {Promise<Object>}
     */
    async getQueueStats() {
        const allOrders = await this.getQueuedOrders();
        return {
            total: allOrders.length,
            pending: allOrders.filter(o => o.status === 'PENDING').length,
            syncing: allOrders.filter(o => o.status === 'SYNCING').length,
            synced: allOrders.filter(o => o.status === 'SYNCED').length,
            failed: allOrders.filter(o => o.status === 'FAILED').length
        };
    }

    /**
     * Close database connection
     */
    close() {
        if (this.db) {
            this.db.close();
            this.db = null;
            this.initPromise = null;
        }
    }
}

// Export singleton instance
const indexedDBService = new IndexedDBService();
export default indexedDBService;
