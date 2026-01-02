import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import api from './api';

describe('api interceptors', () => {
    let mockLocalStorage;

    beforeEach(() => {
        // Mock localStorage
        mockLocalStorage = {
            getItem: vi.fn(),
            setItem: vi.fn(),
            removeItem: vi.fn(),
            clear: vi.fn(),
        };
        global.localStorage = mockLocalStorage;

        // Mock window.location
        delete window.location;
        window.location = { href: '' };

        // Mock console.error
        vi.spyOn(console, 'error').mockImplementation(() => { });
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    describe('request interceptor', () => {
        it('should add Authorization header when token exists', async () => {
            mockLocalStorage.getItem.mockReturnValue('test-token-123');

            const config = {
                headers: {
                    'Content-Type': 'application/json',
                },
            };

            const interceptor = api.interceptors.request.handlers[0];
            const result = await interceptor.fulfilled(config);

            expect(result.headers.Authorization).toBe('Bearer test-token-123');
        });

        it('should not add Authorization header when token does not exist', async () => {
            mockLocalStorage.getItem.mockReturnValue(null);

            const config = {
                headers: {
                    'Content-Type': 'application/json',
                },
            };

            const interceptor = api.interceptors.request.handlers[0];
            const result = await interceptor.fulfilled(config);

            expect(result.headers.Authorization).toBeUndefined();
        });

        it('should handle request interceptor errors', async () => {
            const error = new Error('Request error');
            const interceptor = api.interceptors.request.handlers[0];

            await expect(interceptor.rejected(error)).rejects.toThrow('Request error');
        });
    });

    describe('response interceptor', () => {
        it('should return response when successful', async () => {
            const response = { data: { message: 'Success' }, status: 200 };
            const interceptor = api.interceptors.response.handlers[0];

            const result = await interceptor.fulfilled(response);

            expect(result).toEqual(response);
        });

        it('should clear auth and redirect on 401 error', async () => {
            const error = {
                response: {
                    status: 401,
                    data: { message: 'Unauthorized' },
                },
            };

            const interceptor = api.interceptors.response.handlers[0];

            await expect(interceptor.rejected(error)).rejects.toEqual(error);

            expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('token');
            expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('user');
            expect(window.location.href).toBe('/login');
        });

        it('should not redirect on other error status codes', async () => {
            const error = {
                response: {
                    status: 404,
                    data: { message: 'Not Found' },
                },
            };

            const interceptor = api.interceptors.response.handlers[0];

            await expect(interceptor.rejected(error)).rejects.toEqual(error);

            expect(mockLocalStorage.removeItem).not.toHaveBeenCalled();
            expect(window.location.href).toBe('');
        });

        it('should log network errors', async () => {
            const consoleErrorSpy = vi.spyOn(console, 'error');
            const error = {
                request: {},
                message: 'Network Error',
            };

            const interceptor = api.interceptors.response.handlers[0];

            await expect(interceptor.rejected(error)).rejects.toEqual(error);

            expect(consoleErrorSpy).toHaveBeenCalledWith('Network error:', 'Network Error');
        });

        it('should handle errors without response or request', async () => {
            const error = new Error('Unknown error');

            const interceptor = api.interceptors.response.handlers[0];

            await expect(interceptor.rejected(error)).rejects.toEqual(error);
        });
    });

    describe('api configuration', () => {
        it('should have correct baseURL', () => {
            expect(api.defaults.baseURL).toBe('/api');
        });

        it('should have correct default headers', () => {
            expect(api.defaults.headers['Content-Type']).toBe('application/json');
        });
    });
});
