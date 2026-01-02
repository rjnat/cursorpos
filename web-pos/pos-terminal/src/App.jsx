import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import Login from './pages/Login/Login';
import Layout from './components/Layout';
import PrivateRoute from './components/PrivateRoute';
import Sell from './pages/Sell/Sell';
import OrderHistory from './pages/OrderHistory/OrderHistory';
import Reports from './pages/Reports/Reports';
import ApprovalDashboard from './pages/ApprovalDashboard';

function App() {
  const { isAuthenticated } = useSelector((state) => state.auth);

  return (
    <Router>
      <Routes>
        {/* Public Routes */}
        <Route 
          path="/login" 
          element={isAuthenticated ? <Navigate to="/" replace /> : <Login />} 
        />

        {/* Protected Routes */}
        <Route
          path="/"
          element={
            <PrivateRoute>
              <Layout />
            </PrivateRoute>
          }
        >
          <Route index element={<Sell />} />
          <Route path="history" element={<OrderHistory />} />
          <Route path="reports" element={<Reports />} />
          <Route path="approvals" element={<ApprovalDashboard />} />
        </Route>

        {/* Catch all - redirect to home */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
