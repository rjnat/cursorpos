import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import authService from '../../services/authService';

/**
 * ManagerApprovalModal Component
 * 
 * Modal for requesting manager approval for:
 * - Large discounts (>20%)
 * - Refunds
 * - Price overrides
 * - Other sensitive operations
 * 
 * @param {Object} props
 * @param {boolean} props.isOpen - Whether modal is visible
 * @param {Function} props.onClose - Close handler
 * @param {Function} props.onApprove - Approve handler (receives manager info)
 * @param {Object} props.request - Approval request details
 * @param {string} props.request.type - Request type (DISCOUNT, REFUND, etc)
 * @param {string} props.request.reason - Human-readable reason
 * @param {string} props.request.cashierName - Requesting cashier name
 */
export default function ManagerApprovalModal({ isOpen, onClose, onApprove, request }) {
  const { t } = useTranslation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  
  if (!isOpen || !request) return null;
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    
    try {
      // Authenticate manager
      const response = await authService.login({
        email,
        password,
        tenantId: localStorage.getItem('tenantId')
      });
      
      // Verify manager role
      if (!response.user.roles?.includes('MANAGER') && !response.user.roles?.includes('ADMIN')) {
        setError(t('pos.approvalRequiresManager'));
        setLoading(false);
        return;
      }
      
      // Call approval callback with manager info
      onApprove({
        managerId: response.user.id,
        managerName: response.user.name,
        managerEmail: response.user.email,
        approvedAt: new Date().toISOString()
      });
      
      // Reset form
      setEmail('');
      setPassword('');
      onClose();
    } catch (err) {
      setError(t('pos.invalidManagerCredentials'));
    } finally {
      setLoading(false);
    }
  };
  
  const handleCancel = () => {
    setEmail('');
    setPassword('');
    setError(null);
    onClose();
  };
  
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
        {/* Header */}
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">
            {t('pos.managerApprovalRequired')}
          </h2>
        </div>
        
        {/* Body */}
        <form onSubmit={handleSubmit} className="px-6 py-4 space-y-4">
          {/* Request Details */}
          <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4">
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="font-medium text-yellow-800">
                  {t('pos.requestType')}:
                </span>
                <span className="text-yellow-900">{request.type}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="font-medium text-yellow-800">
                  {t('pos.requestedBy')}:
                </span>
                <span className="text-yellow-900">{request.cashierName}</span>
              </div>
              <div className="text-sm">
                <span className="font-medium text-yellow-800">
                  {t('pos.reason')}:
                </span>
                <p className="text-yellow-900 mt-1">{request.reason}</p>
              </div>
              {request.discountAmount && (
                <div className="flex justify-between text-sm pt-2 border-t border-yellow-300">
                  <span className="font-semibold text-yellow-800">
                    {t('pos.discountAmount')}:
                  </span>
                  <span className="font-semibold text-yellow-900">
                    Rp {request.discountAmount.toLocaleString('id-ID')}
                  </span>
                </div>
              )}
            </div>
          </div>
          
          {/* Manager Credentials */}
          <div className="space-y-3">
            <p className="text-sm text-gray-600">
              {t('pos.enterManagerCredentials')}
            </p>
            
            <div>
              <label htmlFor="manager-email" className="block text-sm font-medium text-gray-700 mb-1">
                {t('auth.email')}
              </label>
              <input
                id="manager-email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                placeholder={t('auth.email')}
                required
                autoFocus
              />
            </div>
            
            <div>
              <label htmlFor="manager-password" className="block text-sm font-medium text-gray-700 mb-1">
                {t('auth.password')}
              </label>
              <input
                id="manager-password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                placeholder={t('auth.password')}
                required
              />
            </div>
          </div>
          
          {/* Error Message */}
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-md p-3">
              <p className="text-sm text-red-800">{error}</p>
            </div>
          )}
          
          {/* Actions */}
          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={handleCancel}
              className="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 transition-colors"
              disabled={loading}
            >
              {t('common.cancel')}
            </button>
            <button
              type="submit"
              className="flex-1 px-4 py-2 bg-primary text-white rounded-md hover:bg-blue-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={loading}
            >
              {loading ? t('common.loading') : t('pos.approve')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
