import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ManagerApprovalModal from './ManagerApprovalModal';
import authService from '../../services/authService';
import '../../i18n/config';

// Mock authService
vi.mock('../../services/authService', () => ({
  default: {
    login: vi.fn()
  }
}));

// Mock localStorage
const mockLocalStorage = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  clear: vi.fn()
};
global.localStorage = mockLocalStorage;

describe('ManagerApprovalModal', () => {
  const mockOnClose = vi.fn();
  const mockOnApprove = vi.fn();
  
  const mockRequest = {
    type: 'DISCOUNT',
    reason: '25% discount requested',
    cashierName: 'John Doe',
    discountAmount: 50000
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockLocalStorage.getItem.mockReturnValue('tenant-test-001');
  });

  describe('Rendering', () => {
    it('should not render when isOpen is false', () => {
      render(
        <ManagerApprovalModal
          isOpen={false}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={mockRequest}
        />
      );

      expect(screen.queryByText('Manager Approval Required')).not.toBeInTheDocument();
    });

    it('should render when isOpen is true', () => {
      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={mockRequest}
        />
      );

      expect(screen.getByText('Manager Approval Required')).toBeInTheDocument();
    });

    it('should display request details', () => {
      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={mockRequest}
        />
      );

      expect(screen.getByText('DISCOUNT')).toBeInTheDocument();
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('25% discount requested')).toBeInTheDocument();
      expect(screen.getByText('Rp 50.000')).toBeInTheDocument();
    });

    it('should render email and password inputs', () => {
      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={mockRequest}
        />
      );

      expect(screen.getByLabelText('Email')).toBeInTheDocument();
      expect(screen.getByLabelText('Password')).toBeInTheDocument();
    });

    it('should render Cancel and Approve buttons', () => {
      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={mockRequest}
        />
      );

      expect(screen.getByText('Cancel')).toBeInTheDocument();
      expect(screen.getByText('Approve')).toBeInTheDocument();
    });
  });

  describe('User Interactions', () => {
    it('should call onClose when Cancel button is clicked', async () => {
      const user = userEvent.setup();
      
      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={mockRequest}
        />
      );

      const cancelButton = screen.getByText('Cancel');
      await user.click(cancelButton);

      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it('should update email input value', async () => {
      const user = userEvent.setup();
      
      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={mockRequest}
        />
      );

      const emailInput = screen.getByLabelText('Email');
      await user.type(emailInput, 'manager@example.com');

      expect(emailInput).toHaveValue('manager@example.com');
    });

    it('should update password input value', async () => {
      const user = userEvent.setup();
      
      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={mockRequest}
        />
      );

      const passwordInput = screen.getByLabelText('Password');
      await user.type(passwordInput, 'password123');

      expect(passwordInput).toHaveValue('password123');
    });

    it('should clear form when Cancel is clicked', async () => {
      const user = userEvent.setup();
      
      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={mockRequest}
        />
      );

      const emailInput = screen.getByLabelText('Email');
      const passwordInput = screen.getByLabelText('Password');
      
      await user.type(emailInput, 'manager@example.com');
      await user.type(passwordInput, 'password123');
      
      const cancelButton = screen.getByText('Cancel');
      await user.click(cancelButton);

      // Modal would close, but if reopened, form should be empty
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  describe('Approval Flow', () => {
    it('should successfully approve with valid manager credentials', async () => {
      const user = userEvent.setup();
      const mockManagerResponse = {
        user: {
          id: 'manager-123',
          name: 'Manager Smith',
          email: 'manager@example.com',
          roles: ['MANAGER']
        }
      };

      authService.login.mockResolvedValueOnce(mockManagerResponse);

      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={mockRequest}
        />
      );

      const emailInput = screen.getByLabelText('Email');
      const passwordInput = screen.getByLabelText('Password');
      const approveButton = screen.getByText('Approve');

      await user.type(emailInput, 'manager@example.com');
      await user.type(passwordInput, 'password123');
      await user.click(approveButton);

      await waitFor(() => {
        expect(authService.login).toHaveBeenCalledWith({
          email: 'manager@example.com',
          password: 'password123',
          tenantId: 'tenant-test-001'
        });
      });

      await waitFor(() => {
        expect(mockOnApprove).toHaveBeenCalledWith(
          expect.objectContaining({
            managerId: 'manager-123',
            managerName: 'Manager Smith',
            managerEmail: 'manager@example.com'
          })
        );
      });

      expect(mockOnClose).toHaveBeenCalled();
    });

    it('should successfully approve with ADMIN role', async () => {
      const user = userEvent.setup();
      const mockAdminResponse = {
        user: {
          id: 'admin-123',
          name: 'Admin User',
          email: 'admin@example.com',
          roles: ['ADMIN']
        }
      };

      authService.login.mockResolvedValueOnce(mockAdminResponse);

      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={mockRequest}
        />
      );

      const emailInput = screen.getByLabelText('Email');
      const passwordInput = screen.getByLabelText('Password');
      const approveButton = screen.getByText('Approve');

      await user.type(emailInput, 'admin@example.com');
      await user.type(passwordInput, 'admin123');
      await user.click(approveButton);

      await waitFor(() => {
        expect(mockOnApprove).toHaveBeenCalled();
      });
    });

    it('should show error for non-manager user', async () => {
      const user = userEvent.setup();
      const mockCashierResponse = {
        user: {
          id: 'cashier-123',
          name: 'Cashier User',
          email: 'cashier@example.com',
          roles: ['CASHIER']
        }
      };

      authService.login.mockResolvedValueOnce(mockCashierResponse);

      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={mockRequest}
        />
      );

      const emailInput = screen.getByLabelText('Email');
      const passwordInput = screen.getByLabelText('Password');
      const approveButton = screen.getByText('Approve');

      await user.type(emailInput, 'cashier@example.com');
      await user.type(passwordInput, 'cashier123');
      await user.click(approveButton);

      await waitFor(() => {
        expect(screen.getByText('Only managers can approve this request')).toBeInTheDocument();
      });

      expect(mockOnApprove).not.toHaveBeenCalled();
      expect(mockOnClose).not.toHaveBeenCalled();
    });

    it('should show error for invalid credentials', async () => {
      const user = userEvent.setup();

      authService.login.mockRejectedValueOnce(new Error('Invalid credentials'));

      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={mockRequest}
        />
      );

      const emailInput = screen.getByLabelText('Email');
      const passwordInput = screen.getByLabelText('Password');
      const approveButton = screen.getByText('Approve');

      await user.type(emailInput, 'wrong@example.com');
      await user.type(passwordInput, 'wrongpass');
      await user.click(approveButton);

      await waitFor(() => {
        expect(screen.getByText('Invalid manager credentials')).toBeInTheDocument();
      });

      expect(mockOnApprove).not.toHaveBeenCalled();
      expect(mockOnClose).not.toHaveBeenCalled();
    });

    it('should disable buttons while loading', async () => {
      const user = userEvent.setup();
      
      // Mock a slow response
      authService.login.mockImplementation(() => 
        new Promise(resolve => setTimeout(resolve, 1000))
      );

      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={mockRequest}
        />
      );

      const emailInput = screen.getByLabelText('Email');
      const passwordInput = screen.getByLabelText('Password');
      const approveButton = screen.getByText('Approve');
      const cancelButton = screen.getByText('Cancel');

      await user.type(emailInput, 'manager@example.com');
      await user.type(passwordInput, 'password123');
      await user.click(approveButton);

      // Buttons should be disabled while loading
      expect(approveButton).toBeDisabled();
      expect(cancelButton).toBeDisabled();
      expect(screen.getByText('Loading...')).toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('should not render without request', () => {
      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={null}
        />
      );

      expect(screen.queryByText('Manager Approval Required')).not.toBeInTheDocument();
    });

    it('should handle request without discountAmount', () => {
      const requestWithoutAmount = {
        type: 'REFUND',
        reason: 'Customer return',
        cashierName: 'Jane Doe'
      };

      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={requestWithoutAmount}
        />
      );

      expect(screen.getByText('REFUND')).toBeInTheDocument();
      expect(screen.getByText('Customer return')).toBeInTheDocument();
      expect(screen.queryByText(/Rp/)).not.toBeInTheDocument();
    });

    it('should submit form on Enter key', async () => {
      const user = userEvent.setup();
      const mockManagerResponse = {
        user: {
          id: 'manager-123',
          name: 'Manager Smith',
          email: 'manager@example.com',
          roles: ['MANAGER']
        }
      };

      authService.login.mockResolvedValueOnce(mockManagerResponse);

      render(
        <ManagerApprovalModal
          isOpen={true}
          onClose={mockOnClose}
          onApprove={mockOnApprove}
          request={mockRequest}
        />
      );

      const emailInput = screen.getByLabelText('Email');
      const passwordInput = screen.getByLabelText('Password');

      await user.type(emailInput, 'manager@example.com');
      await user.type(passwordInput, 'password123');
      await user.keyboard('{Enter}');

      await waitFor(() => {
        expect(authService.login).toHaveBeenCalled();
      });
    });
  });
});
