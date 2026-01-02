import { describe, it, expect, beforeEach, vi } from 'vitest';
import indexedDBService from '../indexedDB';

// Mock IndexedDB
const mockDB = {
    transaction: vi.fn(),
    objectStoreNames: { contains: vi.fn() },
    close: vi.fn()
};

const mockObjectStore = {
    put: vi.fn(),
    get: vi.fn(),
    getAll: vi.fn(),
    delete: vi.fn(),
    clear: vi.fn(),
    index: vi.fn()
};

const mockTransaction = {
    objectStore: vi.fn(() => mockObjectStore)
};

const mockIndex = {
    getAll: vi.fn()
};

describe('IndexedDBService', () => {
    beforeEach(() => {
        vi.clearAllMocks();

        // Reset service state
        indexedDBService.db = null;
        indexedDBService.initPromise = null;

        // Mock indexedDB.open
        global.indexedDB = {
            open: vi.fn((name, version) => {
                const request = {
                    onsuccess: null,
                    onerror: null,
                    onupgradeneeded: null,
                    result: mockDB
                };

                setTimeout(() => {
                    if (request.onupgradeneeded) {
                        request.onupgradeneeded({
                            target: {
                                result: {
                                    objectStoreNames: { contains: () => false },
                                    createObjectStore: vi.fn(() => ({
                                        createIndex: vi.fn()
                                    }))
                                }
                            }
                        });
                    }
                    if (request.onsuccess) {
                        request.onsuccess();
                    }
                }, 0);

                return request;
            })
        };
    });

    describe('init', () => {
        it('should initialize database successfully', async () => {
            await indexedDBService.init();
            expect(indexedDBService.db).toBe(mockDB);
        });

        it('should return existing db if already initialized', async () => {
            await indexedDBService.init();
            const db1 = indexedDBService.db;

            await indexedDBService.init();
            const db2 = indexedDBService.db;

            expect(db1).toBe(db2);
        });

        it('should handle initialization error', async () => {
            global.indexedDB.open = vi.fn(() => {
                const request = {
                    onsuccess: null,
                    onerror: null,
                    error: new Error('DB error')
                };

                setTimeout(() => {
                    if (request.onerror) {
                        request.onerror();
                    }
                }, 0);

                return request;
            });

            await expect(indexedDBService.init()).rejects.toThrow();
        });
    });

    describe('cacheProducts', () => {
        beforeEach(async () => {
            await indexedDBService.init();
            mockDB.transaction.mockReturnValue(mockTransaction);
        });

        it('should cache products successfully', async () => {
            const products = [
                { id: '1', name: 'Product 1', tenantId: 'tenant-1' },
                { id: '2', name: 'Product 2', tenantId: 'tenant-1' }
            ];

            mockObjectStore.put.mockImplementation(() => {
                const request = {
                    onsuccess: null,
                    onerror: null
                };

                setTimeout(() => {
                    if (request.onsuccess) {
                        request.onsuccess();
                    }
                }, 0);

                return request;
            });

            await indexedDBService.cacheProducts(products);

            expect(mockDB.transaction).toHaveBeenCalledWith(['products_cache'], 'readwrite');
            expect(mockObjectStore.put).toHaveBeenCalledTimes(2);
        });

        it('should handle caching error', async () => {
            const products = [{ id: '1', name: 'Product 1' }];

            mockObjectStore.put.mockImplementation(() => {
                const request = {
                    onsuccess: null,
                    onerror: null,
                    error: new Error('Put error')
                };

                setTimeout(() => {
                    if (request.onerror) {
                        request.onerror();
                    }
                }, 0);

                return request;
            });

            await expect(indexedDBService.cacheProducts(products)).rejects.toThrow();
        });
    });

    describe('getCachedProducts', () => {
        beforeEach(async () => {
            await indexedDBService.init();
            mockDB.transaction.mockReturnValue(mockTransaction);
            mockObjectStore.index.mockReturnValue(mockIndex);
        });

        it('should retrieve cached products by tenantId', async () => {
            const tenantId = 'tenant-1';
            const products = [
                { id: '1', name: 'Product 1', tenantId },
                { id: '2', name: 'Product 2', tenantId }
            ];

            mockIndex.getAll.mockImplementation(() => {
                const request = {
                    onsuccess: null,
                    onerror: null,
                    result: products
                };

                setTimeout(() => {
                    if (request.onsuccess) {
                        request.onsuccess();
                    }
                }, 0);

                return request;
            });

            const result = await indexedDBService.getCachedProducts(tenantId);

            expect(result).toEqual(products);
            expect(mockIndex.getAll).toHaveBeenCalledWith(tenantId);
        });

        it('should return empty array if no products found', async () => {
            mockIndex.getAll.mockImplementation(() => {
                const request = {
                    onsuccess: null,
                    result: null
                };

                setTimeout(() => {
                    if (request.onsuccess) {
                        request.onsuccess();
                    }
                }, 0);

                return request;
            });

            const result = await indexedDBService.getCachedProducts('tenant-1');
            expect(result).toEqual([]);
        });
    });

    describe('searchCachedProducts', () => {
        beforeEach(async () => {
            await indexedDBService.init();
            mockDB.transaction.mockReturnValue(mockTransaction);
            mockObjectStore.index.mockReturnValue(mockIndex);
        });

        it('should search cached products by query', async () => {
            const products = [
                { id: '1', name: 'Coffee', sku: 'COF-001', tenantId: 'tenant-1' },
                { id: '2', name: 'Tea', sku: 'TEA-001', tenantId: 'tenant-1' },
                { id: '3', name: 'Coffee Latte', sku: 'COF-002', tenantId: 'tenant-1' }
            ];

            mockIndex.getAll.mockImplementation(() => {
                const request = {
                    onsuccess: null,
                    result: products
                };

                setTimeout(() => {
                    if (request.onsuccess) {
                        request.onsuccess();
                    }
                }, 0);

                return request;
            });

            const result = await indexedDBService.searchCachedProducts('tenant-1', 'coffee');

            expect(result).toHaveLength(2);
            expect(result[0].name).toBe('Coffee');
            expect(result[1].name).toBe('Coffee Latte');
        });

        it('should return all products if query is empty', async () => {
            const products = [
                { id: '1', name: 'Coffee', tenantId: 'tenant-1' },
                { id: '2', name: 'Tea', tenantId: 'tenant-1' }
            ];

            mockIndex.getAll.mockImplementation(() => {
                const request = {
                    onsuccess: null,
                    result: products
                };

                setTimeout(() => {
                    if (request.onsuccess) {
                        request.onsuccess();
                    }
                }, 0);

                return request;
            });

            const result = await indexedDBService.searchCachedProducts('tenant-1', '');
            expect(result).toEqual(products);
        });
    });

    describe('queueOrder', () => {
        beforeEach(async () => {
            await indexedDBService.init();
            mockDB.transaction.mockReturnValue(mockTransaction);

            // Reset add mock
            mockObjectStore.add = vi.fn(() => {
                const request = {
                    onsuccess: null,
                    onerror: null
                };

                setTimeout(() => {
                    if (request.onsuccess) {
                        request.onsuccess();
                    }
                }, 0);

                return request;
            });
        });

        it('should queue order successfully', async () => {
            const order = {
                orderData: { items: [], total: 100 },
                tenantId: 'tenant-1',
                storeId: 'store-1'
            };

            const result = await indexedDBService.queueOrder(order);

            expect(result).toHaveProperty('clientOrderId');
            expect(result).toHaveProperty('status', 'PENDING');
            expect(result).toHaveProperty('syncAttempts', 0);
        });

        it('should generate clientOrderId if not provided', async () => {
            const order = {
                orderData: { items: [] },
                tenantId: 'tenant-1',
                storeId: 'store-1'
            };

            const result = await indexedDBService.queueOrder(order);

            expect(result.clientOrderId).toMatch(/^offline_/);
        });
    });

    describe('getQueuedOrders', () => {
        beforeEach(async () => {
            await indexedDBService.init();
            mockDB.transaction.mockReturnValue(mockTransaction);
            mockObjectStore.index.mockReturnValue(mockIndex);
        });

        it('should get all queued orders', async () => {
            const orders = [
                { clientOrderId: '1', status: 'PENDING' },
                { clientOrderId: '2', status: 'SYNCED' }
            ];

            mockObjectStore.getAll.mockImplementation(() => {
                const request = {
                    onsuccess: null,
                    result: orders
                };

                setTimeout(() => {
                    if (request.onsuccess) {
                        request.onsuccess();
                    }
                }, 0);

                return request;
            });

            const result = await indexedDBService.getQueuedOrders();
            expect(result).toEqual(orders);
        });

        it('should filter orders by status', async () => {
            const pendingOrders = [
                { clientOrderId: '1', status: 'PENDING' }
            ];

            mockIndex.getAll.mockImplementation(() => {
                const request = {
                    onsuccess: null,
                    result: pendingOrders
                };

                setTimeout(() => {
                    if (request.onsuccess) {
                        request.onsuccess();
                    }
                }, 0);

                return request;
            });

            const result = await indexedDBService.getQueuedOrders('PENDING');
            expect(result).toEqual(pendingOrders);
        });
    });

    describe('close', () => {
        it('should close database connection', async () => {
            await indexedDBService.init();

            indexedDBService.close();

            expect(mockDB.close).toHaveBeenCalled();
            expect(indexedDBService.db).toBeNull();
            expect(indexedDBService.initPromise).toBeNull();
        });
    });
});
