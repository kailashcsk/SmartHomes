import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { logout, getCurrentUser } from '../../services/api';
import { useCart } from '../../contexts/CartContext';

function Header() {
    const navigate = useNavigate();
    const currentUser = getCurrentUser();
    const currentUserData = currentUser?.user;
    const { getCartItemsCount } = useCart();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <header className="bg-blue-600 text-white">
            <nav className="container mx-auto px-4 sm:px-6 lg:px-8 py-4">
                <div className="flex items-center justify-between">
                    <Link to="/" className="text-2xl font-bold">SmartHomes</Link>
                    <div className="flex space-x-4">

                        <Link to="/products" className="hover:text-blue-200">Products</Link>
                        <Link to="/orders" className="hover:text-blue-200">Orders</Link>
                        <Link to="/cart" className="hover:text-blue-200 relative">Cart
                            
                            <span className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs">
                                {getCartItemsCount()}
                            </span>
                        </Link>
                        <Link to="/customer-service" className="hover:text-blue-200">Customer Service</Link>
                        {currentUser && currentUserData.role === 'STOREMANAGER' && (
                            <Link to="/store-manager-dashboard" className="hover:text-blue-200">Dashboard</Link>
                        )}

                        {currentUser && currentUserData.role === 'SALESMAN' && (
                            <Link to="/salesman-dashboard" className="hover:text-blue-200">Dashboard</Link>
                        )}

                        {currentUser ? (
                            <>
                                <span className="hover:text-blue-200">{currentUser.name}</span>
                                <button onClick={handleLogout} className="hover:text-blue-200">Logout</button>
                            </>
                        ) : (
                            <Link to="/login" className="hover:text-blue-200">Login</Link>
                        )}

                    </div>
                </div>
            </nav>
        </header>
    );
}

export default Header;