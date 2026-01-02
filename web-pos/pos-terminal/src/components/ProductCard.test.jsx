import { describe, it, expect, vi } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../test/testUtils';
import ProductCard from './ProductCard';

describe('ProductCard', () => {
  const mockProduct = {
    id: 'prod-1',
    name: 'Cappuccino',
    sku: 'SKU-001',
    categoryName: 'Beverages',
    basePrice: 30000,
    availableStock: 50,
  };

  it('should render product information', () => {
    const mockOnAddToCart = vi.fn();

    renderWithProviders(<ProductCard product={mockProduct} onAddToCart={mockOnAddToCart} />);

    expect(screen.getByText('Cappuccino')).toBeInTheDocument();
    expect(screen.getByText('SKU: SKU-001')).toBeInTheDocument();
    expect(screen.getByText('Beverages')).toBeInTheDocument();
    expect(screen.getByText(/30\.000/)).toBeInTheDocument(); // Price formatting
  });

  it('should call onAddToCart when add button clicked', async () => {
    const user = userEvent.setup();
    const mockOnAddToCart = vi.fn();

    renderWithProviders(<ProductCard product={mockProduct} onAddToCart={mockOnAddToCart} />);

    const addButton = screen.getByRole('button', { name: /add to cart/i });
    await user.click(addButton);

    expect(mockOnAddToCart).toHaveBeenCalledWith(mockProduct);
  });

  it('should show in stock indicator', () => {
    const mockOnAddToCart = vi.fn();

    renderWithProviders(<ProductCard product={mockProduct} onAddToCart={mockOnAddToCart} />);

    expect(screen.getByText(/in stock.*50/i)).toBeInTheDocument();
  });

  it('should show low stock warning when stock is below 10', () => {
    const lowStockProduct = { ...mockProduct, availableStock: 5 };
    const mockOnAddToCart = vi.fn();

    renderWithProviders(<ProductCard product={lowStockProduct} onAddToCart={mockOnAddToCart} />);

    expect(screen.getByText(/low stock.*5/i)).toBeInTheDocument();
  });

  it('should show out of stock and disable button when stock is 0', () => {
    const outOfStockProduct = { ...mockProduct, availableStock: 0 };
    const mockOnAddToCart = vi.fn();

    renderWithProviders(<ProductCard product={outOfStockProduct} onAddToCart={mockOnAddToCart} />);

    expect(screen.getByText(/out of stock/i)).toBeInTheDocument();
    const addButton = screen.getByRole('button', { name: /add to cart/i });
    expect(addButton).toBeDisabled();
  });

  it('should not call onAddToCart when product is out of stock', async () => {
    const user = userEvent.setup();
    const outOfStockProduct = { ...mockProduct, availableStock: 0 };
    const mockOnAddToCart = vi.fn();

    renderWithProviders(<ProductCard product={outOfStockProduct} onAddToCart={mockOnAddToCart} />);

    const addButton = screen.getByRole('button', { name: /add to cart/i });
    await user.click(addButton);

    expect(mockOnAddToCart).not.toHaveBeenCalled();
  });

  it('should display product initial when no image', () => {
    const mockOnAddToCart = vi.fn();

    renderWithProviders(<ProductCard product={mockProduct} onAddToCart={mockOnAddToCart} />);

    // Should show 'C' for Cappuccino
    expect(screen.getByText('C')).toBeInTheDocument();
  });

  it('should display product without category name', () => {
    const mockOnAddToCart = vi.fn();
    const productWithoutCategory = { ...mockProduct, categoryName: null };

    renderWithProviders(<ProductCard product={productWithoutCategory} onAddToCart={mockOnAddToCart} />);

    // Should display product name (mockProduct has 'Cappuccino')
    expect(screen.getByText('Cappuccino')).toBeInTheDocument();
    // Should display price
    expect(screen.getByText(/Rp/)).toBeInTheDocument();
    // Should display stock
    expect(screen.getByText(/50/)).toBeInTheDocument();
  });

  it('should format price in IDR currency', () => {
    const mockOnAddToCart = vi.fn();

    renderWithProviders(<ProductCard product={mockProduct} onAddToCart={mockOnAddToCart} />);

    // Should show formatted price (Rp 30.000 or similar)
    const priceElement = screen.getByText(/30/);
    expect(priceElement).toBeInTheDocument();
  });

  it('should display product without category name', () => {
    const mockOnAddToCart = vi.fn();
    const productWithoutCategory = {
      ...mockProduct,
      categoryName: null
    };

    renderWithProviders(<ProductCard product={productWithoutCategory} onAddToCart={mockOnAddToCart} />);

    expect(screen.getByText('Cappuccino')).toBeInTheDocument();
    expect(screen.queryByText(/electronics/i)).not.toBeInTheDocument();
  });
});
