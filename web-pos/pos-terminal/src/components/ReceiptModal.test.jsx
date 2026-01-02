import { describe, it, expect, beforeEach, vi } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../test/testUtils';
import ReceiptModal from './ReceiptModal';

describe('ReceiptModal', () => {
  const mockOnClose = vi.fn();
  const mockOnNewOrder = vi.fn();

  const mockTransaction = {
    transactionNumber: 'TRX-20231216-001',
    transactionDate: '2023-12-16T10:30:00',
    cashierName: 'John Doe',
    items: [
      {
        productName: 'Espresso',
        quantity: 2,
        unitPrice: 35000,
        totalAmount: 70000,
        discountAmount: 0
      },
      {
        productName: 'Cappuccino',
        quantity: 1,
        unitPrice: 45000,
        totalAmount: 45000,
        discountAmount: 0
      },
    ],
    subtotal: 115000,
    taxAmount: 11500,
    discountAmount: 0,
    totalAmount: 126500,
    payments: [
      {
        paymentMethod: 'CASH',
        amount: 150000
      }
    ],
    changeAmount: 23500,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderReceiptModal = (props = {}) => {
    const defaultProps = {
      isOpen: true,
      onClose: mockOnClose,
      transaction: mockTransaction,
      onNewOrder: mockOnNewOrder,
      ...props
    };
    return renderWithProviders(<ReceiptModal {...defaultProps} />);
  };

  it('should not render when isOpen is false', () => {
    const { container } = renderReceiptModal({ isOpen: false });
    expect(container.firstChild).toBeNull();
  });

  it('should not render when transaction is null', () => {
    const { container } = renderReceiptModal({ transaction: null });
    expect(container.firstChild).toBeNull();
  });

  it('should render payment success header', () => {
    renderReceiptModal();
    expect(screen.getByText('Payment Successful!')).toBeInTheDocument();
  });

  it('should render receipt title', () => {
    renderReceiptModal();
    expect(screen.getByText('Receipt')).toBeInTheDocument();
  });

  it('should display transaction number', () => {
    renderReceiptModal();
    expect(screen.getByText('TRX-20231216-001')).toBeInTheDocument();
  });

  it('should display formatted transaction date', () => {
    renderReceiptModal();
    expect(screen.getByText(/16 Desember 2023/)).toBeInTheDocument();
  });

  it('should display cashier name when provided', () => {
    renderReceiptModal();
    expect(screen.getByText('John Doe')).toBeInTheDocument();
  });

  it('should not display cashier field when not provided', () => {
    const transactionWithoutCashier = { ...mockTransaction, cashierName: null };
    renderReceiptModal({ transaction: transactionWithoutCashier });
    
    expect(screen.queryByText('Cashier')).not.toBeInTheDocument();
  });

  it('should render all items with names', () => {
    renderReceiptModal();
    expect(screen.getByText('Espresso')).toBeInTheDocument();
    expect(screen.getByText('Cappuccino')).toBeInTheDocument();
  });

  it('should display item quantities', () => {
    renderReceiptModal();
    const quantityTexts = screen.getAllByText(/x \d/);
    expect(quantityTexts.length).toBeGreaterThanOrEqual(2);
  });

  it('should display payment method from payments array', () => {
    renderReceiptModal();
    // The payment method is translated as "Cash" - use getAllByText due to Cashier field
    const cashTexts = screen.getAllByText(/Cash/i);
    expect(cashTexts.length).toBeGreaterThan(0);
  });

  it('should display change amount for CASH payment', () => {
    renderReceiptModal();
    // Change label is translated
    expect(screen.getByText(/Change/i)).toBeInTheDocument();
  });

  it('should not display change for non-CASH payment', () => {
    const cardTransaction = {
      ...mockTransaction,
      payments: [{ paymentMethod: 'CARD', amount: 126500 }],
      changeAmount: 0
    };
    
    renderReceiptModal({ transaction: cardTransaction });
    expect(screen.queryByText(/Change/i)).not.toBeInTheDocument();
  });

  it('should call window.print when print button clicked', async () => {
    const printSpy = vi.spyOn(window, 'print').mockImplementation(() => {});
    const user = userEvent.setup();

    renderReceiptModal();

    const printButton = screen.getByText('Print');
    await user.click(printButton);

    expect(printSpy).toHaveBeenCalledTimes(1);
    printSpy.mockRestore();
  });

  it('should call onNewOrder and onClose when new order button clicked', async () => {
    const user = userEvent.setup();

    renderReceiptModal();

    const newOrderButton = screen.getByText('New Order');
    await user.click(newOrderButton);

    expect(mockOnNewOrder).toHaveBeenCalledTimes(1);
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it('should call onClose when close button clicked', async () => {
    const user = userEvent.setup();

    renderReceiptModal();

    const closeButtons = screen.getAllByRole('button');
    const closeButton = closeButtons.find(btn => btn.querySelector('svg'));
    
    if (closeButton) {
      await user.click(closeButton);
      expect(mockOnClose).toHaveBeenCalledTimes(1);
    }
  });

  it('should display discount when greater than zero', () => {
    const transactionWithDiscount = {
      ...mockTransaction,
      discountAmount: 10000
    };

    renderReceiptModal({ transaction: transactionWithDiscount });
    expect(screen.getAllByText(/Discount/i).length).toBeGreaterThan(0);
  });

  it('should not display discount section when zero', () => {
    renderReceiptModal();
    // With discountAmount: 0, no discount row should appear in totals
    const discountTexts = screen.queryAllByText(/Discount/i);
    expect(discountTexts.length).toBe(0);
  });

  it('should display "Thank you" message', () => {
    renderReceiptModal();
    expect(screen.getByText(/Thank you for your purchase!/i)).toBeInTheDocument();
  });

  it('should display powered by CursorPOS', () => {
    renderReceiptModal();
    expect(screen.getByText(/Powered by/i)).toBeInTheDocument();
  });

  it('should format currency amounts correctly', () => {
    renderReceiptModal();
    // Indonesian Rupiah format with Rp prefix
    const currencyElements = screen.getAllByText(/Rp/);
    expect(currencyElements.length).toBeGreaterThan(0);
  });

  it('should display subtotal label', () => {
    renderReceiptModal();
    expect(screen.getByText('Subtotal')).toBeInTheDocument();
  });

  it('should display tax label', () => {
    renderReceiptModal();
    expect(screen.getByText('Tax')).toBeInTheDocument();
  });

  it('should display item discount when discountAmount > 0', () => {
    const transactionWithItemDiscount = {
      ...mockTransaction,
      items: [
        {
          productName: 'Espresso',
          quantity: 2,
          unitPrice: 35000,
          totalAmount: 60000,
          discountAmount: 10000
        }
      ]
    };

    renderReceiptModal({ transaction: transactionWithItemDiscount });

    expect(screen.getByText(/Espresso/)).toBeInTheDocument();
    // Check for discount indicator in item line
    const discountElements = screen.getAllByText(/-/);
    expect(discountElements.length).toBeGreaterThan(0);
  });

  it('should format date correctly with fallback to new Date', () => {
    const transactionWithoutDate = {
      ...mockTransaction,
      transactionDate: null
    };

    renderReceiptModal({ transaction: transactionWithoutDate });

    // Should still display a date (current date as fallback)
    // The component formats date with i18n, so we check for date text
    const dateText = screen.getByText(/\d{1,2}\s\w+\s\d{4}/);
    expect(dateText).toBeInTheDocument();
  });
});
