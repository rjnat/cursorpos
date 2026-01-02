import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import toast from 'react-hot-toast';
import ApprovalCard from './ApprovalCard';
import { approvalService } from '../../services/approvalService';

const ApprovalDashboard = () => {
  const { t } = useTranslation();
  const user = useSelector((state) => state.auth.user);
  
  const [approvals, setApprovals] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Filters
  const [statusFilter, setStatusFilter] = useState('PENDING');
  const [dateRangeFilter, setDateRangeFilter] = useState('TODAY');
  const [searchQuery, setSearchQuery] = useState('');
  
  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const itemsPerPage = 20;
  
  // View toggle
  const [view, setView] = useState('pending'); // 'pending' or 'history'

  useEffect(() => {
    fetchApprovals();
  }, [statusFilter, dateRangeFilter, currentPage, view]);

  const fetchApprovals = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const filters = {
        status: view === 'pending' ? 'PENDING' : statusFilter,
        dateRange: dateRangeFilter,
        search: searchQuery,
        page: currentPage,
        limit: itemsPerPage
      };
      
      const response = await approvalService.getApprovals(filters);
      setApprovals(response.data);
      setTotalPages(Math.ceil(response.total / itemsPerPage));
    } catch (err) {
      setError(err.message || t('failedToLoadApprovals'));
      console.error('Failed to fetch approvals:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (approvalId) => {
    if (!window.confirm(t('confirmApproval'))) {
      return;
    }

    try {
      await approvalService.approveRequest(approvalId, user.id);
      toast.success(t('approvalGrantedSuccess'));
      fetchApprovals(); // Refresh list
    } catch (err) {
      console.error('Failed to approve request:', err);
      toast.error(t('approvalFailed'));
    }
  };

  const handleReject = async (approvalId) => {
    if (!window.confirm(t('confirmRejection'))) {
      return;
    }

    try {
      await approvalService.rejectRequest(approvalId, user.id);
      toast.success(t('approvalRejectedSuccess'));
      fetchApprovals(); // Refresh list
    } catch (err) {
      console.error('Failed to reject request:', err);
      toast.error(t('rejectionFailed'));
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(1);
    fetchApprovals();
  };

  const handleExport = async () => {
    try {
      const blob = await approvalService.exportApprovals({
        status: statusFilter,
        dateRange: dateRangeFilter
      });
      
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `approvals_${new Date().toISOString().split('T')[0]}.csv`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      toast.success(t('exportSuccess'));
    } catch (err) {
      console.error('Failed to export approvals:', err);
      toast.error(t('exportFailed'));
    }
  };

  const getDateRangeLabel = (range) => {
    const labels = {
      TODAY: t('today'),
      THIS_WEEK: t('thisWeek'),
      THIS_MONTH: t('thisMonth'),
      ALL: t('allTime')
    };
    return labels[range] || range;
  };

  const getStatusLabel = (status) => {
    const labels = {
      PENDING: t('pending'),
      APPROVED: t('approved'),
      REJECTED: t('rejected'),
      ALL: t('all')
    };
    return labels[status] || status;
  };

  return (
    <div className="container mx-auto px-4 py-6 max-w-7xl">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-800 mb-2">
          {t('managerApprovalDashboard')}
        </h1>
        <p className="text-gray-600">
          {t('managerApprovalDashboardDescription')}
        </p>
      </div>

      {/* View Toggle */}
      <div className="mb-6 flex gap-2">
        <button
          onClick={() => {
            setView('pending');
            setCurrentPage(1);
          }}
          className={`px-6 py-2 rounded-lg font-semibold transition-colors ${
            view === 'pending'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          }`}
        >
          {t('pendingRequests')}
        </button>
        <button
          onClick={() => {
            setView('history');
            setCurrentPage(1);
          }}
          className={`px-6 py-2 rounded-lg font-semibold transition-colors ${
            view === 'history'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          }`}
        >
          {t('approvalHistory')}
        </button>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* Search */}
          <form onSubmit={handleSearch} className="col-span-1 md:col-span-2">
            <div className="flex gap-2">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder={t('searchByCashierOrId')}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              <button
                type="submit"
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
              >
                {t('search')}
              </button>
            </div>
          </form>

          {/* Status Filter (only for history view) */}
          {view === 'history' && (
            <div>
              <select
                value={statusFilter}
                onChange={(e) => {
                  setStatusFilter(e.target.value);
                  setCurrentPage(1);
                }}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="ALL">{getStatusLabel('ALL')}</option>
                <option value="APPROVED">{getStatusLabel('APPROVED')}</option>
                <option value="REJECTED">{getStatusLabel('REJECTED')}</option>
              </select>
            </div>
          )}

          {/* Date Range Filter */}
          <div className={view === 'pending' ? 'col-span-1 md:col-span-2' : ''}>
            <select
              value={dateRangeFilter}
              onChange={(e) => {
                setDateRangeFilter(e.target.value);
                setCurrentPage(1);
              }}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="TODAY">{getDateRangeLabel('TODAY')}</option>
              <option value="THIS_WEEK">{getDateRangeLabel('THIS_WEEK')}</option>
              <option value="THIS_MONTH">{getDateRangeLabel('THIS_MONTH')}</option>
              <option value="ALL">{getDateRangeLabel('ALL')}</option>
            </select>
          </div>
        </div>

        {/* Export Button (history view only) */}
        {view === 'history' && (
          <div className="mt-4 flex justify-end">
            <button
              onClick={handleExport}
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              {t('exportCSV')}
            </button>
          </div>
        )}
      </div>

      {/* Loading State */}
      {loading && (
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
      )}

      {/* Error State */}
      {error && !loading && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
          <p className="text-red-800">{error}</p>
          <button
            onClick={fetchApprovals}
            className="mt-2 text-red-600 hover:text-red-800 font-semibold"
          >
            {t('retry')}
          </button>
        </div>
      )}

      {/* Empty State */}
      {!loading && !error && approvals.length === 0 && (
        <div className="bg-gray-50 rounded-lg p-12 text-center">
          <svg className="w-16 h-16 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          <h3 className="text-xl font-semibold text-gray-700 mb-2">
            {view === 'pending' ? t('noPendingRequests') : t('noApprovalHistory')}
          </h3>
          <p className="text-gray-500">
            {view === 'pending' 
              ? t('noPendingRequestsDescription')
              : t('noApprovalHistoryDescription')
            }
          </p>
        </div>
      )}

      {/* Approval Cards */}
      {!loading && !error && approvals.length > 0 && (
        <div className="space-y-4 mb-6">
          {approvals.map((approval) => (
            <ApprovalCard
              key={approval.id}
              approval={approval}
              onApprove={handleApprove}
              onReject={handleReject}
              view={view}
            />
          ))}
        </div>
      )}

      {/* Pagination */}
      {!loading && !error && totalPages > 1 && (
        <div className="flex justify-center items-center gap-2">
          <button
            onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))}
            disabled={currentPage === 1}
            className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {t('previous')}
          </button>
          <span className="text-gray-600">
            {t('pageXOfY', { current: currentPage, total: totalPages })}
          </span>
          <button
            onClick={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))}
            disabled={currentPage === totalPages}
            className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {t('next')}
          </button>
        </div>
      )}
    </div>
  );
};

export default ApprovalDashboard;
