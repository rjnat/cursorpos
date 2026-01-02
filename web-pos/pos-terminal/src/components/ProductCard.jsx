import { useTranslation } from 'react-i18next';
import { PlusIcon } from '@heroicons/react/24/outline';

/**
 * ProductCard Component
 * Displays a single product with image, name, price, and stock info
 */
function ProductCard({ product, onAddToCart }) {
  const { t } = useTranslation();

  const handleAddToCart = () => {
    onAddToCart(product);
  };

  const isLowStock = product.availableStock !== undefined && product.availableStock < 10;
  const isOutOfStock = product.availableStock !== undefined && product.availableStock === 0;

  return (
    <div 
      className={`bg-white rounded-lg shadow-sm border-2 overflow-hidden
                  transition-all duration-200 hover:shadow-md
                  ${isOutOfStock ? 'opacity-60 border-gray-300' : 'border-transparent hover:border-blue-500'}`}
    >
      {/* Product Image Placeholder */}
      <div className="h-32 bg-gradient-to-br from-blue-50 to-blue-100 flex items-center justify-center">
        <div className="text-4xl font-bold text-blue-300">
          {product.name ? product.name.charAt(0).toUpperCase() : '?'}
        </div>
      </div>

      {/* Product Details */}
      <div className="p-4">
        <div className="mb-2">
          <h3 className="font-semibold text-gray-900 text-sm line-clamp-2 h-10">
            {product.name}
          </h3>
          {product.sku && (
            <p className="text-xs text-gray-500 mt-1">SKU: {product.sku}</p>
          )}
        </div>

        {/* Category */}
        {product.categoryName && (
          <p className="text-xs text-gray-500 mb-2">
            {product.categoryName}
          </p>
        )}

        {/* Price */}
        <div className="flex items-center justify-between mb-2">
          <span className="text-lg font-bold text-gray-900">
            {new Intl.NumberFormat('id-ID', {
              style: 'currency',
              currency: 'IDR',
              minimumFractionDigits: 0
            }).format(product.basePrice)}
          </span>
        </div>

        {/* Stock Status */}
        {product.availableStock !== undefined && (
          <div className="mb-3">
            {isOutOfStock ? (
              <span className="text-xs font-medium text-red-600">
                {t('pos.outOfStock')}
              </span>
            ) : isLowStock ? (
              <span className="text-xs font-medium text-amber-600">
                {t('pos.lowStock')}: {product.availableStock}
              </span>
            ) : (
              <span className="text-xs text-gray-500">
                {t('pos.inStock')}: {product.availableStock}
              </span>
            )}
          </div>
        )}

        {/* Add to Cart Button */}
        <button
          onClick={handleAddToCart}
          disabled={isOutOfStock}
          className={`w-full py-2 px-4 rounded-lg font-medium text-sm
                     flex items-center justify-center gap-2 transition-colors
                     ${isOutOfStock 
                       ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                       : 'bg-blue-600 text-white hover:bg-blue-700 active:bg-blue-800'
                     }`}
        >
          <PlusIcon className="h-4 w-4" />
          {t('pos.addToCart')}
        </button>
      </div>
    </div>
  );
}

export default ProductCard;
