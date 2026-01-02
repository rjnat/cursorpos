import { useTranslation } from 'react-i18next';

const ApprovalCard = ({ approval, onApprove, onReject, view }) => {
  const { t } = useTranslation();

  const getRequestTypeLabel = (type) => {
    const labels = {
      DISCOUNT: t('discount'),
      REFUND: t('refund'),
      PRICE_OVERRIDE: t('priceOverride'),
      VOID_TRANSACTION: t('voidTransaction')
    };
    return labels[type] || type;
  };

  const getRequestTypeBadgeColor = (type) => {
    const colors = {
      DISCOUNT: 'bg-blue-100 text-blue-800',
      REFUND: 'bg-yellow-100 text-yellow-800',
      PRICE_OVERRIDE: 'bg-purple-100 text-purple-800',
      VOID_TRANSACTION: 'bg-red-100 text-red-800'
    };
    return colors[type] || 'bg-gray-100 text-gray-800';
  };

  const getStatusBadge = (status) => {
    const badges = {
      PENDING: { color: 'bg-yellow-100 text-yellow-800', label: t('pending') },
      APPROVED: { color: 'bg-green-100 text-green-800', label: t('approved') },
      REJECTED: { color: 'bg-red-100 text-red-800', label: t('rejected') }
    };
    const badge = badges[status] || badges.PENDING;
    
    return (
      <span className={`px-3 py-1 rounded-full text-xs font-semibold ${badge.color}`}>
        {badge.label}
      </span>
    );
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('id-ID', {
      style: 'currency',
      currency: 'IDR',
      minimumFractionDigits: 0
    }).format(amount);
  };

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow">
      <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
        {/* Left Side - Request Details */}
        <div className="flex-1">
          {/* Header */}
          <div className="flex items-center gap-3 mb-3">
            <span className={`px-3 py-1 rounded-full text-sm font-semibold ${getRequestTypeBadgeColor(approval.requestType)}`}>
              {getRequestTypeLabel(approval.requestType)}
            </span>
            {getStatusBadge(approval.status)}
          </div>

          {/* Request Info */}
          <div className="space-y-2">
            <div className="flex items-center gap-2 text-gray-700">
              <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              <span className="font-semibold">{t('requestedBy')}:</span>
              <span>{approval.cashierName}</span>
            </div>

            <div className="flex items-center gap-2 text-gray-700">
              <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span className="font-semibold">{t('requestTime')}:</span>
              <span>{formatDate(approval.createdAt)}</span>
            </div>

            {approval.discountAmount && (
              <div className="flex items-center gap-2 text-gray-700">
                <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span className="font-semibold">{t('discountAmount')}:</span>
                <span className="text-lg font-bold text-green-600">
                  {formatCurrency(approval.discountAmount)}
                </span>
              </div>
            )}

            {approval.discountPercentage && (
              <div className="flex items-center gap-2 text-gray-700">
                <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                </svg>
                <span className="font-semibold">{t('discountPercentage')}:</span>
                <span className="text-lg font-bold text-green-600">
                  {approval.discountPercentage}%
                </span>
              </div>
            )}

            {approval.reason && (
              <div className="mt-3 p-3 bg-gray-50 rounded-lg">
                <p className="text-sm font-semibold text-gray-700 mb-1">{t('reason')}:</p>
                <p className="text-gray-600">{approval.reason}</p>
              </div>
            )}

            {/* History Info (for approved/rejected) */}
            {view === 'history' && approval.reviewedAt && (
              <div className="mt-3 pt-3 border-t border-gray-200">
                <div className="flex items-center gap-2 text-gray-600 text-sm">
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <span>
                    {approval.status === 'APPROVED' ? t('approvedBy') : t('rejectedBy')}: 
                    <span className="font-semibold ml-1">{approval.reviewedByName}</span>
                  </span>
                  <span className="text-gray-400">•</span>
                  <span>{formatDate(approval.reviewedAt)}</span>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Right Side - Actions (only for pending requests) */}
        {view === 'pending' && approval.status === 'PENDING' && (
          <div className="flex md:flex-col gap-2 md:min-w-[120px]">
            <button
              onClick={() => onApprove(approval.id)}
              className="flex-1 md:flex-none px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors font-semibold flex items-center justify-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
              {t('approve')}
            </button>
            <button
              onClick={() => onReject(approval.id)}
              className="flex-1 md:flex-none px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors font-semibold flex items-center justify-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
              {t('reject')}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ApprovalCard;
