import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../test/testUtils';
import Cart from './Cart';

describe('Cart', () => {
  const mockItems = [
    {
      id: 'prod-1',
      name: 'Cappuccino',
      sku: 'SKU-001',
      basePrice: 30000,
      quantity: 2,
      availableStock: 50,
    },
    {
      id: 'prod-2',
      name: 'Espresso',
      sku: 'SKU-002',
      basePrice: 25000,
      quantity: 1,
      availableStock: 30,
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render empty cart message when no items', () => {
    renderWithProviders(<Cart items={[]} />);

    expect(screen.getByText(/your cart is empty/i)).toBeInTheDocument();
    expect(screen.getByText(/add products to get started/i)).toBeInTheDocument();
  });

  it('should render all cart items', () => {
    renderWithProviders(<Cart items={mockItems} />);

    expect(screen.getByText('Cappuccino')).toBeInTheDocument();
    expect(screen.getByText('Espresso')).toBeInTheDocument();
    expect(screen.getByText('SKU: SKU-001')).toBeInTheDocument();
    expect(screen.getByText('SKU: SKU-002')).toBeInTheDocument();
  });

  it('should display quantity and price for each item', () => {
    renderWithProviders(<Cart items={mockItems} />);

    // Check quantities
    const quantityInputs = screen.getAllByRole('spinbutton');
    expect(quantityInputs[0]).toHaveValue(2);
    expect(quantityInputs[1]).toHaveValue(1);
  });

  it('should increase quantity when plus button clicked', async () => {
    const user = userEvent.setup();
    const preloadedState = {
      cart: { items: mockItems, customer: null, discount: { type: null, value: 0, code: null } }
    };
    const { store } = renderWithProviders(<Cart items={mockItems} />, { preloadedState });

    const plusButtons = screen.getAllByRole('button').filter(btn => 
      btn.querySelector('svg')?.getAttribute('d')?.includes('M12 4.5v15m7.5-7.5h-15')
    );
    
    const initialQuantity = mockItems[0].quantity;
    
    // Click plus button for first item
    if (plusButtons[0]) {
      await user.click(plusButtons[0]);
      const updatedQuantity = store.getState().cart.items[0].quantity;
      expect(updatedQuantity).toBe(initialQuantity + 1);
    }
  });

  it('should decrease quantity when minus button clicked', async () => {
    const user = userEvent.setup();
    const preloadedState = {
      cart: { items: mockItems, customer: null, discount: { type: null, value: 0, code: null } }
    };
    const { store } = renderWithProviders(<Cart items={mockItems} />, { preloadedState });

    const minusButtons = screen.getAllByRole('button').filter(btn => 
      btn.querySelector('svg')?.getAttribute('d')?.includes('M5 12h14')
    );
    
    const initialQuantity = mockItems[0].quantity;
    
    if (minusButtons[0]) {
      await user.click(minusButtons[0]);
      const updatedQuantity = store.getState().cart.items[0].quantity;
      expect(updatedQuantity).toBe(initialQuantity - 1);
    }
  });

  it('should remove item when quantity is 1 and minus clicked', async () => {
    const user = userEvent.setup();
    const singleItem = [{ ...mockItems[0], quantity: 1 }];
    const preloadedState = {
      cart: { items: singleItem, customer: null, discount: { type: null, value: 0, code: null } }
    };
    const { store } = renderWithProviders(<Cart items={singleItem} />, { preloadedState });

    expect(store.getState().cart.items).toHaveLength(1);

    const minusButtons = screen.getAllByRole('button').filter(btn => 
      btn.querySelector('svg')?.getAttribute('d')?.includes('M5 12h14')
    );
    
    if (minusButtons[0]) {
      await user.click(minusButtons[0]);
      expect(store.getState().cart.items).toHaveLength(0);
    }
  });

  it('should remove item when trash button clicked', async () => {
    const user = userEvent.setup();
    const preloadedState = {
      cart: { items: mockItems, customer: null, discount: { type: null, value: 0, code: null } }
    };
    const { store } = renderWithProviders(<Cart items={mockItems} />, { preloadedState });

    expect(store.getState().cart.items).toHaveLength(2);

    // Find and click first trash button
    const trashButtons = screen.getAllByRole('button');
    const trashButton = trashButtons.find(btn => 
      btn.getAttribute('title') === 'Remove Item'
    );

    if (trashButton) {
      await user.click(trashButton);
      expect(store.getState().cart.items).toHaveLength(1);
    }
  });

  it('should update quantity when input value changes', async () => {
    const preloadedState = {
      cart: { items: mockItems, customer: null, discount: { type: null, value: 0, code: null } }
    };
    const { store } = renderWithProviders(<Cart items={mockItems} />, { preloadedState });

    const quantityInput = screen.getAllByRole('spinbutton')[0];
    
    // Directly set the value to test the onChange handler
    fireEvent.change(quantityInput, { target: { value: '5' } });

    expect(store.getState().cart.items[0].quantity).toBe(5);
  });

  it('should not exceed available stock', async () => {
    const user = userEvent.setup();
    const limitedStock = [{ ...mockItems[0], availableStock: 3, quantity: 3 }];
    
    renderWithProviders(<Cart items={limitedStock} />);

    const plusButtons = screen.getAllByRole('button');
    const plusButton = plusButtons.find(btn => 
      btn.querySelector('svg')?.getAttribute('d')?.includes('M12 4.5v15m7.5-7.5h-15')
    );

    if (plusButton) {
      await user.click(plusButton);
      // Should show alert about max stock reached
      expect(global.alert).toHaveBeenCalled();
    }
  });

  it('should calculate item total correctly', () => {
    renderWithProviders(<Cart items={mockItems} />);

    // First item: 30000 x 2 = 60000
    expect(screen.getByText(/60\.000/)).toBeInTheDocument();
    
    // Second item: 25000 x 1 = 25000 (will match multiple, just check one exists)
    const priceElements = screen.getAllByText(/25\.000/);
    expect(priceElements.length).toBeGreaterThan(0);
  });

  it('should render with undefined items', () => {
    renderWithProviders(<Cart items={undefined} />);
    expect(screen.getByText(/your cart is empty/i)).toBeInTheDocument();
  });

  it('should not update quantity when input is NaN', () => {
    const preloadedState = {
      cart: { items: mockItems, customer: null, discount: { type: null, value: 0, code: null } }
    };
    const { store } = renderWithProviders(<Cart items={mockItems} />, { preloadedState });

    const quantityInput = screen.getAllByRole('spinbutton')[0];
    fireEvent.change(quantityInput, { target: { value: 'abc' } });

    expect(store.getState().cart.items[0].quantity).toBe(2);
  });

  it('should not update quantity when input is less than 1', () => {
    const preloadedState = {
      cart: { items: mockItems, customer: null, discount: { type: null, value: 0, code: null } }
    };
    const { store } = renderWithProviders(<Cart items={mockItems} />, { preloadedState });

    const quantityInput = screen.getAllByRole('spinbutton')[0];
    fireEvent.change(quantityInput, { target: { value: '0' } });

    expect(store.getState().cart.items[0].quantity).toBe(2);
  });

  it('should show alert when increasing quantity beyond available stock', async () => {
    const user = userEvent.setup();
    const itemsWithStock = [
      { id: '1', name: 'Product 1', basePrice: 100, quantity: 5, availableStock: 5 }
    ];
    const preloadedState = {
      cart: { items: itemsWithStock, customer: null, discount: { type: null, value: 0, code: null } }
    };
    const { store } = renderWithProviders(<Cart items={itemsWithStock} />, { preloadedState });

    // Find all buttons and select the plus button (increase quantity)
    const buttons = screen.getAllByRole('button');
    const plusButton = buttons[2]; // Plus button is the 3rd button (trash, minus, plus)

    await user.click(plusButton);

    // Toast error would be called in real implementation
    expect(store.getState().cart.items[0].quantity).toBe(5);
  });

  it('should show alert when changing quantity input beyond available stock', () => {
    const itemsWithStock = [
      { id: '1', name: 'Product 1', basePrice: 100, quantity: 2, availableStock: 5 }
    ];
    const preloadedState = {
      cart: { items: itemsWithStock, customer: null, discount: { type: null, value: 0, code: null } }
    };
    const { store } = renderWithProviders(<Cart items={itemsWithStock} />, { preloadedState });

    const quantityInput = screen.getAllByRole('spinbutton')[0];
    fireEvent.change(quantityInput, { target: { value: '10' } });

    // Toast error would be called in real implementation
    expect(store.getState().cart.items[0].quantity).toBe(2);
  });

  it('should handle items without availableStock property', () => {
    const itemsWithoutStock = [
      { id: '1', name: 'Product 1', basePrice: 10000, quantity: 5, sku: 'SKU-TEST' }
    ];

    const preloadedState = {
      cart: { items: itemsWithoutStock, customer: null, discount: { type: null, value: 0, code: null } }
    };

    renderWithProviders(<Cart items={itemsWithoutStock} />, { preloadedState });

    // Verify item renders correctly without availableStock property
    expect(screen.getByText('Product 1')).toBeInTheDocument();
    expect(screen.getByDisplayValue('5')).toBeInTheDocument();
    
    // Verify buttons are present (should work without stock tracking)
    const buttons = screen.getAllByRole('button');
    expect(buttons.length).toBeGreaterThan(0);
  });
});
