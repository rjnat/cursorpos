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

import * as productService from './productService.js';
import api from './api.js';

describe('productService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('searchProducts', () => {
        it('should search products with query and storeId', async () => {
            const mockResponse = {
                data: {
                    content: [
                        { id: '1', name: 'Coffee', basePrice: 25000 },
                        { id: '2', name: 'Cappuccino', basePrice: 30000 },
                    ],
                    totalElements: 2,
                },
            };

            api.get.mockResolvedValue(mockResponse);

            const result = await productService.searchProducts('coffee', 'store-1', 0, 50);

            expect(api.get).toHaveBeenCalledWith('/v1/products/search', {
                params: { q: 'coffee', storeId: 'store-1', page: 0, size: 50 },
            });
            expect(result).toEqual(mockResponse.data);
        });

        it('should search without query parameter', async () => {
            const mockResponse = { data: { content: [], totalElements: 0 } };
            api.get = vi.fn().mockResolvedValue(mockResponse);

            await productService.searchProducts('', 'store-1');

            expect(api.get).toHaveBeenCalledWith('/v1/products/search', {
                params: { page: 0, size: 50, storeId: 'store-1' },
            });
        });

        it('should handle API errors', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
            const mockError = new Error('Network error');
            api.get = vi.fn().mockRejectedValue(mockError);

            await expect(productService.searchProducts('coffee', 'store-1')).rejects.toThrow('Network error');
            expect(consoleErrorSpy).toHaveBeenCalledWith('Error searching products:', mockError);

            consoleErrorSpy.mockRestore();
        });

        it('should trim whitespace from query', async () => {
            const mockResponse = { data: { content: [] } };
            api.get = vi.fn().mockResolvedValue(mockResponse);

            await productService.searchProducts('  coffee  ', 'store-1');

            expect(api.get).toHaveBeenCalledWith('/v1/products/search', {
                params: { q: 'coffee', storeId: 'store-1', page: 0, size: 50 },
            });
        });
    });

    describe('getAllProducts', () => {
        it('should get all products with pagination', async () => {
            const mockResponse = {
                data: {
                    content: [{ id: '1', name: 'Product 1' }],
                    totalElements: 1,
                },
            };

            api.get = vi.fn().mockResolvedValue(mockResponse);

            const result = await productService.getAllProducts(0, 20);

            expect(api.get).toHaveBeenCalledWith('/v1/products', {
                params: { page: 0, size: 20 },
            });
            expect(result).toEqual(mockResponse.data);
        });

        it('should use default pagination values', async () => {
            const mockResponse = { data: { content: [] } };
            api.get = vi.fn().mockResolvedValue(mockResponse);

            await productService.getAllProducts();

            expect(api.get).toHaveBeenCalledWith('/v1/products', {
                params: { page: 0, size: 50 },
            });
        });
    });

    describe('getProductById', () => {
        it('should get product by ID', async () => {
            const mockProduct = { id: 'prod-123', name: 'Coffee', basePrice: 25000 };
            const mockResponse = { data: mockProduct };

            api.get = vi.fn().mockResolvedValue(mockResponse);

            const result = await productService.getProductById('prod-123');

            expect(api.get).toHaveBeenCalledWith('/v1/products/prod-123');
            expect(result).toEqual(mockProduct);
        });

        it('should handle not found error', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
            const mockError = { response: { status: 404, data: { message: 'Product not found' } } };
            api.get = vi.fn().mockRejectedValue(mockError);

            await expect(productService.getProductById('invalid-id')).rejects.toEqual(mockError);
            expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching product:', mockError);

            consoleErrorSpy.mockRestore();
        });
    });

    describe('getProductBySku', () => {
        it('should get product by SKU', async () => {
            const mockProduct = { id: 'prod-123', sku: 'SKU-001', name: 'Coffee' };
            const mockResponse = { data: mockProduct };

            api.get = vi.fn().mockResolvedValue(mockResponse);

            const result = await productService.getProductBySku('SKU-001');

            expect(api.get).toHaveBeenCalledWith('/v1/products/sku/SKU-001');
            expect(result).toEqual(mockProduct);
        });
    });

    describe('getProductByBarcode', () => {
        it('should get product by barcode', async () => {
            const mockProduct = { id: 'prod-123', barcode: '1234567890', name: 'Coffee' };
            const mockResponse = { data: mockProduct };

            api.get = vi.fn().mockResolvedValue(mockResponse);

            const result = await productService.getProductByBarcode('1234567890');

            expect(api.get).toHaveBeenCalledWith('/v1/products/barcode/1234567890');
            expect(result).toEqual(mockProduct);
        });
    });

    describe('getCategories', () => {
        it('should get all categories', async () => {
            const mockCategories = [
                { id: '1', name: 'Beverages' },
                { id: '2', name: 'Food' },
            ];
            const mockResponse = { data: mockCategories };

            api.get = vi.fn().mockResolvedValue(mockResponse);

            const result = await productService.getCategories();

            expect(api.get).toHaveBeenCalledWith('/v1/categories');
            expect(result).toEqual(mockCategories);
        });

        it('should handle errors when fetching categories', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
            const mockError = new Error('Server error');
            api.get = vi.fn().mockRejectedValue(mockError);

            await expect(productService.getCategories()).rejects.toThrow('Server error');
            expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching categories:', mockError);

            consoleErrorSpy.mockRestore();
        });
    });

    describe('getInventory', () => {
        it('should get inventory for product and store', async () => {
            const mockInventory = { productId: 'prod-123', storeId: 'store-1', quantity: 50 };
            const mockResponse = { data: mockInventory };

            api.get = vi.fn().mockResolvedValue(mockResponse);

            const result = await productService.getInventory('prod-123', 'store-1');

            expect(api.get).toHaveBeenCalledWith('/v1/inventory/prod-123', {
                params: { storeId: 'store-1' },
            });
            expect(result).toEqual(mockInventory);
        });

        it('should handle errors when fetching inventory', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
            const mockError = new Error('Inventory not found');
            api.get = vi.fn().mockRejectedValue(mockError);

            await expect(productService.getInventory('prod-123', 'store-1')).rejects.toThrow('Inventory not found');
            expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching inventory:', mockError);

            consoleErrorSpy.mockRestore();
        });
    });

    describe('getProductBySku', () => {
        it('should fetch product by SKU successfully', async () => {
            const mockProduct = { id: 'prod-1', sku: 'SKU-001', name: 'Product 1' };
            api.get = vi.fn().mockResolvedValue({ data: mockProduct });

            const result = await productService.getProductBySku('SKU-001');
            expect(result).toEqual(mockProduct);
            expect(api.get).toHaveBeenCalledWith('/v1/products/sku/SKU-001');
        });

        it('should handle getProductBySku error', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
            const mockError = new Error('SKU not found');
            api.get = vi.fn().mockRejectedValue(mockError);

            await expect(productService.getProductBySku('INVALID')).rejects.toThrow('SKU not found');
            expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching product by SKU:', mockError);

            consoleErrorSpy.mockRestore();
        });
    });

    describe('getProductByBarcode', () => {
        it('should fetch product by barcode successfully', async () => {
            const mockProduct = { id: 'prod-1', barcode: '123456789', name: 'Product 1' };
            api.get = vi.fn().mockResolvedValue({ data: mockProduct });

            const result = await productService.getProductByBarcode('123456789');
            expect(result).toEqual(mockProduct);
            expect(api.get).toHaveBeenCalledWith('/v1/products/barcode/123456789');
        });

        it('should handle getProductByBarcode error', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
            const mockError = new Error('Barcode not found');
            api.get = vi.fn().mockRejectedValue(mockError);

            await expect(productService.getProductByBarcode('INVALID')).rejects.toThrow('Barcode not found');
            expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching product by barcode:', mockError);

            consoleErrorSpy.mockRestore();
        });
    });
});
