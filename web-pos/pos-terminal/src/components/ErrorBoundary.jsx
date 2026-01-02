import { Component } from 'react';
import { useTranslation } from 'react-i18next';

/**
 * ErrorBoundary Component
 * Catches JavaScript errors anywhere in the component tree
 * and displays a fallback UI instead of crashing
 */
class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  handleReload = () => {
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return <ErrorFallback onReload={this.handleReload} error={this.state.error} />;
    }

    return this.props.children;
  }
}

function ErrorFallback({ onReload, error }) {
  const { t } = useTranslation();

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8 text-center">
        <div className="mb-6">
          <div className="mx-auto w-16 h-16 bg-red-100 rounded-full flex items-center justify-center">
            <svg
              className="w-8 h-8 text-red-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
              />
            </svg>
          </div>
        </div>

        <h1 className="text-2xl font-bold text-gray-900 mb-2">
          {t('errors.somethingWrong', 'Oops! Something went wrong')}
        </h1>
        
        <p className="text-gray-600 mb-6">
          {t('errors.unexpectedError', 'An unexpected error occurred. Please try reloading the application.')}
        </p>

        {error && process.env.NODE_ENV === 'development' && (
          <div className="mb-6 p-4 bg-gray-100 rounded text-left">
            <p className="text-sm text-gray-700 font-mono break-all">
              {error.toString()}
            </p>
          </div>
        )}

        <div className="flex flex-col gap-3">
          <button
            onClick={onReload}
            className="w-full px-6 py-3 bg-blue-600 text-white rounded-lg font-medium
                     hover:bg-blue-700 transition-colors"
          >
            {t('errors.reloadApp', 'Reload Application')}
          </button>
          
          <button
            onClick={() => window.history.back()}
            className="w-full px-6 py-3 bg-gray-100 text-gray-700 rounded-lg font-medium
                     hover:bg-gray-200 transition-colors"
          >
            {t('common.goBack', 'Go Back')}
          </button>
        </div>

        <p className="mt-6 text-sm text-gray-500">
          {t('errors.persistsProblem', 'If the problem persists, please contact support.')}
        </p>
      </div>
    </div>
  );
}

export default ErrorBoundary;
