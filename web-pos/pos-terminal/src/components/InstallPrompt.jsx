import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { XMarkIcon, ArrowDownTrayIcon } from '@heroicons/react/24/outline';

/**
 * InstallPrompt Component
 * Shows a banner prompting users to install the PWA
 * Handles beforeinstallprompt event and tracks dismissal
 */
const InstallPrompt = () => {
  const { t } = useTranslation();
  const [deferredPrompt, setDeferredPrompt] = useState(null);
  const [showPrompt, setShowPrompt] = useState(false);

  useEffect(() => {
    // Check if user has previously dismissed the prompt
    const dismissed = localStorage.getItem('installPromptDismissed');
    if (dismissed === 'true') {
      return;
    }

    // Listen for the beforeinstallprompt event
    const handleBeforeInstallPrompt = (e) => {
      // Prevent the mini-infobar from appearing on mobile
      e.preventDefault();
      // Stash the event so it can be triggered later
      setDeferredPrompt(e);
      // Show the install prompt
      setShowPrompt(true);
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);

    // Check if app is already installed
    if (window.matchMedia('(display-mode: standalone)').matches) {
      setShowPrompt(false);
    }

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    };
  }, []);

  const handleInstallClick = async () => {
    if (!deferredPrompt) {
      return;
    }

    // Show the install prompt
    deferredPrompt.prompt();

    // Wait for the user to respond to the prompt
    const { outcome } = await deferredPrompt.userChoice;

    if (outcome === 'accepted') {
      console.log('User accepted the install prompt');
    } else {
      console.log('User dismissed the install prompt');
    }

    // Clear the deferredPrompt
    setDeferredPrompt(null);
    setShowPrompt(false);
  };

  const handleDismiss = () => {
    setShowPrompt(false);
    // Remember dismissal for 7 days
    localStorage.setItem('installPromptDismissed', 'true');
    setTimeout(() => {
      localStorage.removeItem('installPromptDismissed');
    }, 7 * 24 * 60 * 60 * 1000); // 7 days
  };

  if (!showPrompt) {
    return null;
  }

  return (
    <div className="fixed bottom-0 left-0 right-0 z-50 p-4 md:p-6">
      <div className="max-w-2xl mx-auto bg-gradient-to-r from-blue-600 to-blue-700 rounded-lg shadow-2xl border border-blue-500">
        <div className="p-4 md:p-6">
          <div className="flex items-start justify-between gap-4">
            <div className="flex items-start gap-3 flex-1">
              <div className="flex-shrink-0 mt-1">
                <ArrowDownTrayIcon className="h-6 w-6 text-white" />
              </div>
              <div className="flex-1">
                <h3 className="text-lg font-semibold text-white mb-1">
                  {t('pwa.installTitle', 'Install CursorPOS')}
                </h3>
                <p className="text-sm text-blue-100 mb-4">
                  {t('pwa.installDescription', 'Install our app for faster access, offline support, and a better experience.')}
                </p>
                <div className="flex flex-wrap gap-2">
                  <button
                    onClick={handleInstallClick}
                    className="px-4 py-2 bg-white text-blue-600 rounded-lg font-medium text-sm
                             hover:bg-blue-50 transition-colors shadow-md"
                  >
                    {t('pwa.installButton', 'Install Now')}
                  </button>
                  <button
                    onClick={handleDismiss}
                    className="px-4 py-2 bg-blue-700 text-white rounded-lg font-medium text-sm
                             hover:bg-blue-800 transition-colors"
                  >
                    {t('pwa.maybeLater', 'Maybe Later')}
                  </button>
                </div>
              </div>
            </div>
            <button
              onClick={handleDismiss}
              className="flex-shrink-0 text-white hover:text-blue-200 transition-colors"
              aria-label={t('common.close', 'Close')}
            >
              <XMarkIcon className="h-5 w-5" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default InstallPrompt;
