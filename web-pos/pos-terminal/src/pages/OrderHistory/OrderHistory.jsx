import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import * as transactionService from '../../services/transactionService';
import ReceiptModal from '../../components/ReceiptModal';

export default function OrderHistory() {
  const { t } = useTranslation();
  const { user } = useSelector((state) => state.auth);
  
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showReceipt, setShowReceipt] = useState(false);
  
  // Filters
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [dateFilter, setDateFilter] = useState('TODAY');
  
  // Pagination
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 20;

  useEffect(() => {
    loadOrders();
  }, [page, statusFilter, dateFilter]);

  const loadOrders = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const { startDate, endDate } = getDateRange(dateFilter);
      let response;
      
      if (statusFilter !== 'ALL') {
        response = await transactionService.getTransactionsByStatus(statusFilter, {
          page,
          size: pageSize,
          startDate,
          endDate
        });
      } else if (startDate && endDate) {
        response = await transactionService.getTransactionsByDateRange(
          startDate,
          endDate,
          { page, size: pageSize }
        );
      } else {
        response = await transactionService.getAllTransactions({
          page,
          size: pageSize
        });
      }
      
      setOrders(response.content || []);
      setTotalPages(response.totalPages || 0);
      setTotalElements(response.totalElements || 0);
    } catch (err) {
      console.error('Error loading orders:', err);
      setError(t('pos.errorLoadingOrders'));
    } finally {
      setLoading(false);
    }
  };

  const getDateRange = (filter) => {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    
    switch (filter) {
      case 'TODAY':
        return {
          startDate: today.toISOString(),
          endDate: new Date(today.getTime() + 86400000).toISOString()
        };
      case 'YESTERDAY':
        const yesterday = new Date(today.getTime() - 86400000);
        return {
          startDate: yesterday.toISOString(),
          endDate: today.toISOString()
        };
      case 'THIS_WEEK':
        const weekStart = new Date(today);
        weekStart.setDate(today.getDate() - today.getDay());
        return {
          startDate: weekStart.toISOString(),
          endDate: new Date().toISOString()
        };
      case 'THIS_MONTH':
        const monthStart = new Date(today.getFullYear(), today.getMonth(), 1);
        return {
          startDate: monthStart.toISOString(),
          endDate: new Date().toISOString()
        };
      default:
        return { startDate: null, endDate: null };
    }
  };

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      loadOrders();
      return;
    }
    
    setLoading(true);
    setError(null);
    
    try {
      const response = await transactionService.getTransactionByNumber(searchQuery.trim());
      setOrders([response]);
      setTotalPages(1);
      setTotalElements(1);
    } catch (err) {
      console.error('Error searching order:', err);
      setError(t('pos.noOrdersFound'));
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const handleViewDetails = (order) => {
    setSelectedOrder(order);
    setShowReceipt(true);
  };

  const handleReprintReceipt = (order) => {
    setSelectedOrder(order);
    setShowReceipt(true);
    setTimeout(() => {
      window.print();
    }, 100);
  };

  const filteredOrders = orders.filter((order) => {
    if (!searchQuery.trim()) return true;
    
    const query = searchQuery.toLowerCase();
    return (
      order.transactionNumber?.toLowerCase().includes(query) ||
      order.id?.toLowerCase().includes(query) ||
      order.cashierName?.toLowerCase().includes(query)
    );
  });

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('id-ID', {
      style: 'currency',
      currency: 'IDR',
      minimumFractionDigits: 0
    }).format(amount);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('id-ID', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusBadge = (status) => {
    const statusColors = {
      COMPLETED: 'bg-green-100 text-green-800',
      PENDING: 'bg-yellow-100 text-yellow-800',
      CANCELLED: 'bg-red-100 text-red-800',
      REFUNDED: 'bg-gray-100 text-gray-800'
    };
    
    return statusColors[status] || 'bg-gray-100 text-gray-800';
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-6">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900">{t('pos.history')}</h1>
        <p className="text-gray-600 mt-1">
          {totalElements} {t('pos.orders')} {t('common.found')}
        </p>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* Search */}
          <div className="md:col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              {t('pos.searchOrderNumber')}
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                placeholder={t('pos.orderNumberPlaceholder')}
                className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
              />
              <button
                onClick={handleSearch}
                className="px-4 py-2 bg-primary text-white rounded-md hover:bg-blue-600 transition-colors"
              >
                {t('common.search')}
              </button>
              {searchQuery && (
                <button
                  onClick={() => {
                    setSearchQuery('');
                    loadOrders();
                  }}
                  className="px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 transition-colors"
                >
                  {t('common.clear')}
                </button>
              )}
            </div>
          </div>

          {/* Status Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              {t('pos.orderStatus')}
            </label>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
            >
              <option value="ALL">{t('common.all')}</option>
              <option value="COMPLETED">{t('status.completed')}</option>
              <option value="PENDING">{t('status.pending')}</option>
              <option value="CANCELLED">{t('status.cancelled')}</option>
              <option value="REFUNDED">{t('status.refunded')}</option>
            </select>
          </div>

          {/* Date Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              {t('pos.dateRange')}
            </label>
            <select
              value={dateFilter}
              onChange={(e) => setDateFilter(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
            >
              <option value="ALL">{t('common.all')}</option>
              <option value="TODAY">{t('date.today')}</option>
              <option value="YESTERDAY">{t('date.yesterday')}</option>
              <option value="THIS_WEEK">{t('date.thisWeek')}</option>
              <option value="THIS_MONTH">{t('date.thisMonth')}</option>
            </select>
          </div>
        </div>
      </div>

      {/* Orders List */}
      <div className="bg-white rounded-lg shadow-sm">
        {loading ? (
          <div className="p-8 text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
            <p className="mt-2 text-gray-600">{t('common.loading')}</p>
          </div>
        ) : error ? (
          <div className="p-8 text-center">
            <p className="text-red-600">{error}</p>
            <button
              onClick={loadOrders}
              className="mt-4 px-4 py-2 bg-primary text-white rounded-md hover:bg-blue-600"
            >
              {t('common.retry')}
            </button>
          </div>
        ) : filteredOrders.length === 0 ? (
          <div className="p-8 text-center">
            <p className="text-gray-500">{t('pos.noOrdersFound')}</p>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                      {t('pos.orderNumber')}
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                      {t('pos.date')}
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                      {t('pos.cashier')}
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                      {t('pos.items')}
                    </th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                      {t('pos.total')}
                    </th>
                    <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">
                      {t('pos.orderStatus')}
                    </th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                      {t('common.actions')}
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {filteredOrders.map((order) => (
                    <tr key={order.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900">
                          {order.transactionNumber}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-500">
                          {formatDate(order.transactionDate)}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">
                          {order.cashierName}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-500">
                          {order.items?.length || 0} {t('pos.items')}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right">
                        <div className="text-sm font-medium text-gray-900">
                          {formatCurrency(order.totalAmount)}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-center">
                        <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusBadge(order.status)}`}>
                          {t(`status.${order.status?.toLowerCase() || 'pending'}`)}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <button
                          onClick={() => handleViewDetails(order)}
                          className="text-primary hover:text-blue-700 mr-3"
                        >
                          {t('pos.viewDetails')}
                        </button>
                        <button
                          onClick={() => handleReprintReceipt(order)}
                          className="text-green-600 hover:text-green-700"
                        >
                          {t('pos.reprintReceipt')}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
                <div className="text-sm text-gray-700">
                  {t('common.page')} {page + 1} {t('common.of')} {totalPages}
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => setPage(Math.max(0, page - 1))}
                    disabled={page === 0}
                    className="px-3 py-1 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {t('common.previous')}
                  </button>
                  <button
                    onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                    disabled={page >= totalPages - 1}
                    className="px-3 py-1 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {t('common.next')}
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* Receipt Modal */}
      {showReceipt && selectedOrder && (
        <ReceiptModal
          transaction={selectedOrder}
          onClose={() => {
            setShowReceipt(false);
            setSelectedOrder(null);
          }}
        />
      )}
    </div>
  );
}
