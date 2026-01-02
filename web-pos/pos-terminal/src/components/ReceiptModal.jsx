import { useTranslation } from 'react-i18next';
import { XMarkIcon, PrinterIcon } from '@heroicons/react/24/outline';

/**
 * ReceiptModal Component
 * Displays receipt after successful transaction
 */
function ReceiptModal({ isOpen, onClose, transaction, onNewOrder }) {
  const { t } = useTranslation();

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('id-ID', {
      style: 'currency',
      currency: 'IDR',
      minimumFractionDigits: 0
    }).format(amount);
  };

  const formatDateTime = (dateString) => {
    return new Date(dateString).toLocaleString('id-ID', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const handlePrint = () => {
    window.print();
  };

  const handleNewOrder = () => {
    onNewOrder();
    onClose();
  };

  if (!isOpen || !transaction) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-2xl font-bold text-green-600">{t('pos.paymentSuccess')}</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <XMarkIcon className="h-6 w-6" />
          </button>
        </div>

        {/* Receipt Content */}
        <div className="p-6 space-y-4" id="receipt-content">
          {/* Store Header */}
          <div className="text-center border-b border-gray-300 pb-4">
            <h3 className="font-bold text-xl text-gray-900">CursorPOS</h3>
            <p className="text-sm text-gray-600 mt-1">{t('pos.receipt')}</p>
          </div>

          {/* Transaction Info */}
          <div className="space-y-1 text-sm border-b border-gray-300 pb-4">
            <div className="flex justify-between">
              <span className="text-gray-600">{t('pos.transactionNumber')}</span>
              <span className="font-mono font-semibold">{transaction.transactionNumber}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">{t('pos.date')}</span>
              <span>{formatDateTime(transaction.transactionDate || new Date())}</span>
            </div>
            {transaction.cashierName && (
              <div className="flex justify-between">
                <span className="text-gray-600">{t('pos.cashier')}</span>
                <span>{transaction.cashierName}</span>
              </div>
            )}
          </div>

          {/* Items */}
          <div className="space-y-2 border-b border-gray-300 pb-4">
            <h4 className="font-semibold text-gray-900">{t('pos.items')}</h4>
            {transaction.items && transaction.items.map((item, index) => (
              <div key={index} className="text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-900">{item.productName}</span>
                  <span className="font-medium">{formatCurrency(item.totalAmount)}</span>
                </div>
                <div className="text-gray-500 text-xs">
                  {formatCurrency(item.unitPrice)} x {item.quantity}
                  {item.discountAmount > 0 && (
                    <span className="text-green-600 ml-2">
                      (-{formatCurrency(item.discountAmount)})
                    </span>
                  )}
                </div>
              </div>
            ))}
          </div>

          {/* Totals */}
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">{t('pos.subtotal')}</span>
              <span className="font-medium">{formatCurrency(transaction.subtotal)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">{t('pos.tax')}</span>
              <span className="font-medium">{formatCurrency(transaction.taxAmount)}</span>
            </div>
            {transaction.discountAmount > 0 && (
              <div className="flex justify-between">
                <span className="text-green-600">{t('pos.discount')}</span>
                <span className="font-medium text-green-600">-{formatCurrency(transaction.discountAmount)}</span>
              </div>
            )}
            <div className="flex justify-between items-center pt-2 border-t-2 border-gray-300">
              <span className="font-bold text-lg text-gray-900">{t('pos.total')}</span>
              <span className="font-bold text-xl text-gray-900">{formatCurrency(transaction.totalAmount)}</span>
            </div>
          </div>

          {/* Payment Info */}
          {transaction.payments && transaction.payments.length > 0 && (
            <div className="space-y-2 text-sm border-t border-gray-300 pt-4">
              {transaction.payments.map((payment, index) => (
                <div key={index}>
                  <div className="flex justify-between">
                    <span className="text-gray-600">
                      {t(`pos.payment.${payment.paymentMethod.toLowerCase()}`)}
                    </span>
                    <span className="font-medium">{formatCurrency(payment.amount)}</span>
                  </div>
                </div>
              ))}
              {transaction.changeAmount > 0 && (
                <div className="flex justify-between font-semibold text-green-600">
                  <span>{t('pos.change')}</span>
                  <span>{formatCurrency(transaction.changeAmount)}</span>
                </div>
              )}
            </div>
          )}

          {/* Footer */}
          <div className="text-center text-xs text-gray-500 border-t border-gray-300 pt-4">
            <p>{t('pos.thankYou')}</p>
            <p className="mt-1">{t('pos.poweredBy')} CursorPOS</p>
          </div>
        </div>

        {/* Actions */}
        <div className="flex gap-3 p-6 border-t border-gray-200 bg-gray-50">
          <button
            onClick={handlePrint}
            className="flex-1 py-3 px-4 bg-white border-2 border-gray-300 rounded-lg 
                     font-semibold text-gray-700 hover:bg-gray-50 transition-colors
                     flex items-center justify-center gap-2"
          >
            <PrinterIcon className="h-5 w-5" />
            {t('pos.print')}
          </button>
          <button
            onClick={handleNewOrder}
            className="flex-1 py-3 px-4 bg-blue-600 rounded-lg font-semibold text-white 
                     hover:bg-blue-700 transition-colors"
          >
            {t('pos.newOrder')}
          </button>
        </div>
      </div>
    </div>
  );
}

export default ReceiptModal;
