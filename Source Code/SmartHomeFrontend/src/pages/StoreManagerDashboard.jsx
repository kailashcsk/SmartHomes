import React, { useState, useEffect } from 'react';
import ProductManagement from '../components/storeManager/ProductManagement';
import CategoryManagement from '../components/storeManager/CategoryManagement';
import StoreManagement from '../components/storeManager/StoreManagement';
import DiscountManagement from '../components/storeManager/DiscountManagement';
import WarrantyManagement from '../components/storeManager/WarrantyManagement';
import AccessoryManagement from '../components/storeManager/AccessoryManagement';
import InventoryManagement from '../components/storeManager/InventoryManagement';
import SalesReport from '../components/storeManager/SalesReport';

function StoreManagerDashboard() {
    const [activeTab, setActiveTab] = useState(() => {
        return localStorage.getItem('activeTab') || 'products';
    });

    useEffect(() => {
        localStorage.setItem('activeTab', activeTab);
    }, [activeTab]);

    const renderActiveComponent = () => {
        switch (activeTab) {
            case 'products':
                return <ProductManagement />;
            case 'categories':
                return <CategoryManagement />;
            case 'stores':
                return <StoreManagement />;
            case 'discounts':
                return <DiscountManagement />;
            case 'warranties':
                return <WarrantyManagement />;
            case 'accessories':
                return <AccessoryManagement />;
            case 'inventory':
                return <InventoryManagement />;
            case 'salesReport':
                return <SalesReport />;
            default:
                return <ProductManagement />;
        }
    };

    const tabs = ['products', 'categories', 'stores', 'discounts', 'warranties', 'accessories', 'inventory', 'salesReport'];

    return (
        <div className="container mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold mb-8">Store Manager Dashboard</h1>
            <div className="flex mb-8 flex-wrap">
                {tabs.map((tab) => (
                    <button
                        key={tab}
                        onClick={() => setActiveTab(tab)}
                        className={`px-4 py-2 m-1 ${activeTab === tab ? 'bg-blue-600 text-white' : 'bg-gray-200'} rounded`}
                    >
                        {tab === 'salesReport' ? 'Sales Report' : tab.charAt(0).toUpperCase() + tab.slice(1)}
                    </button>
                ))}
            </div>
            {renderActiveComponent()}
        </div>
    );
}

export default StoreManagerDashboard;