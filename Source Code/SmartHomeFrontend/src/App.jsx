import React from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Header from './components/common/Header';
import Footer from './components/common/Footer';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import ProductDetails from './pages/ProductDetails';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import OrderHistory from './pages/OrderHistory';
import OrderDetails from './pages/OrderDetails';
import Profile from './pages/Profile';
import Reviews from './pages/Reviews';
import Trending from './pages/Trending';
import SalesmanDashboard from './pages/SalesmanDashboard';
import StoreManagerDashboard from './pages/StoreManagerDashboard';
import { getCurrentUser } from './services/api';
import { CartProvider } from './contexts/CartContext';
import CustomerService from './pages/CustomerService';

function PrivateRoute({ children, allowedRoles }) {
  const currentUser = getCurrentUser();

  if (!currentUser) {
    return <Navigate to="/login" />;
  }

  if (allowedRoles && !allowedRoles.includes(currentUser.role)) {
    return <Navigate to="/" />;
  }

  return children;
}

function App() {
  return (

    <CartProvider>
      <Router>
        <div className="flex flex-col min-h-screen">
          <Header />
          <main className="flex-grow container mx-auto px-4 sm:px-6 lg:px-8">
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/products" element={<Home />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/products/:id" element={<ProductDetails />} />
              <Route path="/cart" element={<PrivateRoute><Cart /></PrivateRoute>} />
              <Route path="/checkout" element={<PrivateRoute><Checkout /></PrivateRoute>} />
              <Route path="/orders" element={<PrivateRoute><OrderHistory /></PrivateRoute>} />
              <Route path="/orders/:orderId" element={<PrivateRoute><OrderDetails /></PrivateRoute>} />
              <Route path="/reviews/:productId" element={<PrivateRoute><Reviews /></PrivateRoute>} />
              <Route path="/store-manager-dashboard" element={<PrivateRoute><StoreManagerDashboard /></PrivateRoute>} />
              <Route
                path="/salesman-dashboard"
                element={
                  <PrivateRoute>
                    <SalesmanDashboard />
                  </PrivateRoute>
                }
              />
              <Route path="/profile" element={<PrivateRoute><Profile /></PrivateRoute>} />
              <Route path="/trending" element={<Trending />} />

              <Route
                path="/store-manager-dashboard"
                element={
                  <PrivateRoute allowedRoles={['StoreManager']}>
                    <StoreManagerDashboard />
                  </PrivateRoute>
                }
              />
              <Route
                path="/customer-service"
                element={
                  <PrivateRoute>
                    <CustomerService />
                  </PrivateRoute>
                }
              />
            </Routes>
          </main>
          <Footer />
        </div>
      </Router>
    </CartProvider>
  );
}

export default App;