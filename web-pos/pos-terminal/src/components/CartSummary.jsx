import { useTranslation } from 'react-i18next';
import { useSelector, useDispatch } from 'react-redux';
import {
  selectCartSubtotal,
  selectCartTax,
  selectCartDiscount,
  selectCartGrandTotal,
  selectCartItems,
  clearCart,
} from '../store/cartSlice';
import { TrashIcon } from '@heroicons/react/24/outline';

/**
 * CartSummary Component
 * Displays cart totals and checkout button
 */
function CartSummary({ onCheckout }) {
  const { t } = useTranslation();
  const dispatch = useDispatch();
  const items = useSelector(selectCartItems);
  const subtotal = useSelector(selectCartSubtotal);
  const tax = useSelector(selectCartTax);
  const discount = useSelector(selectCartDiscount);
  const grandTotal = useSelector(selectCartGrandTotal);

  const itemCount = items.reduce((total, item) => total + item.quantity, 0);

  const handleClearCart = () => {
    if (window.confirm(t('pos.confirmClearCart'))) {
      dispatch(clearCart());
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('id-ID', {
      style: 'currency',
      currency: 'IDR',
      minimumFractionDigits: 0
    }).format(amount);
  };

  return (
    <div className="bg-white rounded-lg shadow-lg border border-gray-200 p-4 space-y-4">
      {/* Item Count */}
      <div className="flex items-center justify-between pb-3 border-b border-gray-200">
        <span className="text-sm font-medium text-gray-700">
          {t('pos.itemsInCart')}
        </span>
        <span className="text-sm font-bold text-gray-900">
          {itemCount} {itemCount === 1 ? t('pos.item') : t('pos.items')}
        </span>
      </div>

      {/* Price Breakdown */}
      <div className="space-y-2">
        <div className="flex justify-between text-sm">
          <span className="text-gray-600">{t('pos.subtotal')}</span>
          <span className="font-medium text-gray-900">{formatCurrency(subtotal)}</span>
        </div>

        <div className="flex justify-between text-sm">
          <span className="text-gray-600">{t('pos.tax')}</span>
          <span className="font-medium text-gray-900">{formatCurrency(tax)}</span>
        </div>

        {discount > 0 && (
          <div className="flex justify-between text-sm">
            <span className="text-green-600">{t('pos.discount')}</span>
            <span className="font-medium text-green-600">-{formatCurrency(discount)}</span>
          </div>
        )}
      </div>

      {/* Grand Total */}
      <div className="flex justify-between items-center pt-3 border-t-2 border-gray-300">
        <span className="text-lg font-bold text-gray-900">{t('pos.total')}</span>
        <span className="text-2xl font-bold text-blue-600">{formatCurrency(grandTotal)}</span>
      </div>

      {/* Action Buttons */}
      <div className="space-y-2">
        <button
          onClick={onCheckout}
          disabled={items.length === 0}
          className={`w-full py-3 px-4 rounded-lg font-bold text-lg transition-colors
                     ${items.length === 0
                       ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                       : 'bg-blue-600 text-white hover:bg-blue-700 active:bg-blue-800'
                     }`}
        >
          {t('pos.checkout')}
        </button>

        <button
          onClick={handleClearCart}
          disabled={items.length === 0}
          className={`w-full py-2 px-4 rounded-lg font-medium text-sm 
                     flex items-center justify-center gap-2 transition-colors
                     ${items.length === 0
                       ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                       : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                     }`}
        >
          <TrashIcon className="h-4 w-4" />
          {t('pos.clearCart')}
        </button>
      </div>
    </div>
  );
}

export default CartSummary;
