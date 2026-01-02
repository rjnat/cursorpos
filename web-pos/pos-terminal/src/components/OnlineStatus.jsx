import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import syncService from '../services/syncService';

/**
 * OnlineStatus Component
 * Displays online/offline indicator and sync progress
 */
export default function OnlineStatus() {
  const { t } = useTranslation();
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [syncProgress, setSyncProgress] = useState(null);

  useEffect(() => {
    // Subscribe to online/offline status
    const unsubscribeStatus = syncService.subscribe((status) => {
      setIsOnline(status.online);
    });

    // Subscribe to sync progress
    const unsubscribeSync = syncService.subscribeSyncProgress((progress) => {
      setSyncProgress(progress);
    });

    return () => {
      unsubscribeStatus();
      unsubscribeSync();
    };
  }, []);

  const handleManualSync = async () => {
    if (isOnline) {
      await syncService.manualSync();
    }
  };

  return (
    <div className="flex items-center gap-3">
      {/* Online/Offline Indicator */}
      <div className="flex items-center gap-2">
        <div
          className={`w-2 h-2 rounded-full ${
            isOnline ? 'bg-green-500 animate-pulse' : 'bg-red-500'
          }`}
        />
        <span className="text-sm font-medium text-gray-700">
          {isOnline ? t('offline.online') : t('offline.offline')}
        </span>
      </div>

      {/* Sync Progress */}
      {syncProgress?.syncing && (
        <div className="flex items-center gap-2 text-sm text-blue-600">
          <svg
            className="animate-spin h-4 w-4"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
            />
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
          <span>
            {t('offline.syncing')} {syncProgress.completed}/{syncProgress.total}
          </span>
        </div>
      )}

      {/* Sync Complete */}
      {syncProgress && !syncProgress.syncing && syncProgress.success > 0 && (
        <div className="flex items-center gap-2 text-sm text-green-600">
          <svg
            className="w-4 h-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M5 13l4 4L19 7"
            />
          </svg>
          <span>
            {t('offline.syncComplete', { count: syncProgress.success })}
          </span>
        </div>
      )}

      {/* Manual Sync Button */}
      {isOnline && !syncProgress?.syncing && (
        <button
          onClick={handleManualSync}
          className="text-sm text-blue-600 hover:text-blue-700 font-medium"
          title={t('offline.syncNow')}
        >
          <svg
            className="w-4 h-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
            />
          </svg>
        </button>
      )}
    </div>
  );
}
