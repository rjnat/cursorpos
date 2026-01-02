import { describe, it, expect, beforeEach } from 'vitest';
import cartReducer, {
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    setCustomer,
    applyDiscount,
    removeDiscount,
    selectCartItems,
    selectCartSubtotal,
    selectCartTax,
    selectCartDiscount,
    selectCartGrandTotal,
    selectCartItemCount,
} from './cartSlice.js';

describe('cartSlice', () => {
    let initialState;

    beforeEach(() => {
        initialState = {
            items: [],
            customer: null,
            discount: {
                type: null,
                value: 0,
                code: null,
            },
        };
    });

    describe('reducers', () => {
        it('should return the initial state', () => {
            expect(cartReducer(undefined, { type: 'unknown' })).toEqual(initialState);
        });

        describe('addItem', () => {
            it('should add a new item to empty cart', () => {
                const product = {
                    id: '1',
                    name: 'Coffee',
                    basePrice: 25000,
                    taxRate: 10,
                };

                const actual = cartReducer(initialState, addItem(product));

                expect(actual.items).toHaveLength(1);
                expect(actual.items[0]).toEqual({ ...product, quantity: 1 });
            });

            it('should increment quantity if item already exists', () => {
                const stateWithItem = {
                    ...initialState,
                    items: [{ id: '1', name: 'Coffee', basePrice: 25000, quantity: 1 }],
                };

                const actual = cartReducer(stateWithItem, addItem({ id: '1', name: 'Coffee', basePrice: 25000 }));

                expect(actual.items).toHaveLength(1);
                expect(actual.items[0].quantity).toBe(2);
            });

            it('should add multiple different items', () => {
                const state1 = cartReducer(initialState, addItem({ id: '1', name: 'Coffee', basePrice: 25000 }));
                const state2 = cartReducer(state1, addItem({ id: '2', name: 'Tea', basePrice: 15000 }));

                expect(state2.items).toHaveLength(2);
                expect(state2.items[0].id).toBe('1');
                expect(state2.items[1].id).toBe('2');
            });
        });

        describe('removeItem', () => {
            it('should remove item from cart', () => {
                const stateWithItems = {
                    ...initialState,
                    items: [
                        { id: '1', name: 'Coffee', basePrice: 25000, quantity: 1 },
                        { id: '2', name: 'Tea', basePrice: 15000, quantity: 1 },
                    ],
                };

                const actual = cartReducer(stateWithItems, removeItem('1'));

                expect(actual.items).toHaveLength(1);
                expect(actual.items[0].id).toBe('2');
            });

            it('should handle removing non-existent item', () => {
                const stateWithItem = {
                    ...initialState,
                    items: [{ id: '1', name: 'Coffee', basePrice: 25000, quantity: 1 }],
                };

                const actual = cartReducer(stateWithItem, removeItem('999'));

                expect(actual.items).toHaveLength(1);
            });
        });

        describe('updateQuantity', () => {
            it('should update item quantity', () => {
                const stateWithItem = {
                    ...initialState,
                    items: [{ id: '1', name: 'Coffee', basePrice: 25000, quantity: 1 }],
                };

                const actual = cartReducer(stateWithItem, updateQuantity({ id: '1', quantity: 5 }));

                expect(actual.items[0].quantity).toBe(5);
            });

            it('should not update if quantity is zero or negative', () => {
                const stateWithItem = {
                    ...initialState,
                    items: [{ id: '1', name: 'Coffee', basePrice: 25000, quantity: 3 }],
                };

                const actual = cartReducer(stateWithItem, updateQuantity({ id: '1', quantity: 0 }));

                expect(actual.items[0].quantity).toBe(3);
            });

            it('should not update if item not found', () => {
                const stateWithItem = {
                    ...initialState,
                    items: [{ id: '1', name: 'Coffee', basePrice: 25000, quantity: 2 }],
                };

                const actual = cartReducer(stateWithItem, updateQuantity({ id: '999', quantity: 5 }));

                expect(actual.items[0].quantity).toBe(2);
            });
        });

        describe('clearCart', () => {
            it('should clear all items and reset state', () => {
                const stateWithData = {
                    items: [
                        { id: '1', name: 'Coffee', basePrice: 25000, quantity: 2 },
                        { id: '2', name: 'Tea', basePrice: 15000, quantity: 1 },
                    ],
                    customer: { id: 'cust-1', name: 'John Doe' },
                    discount: { type: 'percentage', value: 10, code: 'SAVE10' },
                };

                const actual = cartReducer(stateWithData, clearCart());

                expect(actual.items).toHaveLength(0);
                expect(actual.customer).toBeNull();
                expect(actual.discount).toEqual({ type: null, value: 0, code: null });
            });
        });

        describe('setCustomer', () => {
            it('should set customer', () => {
                const customer = { id: 'cust-1', name: 'John Doe', email: 'john@example.com' };
                const actual = cartReducer(initialState, setCustomer(customer));

                expect(actual.customer).toEqual(customer);
            });
        });

        describe('discount actions', () => {
            it('should apply percentage discount', () => {
                const discount = { type: 'percentage', value: 15, code: 'SAVE15' };
                const actual = cartReducer(initialState, applyDiscount(discount));

                expect(actual.discount).toEqual(discount);
            });

            it('should apply fixed discount', () => {
                const discount = { type: 'fixed', value: 10000, code: 'FIXED10K' };
                const actual = cartReducer(initialState, applyDiscount(discount));

                expect(actual.discount).toEqual(discount);
            });

            it('should remove discount', () => {
                const stateWithDiscount = {
                    ...initialState,
                    discount: { type: 'percentage', value: 10, code: 'SAVE10' },
                };

                const actual = cartReducer(stateWithDiscount, removeDiscount());

                expect(actual.discount).toEqual({ type: null, value: 0, code: null });
            });
        });
    });

    describe('selectors', () => {
        const mockStateWithItems = {
            cart: {
                items: [
                    { id: '1', name: 'Coffee', basePrice: 25000, quantity: 2, taxRate: 10 },
                    { id: '2', name: 'Tea', basePrice: 15000, quantity: 1, taxRate: 10 },
                ],
                customer: null,
                discount: { type: null, value: 0, code: null },
            },
        };

        it('should select cart items', () => {
            const items = selectCartItems(mockStateWithItems);
            expect(items).toHaveLength(2);
            expect(items[0].name).toBe('Coffee');
        });

        it('should calculate subtotal correctly', () => {
            const subtotal = selectCartSubtotal(mockStateWithItems);
            // (25000 * 2) + (15000 * 1) = 65000
            expect(subtotal).toBe(65000);
        });

        it('should calculate tax correctly', () => {
            const tax = selectCartTax(mockStateWithItems);
            // (25000 * 2 * 0.1) + (15000 * 1 * 0.1) = 5000 + 1500 = 6500
            expect(tax).toBe(6500);
        });

        it('should calculate percentage discount correctly', () => {
            const stateWithDiscount = {
                cart: {
                    ...mockStateWithItems.cart,
                    discount: { type: 'percentage', value: 10, code: 'SAVE10' },
                },
            };

            const discount = selectCartDiscount(stateWithDiscount);
            // 10% of 65000 = 6500
            expect(discount).toBe(6500);
        });

        it('should calculate fixed discount correctly', () => {
            const stateWithDiscount = {
                cart: {
                    ...mockStateWithItems.cart,
                    discount: { type: 'fixed', value: 5000, code: 'FIXED5K' },
                },
            };

            const discount = selectCartDiscount(stateWithDiscount);
            expect(discount).toBe(5000);
        });

        it('should return zero discount when no discount applied', () => {
            const discount = selectCartDiscount(mockStateWithItems);
            expect(discount).toBe(0);
        });

        it('should calculate grand total correctly', () => {
            const grandTotal = selectCartGrandTotal(mockStateWithItems);
            // subtotal(65000) + tax(6500) - discount(0) = 71500
            expect(grandTotal).toBe(71500);
        });

        it('should calculate grand total with discount', () => {
            const stateWithDiscount = {
                cart: {
                    ...mockStateWithItems.cart,
                    discount: { type: 'fixed', value: 10000, code: 'FIXED10K' },
                },
            };

            const grandTotal = selectCartGrandTotal(stateWithDiscount);
            // subtotal(65000) + tax(6500) - discount(10000) = 61500
            expect(grandTotal).toBe(61500);
        });

        it('should handle empty cart', () => {
            const emptyState = { cart: { items: [], customer: null, discount: { type: null, value: 0, code: null } } };

            expect(selectCartSubtotal(emptyState)).toBe(0);
            expect(selectCartTax(emptyState)).toBe(0);
            expect(selectCartDiscount(emptyState)).toBe(0);
            expect(selectCartGrandTotal(emptyState)).toBe(0);
        });

        it('should return 0 for item count with empty cart', () => {
            const emptyState = { cart: { items: [] } };
            expect(selectCartItemCount(emptyState)).toBe(0);
        });

        it('should handle items without tax rate', () => {
            const stateWithoutTax = {
                cart: {
                    items: [{ id: '1', basePrice: 10000, quantity: 2 }],
                    discount: { type: null, value: 0 }
                }
            };
            expect(selectCartTax(stateWithoutTax)).toBe(0);
        });
    });
});
