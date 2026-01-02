import { describe, it, expect, beforeEach, vi } from 'vitest';
import authReducer, {
    loginSuccess,
    logout,
    loginStart,
    loginFailure,
    restoreAuth,
    clearError
} from './authSlice.js';

// Selectors (not exported from authSlice, so we define them here for testing)
const selectCurrentUser = (state) => state.auth?.user;
const selectCurrentToken = (state) => state.auth?.token;
const selectIsAuthenticated = (state) => state.auth?.isAuthenticated || false;

describe('authSlice', () => {
    let initialState;

    beforeEach(() => {
        initialState = {
            user: null,
            token: null,
            isAuthenticated: false,
            loading: false,
            error: null,
        };
    });

    describe('reducers', () => {
        it('should return the initial state', () => {
            expect(authReducer(undefined, { type: 'unknown' })).toEqual(initialState);
        });

        it('should handle loginSuccess', () => {
            const user = {
                id: '123',
                email: 'test@example.com',
                name: 'Test User',
                tenantId: 'tenant-001',
            };
            const token = 'fake-jwt-token';

            const actual = authReducer(initialState, loginSuccess({ user, token }));

            expect(actual.user).toEqual(user);
            expect(actual.token).toBe(token);
            expect(actual.isAuthenticated).toBe(true);
        });

        it('should handle logout', () => {
            const authenticatedState = {
                user: { id: '123', email: 'test@example.com' },
                token: 'fake-jwt-token',
                isAuthenticated: true,
            };

            const actual = authReducer(authenticatedState, logout());

            expect(actual.user).toBeNull();
            expect(actual.token).toBeNull();
            expect(actual.isAuthenticated).toBe(false);
        });
    });

    describe('selectors', () => {
        const mockState = {
            auth: {
                user: { id: '123', email: 'test@example.com', name: 'Test User' },
                token: 'fake-jwt-token',
                isAuthenticated: true,
            },
        };

        it('should select current user', () => {
            expect(selectCurrentUser(mockState)).toEqual(mockState.auth.user);
        });

        it('should select current token', () => {
            expect(selectCurrentToken(mockState)).toBe('fake-jwt-token');
        });

        it('should select isAuthenticated', () => {
            expect(selectIsAuthenticated(mockState)).toBe(true);
        });

        it('should return null for user when not authenticated', () => {
            const unauthenticatedState = { auth: initialState };
            expect(selectCurrentUser(unauthenticatedState)).toBeNull();
        });
    });

    describe('additional reducers', () => {
        it('should handle loginStart', () => {
            const actual = authReducer(initialState, loginStart());
            expect(actual.loading).toBe(true);
            expect(actual.error).toBeNull();
        });

        it('should handle loginFailure', () => {
            const error = 'Invalid credentials';
            const actual = authReducer(initialState, loginFailure(error));
            expect(actual.loading).toBe(false);
            expect(actual.isAuthenticated).toBe(false);
            expect(actual.user).toBeNull();
            expect(actual.token).toBeNull();
            expect(actual.error).toBe(error);
        });

        it('should handle restoreAuth with valid token and user', () => {
            // Mock the global localStorage to return specific values
            localStorage.getItem.mockImplementation((key) => {
                if (key === 'token') return 'stored-token';
                if (key === 'user') return JSON.stringify({ id: '456', name: 'Stored User' });
                return null;
            });

            const stateBeforeRestore = { ...initialState, token: null, user: null };
            const actual = authReducer(stateBeforeRestore, restoreAuth());

            expect(actual.token).toBe('stored-token');
            expect(actual.user).toEqual({ id: '456', name: 'Stored User' });
            expect(actual.isAuthenticated).toBe(true);

            localStorage.getItem.mockClear();
        });

        it('should handle restoreAuth with no stored data', () => {
            localStorage.getItem.mockReturnValue(null);

            const actual = authReducer(initialState, restoreAuth());
            expect(actual.token).toBeNull();
            expect(actual.user).toBeNull();
            expect(actual.isAuthenticated).toBe(false);

            localStorage.getItem.mockClear();
        });

        it('should handle clearError', () => {
            const stateWithError = { ...initialState, error: 'Some error' };
            const actual = authReducer(stateWithError, clearError());
            expect(actual.error).toBeNull();
        });
    });
});