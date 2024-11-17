import React, { useState, useEffect, useRef } from 'react';
import OrderManagement from '../components/salesman/OrderManagement';
import UserManagement from '../components/salesman/UserManagement';

function SalesmanDashboard() {
    const [activeTab, setActiveTab] = useState(() => {
        // Get the active tab from localStorage or default to 'orders'
        return localStorage.getItem('activeTab') || 'orders';
    });

    const orderManagementRef = useRef();

    // Update localStorage whenever the active tab changes
    useEffect(() => {
        localStorage.setItem('activeTab', activeTab);
    }, [activeTab]);

    const handleRefresh = () => {
        if (activeTab === 'orders' && orderManagementRef.current) {
            orderManagementRef.current.resetFields();
        }
    };

    return (
        <div className="container mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold mb-8">Salesman Dashboard</h1>
            <div className="flex mb-8">
                    <button
                        onClick={() => setActiveTab('orders')}
                        className={`px-4 py-2 ${activeTab === 'orders' ? 'bg-blue-600 text-white' : 'bg-gray-200'}`}
                    >
                        Orders
                    </button>
                <button
                    onClick={() => setActiveTab('users')}
                    className={`px-4 py-2 ${activeTab === 'users' ? 'bg-blue-600 text-white' : 'bg-gray-200'}`}
                >
                    Users
                </button>
            </div>
            {activeTab === 'orders' ? <OrderManagement /> : <UserManagement />}
        </div>
    );
}

export default SalesmanDashboard;
