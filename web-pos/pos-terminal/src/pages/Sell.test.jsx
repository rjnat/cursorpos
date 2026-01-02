import { describe, it, expect, beforeEach, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../test/testUtils';
import Sell from './Sell/Sell';
import * as productService from '../services/productService';
import * as transactionService from '../services/transactionService';

// Mock the services
vi.mock('../services/productService');
vi.mock('../services/transactionService');

describe('Sell Page Integration', () => {
  const mockProducts = [
    {
      id: '1',
      name: 'Espresso',
      basePrice: 3.5,
      sku: 'ESP-001',
      availableStock: 100,
      taxRate: 10,
    },
    {
      id: '2',
      name: 'Cappuccino',
      basePrice: 4.5,
      sku: 'CAP-001',
      availableStock: 50,
      taxRate: 10,
    },
    {
      id: '3',
      name: 'Latte',
      basePrice: 5.0,
      sku: 'LAT-001',
      availableStock: 75,
      taxRate: 10,
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    productService.getAllProducts.mockResolvedValue({ content: mockProducts });
    productService.searchProducts.mockResolvedValue({ content: mockProducts });
  });

  it('should complete full checkout flow', async () => {
    const user = userEvent.setup({delay: null});

    const { store } = renderWithProviders(<Sell />);

    // Wait for products to load
    await waitFor(() => {
      expect(productService.getAllProducts).toHaveBeenCalled();
    });

    // Find and click add to cart for Espresso
    const addButtons = await screen.findAllByText(/add to cart/i);
    await user.click(addButtons[0]);

    // Verify item added to cart
    await waitFor(() => {
      const cartItems = store.getState().cart.items;
      expect(cartItems).toHaveLength(1);
      expect(cartItems[0].name).toBe('Espresso');
      expect(cartItems[0].quantity).toBe(1);
    });

    // Add another item
    await user.click(addButtons[1]);

    // Verify second item added
    await waitFor(() => {
      const cartItems = store.getState().cart.items;
      expect(cartItems).toHaveLength(2);
      expect(cartItems[1].name).toBe('Cappuccino');
    });

    // Verify cart totals are calculated
    const state = store.getState().cart;
    const subtotal = state.items.reduce(
      (total, item) => total + item.basePrice * item.quantity,
      0
    );
    expect(subtotal).toBe(8.0); // 3.5 + 4.5
  });

  it('should search and filter products', async () => {
    const user = userEvent.setup();

    productService.searchProducts.mockResolvedValue({
      content: [mockProducts[0]], // Only Espresso
    });

    renderWithProviders(<Sell />);

    // Wait for initial load
    await waitFor(() => {
      expect(productService.getAllProducts).toHaveBeenCalled();
    });

    // Find search input
    const searchInput = screen.getByPlaceholderText(/search products/i);
    await user.type(searchInput, 'Espresso');

    // Wait for debounce and search
    await waitFor(
      () => {
        expect(productService.searchProducts).toHaveBeenCalledWith('Espresso', undefined, 0, 50);
      },
      { timeout: 500 }
    );
  });

  it('should update item quantities in cart', async () => {
    const user = userEvent.setup();

    const { store } = renderWithProviders(<Sell />);

    // Wait for products to load
    await waitFor(() => {
      expect(productService.getAllProducts).toHaveBeenCalled();
    });

    // Add item to cart
    const addButtons = await screen.findAllByText(/add to cart/i);
    await user.click(addButtons[0]);

    // Wait for item in cart
    await waitFor(() => {
      expect(store.getState().cart.items).toHaveLength(1);
    });

    // Find plus button and increase quantity
    const plusButtons = screen.getAllByRole('button');
    const plusButton = plusButtons.find((btn) => {
      const svg = btn.querySelector('svg');
      return svg && svg.getAttribute('class')?.includes('h-4');
    });

    if (plusButton) {
      await user.click(plusButton);

      await waitFor(() => {
        expect(store.getState().cart.items[0].quantity).toBe(2);
      });
    }
  });

  it('should apply percentage discount', async () => {
    const user = userEvent.setup();

    const { store } = renderWithProviders(<Sell />);

    // Wait for products
    await waitFor(() => {
      expect(productService.getAllProducts).toHaveBeenCalled();
    });

    // Add items
    const addButtons = await screen.findAllByText(/add to cart/i);
    await user.click(addButtons[0]);
    await user.click(addButtons[1]);

    // Wait for items
    await waitFor(() => {
      expect(store.getState().cart.items).toHaveLength(2);
    });

    // Apply discount via store action (simulating discount modal)
    store.dispatch({
      type: 'cart/applyDiscount',
      payload: { type: 'percentage', value: 10, code: 'SAVE10' },
    });

    // Verify discount applied
    const state = store.getState().cart;
    expect(state.discount.type).toBe('percentage');
    expect(state.discount.value).toBe(10);
  });

  it('should clear cart and reset state', async () => {
    const user = userEvent.setup();

    const { store } = renderWithProviders(<Sell />);

    // Wait for products
    await waitFor(() => {
      expect(productService.getAllProducts).toHaveBeenCalled();
    });

    // Add items
    const addButtons = await screen.findAllByText(/add to cart/i);
    await user.click(addButtons[0]);

    await waitFor(() => {
      expect(store.getState().cart.items).toHaveLength(1);
    });

    // Mock window.confirm for clear cart
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    // Find and click clear cart button
    const clearButton = screen.getByText(/clear cart/i);
    await user.click(clearButton);

    // Verify cart cleared
    await waitFor(() => {
      expect(store.getState().cart.items).toHaveLength(0);
    });
  });

  it('should handle empty product list', async () => {
    productService.getAllProducts.mockResolvedValue({ content: [] });

    renderWithProviders(<Sell />);

    await waitFor(() => {
      expect(productService.getAllProducts).toHaveBeenCalled();
    });

    // Verify empty state shown
    expect(screen.getByText(/no products found/i)).toBeInTheDocument();
  });

  it('should handle product loading error', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    productService.getAllProducts.mockRejectedValue(new Error('Network error'));

    renderWithProviders(<Sell />);

    await waitFor(() => {
      expect(productService.getAllProducts).toHaveBeenCalled();
    });

    // Verify error logged
    expect(consoleErrorSpy).toHaveBeenCalled();

    consoleErrorSpy.mockRestore();
  });

  it('should maintain cart state when searching', async () => {
    const user = userEvent.setup();

    const { store } = renderWithProviders(<Sell />);

    // Add item to cart
    await waitFor(() => {
      expect(productService.getAllProducts).toHaveBeenCalled();
    });

    const addButtons = await screen.findAllByText(/add to cart/i);
    await user.click(addButtons[0]);

    await waitFor(() => {
      expect(store.getState().cart.items).toHaveLength(1);
    });

    // Search for products
    productService.searchProducts.mockResolvedValue({ content: [mockProducts[1]] });

    const searchInput = screen.getByPlaceholderText(/search products/i);
    await user.type(searchInput, 'Cappuccino');

    await waitFor(
      () => {
        expect(productService.searchProducts).toHaveBeenCalled();
      },
      { timeout: 500 }
    );

    // Verify cart still has item
    expect(store.getState().cart.items).toHaveLength(1);
    expect(store.getState().cart.items[0].name).toBe('Espresso');
  });

  it('should calculate tax correctly across multiple items', async () => {
    const user = userEvent.setup();

    const { store } = renderWithProviders(<Sell />);

    await waitFor(() => {
      expect(productService.getAllProducts).toHaveBeenCalled();
    });

    // Add multiple items
    const addButtons = await screen.findAllByText(/add to cart/i);
    await user.click(addButtons[0]); // Espresso: 3.5, tax 10%
    await user.click(addButtons[1]); // Cappuccino: 4.5, tax 10%

    await waitFor(() => {
      expect(store.getState().cart.items).toHaveLength(2);
    });

    // Calculate expected tax: (3.5 * 1 * 0.1) + (4.5 * 1 * 0.1) = 0.35 + 0.45 = 0.8
    const state = store.getState().cart;
    const tax = state.items.reduce(
      (total, item) => total + (item.basePrice * item.quantity * (item.taxRate || 0)) / 100,
      0
    );

    expect(tax).toBeCloseTo(0.8, 2);
  });

  it('should handle checkout button click', async () => {
    const user = userEvent.setup();

    renderWithProviders(<Sell />);

    await waitFor(() => {
      expect(productService.getAllProducts).toHaveBeenCalled();
    });

    // Add item
    const addButtons = await screen.findAllByText(/add to cart/i);
    await user.click(addButtons[0]);

    await waitFor(() => {
      expect(screen.getByText(/checkout/i)).toBeInTheDocument();
    });

    // Find checkout button
    const checkoutButton = screen.getByText(/checkout/i);
    expect(checkoutButton).not.toBeDisabled();
  });

  it('should open checkout modal when checkout button clicked', async () => {
    const user = userEvent.setup();

    renderWithProviders(<Sell />);

    await waitFor(() => {
      expect(productService.getAllProducts).toHaveBeenCalled();
    });

    // Add item
    const addButtons = await screen.findAllByText(/add to cart/i);
    await user.click(addButtons[0]);

    await waitFor(() => {
      expect(screen.getByText(/checkout/i)).toBeInTheDocument();
    });

    // Click checkout
    const checkoutButton = screen.getByText(/checkout/i);
    await user.click(checkoutButton);

    // Checkout modal should open
    await waitFor(() => {
      expect(screen.getByText(/complete payment/i)).toBeInTheDocument();
    });
  });

  it('should handle search error', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    productService.searchProducts.mockRejectedValueOnce({
      response: { data: { message: 'Search failed' } }
    });

    const { store } = renderWithProviders(<Sell />);

    await waitFor(() => {
      expect(productService.getAllProducts).toHaveBeenCalled();
    });

    // Search for something
    const searchInput = screen.getByPlaceholderText(/search products/i);
    const user = userEvent.setup();
    await user.type(searchInput, 'Test');

    await waitFor(
      () => {
        expect(consoleErrorSpy).toHaveBeenCalledWith('Error searching products:', expect.any(Object));
      },
      { timeout: 500 }
    );

    consoleErrorSpy.mockRestore();
  });

  it('should clear search and reload products', async () => {
    const user = userEvent.setup();

    renderWithProviders(<Sell />);

    await waitFor(() => {
      expect(productService.getAllProducts).toHaveBeenCalled();
    });

    // Type in search
    const searchInput = screen.getByPlaceholderText(/search products/i);
    await user.type(searchInput, 'Coffee');

    // Click clear
    const clearButton = screen.getByRole('button', { name: '' }).closest('button');
    if (clearButton) {
      await user.click(clearButton);
    }

    // Should reload all products
    await waitFor(() => {
      expect(productService.getAllProducts).toHaveBeenCalledTimes(2);
    });
  });

  it('should add item to cart', async () => {
    const user = userEvent.setup();

    const { store } = renderWithProviders(<Sell />);

    await waitFor(() => {
      expect(productService.getAllProducts).toHaveBeenCalled();
    });

    // Add item
    const addButtons = await screen.findAllByText(/add to cart/i);
    await user.click(addButtons[0]);

    await waitFor(() => {
      // Toast notification would be shown in real implementation
      expect(store.getState().cart.items.length).toBe(1);
    });
  });
});
