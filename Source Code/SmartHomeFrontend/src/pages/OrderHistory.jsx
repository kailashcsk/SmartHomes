import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';


function OrderHistory() {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchOrders = async () => {
            try {
                const response = await api.get('/orders');
                setOrders(response.data);
                setLoading(false);
            } catch (err) {
                console.error('Error fetching orders:', err);
                setError('Failed to load orders. Please try again.');
                setLoading(false);
            }
        };

        fetchOrders();
    }, []);

    const generateConfirmationNumber = (orderId) => {
        return `ORD-${orderId.toString().padStart(6, '0')}`;
    };
    const formatDate = (timestamp) => {
        return new Date(timestamp).toLocaleDateString();
    };

    if (loading) return <div className="text-center mt-8">Loading...</div>;
    if (error) return <div className="text-center mt-8 text-red-600">{error}</div>;

    return (
        <div className="container mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold mb-8">Order History</h1>
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                {orders.map((order) => (
                    <div key={order.id} className="bg-white shadow-md rounded-lg p-6">
                        <h2 className="text-xl font-semibold mb-2">Order #{generateConfirmationNumber(order.id)}</h2>
                        <p className="text-gray-600 mb-2">Status: <span className="font-semibold">{order.status}</span></p>
                        <p className="text-gray-600 mb-2">Order Date: {formatDate(order.orderDate)}</p>
                        <p className="text-gray-600 mb-4">
                            {order.deliveryMethod === 'HOME_DELIVERY' ? 'Shipping' : 'Pickup'} Date:
                            {formatDate(order.shipDate)}
                        </p>
                        <p className="text-lg font-bold mb-4">Total: ${order.finalSaleAmount.toFixed(2)}</p>
                        <Link
                            to={`/orders/${order.id}`}
                            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition duration-300"
                        >
                            View Details
                        </Link>
                    </div>
                ))}
            </div>
        </div>
    );

}

export default OrderHistory;