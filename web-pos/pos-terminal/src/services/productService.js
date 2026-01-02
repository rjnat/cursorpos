import api from './api';
import indexedDBService from './indexedDB';

/**
 * Product Service - API calls for product management with offline support
 */

/**
 * Search products by name or barcode (with offline cache)
 * @param {string} query - Search term
 * @param {string} storeId - Store ID for inventory context
 * @param {number} page - Page number (default: 0)
 * @param {number} size - Page size (default: 50)
 * @returns {Promise} - Product list with pagination
 */
export const searchProducts = async (query, storeId, page = 0, size = 50) => {
    try {
        const params = { page, size };
        if (query && query.trim()) {
            params.q = query.trim();
        }
        if (storeId) {
            params.storeId = storeId;
        }

        // Try to fetch from server
        const response = await api.get('/v1/products/search', { params });

        // Cache products in IndexedDB for offline use
        if (response.data?.content?.length > 0) {
            try {
                await indexedDBService.cacheProducts(response.data.content);
            } catch (cacheError) {
                console.warn('Failed to cache products:', cacheError);
            }
        }

        return response.data;
    } catch (error) {
        console.error('Error searching products:', error);

        // If offline, try to use cached data
        if (!navigator.onLine) {
            console.log('Offline - searching cached products');
            try {
                const tenantId = JSON.parse(localStorage.getItem('user'))?.tenantId;
                if (tenantId) {
                    const cachedProducts = await indexedDBService.searchCachedProducts(tenantId, query);
                    return {
                        content: cachedProducts,
                        totalElements: cachedProducts.length,
                        totalPages: 1,
                        number: 0,
                        size: cachedProducts.length,
                        offline: true
                    };
                }
            } catch (cacheError) {
                console.error('Failed to read from cache:', cacheError);
            }
        }

        throw error;
    }
};

/**
 * Get all products (paginated)
 * @param {number} page - Page number (default: 0)
 * @param {number} size - Page size (default: 50)
 * @returns {Promise} - Product list with pagination
 */
export const getAllProducts = async (page = 0, size = 50) => {
    try {
        const response = await api.get('/v1/products', {
            params: { page, size }
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching products:', error);
        throw error;
    }
};

/**
 * Get product by ID
 * @param {string} productId - Product UUID
 * @returns {Promise} - Product details
 */
export const getProductById = async (productId) => {
    try {
        const response = await api.get(`/v1/products/${productId}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching product:', error);
        throw error;
    }
};

/**
 * Get product by SKU
 * @param {string} sku - Product SKU
 * @returns {Promise} - Product details
 */
export const getProductBySku = async (sku) => {
    try {
        const response = await api.get(`/v1/products/sku/${sku}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching product by SKU:', error);
        throw error;
    }
};

/**
 * Get product by barcode
 * @param {string} barcode - Product barcode
 * @returns {Promise} - Product details
 */
export const getProductByBarcode = async (barcode) => {
    try {
        const response = await api.get(`/v1/products/barcode/${barcode}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching product by barcode:', error);
        throw error;
    }
};

/**
 * Get all categories
 * @returns {Promise} - Category list
 */
export const getCategories = async () => {
    try {
        const response = await api.get('/v1/categories');
        return response.data;
    } catch (error) {
        console.error('Error fetching categories:', error);
        throw error;
    }
};

/**
 * Get inventory for a product at a specific store
 * @param {string} productId - Product UUID
 * @param {string} storeId - Store UUID
 * @returns {Promise} - Inventory details
 */
export const getInventory = async (productId, storeId) => {
    try {
        const response = await api.get(`/v1/inventory/${productId}`, {
            params: { storeId }
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching inventory:', error);
        throw error;
    }
};
