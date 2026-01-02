import { useTranslation } from 'react-i18next';

/**
 * EmptyState Component
 * Displays a friendly message when no data is available
 */
const EmptyState = ({ 
  icon = '📦', 
  title, 
  message, 
  action, 
  actionLabel 
}) => {
  const { t } = useTranslation();

  return (
    <div className="flex flex-col items-center justify-center py-12 px-4">
      <div className="text-6xl mb-4">{icon}</div>
      
      <h3 className="text-xl font-semibold text-gray-900 mb-2">
        {title || t('common.noDataAvailable', 'No data available')}
      </h3>
      
      <p className="text-gray-600 text-center max-w-md mb-6">
        {message || t('common.noDataMessage', 'There is no data to display at the moment.')}
      </p>
      
      {action && actionLabel && (
        <button
          onClick={action}
          className="px-6 py-2 bg-blue-600 text-white rounded-lg font-medium
                   hover:bg-blue-700 transition-colors"
        >
          {actionLabel}
        </button>
      )}
    </div>
  );
};

export default EmptyState;
