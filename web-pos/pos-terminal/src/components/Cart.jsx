import { useTranslation } from 'react-i18next';
import { useDispatch } from 'react-redux';
import toast from 'react-hot-toast';
import { removeItem, updateQuantity } from '../store/cartSlice';
import { TrashIcon, MinusIcon, PlusIcon } from '@heroicons/react/24/outline';

/**
 * Cart Component
 * Displays cart items with quantity controls
 */
function Cart({ items }) {
  const { t } = useTranslation();
  const dispatch = useDispatch();

  const handleRemoveItem = (itemId) => {
    dispatch(removeItem(itemId));
  };

  const handleDecreaseQuantity = (item) => {
    if (item.quantity > 1) {
      dispatch(updateQuantity({ id: item.id, quantity: item.quantity - 1 }));
    } else {
      dispatch(removeItem(item.id));
    }
  };

  const handleIncreaseQuantity = (item) => {
    // Check stock availability if available
    if (item.availableStock !== undefined && item.quantity >= item.availableStock) {
      toast.error(t('pos.maxStockReached'));
      return;
    }
    dispatch(updateQuantity({ id: item.id, quantity: item.quantity + 1 }));
  };

  const handleQuantityChange = (item, newQuantity) => {
    const quantity = parseInt(newQuantity);
    if (isNaN(quantity) || quantity < 1) {
      return;
    }
    if (item.availableStock !== undefined && quantity > item.availableStock) {
      toast.error(t('pos.maxStockReached'));
      return;
    }
    dispatch(updateQuantity({ id: item.id, quantity }));
  };

  if (!items || items.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-full text-gray-400 py-12">
        <svg className="h-16 w-16 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>
        <p className="text-lg font-medium">{t('pos.emptyCart')}</p>
        <p className="text-sm mt-1">{t('pos.addProductsToStart')}</p>
      </div>
    );
  }

  return (
    <div className="space-y-2 overflow-y-auto">
      {items.map((item) => (
        <div
          key={item.id}
          className="bg-white rounded-lg shadow-sm border border-gray-200 p-3 
                     hover:shadow-md transition-shadow"
        >
          <div className="flex items-start justify-between mb-2">
            <div className="flex-1 min-w-0 pr-2">
              <h4 className="font-medium text-gray-900 text-sm truncate">
                {item.name}
              </h4>
              {item.sku && (
                <p className="text-xs text-gray-500">SKU: {item.sku}</p>
              )}
            </div>
            <button
              onClick={() => handleRemoveItem(item.id)}
              className="text-red-500 hover:text-red-700 p-1"
              title={t('pos.removeItem')}
            >
              <TrashIcon className="h-5 w-5" />
            </button>
          </div>

          <div className="flex items-center justify-between">
            {/* Quantity Controls */}
            <div className="flex items-center space-x-2">
              <button
                onClick={() => handleDecreaseQuantity(item)}
                className="w-8 h-8 flex items-center justify-center rounded-md 
                         bg-gray-100 hover:bg-gray-200 text-gray-700"
              >
                <MinusIcon className="h-4 w-4" />
              </button>
              
              <input
                type="number"
                value={item.quantity}
                onChange={(e) => handleQuantityChange(item, e.target.value)}
                className="w-16 text-center border border-gray-300 rounded-md py-1 
                         focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                min="1"
                max={item.availableStock || 999}
              />
              
              <button
                onClick={() => handleIncreaseQuantity(item)}
                className="w-8 h-8 flex items-center justify-center rounded-md 
                         bg-gray-100 hover:bg-gray-200 text-gray-700"
              >
                <PlusIcon className="h-4 w-4" />
              </button>
            </div>

            {/* Item Total */}
            <div className="text-right">
              <p className="text-xs text-gray-500">
                {new Intl.NumberFormat('id-ID', {
                  style: 'currency',
                  currency: 'IDR',
                  minimumFractionDigits: 0
                }).format(item.basePrice)} x {item.quantity}
              </p>
              <p className="font-bold text-gray-900">
                {new Intl.NumberFormat('id-ID', {
                  style: 'currency',
                  currency: 'IDR',
                  minimumFractionDigits: 0
                }).format(item.basePrice * item.quantity)}
              </p>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

export default Cart;
