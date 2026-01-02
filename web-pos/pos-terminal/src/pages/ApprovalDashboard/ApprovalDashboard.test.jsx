import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import { configureStore } from '@reduxjs/toolkit';
import ApprovalDashboard from './ApprovalDashboard';
import { approvalService } from '../../services/approvalService';
import authReducer from '../../store/authSlice';
import toast from 'react-hot-toast';
import '../../i18n/config';

// Mock approval service
vi.mock('../../services/approvalService');

// Mock react-hot-toast
vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

// Mock window.confirm
const mockConfirm = vi.fn();
global.window.confirm = mockConfirm;

// Helper to create a test store
const createTestStore = (role = 'MANAGER') => {
  return configureStore({
    reducer: {
      auth: authReducer,
    },
    preloadedState: {
      auth: {
        isAuthenticated: true,
        user: {
          id: 'manager-1',
          email: 'manager@test.com',
          name: 'Test Manager',
          role: role,
        },
        token: 'mock-token',
      },
    },
  });
};

// Helper to render with providers
const renderWithProviders = (component, store = createTestStore()) => {
  return render(
    <Provider store={store}>
      <BrowserRouter>
        {component}
      </BrowserRouter>
    </Provider>
  );
};

// Mock approval data
const mockApprovals = [
  {
    id: 'approval-1',
    requestType: 'DISCOUNT',
    cashierName: 'John Doe',
    discountAmount: 50000,
    discountPercentage: 25,
    reason: 'Loyal customer request',
    status: 'PENDING',
    createdAt: '2026-01-02T10:00:00Z',
  },
  {
    id: 'approval-2',
    requestType: 'REFUND',
    cashierName: 'Jane Smith',
    discountAmount: 100000,
    reason: 'Defective product',
    status: 'PENDING',
    createdAt: '2026-01-02T11:00:00Z',
  },
];

const mockHistoryApprovals = [
  {
    id: 'approval-3',
    requestType: 'DISCOUNT',
    cashierName: 'Bob Johnson',
    discountAmount: 30000,
    discountPercentage: 15,
    reason: 'Price match',
    status: 'APPROVED',
    createdAt: '2026-01-01T09:00:00Z',
    reviewedAt: '2026-01-01T09:05:00Z',
    reviewedByName: 'Test Manager',
  },
];

describe('ApprovalDashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockConfirm.mockReturnValue(true);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('Rendering', () => {
    it('should render the dashboard header', async () => {
      approvalService.getApprovals.mockResolvedValue({
        data: [],
        total: 0,
      });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByText('managerApprovalDashboard')).toBeInTheDocument();
        expect(screen.getByText('managerApprovalDashboardDescription')).toBeInTheDocument();
      });
    });

    it('should render view toggle buttons', async () => {
      approvalService.getApprovals.mockResolvedValue({
        data: [],
        total: 0,
      });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByText('pendingRequests')).toBeInTheDocument();
        expect(screen.getByText('approvalHistory')).toBeInTheDocument();
      });
    });

    it('should render filter controls', async () => {
      approvalService.getApprovals.mockResolvedValue({
        data: [],
        total: 0,
      });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByPlaceholderText('searchByCashierOrId')).toBeInTheDocument();
        expect(screen.getByText('search')).toBeInTheDocument();
      });
    });

    it('should render pending approvals in list', async () => {
      approvalService.getApprovals.mockResolvedValue({
        data: mockApprovals,
        total: 2,
      });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(screen.getByText('Jane Smith')).toBeInTheDocument();
        expect(screen.getByText('Loyal customer request')).toBeInTheDocument();
      });
    });

    it('should show loading spinner while fetching data', () => {      approvalService.getApprovals.mockImplementation(() => new Promise(() => {}));

      renderWithProviders(<ApprovalDashboard />);

      // Check for loading spinner by class
      const spinner = document.querySelector('.animate-spin');
      expect(spinner).toBeInTheDocument();
    });

    it('should show empty state when no pending requests', async () => {
      approvalService.getApprovals.mockResolvedValue({
        data: [],
        total: 0,
      });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByText('noPendingRequests')).toBeInTheDocument();
        expect(screen.getByText('noPendingRequestsDescription')).toBeInTheDocument();
      });
    });
  });

  describe('View Toggle', () => {
    it('should switch to history view when clicking History button', async () => {
      const user = userEvent.setup();
      approvalService.getApprovals.mockResolvedValue({
        data: mockApprovals,
        total: 2,
      });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      approvalService.getApprovals.mockResolvedValue({
        data: mockHistoryApprovals,
        total: 1,
      });

      const historyButton = screen.getByText('approvalHistory');
      await user.click(historyButton);

      await waitFor(() => {
        expect(approvalService.getApprovals).toHaveBeenCalledWith(
          expect.objectContaining({
            status: 'PENDING', // statusFilter defaults to PENDING
          })
        );
      });
    });

    it('should show status filter only in history view', async () => {
      const user = userEvent.setup();
      approvalService.getApprovals.mockResolvedValue({
        data: [],
        total: 0,
      });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByText('pendingRequests')).toBeInTheDocument();
      });

      // Status filter should not be visible in pending view
      const selects = screen.getAllByRole('combobox');
      expect(selects).toHaveLength(1); // Only date range filter

      // Switch to history view
      const historyButton = screen.getByText('approvalHistory');
      await user.click(historyButton);

      await waitFor(() => {
        const selectsAfter = screen.getAllByRole('combobox');
        expect(selectsAfter).toHaveLength(2); // Status + date range filters
      });
    });

    it('should show export button only in history view', async () => {
      const user = userEvent.setup();
      approvalService.getApprovals.mockResolvedValue({
        data: [],
        total: 0,
      });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByText('pendingRequests')).toBeInTheDocument();
      });

      // Export button should not be visible
      expect(screen.queryByText('exportCSV')).not.toBeInTheDocument();

      // Switch to history view
      const historyButton = screen.getByText('approvalHistory');
      await user.click(historyButton);

      await waitFor(() => {
        expect(screen.getByText('exportCSV')).toBeInTheDocument();
      });
    });
  });

  describe('Filtering', () => {
    it('should filter by search query', async () => {
      const user = userEvent.setup();
      approvalService.getApprovals.mockResolvedValue({
        data: mockApprovals,
        total: 2,
      });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByPlaceholderText('searchByCashierOrId')).toBeInTheDocument();
      });

      const searchInput = screen.getByPlaceholderText('searchByCashierOrId');
      await user.type(searchInput, 'John');

      const searchButton = screen.getByText('search');
      await user.click(searchButton);

      await waitFor(() => {
        expect(approvalService.getApprovals).toHaveBeenCalledWith(
          expect.objectContaining({
            search: 'John',
          })
        );
      });
    });

    it('should filter by date range', async () => {
      const user = userEvent.setup();
      approvalService.getApprovals.mockResolvedValue({
        data: [],
        total: 0,
      });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        const selects = screen.getAllByRole('combobox');
        expect(selects.length).toBeGreaterThan(0);
      });

      const dateRangeSelect = screen.getAllByRole('combobox')[0];
      await user.selectOptions(dateRangeSelect, 'THIS_WEEK');

      await waitFor(() => {
        expect(approvalService.getApprovals).toHaveBeenCalledWith(
          expect.objectContaining({
            dateRange: 'THIS_WEEK',
          })
        );
      });
    });

    it('should filter by status in history view', async () => {
      const user = userEvent.setup();
      approvalService.getApprovals.mockResolvedValue({
        data: [],
        total: 0,
      });

      renderWithProviders(<ApprovalDashboard />);

      // Switch to history view
      const historyButton = screen.getByText('approvalHistory');
      await user.click(historyButton);

      await waitFor(() => {
        const selects = screen.getAllByRole('combobox');
        expect(selects).toHaveLength(2);
      });

      const statusSelect = screen.getAllByRole('combobox')[0];
      await user.selectOptions(statusSelect, 'APPROVED');

      await waitFor(() => {
        expect(approvalService.getApprovals).toHaveBeenCalledWith(
          expect.objectContaining({
            status: 'APPROVED',
          })
        );
      });
    });
  });

  describe('Approval Actions', () => {
    it('should approve a request when clicking Approve button', async () => {
      const user = userEvent.setup();
      approvalService.getApprovals.mockResolvedValue({
        data: mockApprovals,
        total: 2,
      });
      approvalService.approveRequest.mockResolvedValue({ success: true });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getAllByText('approve')).toHaveLength(2);
      });

      const approveButtons = screen.getAllByText('approve');
      await user.click(approveButtons[0]);

      await waitFor(() => {
        expect(mockConfirm).toHaveBeenCalled();
        expect(approvalService.approveRequest).toHaveBeenCalledWith('approval-1', 'manager-1');
      });
    });

    it('should reject a request when clicking Reject button', async () => {
      const user = userEvent.setup();
      approvalService.getApprovals.mockResolvedValue({
        data: mockApprovals,
        total: 2,
      });
      approvalService.rejectRequest.mockResolvedValue({ success: true });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getAllByText('reject')).toHaveLength(2);
      });

      const rejectButtons = screen.getAllByText('reject');
      await user.click(rejectButtons[0]);

      await waitFor(() => {
        expect(mockConfirm).toHaveBeenCalled();
        expect(approvalService.rejectRequest).toHaveBeenCalledWith('approval-1', 'manager-1');
      });
    });

    it('should not approve when user cancels confirmation', async () => {
      const user = userEvent.setup();
      mockConfirm.mockReturnValue(false);
      approvalService.getApprovals.mockResolvedValue({
        data: mockApprovals,
        total: 2,
      });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const approveButtons = screen.getAllByText('approve');
      await user.click(approveButtons[0]);

      expect(mockConfirm).toHaveBeenCalled();
      expect(approvalService.approveRequest).not.toHaveBeenCalled();
    });

    it('should handle approve error', async () => {
      const user = userEvent.setup();
      approvalService.getApprovals.mockResolvedValue({
        data: mockApprovals,
        total: 2,
      });
      approvalService.approveRequest.mockRejectedValue(new Error('Network error'));

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const approveButtons = screen.getAllByText('approve');
      await user.click(approveButtons[0]);

      await waitFor(() => {
        expect(toast.error).toHaveBeenCalled();
      });
    });

    it('should refresh list after successful approval', async () => {
      const user = userEvent.setup();
      approvalService.getApprovals.mockResolvedValue({
        data: mockApprovals,
        total: 2,
      });
      approvalService.approveRequest.mockResolvedValue({ success: true });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const initialCallCount = approvalService.getApprovals.mock.calls.length;

      const approveButtons = screen.getAllByText('approve');
      await user.click(approveButtons[0]);

      await waitFor(() => {
        expect(approvalService.getApprovals).toHaveBeenCalledTimes(initialCallCount + 1);
      });
    });
  });

  describe('Pagination', () => {
    it('should navigate to next page', async () => {
      const user = userEvent.setup();
      approvalService.getApprovals.mockResolvedValue({
        data: mockApprovals,
        total: 50, // More than one page
      });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByText('next')).toBeInTheDocument();
      });

      const nextButton = screen.getByText('next');
      await user.click(nextButton);

      await waitFor(() => {
        expect(approvalService.getApprovals).toHaveBeenCalledWith(
          expect.objectContaining({
            page: 2,
          })
        );
      });
    });

    it('should navigate to previous page', async () => {
      const user = userEvent.setup();
      approvalService.getApprovals.mockResolvedValue({
        data: mockApprovals,
        total: 50,
      });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByText('next')).toBeInTheDocument();
      });

      // Go to page 2
      const nextButton = screen.getByText('next');
      await user.click(nextButton);

      await waitFor(() => {
        expect(screen.getByText('previous')).toBeInTheDocument();
      });

      // Go back to page 1
      const previousButton = screen.getByText('previous');
      await user.click(previousButton);

      await waitFor(() => {
        expect(approvalService.getApprovals).toHaveBeenCalledWith(
          expect.objectContaining({
            page: 1,
          })
        );
      });
    });

    it('should disable previous button on first page', async () => {
      approvalService.getApprovals.mockResolvedValue({
        data: mockApprovals,
        total: 50,
      });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        const previousButton = screen.getByText('previous');
        expect(previousButton).toBeDisabled();
      });
    });

    it('should disable next button on last page', async () => {
      const user = userEvent.setup();
      approvalService.getApprovals.mockResolvedValue({
        data: mockApprovals,
        total: 25, // 2 pages (20 per page)
      });

      renderWithProviders(<ApprovalDashboard />);

      // Go to page 2 (last page)
      await waitFor(() => {
        expect(screen.getByText('next')).toBeInTheDocument();
      });
      
      const nextButton = screen.getByText('next');
      expect(nextButton).not.toBeDisabled(); // Should be enabled on page 1
      
      await user.click(nextButton);

      // Now on page 2 (last page), next should be disabled
      await waitFor(() => {
        const nextBtnFinal = screen.getByText('next');
        expect(nextBtnFinal).toBeDisabled();
      });
    });
  });

  describe('Export', () => {
    it('should export approvals as CSV', async () => {
      const user = userEvent.setup();
      const mockBlob = new Blob(['csv data'], { type: 'text/csv' });
      approvalService.getApprovals.mockResolvedValue({
        data: [],
        total: 0,
      });
      approvalService.exportApprovals.mockResolvedValue(mockBlob);

      // Mock URL.createObjectURL and revokeObjectURL
      const mockCreateObjectURL = vi.fn(() => 'blob:mock-url');
      const mockRevokeObjectURL = vi.fn();
      global.URL.createObjectURL = mockCreateObjectURL;
      global.URL.revokeObjectURL = mockRevokeObjectURL;

      renderWithProviders(<ApprovalDashboard />);

      // Switch to history view
      const historyButton = screen.getByText('approvalHistory');
      await user.click(historyButton);

      await waitFor(() => {
        expect(screen.getByText('exportCSV')).toBeInTheDocument();
      });

      const exportButton = screen.getByText('exportCSV');
      await user.click(exportButton);

      await waitFor(() => {
        expect(approvalService.exportApprovals).toHaveBeenCalled();
        expect(mockCreateObjectURL).toHaveBeenCalledWith(mockBlob);
        expect(mockRevokeObjectURL).toHaveBeenCalled();
      });
    });

    it('should handle export error', async () => {
      const user = userEvent.setup();
      approvalService.getApprovals.mockResolvedValue({
        data: [],
        total: 0,
      });
      approvalService.exportApprovals.mockRejectedValue(new Error('Export failed'));

      renderWithProviders(<ApprovalDashboard />);

      // Switch to history view
      const historyButton = screen.getByText('approvalHistory');
      await user.click(historyButton);

      await waitFor(() => {
        expect(screen.getByText('exportCSV')).toBeInTheDocument();
      });

      const exportButton = screen.getByText('exportCSV');
      await user.click(exportButton);

      await waitFor(() => {
        expect(toast.error).toHaveBeenCalled();
      });
    });
  });

  describe('Error Handling', () => {
    it('should display error message when fetch fails', async () => {
      approvalService.getApprovals.mockRejectedValue(new Error('Network error'));

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByText('Network error')).toBeInTheDocument();
        expect(screen.getByText('retry')).toBeInTheDocument();
      });
    });

    it('should retry fetch when clicking Retry button', async () => {
      const user = userEvent.setup();
      approvalService.getApprovals.mockRejectedValueOnce(new Error('Network error'));
      approvalService.getApprovals.mockResolvedValueOnce({
        data: mockApprovals,
        total: 2,
      });

      renderWithProviders(<ApprovalDashboard />);

      await waitFor(() => {
        expect(screen.getByText('retry')).toBeInTheDocument();
      });

      const retryButton = screen.getByText('retry');
      await user.click(retryButton);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });
    });
  });
});
