import { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { MagnifyingGlassIcon, XMarkIcon } from '@heroicons/react/24/outline';

/**
 * ProductSearch Component
 * Search bar with debounced input for product search
 */
function ProductSearch({ onSearch, onClear }) {
  const { t } = useTranslation();
  const [searchTerm, setSearchTerm] = useState('');

  // Debounce search to avoid excessive API calls
  useEffect(() => {
    const timer = setTimeout(() => {
      if (searchTerm.trim()) {
        onSearch(searchTerm.trim());
      } else {
        onClear();
      }
    }, 300); // 300ms debounce

    return () => clearTimeout(timer);
  }, [searchTerm, onSearch, onClear]);

  const handleClear = () => {
    setSearchTerm('');
    onClear();
  };

  return (
    <div className="relative">
      <div className="relative">
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <MagnifyingGlassIcon className="h-5 w-5 text-gray-400" />
        </div>
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder={t('pos.searchProducts')}
          className="block w-full pl-10 pr-10 py-3 border border-gray-300 rounded-lg 
                     focus:ring-2 focus:ring-blue-500 focus:border-transparent
                     text-base placeholder-gray-400"
          autoFocus
        />
        {searchTerm && (
          <button
            onClick={handleClear}
            className="absolute inset-y-0 right-0 pr-3 flex items-center"
          >
            <XMarkIcon className="h-5 w-5 text-gray-400 hover:text-gray-600" />
          </button>
        )}
      </div>
      <p className="mt-1 text-sm text-gray-500">
        {t('pos.searchHint')}
      </p>
    </div>
  );
}

export default ProductSearch;
