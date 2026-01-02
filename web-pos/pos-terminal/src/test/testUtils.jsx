import { render } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { BrowserRouter } from 'react-router-dom';
import { I18nextProvider } from 'react-i18next';
import i18n from '../i18n/config';
import authReducer from '../store/authSlice';
import cartReducer from '../store/cartSlice';

/**
 * Render a component with all providers (Redux, Router, i18n)
 * @param {ReactElement} ui - Component to render
 * @param {Object} options - Render options
 * @param {Object} options.preloadedState - Initial Redux state
 * @param {Object} options.store - Custom store
 * @returns {Object} - Render result with store
 */
export function renderWithProviders(
  ui,
  {
    preloadedState = {},
    store = configureStore({
      reducer: {
        auth: authReducer,
        cart: cartReducer,
      },
      preloadedState,
    }),
    ...renderOptions
  } = {}
) {
  function Wrapper({ children }) {
    return (
      <Provider store={store}>
        <BrowserRouter>
          <I18nextProvider i18n={i18n}>
            {children}
          </I18nextProvider>
        </BrowserRouter>
      </Provider>
    );
  }

  return { store, ...render(ui, { wrapper: Wrapper, ...renderOptions }) };
}

/**
 * Create a mock store with custom state
 * @param {Object} initialState - Initial Redux state
 * @returns {Store} - Configured Redux store
 */
export function createMockStore(initialState = {}) {
  return configureStore({
    reducer: {
      auth: authReducer,
      cart: cartReducer,
    },
    preloadedState: initialState,
  });
}

export * from '@testing-library/react';
