import { describe, it, expect, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../test/testUtils';
import ProductSearch from './ProductSearch';

describe('ProductSearch', () => {
  it('should render search input', () => {
    const mockOnSearch = vi.fn();
    const mockOnClear = vi.fn();

    renderWithProviders(<ProductSearch onSearch={mockOnSearch} onClear={mockOnClear} />);

    expect(screen.getByPlaceholderText(/search products/i)).toBeInTheDocument();
  });

  it('should call onSearch after debounce delay', async () => {
    const user = userEvent.setup();
    const mockOnSearch = vi.fn();
    const mockOnClear = vi.fn();

    renderWithProviders(<ProductSearch onSearch={mockOnSearch} onClear={mockOnClear} />);

    const input = screen.getByPlaceholderText(/search products/i);
    await user.type(input, 'coffee');

    // Wait for debounce (300ms)
    await waitFor(() => expect(mockOnSearch).toHaveBeenCalledWith('coffee'), {
      timeout: 500,
    });
  });

  it('should show clear button when input has text', async () => {
    const user = userEvent.setup();
    const mockOnSearch = vi.fn();
    const mockOnClear = vi.fn();

    renderWithProviders(<ProductSearch onSearch={mockOnSearch} onClear={mockOnClear} />);

    const input = screen.getByPlaceholderText(/search products/i);
    
    // Clear button should not be visible initially
    expect(screen.queryByRole('button')).not.toBeInTheDocument();

    await user.type(input, 'coffee');

    // Clear button should appear
    expect(screen.getByRole('button')).toBeInTheDocument();
  });

  it('should clear input and call onClear when clear button clicked', async () => {
    const user = userEvent.setup();
    const mockOnSearch = vi.fn();
    const mockOnClear = vi.fn();

    renderWithProviders(<ProductSearch onSearch={mockOnSearch} onClear={mockOnClear} />);

    const input = screen.getByPlaceholderText(/search products/i);
    await user.type(input, 'coffee');

    const clearButton = screen.getByRole('button');
    await user.click(clearButton);

    expect(input.value).toBe('');
    expect(mockOnClear).toHaveBeenCalled();
  });

  it('should call onClear when input is empty', async () => {
    const user = userEvent.setup();
    const mockOnSearch = vi.fn();
    const mockOnClear = vi.fn();

    renderWithProviders(<ProductSearch onSearch={mockOnSearch} onClear={mockOnClear} />);

    const input = screen.getByPlaceholderText(/search products/i);
    await user.type(input, 'c');
    await user.clear(input);

    await waitFor(() => expect(mockOnClear).toHaveBeenCalled(), {
      timeout: 500,
    });
  });

  it('should trim whitespace before calling onSearch', async () => {
    const user = userEvent.setup();
    const mockOnSearch = vi.fn();
    const mockOnClear = vi.fn();

    renderWithProviders(<ProductSearch onSearch={mockOnSearch} onClear={mockOnClear} />);

    const input = screen.getByPlaceholderText(/search products/i);
    await user.type(input, '  coffee  ');

    await waitFor(() => expect(mockOnSearch).toHaveBeenCalledWith('coffee'), {
      timeout: 500,
    });
  });
});
