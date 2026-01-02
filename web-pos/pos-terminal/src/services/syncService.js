/**
 * Sync Service for Offline/Online Synchronization
 * Handles automatic syncing of queued orders when online
 */

import toast from 'react-hot-toast';
import indexedDBService from './indexedDB';
import api from './api';

class SyncService {
    constructor() {
        this.isOnline = navigator.onLine;
        this.isSyncing = false;
        this.syncInterval = null;
        this.listeners = new Set();
        this.syncListeners = new Set();

        // Listen for online/offline events
        window.addEventListener('online', () => this.handleOnline());
        window.addEventListener('offline', () => this.handleOffline());
    }

    /**
     * Check if browser is online
     * @returns {boolean}
     */
    getIsOnline() {
        return navigator.onLine;
    }

    /**
     * Handle online event
     */
    handleOnline() {
        console.log('[SyncService] Connection restored');
        toast.success('Connection restored. Syncing orders...', { icon: '🌐' });
        this.isOnline = true;
        this.notifyListeners({ online: true });
        this.startAutoSync();
    }

    /**
     * Handle offline event
     */
    handleOffline() {
        console.log('[SyncService] Connection lost');
        toast.error('Connection lost. Working offline.', { icon: '📡', duration: 4000 });
        this.isOnline = false;
        this.notifyListeners({ online: false });
        this.stopAutoSync();
    }

    /**
     * Subscribe to online/offline status changes
     * @param {Function} callback - Callback function
     * @returns {Function} Unsubscribe function
     */
    subscribe(callback) {
        this.listeners.add(callback);
        // Immediately notify with current status
        callback({ online: this.isOnline });

        return () => {
            this.listeners.delete(callback);
        };
    }

    /**
     * Subscribe to sync progress updates
     * @param {Function} callback - Callback function
     * @returns {Function} Unsubscribe function
     */
    subscribeSyncProgress(callback) {
        this.syncListeners.add(callback);
        return () => {
            this.syncListeners.delete(callback);
        };
    }

    /**
     * Notify all listeners of status change
     * @param {Object} status - Status object
     */
    notifyListeners(status) {
        this.listeners.forEach((callback) => {
            try {
                callback(status);
            } catch (error) {
                console.error('[SyncService] Listener error:', error);
            }
        });
    }

    /**
     * Notify sync progress listeners
     * @param {Object} progress - Progress object
     */
    notifySyncProgress(progress) {
        this.syncListeners.forEach((callback) => {
            try {
                callback(progress);
            } catch (error) {
                console.error('[SyncService] Sync listener error:', error);
            }
        });
    }

    /**
     * Start automatic sync (every 30 seconds)
     */
    startAutoSync() {
        if (this.syncInterval) {
            return;
        }

        console.log('[SyncService] Starting auto-sync');

        // Sync immediately
        this.syncQueuedOrders();

        // Then sync every 30 seconds
        this.syncInterval = setInterval(() => {
            this.syncQueuedOrders();
        }, 30000);
    }

    /**
     * Stop automatic sync
     */
    stopAutoSync() {
        if (this.syncInterval) {
            console.log('[SyncService] Stopping auto-sync');
            clearInterval(this.syncInterval);
            this.syncInterval = null;
        }
    }

    /**
     * Sync all queued orders
     * @returns {Promise<Object>} Sync results
     */
    async syncQueuedOrders() {
        if (!this.isOnline) {
            console.log('[SyncService] Cannot sync - offline');
            return { success: 0, failed: 0, total: 0 };
        }

        if (this.isSyncing) {
            console.log('[SyncService] Sync already in progress');
            return null;
        }

        try {
            this.isSyncing = true;
            const pendingOrders = await indexedDBService.getQueuedOrders('PENDING');

            if (pendingOrders.length === 0) {
                console.log('[SyncService] No orders to sync');
                return { success: 0, failed: 0, total: 0 };
            }

            console.log(`[SyncService] Syncing ${pendingOrders.length} orders`);
            this.notifySyncProgress({
                syncing: true,
                total: pendingOrders.length,
                completed: 0
            });

            let successCount = 0;
            let failedCount = 0;

            for (let i = 0; i < pendingOrders.length; i++) {
                const order = pendingOrders[i];

                try {
                    // Mark as syncing
                    await indexedDBService.updateOrderStatus(order.clientOrderId, 'SYNCING');

                    // Send to server
                    const response = await api.post('/transactions', order.orderData);

                    // Mark as synced
                    await indexedDBService.updateOrderStatus(order.clientOrderId, 'SYNCED');
                    successCount++;

                    console.log(`[SyncService] Order synced: ${order.clientOrderId} -> ${response.data.transactionNumber}`);

                } catch (error) {
                    console.error(`[SyncService] Failed to sync order ${order.clientOrderId}:`, error);

                    // Mark as failed
                    const errorMessage = error.response?.data?.message || error.message;
                    await indexedDBService.updateOrderStatus(
                        order.clientOrderId,
                        'FAILED',
                        errorMessage
                    );
                    failedCount++;
                }

                // Notify progress
                this.notifySyncProgress({
                    syncing: true,
                    total: pendingOrders.length,
                    completed: i + 1
                });
            }

            // Clean up synced orders
            await indexedDBService.clearSyncedOrders();

            console.log(`[SyncService] Sync complete: ${successCount} succeeded, ${failedCount} failed`);

            this.notifySyncProgress({
                syncing: false,
                total: pendingOrders.length,
                completed: pendingOrders.length,
                success: successCount,
                failed: failedCount
            });

            return {
                success: successCount,
                failed: failedCount,
                total: pendingOrders.length
            };

        } catch (error) {
            console.error('[SyncService] Sync error:', error);
            this.notifySyncProgress({ syncing: false, error: error.message });
            return { success: 0, failed: 0, total: 0 };
        } finally {
            this.isSyncing = false;
        }
    }

    /**
     * Manually trigger sync
     * @returns {Promise<Object>}
     */
    async manualSync() {
        console.log('[SyncService] Manual sync triggered');
        return await this.syncQueuedOrders();
    }

    /**
     * Get sync statistics
     * @returns {Promise<Object>}
     */
    async getSyncStats() {
        const queueStats = await indexedDBService.getQueueStats();
        return {
            ...queueStats,
            online: this.isOnline,
            syncing: this.isSyncing
        };
    }

    /**
     * Initialize sync service
     */
    async init() {
        console.log('[SyncService] Initializing');
        await indexedDBService.init();

        if (this.isOnline) {
            this.startAutoSync();
        }
    }

    /**
     * Cleanup
     */
    destroy() {
        this.stopAutoSync();
        this.listeners.clear();
        this.syncListeners.clear();
        window.removeEventListener('online', this.handleOnline);
        window.removeEventListener('offline', this.handleOffline);
    }
}

// Export singleton instance
const syncService = new SyncService();
export default syncService;
