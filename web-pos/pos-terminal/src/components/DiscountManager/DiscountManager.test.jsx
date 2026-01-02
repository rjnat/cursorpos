import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../../store/authSlice';
import cartReducer, { applyDiscount } from '../../store/cartSlice';
import DiscountManager from './DiscountManager';
import '../../i18n/config';

const createTestStore = (initialState = {}) => {
  return configureStore({
    reducer: {
      auth: authReducer,
      cart: cartReducer
    },
    preloadedState: {
      auth: {
        isAuthenticated: true,
        user: {
          id: 'user-1',
          name: 'Test Cashier',
          role: 'CASHIER',
          ...initialState.auth?.user
        },
        token: 'test-token'
      },
      cart: {
        items: [],
        discount: null,
        customer: null,
        ...initialState.cart
      }
    }
  });
};

const renderWithStore = (component, store = createTestStore()) => {
  return {
    ...render(
      <Provider store={store}>
        {component}
      </Provider>
    ),
    store
  };
};

describe('DiscountManager', () => {
  const mockOnRequestApproval = vi.fn();
  const subtotal = 100000; // Rp 100,000

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('rendering', () => {
    it('should render discount manager component', () => {
      renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />
      );
      
      expect(screen.getByText('Discount')).toBeInTheDocument();
      expect(screen.getByText('Quick Discount')).toBeInTheDocument();
      expect(screen.getByText('Custom Discount')).toBeInTheDocument();
    });

    it('should render quick discount buttons', () => {
      renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />
      );
      
      expect(screen.getByText('5%')).toBeInTheDocument();
      expect(screen.getByText('10%')).toBeInTheDocument();
      expect(screen.getByText('15%')).toBeInTheDocument();
      expect(screen.getByText('20%')).toBeInTheDocument();
    });

    it('should render discount type toggle buttons', () => {
      renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />
      );
      
      expect(screen.getByText('Percentage (%)')).toBeInTheDocument();
      expect(screen.getByText('Amount (Rp)')).toBeInTheDocument();
    });

    it('should show applied discount when present', () => {
      const store = createTestStore({
        cart: {
          items: [],
          discount: {
            type: 'percentage',
            value: 10,
            amount: 10000
          },
          customer: null
        }
      });

      renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />,
        store
      );
      
      expect(screen.getByText('10% Discount')).toBeInTheDocument();
      expect(screen.getByText('Remove')).toBeInTheDocument();
    });
  });

  describe('quick discount buttons', () => {
    it('should apply 5% quick discount without approval', async () => {
      const user = userEvent.setup();
      const { store } = renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />
      );
      
      const button5 = screen.getByText('5%');
      await user.click(button5);
      
      const state = store.getState();
      expect(state.cart.discount).toEqual({
        type: 'percentage',
        value: 5,
        amount: 5000
      });
      expect(mockOnRequestApproval).not.toHaveBeenCalled();
    });

    it('should apply 10% quick discount without approval', async () => {
      const user = userEvent.setup();
      const { store } = renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />
      );
      
      const button10 = screen.getByText('10%');
      await user.click(button10);
      
      const state = store.getState();
      expect(state.cart.discount).toEqual({
        type: 'percentage',
        value: 10,
        amount: 10000
      });
      expect(mockOnRequestApproval).not.toHaveBeenCalled();
    });

    it('should apply 20% quick discount without approval', async () => {
      const user = userEvent.setup();
      const { store } = renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />
      );
      
      const button20 = screen.getByText('20%');
      await user.click(button20);
      
      const state = store.getState();
      expect(state.cart.discount).toEqual({
        type: 'percentage',
        value: 20,
        amount: 20000
      });
      expect(mockOnRequestApproval).not.toHaveBeenCalled();
    });
  });

  describe('custom discount - percentage', () => {
    it('should apply custom percentage discount', async () => {
      const user = userEvent.setup();
      const { store } = renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />
      );
      
      const input = screen.getByPlaceholderText('0');
      const applyButton = screen.getByText('Apply');
      
      await user.type(input, '15');
      await user.click(applyButton);
      
      const state = store.getState();
      expect(state.cart.discount).toEqual({
        type: 'percentage',
        value: 15,
        amount: 15000
      });
    });

    it('should show discount preview for percentage', async () => {
      const user = userEvent.setup();
      renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />
      );
      
      const input = screen.getByPlaceholderText('0');
      await user.type(input, '15');
      
      await waitFor(() => {
        expect(screen.getByText('Discount (15.0%):')).toBeInTheDocument();
      });
    });

    it('should validate max percentage', async () => {
      const user = userEvent.setup();
      renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />
      );
      
      const input = screen.getByPlaceholderText('0');
      const applyButton = screen.getByText('Apply');
      
      await user.type(input, '150');
      await user.click(applyButton);
      
      await waitFor(() => {
        expect(screen.getByText(/Discount cannot exceed 100%/i)).toBeInTheDocument();
      });
    });

    it('should request approval for high percentage discount', async () => {
      const user = userEvent.setup();
      renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />
      );
      
      const input = screen.getByPlaceholderText('0');
      const applyButton = screen.getByText('Apply');
      
      await user.type(input, '25');
      
      await waitFor(() => {
        expect(screen.getByText(/requires manager approval/i)).toBeInTheDocument();
      });
      
      await user.click(applyButton);
      
      expect(mockOnRequestApproval).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'DISCOUNT',
          discountType: 'percentage',
          discountValue: 25,
          discountAmount: 25000
        })
      );
    });
  });

  describe('custom discount - amount', () => {
    it('should switch to amount discount type', async () => {
      const user = userEvent.setup();
      renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />
      );
      
      const amountButton = screen.getByText('Amount (Rp)');
      await user.click(amountButton);
      
      expect(amountButton).toHaveClass('bg-primary');
    });

    it('should apply custom amount discount', async () => {
      const user = userEvent.setup();
      const { store } = renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />
      );
      
      const amountButton = screen.getByText('Amount (Rp)');
      await user.click(amountButton);
      
      const input = screen.getByPlaceholderText('0');
      const applyButton = screen.getByText('Apply');
      
      await user.clear(input);
      await user.type(input, '15000');
      await user.click(applyButton);
      
      const state = store.getState();
      expect(state.cart.discount).toEqual({
        type: 'amount',
        value: 15000,
        amount: 15000
      });
    });

    it('should validate amount exceeds subtotal', async () => {
      const user = userEvent.setup();
      renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />
      );
      
      const amountButton = screen.getByText('Amount (Rp)');
      await user.click(amountButton);
      
      const input = screen.getByPlaceholderText('0');
      const applyButton = screen.getByText('Apply');
      
      await user.clear(input);
      await user.type(input, '150000');
      await user.click(applyButton);
      
      await waitFor(() => {
        expect(screen.getByText('Discount cannot exceed subtotal')).toBeInTheDocument();
      });
    });

    it('should request approval for high amount discount', async () => {
      const user = userEvent.setup();
      renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />
      );
      
      const amountButton = screen.getByText('Amount (Rp)');
      await user.click(amountButton);
      
      const input = screen.getByPlaceholderText('0');
      const applyButton = screen.getByText('Apply');
      
      await user.clear(input);
      await user.type(input, '25000'); // >20% of subtotal
      
      await waitFor(() => {
        expect(screen.getByText(/requires manager approval/i)).toBeInTheDocument();
      });
      
      await user.click(applyButton);
      
      expect(mockOnRequestApproval).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'DISCOUNT',
          discountType: 'amount',
          discountValue: 25000,
          discountAmount: 25000
        })
      );
    });
  });

  describe('discount removal', () => {
    it('should remove applied discount', async () => {
      const user = userEvent.setup();
      const { store } = renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />,
        createTestStore({
          cart: {
            items: [],
            discount: {
              type: 'percentage',
              value: 10,
              amount: 10000
            },
            customer: null
          }
        })
      );
      
      const removeButton = screen.getByText('Remove');
      await user.click(removeButton);
      
      const state = store.getState();
      expect(state.cart.discount).toEqual({
        type: null,
        value: 0,
        code: null
      });
    });
  });

  describe('keyboard interactions', () => {
    it('should apply discount on Enter key', async () => {
      const user = userEvent.setup();
      const { store } = renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />
      );
      
      const input = screen.getByPlaceholderText('0');
      await user.type(input, '10');
      await user.keyboard('{Enter}');
      
      const state = store.getState();
      expect(state.cart.discount).toEqual({
        type: 'percentage',
        value: 10,
        amount: 10000
      });
    });
  });

  describe('discount summary display', () => {
    it('should display discount summary with applied discount', () => {
      const store = createTestStore({
        cart: {
          items: [],
          discount: {
            type: 'percentage',
            value: 10,
            amount: 10000
          },
          customer: null
        }
      });

      renderWithStore(
        <DiscountManager 
          subtotal={subtotal} 
          onRequestApproval={mockOnRequestApproval} 
        />,
        store
      );
      
      expect(screen.getByText('Subtotal:')).toBeInTheDocument();
      expect(screen.getByText(/Discount \(10.0%\):/)).toBeInTheDocument();
      expect(screen.getByText('Total:')).toBeInTheDocument();
    });
  });
});
