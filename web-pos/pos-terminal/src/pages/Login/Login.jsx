import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import toast from 'react-hot-toast';
import { loginStart, loginSuccess, loginFailure } from '../../store/authSlice';
import authService from '../../services/authService';

const schema = yup.object({
  tenantId: yup.string().required('Tenant ID is required'),
  email: yup.string().email('Invalid email').required('Email is required'),
  password: yup.string().min(6, 'Password must be at least 6 characters').required('Password is required'),
}).required();

function Login() {
  const { t, i18n } = useTranslation();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { isAuthenticated, loading, error } = useSelector((state) => state.auth);
  const [stores, setStores] = useState([]);
  const [selectedStore, setSelectedStore] = useState('');
  const [showStoreSelection, setShowStoreSelection] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(schema),
  });

  useEffect(() => {
    if (isAuthenticated && !showStoreSelection) {
      navigate('/');
    }
  }, [isAuthenticated, navigate, showStoreSelection]);

  const onSubmit = async (data) => {
    try {
      dispatch(loginStart());
      const { token, user } = await authService.login(data.tenantId, data.email, data.password);
      
      // Get user stores
      const userStores = await authService.getUserStores();
      
      if (userStores.length > 1) {
        // Show store selection
        setStores(userStores);
        setShowStoreSelection(true);
        dispatch(loginSuccess({ user, token }));
      } else if (userStores.length === 1) {
        // Auto-select single store
        const store = userStores[0];
        localStorage.setItem('selectedStore', JSON.stringify(store));
        dispatch(loginSuccess({ user, token }));
        navigate('/');
      } else {
        dispatch(loginFailure('No stores assigned to this user'));
      }
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Login failed. Please try again.';
      dispatch(loginFailure(errorMessage));
    }
  };

  const handleStoreSelect = () => {
    if (!selectedStore) {
      toast.error(t('auth.selectStore'));
      return;
    }
    const store = stores.find((s) => s.id === selectedStore);
    localStorage.setItem('selectedStore', JSON.stringify(store));
    navigate('/');
  };

  const changeLanguage = (lng) => {
    i18n.changeLanguage(lng);
  };

  if (showStoreSelection) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-primary to-blue-700 flex items-center justify-center p-4">
        <div className="bg-white rounded-lg shadow-xl p-8 max-w-md w-full">
          <div className="text-center mb-6">
            <h1 className="text-3xl font-bold text-gray-800 mb-2">
              {t('auth.selectStore')}
            </h1>
            <p className="text-gray-600">{t('auth.chooseStoreMessage')}</p>
          </div>

          <div className="space-y-4">
            {stores.map((store) => (
              <label
                key={store.id}
                className={`block p-4 border-2 rounded-lg cursor-pointer transition-all ${
                  selectedStore === store.id
                    ? 'border-primary bg-blue-50'
                    : 'border-gray-200 hover:border-primary'
                }`}
              >
                <input
                  type="radio"
                  name="store"
                  value={store.id}
                  checked={selectedStore === store.id}
                  onChange={(e) => setSelectedStore(e.target.value)}
                  className="sr-only"
                />
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="font-semibold text-gray-800">{store.name}</h3>
                    <p className="text-sm text-gray-600">{store.address}</p>
                  </div>
                  {selectedStore === store.id && (
                    <svg
                      className="w-6 h-6 text-primary"
                      fill="currentColor"
                      viewBox="0 0 20 20"
                    >
                      <path
                        fillRule="evenodd"
                        d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                        clipRule="evenodd"
                      />
                    </svg>
                  )}
                </div>
              </label>
            ))}
          </div>

          <button
            onClick={handleStoreSelect}
            className="w-full mt-6 bg-primary text-white py-3 rounded-lg font-semibold hover:bg-blue-600 transition-colors"
          >
            {t('auth.continue')}
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary to-blue-700 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg shadow-xl p-8 max-w-md w-full">
        {/* Language Selector */}
        <div className="flex justify-end mb-4 space-x-2">
          <button
            onClick={() => changeLanguage('en')}
            className={`px-3 py-1 rounded ${
              i18n.language === 'en'
                ? 'bg-primary text-white'
                : 'bg-gray-200 text-gray-700'
            }`}
          >
            EN
          </button>
          <button
            onClick={() => changeLanguage('id')}
            className={`px-3 py-1 rounded ${
              i18n.language === 'id'
                ? 'bg-primary text-white'
                : 'bg-gray-200 text-gray-700'
            }`}
          >
            ID
          </button>
        </div>

        {/* Logo/Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-800 mb-2">CursorPOS</h1>
          <p className="text-gray-600">{t('auth.signInMessage')}</p>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
            {error}
          </div>
        )}

        {/* Login Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* Tenant ID */}
          <div>
            <label htmlFor="tenantId" className="block text-sm font-medium text-gray-700 mb-1">
              {t('auth.tenantId')}
            </label>
            <input
              id="tenantId"
              type="text"
              {...register('tenantId')}
              className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary ${
                errors.tenantId ? 'border-red-500' : 'border-gray-300'
              }`}
              placeholder="e.g., tenant-coffee-001"
            />
            {errors.tenantId && (
              <p className="mt-1 text-sm text-red-500">{errors.tenantId.message}</p>
            )}
          </div>

          {/* Email */}
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
              {t('auth.email')}
            </label>
            <input
              id="email"
              type="email"
              {...register('email')}
              className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary ${
                errors.email ? 'border-red-500' : 'border-gray-300'
              }`}
              placeholder="cashier@example.com"
            />
            {errors.email && (
              <p className="mt-1 text-sm text-red-500">{errors.email.message}</p>
            )}
          </div>

          {/* Password */}
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
              {t('auth.password')}
            </label>
            <input
              id="password"
              type="password"
              {...register('password')}
              className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary ${
                errors.password ? 'border-red-500' : 'border-gray-300'
              }`}
              placeholder="••••••••"
            />
            {errors.password && (
              <p className="mt-1 text-sm text-red-500">{errors.password.message}</p>
            )}
          </div>

          {/* Remember Me */}
          <div className="flex items-center">
            <input
              id="rememberMe"
              type="checkbox"
              checked={rememberMe}
              onChange={(e) => setRememberMe(e.target.checked)}
              className="h-4 w-4 text-primary focus:ring-primary border-gray-300 rounded"
            />
            <label htmlFor="rememberMe" className="ml-2 block text-sm text-gray-700">
              {t('auth.rememberMe')}
            </label>
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-primary text-white py-3 rounded-lg font-semibold hover:bg-blue-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? t('auth.signingIn') : t('auth.signIn')}
          </button>
        </form>

        {/* Footer */}
        <div className="mt-6 text-center text-sm text-gray-600">
          <p>{t('auth.needHelp')}</p>
        </div>
      </div>
    </div>
  );
}

export default Login;
