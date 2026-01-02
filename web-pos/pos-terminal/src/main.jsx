import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { Provider } from 'react-redux';
import { PersistGate } from 'redux-persist/integration/react';
import { Toaster } from 'react-hot-toast';
import { store, persistor } from './store';
import './i18n/config';
import './index.css';
import App from './App.jsx';
import ErrorBoundary from './components/ErrorBoundary';
import syncService from './services/syncService';

// Initialize sync service
syncService.init();

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ErrorBoundary>
      <Provider store={store}>
        <PersistGate loading={null} persistor={persistor}>
          <App />
          <Toaster
            position="top-right"
            toastOptions={{
              duration: 3000,
              success: {
                duration: 3000,
                iconTheme: {
                  primary: '#10b981',
                  secondary: '#fff',
                },
              },
            error: {
              duration: 5000,
              iconTheme: {
                primary: '#ef4444',
                secondary: '#fff',
              },
            },
            style: {
              background: '#fff',
              color: '#1f2937',
              boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
              borderRadius: '0.5rem',
              padding: '1rem',
            },
          }}
        />
      </PersistGate>
    </Provider>
    </ErrorBoundary>
  </StrictMode>,
);
