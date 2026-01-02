import { createSlice } from '@reduxjs/toolkit';

const initialState = {
    items: [],
    customer: null,
    discount: {
        type: null, // 'percentage' or 'fixed'
        value: 0,
        code: null,
    },
};

const cartSlice = createSlice({
    name: 'cart',
    initialState,
    reducers: {
        addItem: (state, action) => {
            const existingItem = state.items.find(
                (item) => item.id === action.payload.id
            );
            if (existingItem) {
                existingItem.quantity += 1;
            } else {
                state.items.push({ ...action.payload, quantity: 1 });
            }
        },
        removeItem: (state, action) => {
            state.items = state.items.filter((item) => item.id !== action.payload);
        },
        updateQuantity: (state, action) => {
            const { id, quantity } = action.payload;
            const item = state.items.find((item) => item.id === id);
            if (item && quantity > 0) {
                item.quantity = quantity;
            }
        },
        clearCart: (state) => {
            state.items = [];
            state.customer = null;
            state.discount = { type: null, value: 0, code: null };
        },
        setCustomer: (state, action) => {
            state.customer = action.payload;
        },
        applyDiscount: (state, action) => {
            state.discount = action.payload;
        },
        removeDiscount: (state) => {
            state.discount = { type: null, value: 0, code: null };
        },
    },
});

export const {
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    setCustomer,
    applyDiscount,
    removeDiscount,
} = cartSlice.actions;

// Selectors
export const selectCartItems = (state) => state.cart.items;
export const selectCartTotal = (state) => {
    const subtotal = state.cart.items.reduce(
        (total, item) => total + item.basePrice * item.quantity,
        0
    );
    return subtotal;
};
export const selectCartSubtotal = (state) => {
    return state.cart.items.reduce(
        (total, item) => total + item.basePrice * item.quantity,
        0
    );
};
export const selectCartTax = (state) => {
    const subtotal = selectCartSubtotal(state);
    // Tax calculation: sum of (item price * quantity * tax rate)
    const tax = state.cart.items.reduce(
        (total, item) => total + (item.basePrice * item.quantity * (item.taxRate || 0)) / 100,
        0
    );
    return tax;
};
export const selectCartDiscount = (state) => {
    const subtotal = selectCartSubtotal(state);
    const { type, value } = state.cart.discount;
    if (type === 'percentage') {
        return (subtotal * value) / 100;
    } else if (type === 'fixed') {
        return value;
    }
    return 0;
};
export const selectCartGrandTotal = (state) => {
    const subtotal = selectCartSubtotal(state);
    const tax = selectCartTax(state);
    const discount = selectCartDiscount(state);
    return subtotal + tax - discount;
};
export const selectCartItemCount = (state) => {
    return state.cart.items.reduce((total, item) => total + item.quantity, 0);
};

export default cartSlice.reducer;
