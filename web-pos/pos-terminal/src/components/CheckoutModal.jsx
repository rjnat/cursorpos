import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import toast from 'react-hot-toast';
import {
  selectCartSubtotal,
  selectCartTax,
  selectCartDiscount,
  selectCartGrandTotal,
} from '../store/cartSlice';
import { XMarkIcon } from '@heroicons/react/24/outline';

/**
 * CheckoutModal Component
 * Modal for payment processing with different payment methods
 */
function CheckoutModal({ isOpen, onClose, onComplete }) {
  const { t } = useTranslation();
  const [paymentMethod, setPaymentMethod] = useState('CASH');
  const [cashAmount, setCashAmount] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);

  const subtotal = useSelector(selectCartSubtotal);
  const tax = useSelector(selectCartTax);
  const discount = useSelector(selectCartDiscount);
  const grandTotal = useSelector(selectCartGrandTotal);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('id-ID', {
      style: 'currency',
      currency: 'IDR',
      minimumFractionDigits: 0
    }).format(amount);
  };

  const calculateChange = () => {
    const cash = parseFloat(cashAmount) || 0;
    return cash - grandTotal;
  };

  const change = calculateChange();
  const isValidPayment = paymentMethod === 'CASH' 
    ? parseFloat(cashAmount) >= grandTotal 
    : true;

  const handleQuickCash = (amount) => {
    setCashAmount(amount.toString());
  };

  const handleComplete = async () => {
    if (!isValidPayment) return;

    setIsProcessing(true);
    try {
      await onComplete({
        paymentMethod,
        paidAmount: paymentMethod === 'CASH' ? parseFloat(cashAmount) : grandTotal,
        changeAmount: paymentMethod === 'CASH' ? change : 0,
      });
      // Reset form
      setPaymentMethod('CASH');
      setCashAmount('');
    } catch (error) {
      console.error('Payment error:', error);
      toast.error(t('pos.paymentError'));
    } finally {
      setIsProcessing(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-2xl font-bold text-gray-900">{t('pos.checkout')}</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <XMarkIcon className="h-6 w-6" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Order Summary */}
          <div className="bg-gray-50 rounded-lg p-4 space-y-2">
            <h3 className="font-semibold text-gray-900 mb-3">{t('pos.orderSummary')}</h3>
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">{t('pos.subtotal')}</span>
              <span className="font-medium">{formatCurrency(subtotal)}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">{t('pos.tax')}</span>
              <span className="font-medium">{formatCurrency(tax)}</span>
            </div>
            {discount > 0 && (
              <div className="flex justify-between text-sm">
                <span className="text-green-600">{t('pos.discount')}</span>
                <span className="font-medium text-green-600">-{formatCurrency(discount)}</span>
              </div>
            )}
            <div className="flex justify-between items-center pt-2 border-t border-gray-300">
              <span className="font-bold text-lg text-gray-900">{t('pos.total')}</span>
              <span className="font-bold text-2xl text-blue-600">{formatCurrency(grandTotal)}</span>
            </div>
          </div>

          {/* Payment Method */}
          <div>
            <h3 className="font-semibold text-gray-900 mb-3">{t('pos.paymentMethod')}</h3>
            <div className="grid grid-cols-2 gap-3">
              {['CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'E_WALLET'].map((method) => (
                <button
                  key={method}
                  onClick={() => setPaymentMethod(method)}
                  className={`py-3 px-4 rounded-lg font-medium border-2 transition-all
                             ${paymentMethod === method
                               ? 'border-blue-600 bg-blue-50 text-blue-700'
                               : 'border-gray-300 bg-white text-gray-700 hover:border-gray-400'
                             }`}
                >
                  {t(`pos.payment.${method.toLowerCase()}`)}
                </button>
              ))}
            </div>
          </div>

          {/* Cash Payment Details */}
          {paymentMethod === 'CASH' && (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  {t('pos.cashReceived')}
                </label>
                <input
                  type="number"
                  value={cashAmount}
                  onChange={(e) => setCashAmount(e.target.value)}
                  placeholder="0"
                  className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg text-lg font-semibold
                           focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  autoFocus
                />
              </div>

              {/* Quick Cash Buttons */}
              <div className="grid grid-cols-4 gap-2">
                {[50000, 100000, 200000, 500000].map((amount) => (
                  <button
                    key={amount}
                    onClick={() => handleQuickCash(amount)}
                    className="py-2 px-3 bg-gray-100 hover:bg-gray-200 rounded-lg 
                             font-medium text-sm transition-colors"
                  >
                    {formatCurrency(amount)}
                  </button>
                ))}
              </div>

              {/* Change Display */}
              {cashAmount && (
                <div className={`p-4 rounded-lg ${change >= 0 ? 'bg-green-50' : 'bg-red-50'}`}>
                  <div className="flex justify-between items-center">
                    <span className={`font-semibold ${change >= 0 ? 'text-green-900' : 'text-red-900'}`}>
                      {change >= 0 ? t('pos.change') : t('pos.insufficient')}
                    </span>
                    <span className={`font-bold text-xl ${change >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                      {formatCurrency(Math.abs(change))}
                    </span>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="flex gap-3 p-6 border-t border-gray-200 bg-gray-50">
          <button
            onClick={onClose}
            disabled={isProcessing}
            className="flex-1 py-3 px-4 bg-white border-2 border-gray-300 rounded-lg 
                     font-semibold text-gray-700 hover:bg-gray-50 transition-colors
                     disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {t('pos.cancel')}
          </button>
          <button
            onClick={handleComplete}
            disabled={!isValidPayment || isProcessing}
            className="flex-1 py-3 px-4 bg-blue-600 rounded-lg font-semibold text-white 
                     hover:bg-blue-700 transition-colors disabled:opacity-50 
                     disabled:cursor-not-allowed"
          >
            {isProcessing ? t('pos.processing') : t('pos.completePayment')}
          </button>
        </div>
      </div>
    </div>
  );
}

export default CheckoutModal;
