import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useDispatch, useSelector } from 'react-redux';
import { applyDiscount, removeDiscount } from '../../store/cartSlice';

/**
 * DiscountManager Component
 * 
 * Enhanced discount management with:
 * - Quick discount buttons (5%, 10%, 20%, custom)
 * - Real-time discount preview
 * - Validation (min/max discount limits)
 * - Manager approval request for large discounts
 * 
 * @param {Object} props
 * @param {number} props.subtotal - Current cart subtotal
 * @param {Function} props.onRequestApproval - Callback when approval needed
 */
export default function DiscountManager({ subtotal, onRequestApproval }) {
  const { t } = useTranslation();
  const dispatch = useDispatch();
  const discount = useSelector(state => state.cart.discount);
  const user = useSelector(state => state.auth.user);
  
  const [discountType, setDiscountType] = useState('percentage'); // 'percentage' or 'amount'
  const [discountValue, setDiscountValue] = useState('');
  const [error, setError] = useState(null);
  
  // Discount limits
  const MAX_PERCENTAGE_WITHOUT_APPROVAL = 20;
  const MAX_AMOUNT_WITHOUT_APPROVAL = subtotal * 0.2; // 20% of subtotal
  const MIN_DISCOUNT = 0;
  const MAX_PERCENTAGE = 100;
  
  // Quick discount presets
  const quickDiscounts = [5, 10, 15, 20];
  
  const calculateDiscountAmount = (value, type) => {
    if (!value || isNaN(value)) return 0;
    
    if (type === 'percentage') {
      return Math.min((subtotal * value) / 100, subtotal);
    }
    return Math.min(value, subtotal);
  };
  
  const calculateDiscountPercentage = (amount) => {
    if (subtotal === 0) return 0;
    return (amount / subtotal) * 100;
  };
  
  const needsApproval = (value, type) => {
    if (type === 'percentage') {
      return value > MAX_PERCENTAGE_WITHOUT_APPROVAL;
    }
    return value > MAX_AMOUNT_WITHOUT_APPROVAL;
  };
  
  const validateDiscount = (value, type) => {
    if (!value || value === '') {
      return null;
    }
    
    const numValue = parseFloat(value);
    
    if (isNaN(numValue)) {
      return t('pos.invalidDiscountValue');
    }
    
    if (numValue < MIN_DISCOUNT) {
      return t('pos.discountTooLow');
    }
    
    if (type === 'percentage') {
      if (numValue > MAX_PERCENTAGE) {
        return t('pos.discountTooHigh', { max: MAX_PERCENTAGE });
      }
    } else {
      if (numValue > subtotal) {
        return t('pos.discountExceedsSubtotal');
      }
    }
    
    return null;
  };
  
  const handleQuickDiscount = (percentage) => {
    setDiscountType('percentage');
    setDiscountValue(percentage.toString());
    setError(null);
    
    const amount = calculateDiscountAmount(percentage, 'percentage');
    
    if (needsApproval(percentage, 'percentage')) {
      if (onRequestApproval) {
        onRequestApproval({
          type: 'DISCOUNT',
          discountType: 'percentage',
          discountValue: percentage,
          discountAmount: amount,
          reason: `${percentage}% discount requested`,
          cashierId: user?.id,
          cashierName: user?.name
        });
      }
    } else {
      dispatch(applyDiscount({
        type: 'percentage',
        value: percentage,
        amount: amount
      }));
    }
  };
  
  const handleApplyDiscount = () => {
    const validationError = validateDiscount(discountValue, discountType);
    if (validationError) {
      setError(validationError);
      return;
    }
    
    const numValue = parseFloat(discountValue);
    const amount = calculateDiscountAmount(numValue, discountType);
    
    if (needsApproval(numValue, discountType)) {
      if (onRequestApproval) {
        onRequestApproval({
          type: 'DISCOUNT',
          discountType,
          discountValue: numValue,
          discountAmount: amount,
          reason: `${discountType === 'percentage' ? numValue + '%' : 'Rp ' + numValue.toLocaleString()} discount requested`,
          cashierId: user?.id,
          cashierName: user?.name
        });
      }
    } else {
      dispatch(applyDiscount({
        type: discountType,
        value: numValue,
        amount: amount
      }));
    }
    
    setError(null);
  };
  
  const handleRemoveDiscount = () => {
    dispatch(removeDiscount());
    setDiscountValue('');
    setError(null);
  };
  
  const previewAmount = discountValue ? calculateDiscountAmount(parseFloat(discountValue), discountType) : 0;
  const previewPercentage = previewAmount > 0 ? calculateDiscountPercentage(previewAmount) : 0;
  const finalTotal = subtotal - (discount?.amount || 0);
  
  return (
    <div className="bg-white rounded-lg shadow-sm p-4 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold text-gray-900">
          {t('pos.discount')}
        </h3>
        {discount && (
          <button
            onClick={handleRemoveDiscount}
            className="text-sm text-red-600 hover:text-red-700"
          >
            {t('common.remove')}
          </button>
        )}
      </div>
      
      {/* Current Discount Display */}
      {discount && (
        <div className="bg-green-50 border border-green-200 rounded-md p-3">
          <div className="flex justify-between items-center">
            <span className="text-sm font-medium text-green-800">
              {discount.type === 'percentage' ? (
                `${discount.value}% ${t('pos.discount')}`
              ) : (
                `${t('pos.discount')} ${formatCurrency(discount.value)}`
              )}
            </span>
            <span className="text-sm font-semibold text-green-900">
              - {formatCurrency(discount.amount)}
            </span>
          </div>
        </div>
      )}
      
      {/* Quick Discount Buttons */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          {t('pos.quickDiscount')}
        </label>
        <div className="grid grid-cols-4 gap-2">
          {quickDiscounts.map((percentage) => (
            <button
              key={percentage}
              onClick={() => handleQuickDiscount(percentage)}
              className="px-3 py-2 bg-blue-50 text-blue-700 rounded-md hover:bg-blue-100 transition-colors text-sm font-medium"
            >
              {percentage}%
            </button>
          ))}
        </div>
      </div>
      
      {/* Custom Discount Input */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          {t('pos.customDiscount')}
        </label>
        
        {/* Discount Type Toggle */}
        <div className="flex gap-2 mb-2">
          <button
            onClick={() => {
              setDiscountType('percentage');
              setDiscountValue('');
              setError(null);
            }}
            className={`flex-1 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
              discountType === 'percentage'
                ? 'bg-primary text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            {t('pos.percentage')} (%)
          </button>
          <button
            onClick={() => {
              setDiscountType('amount');
              setDiscountValue('');
              setError(null);
            }}
            className={`flex-1 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
              discountType === 'amount'
                ? 'bg-primary text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            {t('pos.amount')} (Rp)
          </button>
        </div>
        
        {/* Discount Value Input */}
        <div className="flex gap-2">
          <div className="flex-1 relative">
            {discountType === 'amount' && (
              <span className="absolute left-3 top-2 text-gray-500">
                Rp
              </span>
            )}
            <input
              type="number"
              value={discountValue}
              onChange={(e) => {
                setDiscountValue(e.target.value);
                setError(null);
              }}
              onKeyPress={(e) => {
                if (e.key === 'Enter') {
                  handleApplyDiscount();
                }
              }}
              placeholder={discountType === 'percentage' ? '0' : '0'}
              className={`w-full ${
                discountType === 'amount' ? 'pl-10' : 'pl-3'
              } pr-3 py-2 border ${
                error ? 'border-red-300' : 'border-gray-300'
              } rounded-md focus:outline-none focus:ring-2 focus:ring-primary`}
              min="0"
              max={discountType === 'percentage' ? MAX_PERCENTAGE : subtotal}
              step={discountType === 'percentage' ? '1' : '1000'}
            />
            {discountType === 'percentage' && discountValue && (
              <span className="absolute right-3 top-2 text-gray-500">
                %
              </span>
            )}
          </div>
          <button
            onClick={handleApplyDiscount}
            disabled={!discountValue || !!error}
            className="px-4 py-2 bg-primary text-white rounded-md hover:bg-blue-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {t('common.apply')}
          </button>
        </div>
        
        {/* Error Message */}
        {error && (
          <p className="mt-1 text-sm text-red-600">{error}</p>
        )}
        
        {/* Approval Warning */}
        {discountValue && needsApproval(parseFloat(discountValue), discountType) && !error && (
          <div className="mt-2 p-2 bg-yellow-50 border border-yellow-200 rounded-md">
            <p className="text-xs text-yellow-800">
              ⚠️ {t('pos.requiresManagerApproval')}
            </p>
          </div>
        )}
      </div>
      
      {/* Discount Preview */}
      {discountValue && !error && !discount && (
        <div className="bg-blue-50 border border-blue-200 rounded-md p-3 space-y-1">
          <div className="flex justify-between text-sm">
            <span className="text-blue-700">{t('pos.subtotal')}:</span>
            <span className="text-blue-900 font-medium">{formatCurrency(subtotal)}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-blue-700">
              {t('pos.discount')} ({previewPercentage.toFixed(1)}%):
            </span>
            <span className="text-blue-900 font-medium">- {formatCurrency(previewAmount)}</span>
          </div>
          <div className="flex justify-between text-sm font-semibold pt-2 border-t border-blue-300">
            <span className="text-blue-800">{t('pos.total')}:</span>
            <span className="text-blue-900">{formatCurrency(subtotal - previewAmount)}</span>
          </div>
        </div>
      )}
      
      {/* Applied Discount Summary */}
      {discount && (
        <div className="bg-gray-50 rounded-md p-3 space-y-1">
          <div className="flex justify-between text-sm">
            <span className="text-gray-700">{t('pos.subtotal')}:</span>
            <span className="text-gray-900 font-medium">{formatCurrency(subtotal)}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-gray-700">
              {t('pos.discount')} ({calculateDiscountPercentage(discount.amount).toFixed(1)}%):
            </span>
            <span className="text-green-600 font-medium">- {formatCurrency(discount.amount)}</span>
          </div>
          <div className="flex justify-between text-base font-bold pt-2 border-t border-gray-300">
            <span className="text-gray-900">{t('pos.total')}:</span>
            <span className="text-gray-900">{formatCurrency(finalTotal)}</span>
          </div>
        </div>
      )}
    </div>
  );
}

function formatCurrency(amount) {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount);
}
