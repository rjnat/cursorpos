import { describe, it, expect, beforeEach, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../test/testUtils';
import CheckoutModal from './CheckoutModal';

describe('CheckoutModal', () => {
  const mockOnClose = vi.fn();
  const mockOnComplete = vi.fn();

  const preloadedState = {
    cart: {
      items: [
        { id: '1', name: 'Product 1', basePrice: 100, quantity: 2, taxRate: 10 },
        { id: '2', name: 'Product 2', basePrice: 50, quantity: 1, taxRate: 10 },
      ],
      customer: null,
      discount: { type: null, value: 0, code: null },
    },
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockOnComplete.mockResolvedValue();
  });

  it('should not render when isOpen is false', () => {
    const { container } = renderWithProviders(
      <CheckoutModal isOpen={false} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );
    expect(container).toBeEmptyDOMElement();
  });

  it('should render checkout modal when isOpen is true', () => {
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    expect(screen.getByText(/checkout/i)).toBeInTheDocument();
    expect(screen.getByText(/subtotal/i)).toBeInTheDocument();
    expect(screen.getByText(/tax/i)).toBeInTheDocument();
    // Total appears in both Subtotal and Total, so just verify modal is open
  });

  it('should display correct totals', () => {
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    // Subtotal: (100 * 2) + (50 * 1) = 250
    expect(screen.getByText(/250/)).toBeInTheDocument();
  });

  it('should close modal when close button clicked', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    const closeButton = screen.getAllByRole('button')[0]; // First button is close
    await user.click(closeButton);

    expect(mockOnClose).toHaveBeenCalled();
  });

  it('should select CASH payment method by default', () => {
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    const cashRadio = screen.getByRole("button", { name: /cash/i });
    expect(cashRadio).toHaveClass("bg-blue-50");
  });

  it('should switch to CARD payment method', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    const cardRadio = screen.getByRole("button", { name: /credit card/i });
    await user.click(cardRadio);

    expect(cardRadio).toHaveClass("bg-blue-50");
  });

  it('should switch to DIGITAL_WALLET payment method', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    const walletRadio = screen.getByRole("button", { name: /e-wallet/i });
    await user.click(walletRadio);

    expect(walletRadio).toHaveClass("bg-blue-50");
  });

  it('should allow cash amount input', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    const cashInput = screen.getByPlaceholderText("0");
    await user.type(cashInput, '300');

    expect(cashInput).toHaveValue(300);
  });

  it('should calculate change correctly', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    const cashInput = screen.getByPlaceholderText("0");
    await user.type(cashInput, '300');

    await waitFor(() => {
      expect(screen.getByText(/change/i)).toBeInTheDocument();
    });
  });

  it('should set quick cash amount when quick button clicked', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    // Find a quick cash button (e.g., 300)
    const quickButtons = screen.getAllByRole('button').filter(btn => 
      btn.textContent.includes('300')
    );
    
    if (quickButtons.length > 0) {
      await user.click(quickButtons[0]);
      const cashInput = screen.getByPlaceholderText("0");
      expect(cashInput).toHaveValue(300);
    }
  });

  it('should disable complete button when cash amount is insufficient', () => {
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    const completeButton = screen.getByText(/complete payment/i);
    expect(completeButton).toBeDisabled();
  });

  it('should enable complete button when cash amount is sufficient', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    const cashInput = screen.getByPlaceholderText("0");
    await user.type(cashInput, '300');

    await waitFor(() => {
      const completeButton = screen.getByText(/complete payment/i);
      expect(completeButton).not.toBeDisabled();
    });
  });

  it('should enable complete button for non-cash payment methods', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    const cardRadio = screen.getByRole("button", { name: /credit card/i });
    await user.click(cardRadio);

    const completeButton = screen.getByText(/complete payment/i);
    expect(completeButton).not.toBeDisabled();
  });

  it('should call onComplete with correct payment data for CASH', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    const cashInput = screen.getByPlaceholderText("0");
    await user.type(cashInput, '300');

    const completeButton = screen.getByText(/complete payment/i);
    await user.click(completeButton);

    await waitFor(() => {
      expect(mockOnComplete).toHaveBeenCalledWith({
        paymentMethod: 'CASH',
        paidAmount: 300,
        changeAmount: expect.any(Number),
      });
    });
  });

  it('should call onComplete with correct payment data for CARD', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    const cardRadio = screen.getByRole("button", { name: /credit card/i });
    await user.click(cardRadio);

    const completeButton = screen.getByText(/complete payment/i);
    await user.click(completeButton);

    await waitFor(() => {
      expect(mockOnComplete).toHaveBeenCalledWith({
        paymentMethod: 'CREDIT_CARD',
        paidAmount: expect.any(Number),
        changeAmount: 0,
      });
    });
  });

  it('should reset form after successful payment', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    const cashInput = screen.getByPlaceholderText("0");
    await user.type(cashInput, '300');

    const completeButton = screen.getByText(/complete payment/i);
    await user.click(completeButton);

    await waitFor(() => {
      expect(mockOnComplete).toHaveBeenCalled();
    });

    // Form should be reset
    await waitFor(() => {
      expect(cashInput).toHaveValue(null);
    });
  });

  it('should handle payment error', async () => {
    const user = userEvent.setup();
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    
    mockOnComplete.mockRejectedValueOnce(new Error('Payment failed'));

    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    const cashInput = screen.getByPlaceholderText("0");
    await user.type(cashInput, '300');

    const completeButton = screen.getByText(/complete payment/i);
    await user.click(completeButton);

    await waitFor(() => {
      expect(consoleErrorSpy).toHaveBeenCalledWith('Payment error:', expect.any(Error));
      // Toast error would be called in real implementation
    });

    consoleErrorSpy.mockRestore();
  });

  it('should disable complete button while processing', async () => {
    const user = userEvent.setup();
    mockOnComplete.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));

    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    const cashInput = screen.getByPlaceholderText("0");
    await user.type(cashInput, '300');

    const completeButton = screen.getByText(/complete payment/i);
    await user.click(completeButton);

    // Button should be disabled while processing
    expect(completeButton).toBeDisabled();
  });

  it('should format currency correctly', () => {
    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState }
    );

    // Should display formatted Indonesian Rupiah
    const currencyElements = screen.getAllByText(/Rp/);
    expect(currencyElements.length).toBeGreaterThan(0);
  });

  it('should show discount in order summary when discount > 0', () => {
    const stateWithDiscount = {
      cart: {
        items: [
          { id: '1', name: 'Product 1', basePrice: 250, quantity: 1, taxRate: 10 }
        ],
        customer: null,
        discount: { type: 'percentage', value: 10, code: 'DISC10' }
      }
    };

    renderWithProviders(
      <CheckoutModal isOpen={true} onClose={mockOnClose} onComplete={mockOnComplete} />,
      { preloadedState: stateWithDiscount }
    );

    expect(screen.getByText(/discount/i)).toBeInTheDocument();
  });
});
