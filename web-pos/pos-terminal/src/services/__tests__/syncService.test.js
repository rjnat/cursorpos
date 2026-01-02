import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import syncService from '../syncService';
import indexedDBService from '../indexedDB';
import api from '../api';

// Mock dependencies
vi.mock('../indexedDB');
vi.mock('../api');

describe('SyncService', () => {
    let onlineCallback;
    let offlineCallback;

    beforeEach(() => {
        vi.clearAllMocks();

        // Mock navigator.onLine
        Object.defineProperty(navigator, 'onLine', {
            writable: true,
            value: true
        });

        // Mock window event listeners
        global.addEventListener = vi.fn((event, callback) => {
            if (event === 'online') onlineCallback = callback;
            if (event === 'offline') offlineCallback = callback;
        });

        global.removeEventListener = vi.fn();

        // Reset service state
        syncService.isOnline = true;
        syncService.isSyncing = false;
        syncService.syncInterval = null;
        syncService.listeners.clear();
        syncService.syncListeners.clear();
    });

    afterEach(() => {
        syncService.stopAutoSync();
    });

    describe('initialization', () => {
        it('should detect online status on init', () => {
            expect(syncService.getIsOnline()).toBe(true);
        });

        it('should start auto-sync when online', async () => {
            vi.spyOn(syncService, 'startAutoSync');

            await syncService.init();

            expect(syncService.startAutoSync).toHaveBeenCalled();
        });

        it('should not start auto-sync when offline', async () => {
            navigator.onLine = false;
            syncService.isOnline = false;

            vi.spyOn(syncService, 'startAutoSync');

            await syncService.init();

            expect(syncService.startAutoSync).not.toHaveBeenCalled();
        });
    });

    describe('online/offline detection', () => {
        it('should handle online event', () => {
            syncService.isOnline = false;
            vi.spyOn(syncService, 'startAutoSync');

            syncService.handleOnline();

            expect(syncService.isOnline).toBe(true);
            expect(syncService.startAutoSync).toHaveBeenCalled();
        });

        it('should handle offline event', () => {
            syncService.isOnline = true;
            vi.spyOn(syncService, 'stopAutoSync');

            syncService.handleOffline();

            expect(syncService.isOnline).toBe(false);
            expect(syncService.stopAutoSync).toHaveBeenCalled();
        });

        it('should notify listeners on status change', () => {
            const listener = vi.fn();
            syncService.subscribe(listener);

            syncService.handleOnline();

            expect(listener).toHaveBeenCalledWith({ online: true });
        });
    });

    describe('subscribe', () => {
        it('should add listener and notify immediately', () => {
            const listener = vi.fn();

            syncService.subscribe(listener);

            expect(listener).toHaveBeenCalledWith({ online: true });
        });

        it('should return unsubscribe function', () => {
            const listener = vi.fn();

            const unsubscribe = syncService.subscribe(listener);
            unsubscribe();

            syncService.handleOnline();

            // Listener should be called once during subscribe, but not after unsubscribe
            expect(listener).toHaveBeenCalledTimes(1);
        });

        it('should notify multiple listeners', () => {
            const listener1 = vi.fn();
            const listener2 = vi.fn();

            syncService.subscribe(listener1);
            syncService.subscribe(listener2);

            syncService.handleOffline();

            expect(listener1).toHaveBeenCalledWith({ online: false });
            expect(listener2).toHaveBeenCalledWith({ online: false });
        });
    });

    describe('syncQueuedOrders', () => {
        beforeEach(() => {
            syncService.isOnline = true;
        });

        it('should not sync when offline', async () => {
            syncService.isOnline = false;

            const result = await syncService.syncQueuedOrders();

            expect(result).toEqual({ success: 0, failed: 0, total: 0 });
            expect(indexedDBService.getQueuedOrders).not.toHaveBeenCalled();
        });

        it('should return early if no orders to sync', async () => {
            indexedDBService.getQueuedOrders.mockResolvedValue([]);

            const result = await syncService.syncQueuedOrders();

            expect(result).toEqual({ success: 0, failed: 0, total: 0 });
            expect(api.post).not.toHaveBeenCalled();
        });

        it('should sync pending orders successfully', async () => {
            const orders = [
                {
                    clientOrderId: 'offline_1',
                    orderData: { items: [], total: 100 }
                },
                {
                    clientOrderId: 'offline_2',
                    orderData: { items: [], total: 200 }
                }
            ];

            indexedDBService.getQueuedOrders.mockResolvedValue(orders);
            indexedDBService.updateOrderStatus.mockResolvedValue();
            indexedDBService.clearSyncedOrders.mockResolvedValue(2);
            api.post.mockResolvedValue({ data: { transactionNumber: 'TRX-001' } });

            const result = await syncService.syncQueuedOrders();

            expect(result).toEqual({ success: 2, failed: 0, total: 2 });
            expect(api.post).toHaveBeenCalledTimes(2);
            expect(indexedDBService.updateOrderStatus).toHaveBeenCalledWith('offline_1', 'SYNCING');
            expect(indexedDBService.updateOrderStatus).toHaveBeenCalledWith('offline_1', 'SYNCED');
            expect(indexedDBService.clearSyncedOrders).toHaveBeenCalled();
        });

        it('should handle sync failures', async () => {
            const orders = [
                {
                    clientOrderId: 'offline_1',
                    orderData: { items: [] }
                }
            ];

            indexedDBService.getQueuedOrders.mockResolvedValue(orders);
            indexedDBService.updateOrderStatus.mockResolvedValue();
            indexedDBService.clearSyncedOrders.mockResolvedValue(0);
            api.post.mockRejectedValue(new Error('Network error'));

            const result = await syncService.syncQueuedOrders();

            expect(result).toEqual({ success: 0, failed: 1, total: 1 });
            expect(indexedDBService.updateOrderStatus).toHaveBeenCalledWith(
                'offline_1',
                'FAILED',
                'Network error'
            );
        });

        it('should not sync if already syncing', async () => {
            syncService.isSyncing = true;

            const result = await syncService.syncQueuedOrders();

            expect(result).toBeNull();
            expect(indexedDBService.getQueuedOrders).not.toHaveBeenCalled();
        });

        it('should notify sync progress listeners', async () => {
            const orders = [
                { clientOrderId: 'offline_1', orderData: {} }
            ];

            indexedDBService.getQueuedOrders.mockResolvedValue(orders);
            indexedDBService.updateOrderStatus.mockResolvedValue();
            indexedDBService.clearSyncedOrders.mockResolvedValue(1);
            api.post.mockResolvedValue({ data: { transactionNumber: 'TRX-001' } });

            const progressListener = vi.fn();
            syncService.subscribeSyncProgress(progressListener);

            await syncService.syncQueuedOrders();

            expect(progressListener).toHaveBeenCalledWith({
                syncing: true,
                total: 1,
                completed: 0
            });

            expect(progressListener).toHaveBeenCalledWith({
                syncing: false,
                total: 1,
                completed: 1,
                success: 1,
                failed: 0
            });
        });
    });

    describe('manualSync', () => {
        it('should trigger sync manually', async () => {
            vi.spyOn(syncService, 'syncQueuedOrders').mockResolvedValue({ success: 1, failed: 0, total: 1 });

            const result = await syncService.manualSync();

            expect(syncService.syncQueuedOrders).toHaveBeenCalled();
            expect(result).toEqual({ success: 1, failed: 0, total: 1 });
        });
    });

    describe('getSyncStats', () => {
        it('should return sync statistics', async () => {
            const queueStats = {
                total: 5,
                pending: 2,
                syncing: 0,
                synced: 2,
                failed: 1
            };

            indexedDBService.getQueueStats.mockResolvedValue(queueStats);

            const stats = await syncService.getSyncStats();

            expect(stats).toEqual({
                ...queueStats,
                online: true,
                syncing: false
            });
        });
    });

    describe('startAutoSync', () => {
        it('should start sync interval', () => {
            vi.useFakeTimers();
            vi.spyOn(syncService, 'syncQueuedOrders');

            syncService.startAutoSync();

            // Should sync immediately
            expect(syncService.syncQueuedOrders).toHaveBeenCalledTimes(1);

            // Should sync after 30 seconds
            vi.advanceTimersByTime(30000);
            expect(syncService.syncQueuedOrders).toHaveBeenCalledTimes(2);

            vi.useRealTimers();
        });

        it('should not start multiple intervals', () => {
            vi.useFakeTimers();
            vi.spyOn(syncService, 'syncQueuedOrders');

            syncService.startAutoSync();
            syncService.startAutoSync();

            vi.advanceTimersByTime(30000);

            // Should only sync once per interval
            expect(syncService.syncQueuedOrders).toHaveBeenCalledTimes(2); // Initial + 1 interval

            vi.useRealTimers();
        });
    });

    describe('stopAutoSync', () => {
        it('should stop sync interval', () => {
            vi.useFakeTimers();
            vi.spyOn(syncService, 'syncQueuedOrders');

            syncService.startAutoSync();
            syncService.stopAutoSync();

            vi.advanceTimersByTime(60000);

            // Should only sync once (initial), not from intervals
            expect(syncService.syncQueuedOrders).toHaveBeenCalledTimes(1);

            vi.useRealTimers();
        });
    });

    describe('destroy', () => {
        it('should cleanup resources', () => {
            syncService.startAutoSync();
            const listener = vi.fn();
            syncService.subscribe(listener);

            syncService.destroy();

            expect(syncService.syncInterval).toBeNull();
            expect(syncService.listeners.size).toBe(0);
            expect(syncService.syncListeners.size).toBe(0);
        });
    });
});
