import { useState } from 'react';
import { useNavigate, Outlet, NavLink } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { logout } from '../store/authSlice';
import { clearCart } from '../store/cartSlice';
import authService from '../services/authService';
import OnlineStatus from './OnlineStatus';
import LanguageSwitcher from './LanguageSwitcher';
import InstallPrompt from './InstallPrompt';

function Layout() {
  const { t } = useTranslation();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user } = useSelector((state) => state.auth);
  const cartItemCount = useSelector((state) => 
    state.cart.items.reduce((total, item) => total + item.quantity, 0)
  );
  const [showUserMenu, setShowUserMenu] = useState(false);

  const handleLogout = () => {
    authService.logout();
    dispatch(logout());
    dispatch(clearCart());
    navigate('/login');
  };

  const isManagerOrAdmin = user?.role === 'MANAGER' || user?.role === 'ADMIN';

  const navItems = [
    { path: '/', label: t('pos.sell'), icon: '🛒' },
    { path: '/history', label: t('pos.history'), icon: '📋' },
    { path: '/reports', label: t('pos.reports'), icon: '📊' },
  ];

  // Add approvals link for managers and admins
  if (isManagerOrAdmin) {
    navItems.push({ path: '/approvals', label: t('pos.approvals'), icon: '✅' });
  }

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* Top Navigation */}
      <nav className="bg-primary text-white shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <div className="flex items-center gap-4">
              <h1 className="text-2xl font-bold">CursorPOS</h1>
              {cartItemCount > 0 && (
                <span className="bg-white text-primary px-3 py-1 rounded-full text-sm font-semibold">
                  {cartItemCount} {t('pos.items')}
                </span>
              )}
              {/* Online Status Indicator */}
              <div className="bg-white px-3 py-1 rounded-md">
                <OnlineStatus />
              </div>
            </div>

            {/* Navigation Links */}
            <div className="hidden md:flex space-x-4">
              {navItems.map((item) => (
                <NavLink
                  key={item.path}
                  to={item.path}
                  end={item.path === '/'}
                  className={({ isActive }) =>
                    `px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                      isActive
                        ? 'bg-blue-700'
                        : 'hover:bg-blue-600'
                    }`
                  }
                >
                  <span className="mr-2">{item.icon}</span>
                  {item.label}
                </NavLink>
              ))}
            </div>

            {/* User Menu and Language Switcher */}
            <div className="flex items-center gap-2">
              <LanguageSwitcher />
              
              <div className="relative">
                <button
                  onClick={() => setShowUserMenu(!showUserMenu)}
                  className="flex items-center space-x-2 hover:bg-blue-600 px-3 py-2 rounded-md transition-colors"
                >
                  <div className="w-8 h-8 bg-white text-primary rounded-full flex items-center justify-center font-bold">
                    {user?.name?.[0]?.toUpperCase() || 'U'}
                  </div>
                  <span className="hidden md:block">{user?.name || 'User'}</span>
                  <svg
                    className={`w-4 h-4 transition-transform ${showUserMenu ? 'rotate-180' : ''}`}
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path
                      fillRule="evenodd"
                      d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                      clipRule="evenodd"
                    />
                  </svg>
                </button>

                {/* Dropdown */}
                {showUserMenu && (
                  <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-10">
                    <div className="px-4 py-2 border-b">
                      <p className="text-sm text-gray-700 font-semibold">{user?.name}</p>
                      <p className="text-xs text-gray-500">{user?.email}</p>
                    </div>
                    <button
                      onClick={handleLogout}
                      className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    >
                      {t('auth.logout')}
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Mobile Navigation */}
        <div className="md:hidden border-t border-blue-600">
          <div className="flex justify-around py-2">
            {navItems.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                end={item.path === '/'}
                className={({ isActive }) =>
                  `px-3 py-2 rounded-md text-sm font-medium ${
                    isActive
                      ? 'bg-blue-700'
                      : 'hover:bg-blue-600'
                  }`
                }
              >
                <div className="flex flex-col items-center">
                  <span className="text-xl">{item.icon}</span>
                  <span className="text-xs mt-1">{item.label}</span>
                </div>
              </NavLink>
            ))}
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>

      {/* Install Prompt */}
      <InstallPrompt />
    </div>
  );
}

export default Layout;
