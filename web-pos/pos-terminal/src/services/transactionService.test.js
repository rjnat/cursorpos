import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock the api module - factory function must not reference external variables
vi.mock('./api.js', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
        interceptors: {
            request: { use: vi.fn(), eject: vi.fn() },
            response: { use: vi.fn(), eject: vi.fn() }
        }
    }
}));

import * as transactionService from './transactionService.js';
import api from './api.js';

describe('transactionService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('createTransaction', () => {
        it('should create a transaction', async () => {
            const transactionData = {
                branchId: 'branch-1',
                type: 'SALE',
                items: [
                    { productId: 'prod-1', quantity: 2, unitPrice: 25000 },
                ],
                payments: [
                    { paymentMethod: 'CASH', amount: 50000 },
                ],
            };

            const mockResponse = {
                data: {
                    id: 'txn-123',
                    transactionNumber: 'TRX-20231216-001',
                    ...transactionData,
                },
            };

            api.post = vi.fn().mockResolvedValue(mockResponse);

            const result = await transactionService.createTransaction(transactionData);

            expect(api.post).toHaveBeenCalledWith('/transactions', transactionData);
            expect(result.id).toBe('txn-123');
            expect(result.transactionNumber).toBe('TRX-20231216-001');
        });

        it('should handle validation errors', async () => {
            const invalidData = { branchId: null };
            const mockError = {
                response: {
                    status: 400,
                    data: { message: 'Validation failed' },
                },
            };

            api.post = vi.fn().mockRejectedValue(mockError);

            await expect(transactionService.createTransaction(invalidData)).rejects.toEqual(mockError);
        });
    });

    describe('getTransactionById', () => {
        it('should get transaction by ID', async () => {
            const mockTransaction = {
                id: 'txn-123',
                transactionNumber: 'TRX-001',
                totalAmount: 50000,
            };
            const mockResponse = { data: mockTransaction };

            api.get = vi.fn().mockResolvedValue(mockResponse);

            const result = await transactionService.getTransactionById('txn-123');

            expect(api.get).toHaveBeenCalledWith('/transactions/txn-123');
            expect(result).toEqual(mockTransaction);
        });

        it('should handle errors when fetching transaction', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
            const mockError = new Error('Transaction not found');
            api.get = vi.fn().mockRejectedValue(mockError);

            await expect(transactionService.getTransactionById('txn-123')).rejects.toThrow('Transaction not found');
            expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching transaction:', mockError);

            consoleErrorSpy.mockRestore();
        });
    });

    describe('getTransactionByNumber', () => {
        it('should get transaction by number', async () => {
            const mockTransaction = {
                id: 'txn-123',
                transactionNumber: 'TRX-20231216-001',
            };
            const mockResponse = { data: mockTransaction };

            api.get = vi.fn().mockResolvedValue(mockResponse);

            const result = await transactionService.getTransactionByNumber('TRX-20231216-001');

            expect(api.get).toHaveBeenCalledWith('/transactions/number/TRX-20231216-001');
            expect(result).toEqual(mockTransaction);
        });
    });

    describe('getAllTransactions', () => {
        it('should get all transactions with default params', async () => {
            const mockResponse = {
                data: {
                    content: [{ id: 'txn-1' }, { id: 'txn-2' }],
                    totalElements: 2,
                },
            };

            api.get = vi.fn().mockResolvedValue(mockResponse);

            const result = await transactionService.getAllTransactions();

            expect(api.get).toHaveBeenCalledWith('/transactions', { params: {} });
            expect(result.content).toHaveLength(2);
        });

        it('should get transactions with custom params', async () => {
            const params = { page: 1, size: 20 };
            const mockResponse = { data: { content: [] } };

            api.get = vi.fn().mockResolvedValue(mockResponse);

            await transactionService.getAllTransactions(params);

            expect(api.get).toHaveBeenCalledWith('/transactions', { params });
        });
    });

    describe('getTransactionsByBranch', () => {
        it('should get transactions by branch', async () => {
            const mockResponse = { data: { content: [{ id: 'txn-1' }] } };

            api.get = vi.fn().mockResolvedValue(mockResponse);

            await transactionService.getTransactionsByBranch('branch-1', { page: 0, size: 10 });

            expect(api.get).toHaveBeenCalledWith('/transactions/branch/branch-1', {
                params: { page: 0, size: 10 },
            });
        });
        it('should handle errors when fetching branch transactions', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
            const mockError = new Error('Branch not found');
            api.get = vi.fn().mockRejectedValue(mockError);

            await expect(transactionService.getTransactionsByBranch('branch-1')).rejects.toThrow('Branch not found');
            expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching transactions by branch:', mockError);

            consoleErrorSpy.mockRestore();
        });
    });

    describe('getTransactionsByStatus', () => {
        it('should get transactions by status', async () => {
            const mockResponse = { data: { content: [] } };

            api.get = vi.fn().mockResolvedValue(mockResponse);

            await transactionService.getTransactionsByStatus('COMPLETED');

            expect(api.get).toHaveBeenCalledWith('/transactions/status/COMPLETED', {
                params: {},
            });
        });

        it('should handle errors when fetching transactions by status', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
            const mockError = new Error('Invalid status');
            api.get = vi.fn().mockRejectedValue(mockError);

            await expect(transactionService.getTransactionsByStatus('INVALID')).rejects.toThrow('Invalid status');
            expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching transactions by status:', mockError);

            consoleErrorSpy.mockRestore();
        });
    });

    describe('getTransactionsByDateRange', () => {
        it('should get transactions by date range', async () => {
            const mockResponse = { data: { content: [] } };
            const startDate = '2023-12-01';
            const endDate = '2023-12-31';

            api.get = vi.fn().mockResolvedValue(mockResponse);

            await transactionService.getTransactionsByDateRange(startDate, endDate, { page: 0 });

            expect(api.get).toHaveBeenCalledWith('/transactions/date-range', {
                params: { startDate, endDate, page: 0 },
            });
        });

        it('should handle errors when fetching transactions by date range', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
            const mockError = new Error('Invalid date range');
            api.get = vi.fn().mockRejectedValue(mockError);

            await expect(transactionService.getTransactionsByDateRange('2023-12-01', '2023-12-31')).rejects.toThrow('Invalid date range');
            expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching transactions by date range:', mockError);

            consoleErrorSpy.mockRestore();
        });
    });

    describe('cancelTransaction', () => {
        it('should cancel a transaction', async () => {
            const mockResponse = {
                data: { id: 'txn-123', status: 'CANCELLED' },
            };

            api.put = vi.fn().mockResolvedValue(mockResponse);

            const result = await transactionService.cancelTransaction('txn-123');

            expect(api.put).toHaveBeenCalledWith('/transactions/txn-123/cancel');
            expect(result.status).toBe('CANCELLED');
        });

        it('should handle errors when cancelling transaction', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
            const mockError = new Error('Cannot cancel completed transaction');
            api.put = vi.fn().mockRejectedValue(mockError);

            await expect(transactionService.cancelTransaction('txn-123')).rejects.toThrow('Cannot cancel completed transaction');
            expect(consoleErrorSpy).toHaveBeenCalledWith('Error cancelling transaction:', mockError);

            consoleErrorSpy.mockRestore();
        });
    });

    describe('Receipt operations', () => {
        describe('generateReceipt', () => {
            it('should generate receipt for transaction', async () => {
                const mockReceipt = {
                    id: 'receipt-1',
                    transactionId: 'txn-123',
                    receiptNumber: 'RCP-001',
                };
                const mockResponse = { data: mockReceipt };

                api.post = vi.fn().mockResolvedValue(mockResponse);

                const result = await transactionService.generateReceipt('txn-123');

                expect(api.post).toHaveBeenCalledWith('/receipts/transaction/txn-123');
                expect(result).toEqual(mockReceipt);
            });

            it('should handle errors when generating receipt', async () => {
                const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
                const mockError = new Error('Transaction not found');
                api.post = vi.fn().mockRejectedValue(mockError);

                await expect(transactionService.generateReceipt('txn-123')).rejects.toThrow('Transaction not found');
                expect(consoleErrorSpy).toHaveBeenCalledWith('Error generating receipt:', mockError);

                consoleErrorSpy.mockRestore();
            });
        });

        describe('getReceiptById', () => {
            it('should get receipt by ID', async () => {
                const mockReceipt = { id: 'receipt-1' };
                const mockResponse = { data: mockReceipt };

                api.get = vi.fn().mockResolvedValue(mockResponse);

                const result = await transactionService.getReceiptById('receipt-1');

                expect(api.get).toHaveBeenCalledWith('/receipts/receipt-1');
                expect(result).toEqual(mockReceipt);
            });

            it('should handle errors when fetching receipt', async () => {
                const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
                const mockError = new Error('Receipt not found');
                api.get = vi.fn().mockRejectedValue(mockError);

                await expect(transactionService.getReceiptById('receipt-1')).rejects.toThrow('Receipt not found');
                expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching receipt:', mockError);

                consoleErrorSpy.mockRestore();
            });
        });

        describe('getReceiptByTransactionId', () => {
            it('should get receipt by transaction ID', async () => {
                const mockReceipt = { id: 'receipt-1', transactionId: 'txn-123' };
                const mockResponse = { data: mockReceipt };

                api.get = vi.fn().mockResolvedValue(mockResponse);

                const result = await transactionService.getReceiptByTransactionId('txn-123');

                expect(api.get).toHaveBeenCalledWith('/receipts/transaction/txn-123');
                expect(result).toEqual(mockReceipt);
            });

            it('should handle errors when fetching receipt by transaction', async () => {
                const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
                const mockError = new Error('Receipt not found');
                api.get = vi.fn().mockRejectedValue(mockError);

                await expect(transactionService.getReceiptByTransactionId('txn-123')).rejects.toThrow('Receipt not found');
                expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching receipt by transaction:', mockError);

                consoleErrorSpy.mockRestore();
            });
        });

        describe('printReceipt', () => {
            it('should mark receipt as printed', async () => {
                const mockReceipt = { id: 'receipt-1', printCount: 1 };
                const mockResponse = { data: mockReceipt };

                api.put = vi.fn().mockResolvedValue(mockResponse);

                const result = await transactionService.printReceipt('receipt-1');

                expect(api.put).toHaveBeenCalledWith('/receipts/receipt-1/print');
                expect(result.printCount).toBe(1);
            });

            it('should handle errors when printing receipt', async () => {
                const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
                const mockError = new Error('Receipt not found');
                api.put = vi.fn().mockRejectedValue(mockError);

                await expect(transactionService.printReceipt('receipt-1')).rejects.toThrow('Receipt not found');
                expect(consoleErrorSpy).toHaveBeenCalledWith('Error marking receipt as printed:', mockError);

                consoleErrorSpy.mockRestore();
            });
        });
        describe('getReceiptByTransactionId', () => {
            it('should get receipt by transaction ID', async () => {
                const mockReceipt = { id: 'receipt-1', transactionId: 'txn-123' };
                const mockResponse = { data: mockReceipt };

                api.get = vi.fn().mockResolvedValue(mockResponse);

                const result = await transactionService.getReceiptByTransactionId('txn-123');

                expect(api.get).toHaveBeenCalledWith('/receipts/transaction/txn-123');
                expect(result).toEqual(mockReceipt);
            });

            it('should handle errors when fetching receipt by transaction ID', async () => {
                const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
                const mockError = new Error('Receipt not found');
                api.get = vi.fn().mockRejectedValue(mockError);

                await expect(transactionService.getReceiptByTransactionId('txn-123')).rejects.toThrow('Receipt not found');
                expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching receipt by transaction:', mockError);

                consoleErrorSpy.mockRestore();
            });
        });
    });
});
