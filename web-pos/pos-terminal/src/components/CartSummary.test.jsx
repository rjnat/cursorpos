import { describe, it, expect, vi } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../test/testUtils';
import CartSummary from './CartSummary';

describe('CartSummary', () => {
  it('should render empty cart state', () => {
    const mockOnCheckout = vi.fn();
    const initialState = {
      cart: {
        items: [],
        customer: null,
        discount: { type: null, value: 0, code: null },
      },
    };

    renderWithProviders(<CartSummary onCheckout={mockOnCheckout} />, {
      preloadedState: initialState,
    });

    expect(screen.getByText(/0.*item/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /checkout/i })).toBeDisabled();
  });

  it('should display correct item count', () => {
    const mockOnCheckout = vi.fn();
    const stateWithItems = {
      cart: {
        items: [
          { id: '1', name: 'Coffee', basePrice: 25000, quantity: 2, taxRate: 10 },
          { id: '2', name: 'Tea', basePrice: 15000, quantity: 1, taxRate: 10 },
        ],
        customer: null,
        discount: { type: null, value: 0, code: null },
      },
    };

    renderWithProviders(<CartSummary onCheckout={mockOnCheckout} />, {
      preloadedState: stateWithItems,
    });

    expect(screen.getByText(/3.*items/i)).toBeInTheDocument();
  });

  it('should calculate and display subtotal correctly', () => {
    const mockOnCheckout = vi.fn();
    const stateWithItems = {
      cart: {
        items: [
          { id: '1', name: 'Coffee', basePrice: 25000, quantity: 2, taxRate: 10 },
        ],
        customer: null,
        discount: { type: null, value: 0, code: null },
      },
    };

    renderWithProviders(<CartSummary onCheckout={mockOnCheckout} />, {
      preloadedState: stateWithItems,
    });

    // Subtotal: 25000 * 2 = 50000
    expect(screen.getByText(/50\.000/)).toBeInTheDocument();
  });

  it('should calculate and display tax correctly', () => {
    const mockOnCheckout = vi.fn();
    const stateWithItems = {
      cart: {
        items: [
          { id: '1', name: 'Coffee', basePrice: 25000, quantity: 2, taxRate: 10 },
        ],
        customer: null,
        discount: { type: null, value: 0, code: null },
      },
    };

    renderWithProviders(<CartSummary onCheckout={mockOnCheckout} />, {
      preloadedState: stateWithItems,
    });

    // Tax: (25000 * 2) * 0.10 = 5000
    const taxElements = screen.getAllByText(/5\.000/);
    expect(taxElements[0]).toBeInTheDocument();
  });

  it('should display discount when applied', () => {
    const mockOnCheckout = vi.fn();
    const stateWithDiscount = {
      cart: {
        items: [
          { id: '1', name: 'Coffee', basePrice: 25000, quantity: 2, taxRate: 10 },
        ],
        customer: null,
        discount: { type: 'fixed', value: 5000, code: 'SAVE5K' },
      },
    };

    renderWithProviders(<CartSummary onCheckout={mockOnCheckout} />, {
      preloadedState: stateWithDiscount,
    });

    expect(screen.getByText(/discount/i)).toBeInTheDocument();
    expect(screen.getByText(/-.*5\.000/)).toBeInTheDocument();
  });

  it('should calculate grand total correctly', () => {
    const mockOnCheckout = vi.fn();
    const stateWithItems = {
      cart: {
        items: [
          { id: '1', name: 'Coffee', basePrice: 25000, quantity: 2, taxRate: 10 },
        ],
        customer: null,
        discount: { type: null, value: 0, code: null },
      },
    };

    renderWithProviders(<CartSummary onCheckout={mockOnCheckout} />, {
      preloadedState: stateWithItems,
    });

    // Grand total: subtotal(50000) + tax(5000) = 55000
    expect(screen.getByText(/55\.000/)).toBeInTheDocument();
  });

  it('should call onCheckout when checkout button clicked', async () => {
    const user = userEvent.setup();
    const mockOnCheckout = vi.fn();
    const stateWithItems = {
      cart: {
        items: [
          { id: '1', name: 'Coffee', basePrice: 25000, quantity: 1, taxRate: 10 },
        ],
        customer: null,
        discount: { type: null, value: 0, code: null },
      },
    };

    renderWithProviders(<CartSummary onCheckout={mockOnCheckout} />, {
      preloadedState: stateWithItems,
    });

    const checkoutButton = screen.getByRole('button', { name: /checkout/i });
    expect(checkoutButton).not.toBeDisabled();

    await user.click(checkoutButton);
    expect(mockOnCheckout).toHaveBeenCalled();
  });

  it('should clear cart when clear button clicked and confirmed', async () => {
    const user = userEvent.setup();
    const mockOnCheckout = vi.fn();
    const stateWithItems = {
      cart: {
        items: [
          { id: '1', name: 'Coffee', basePrice: 25000, quantity: 1, taxRate: 10 },
        ],
        customer: null,
        discount: { type: null, value: 0, code: null },
      },
    };

    global.confirm = vi.fn(() => true);

    const { store } = renderWithProviders(<CartSummary onCheckout={mockOnCheckout} />, {
      preloadedState: stateWithItems,
    });

    const clearButton = screen.getByRole('button', { name: /clear cart/i });
    await user.click(clearButton);

    expect(global.confirm).toHaveBeenCalled();
    expect(store.getState().cart.items).toHaveLength(0);
  });

  it('should not clear cart when clear is cancelled', async () => {
    const user = userEvent.setup();
    const mockOnCheckout = vi.fn();
    const stateWithItems = {
      cart: {
        items: [
          { id: '1', name: 'Coffee', basePrice: 25000, quantity: 1, taxRate: 10 },
        ],
        customer: null,
        discount: { type: null, value: 0, code: null },
      },
    };

    global.confirm = vi.fn(() => false);

    const { store } = renderWithProviders(<CartSummary onCheckout={mockOnCheckout} />, {
      preloadedState: stateWithItems,
    });

    const clearButton = screen.getByRole('button', { name: /clear cart/i });
    await user.click(clearButton);

    expect(store.getState().cart.items).toHaveLength(1);
  });

  it('should disable clear button when cart is empty', () => {
    const mockOnCheckout = vi.fn();
    const initialState = {
      cart: {
        items: [],
        customer: null,
        discount: { type: null, value: 0, code: null },
      },
    };

    renderWithProviders(<CartSummary onCheckout={mockOnCheckout} />, {
      preloadedState: initialState,
    });

    const clearButton = screen.getByRole('button', { name: /clear cart/i });
    expect(clearButton).toBeDisabled();
  });
});
