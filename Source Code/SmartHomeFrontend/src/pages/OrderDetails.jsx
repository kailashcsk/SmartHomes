import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';

function OrderDetails() {
    const { orderId } = useParams();
    const navigate = useNavigate();
    const [order, setOrder] = useState(null);
    const [store, setStore] = useState(null);
    const [products, setProducts] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchOrderDetails = async () => {
            try {
                const orderResponse = await api.get(`/orders/${orderId}`);
                setOrder(orderResponse.data);

                const storeResponse = await api.get(`/stores/${orderResponse.data.storeId}`);
                setStore(storeResponse.data);

                const productPromises = orderResponse.data.orderItems.map(item =>
                    api.get(`/products/${item.productId}`)
                );
                const productResponses = await Promise.all(productPromises);
                const productMap = {};
                productResponses.forEach(response => {
                    productMap[response.data.id] = response.data;
                });
                setProducts(productMap);

                setLoading(false);
            } catch (err) {
                console.error('Error fetching order details:', err);
                setError('Failed to load order details. Please try again.');
                setLoading(false);
            }
        };

        fetchOrderDetails();
    }, [orderId]);

    const handleCancelOrder = async () => {
        try {
            await api.delete(`/orders/${orderId}`);
            alert('Order cancelled successfully');
            navigate('/orders');
        } catch (err) {
            console.error('Error cancelling order:', err);
            alert('Failed to cancel order. Please try again.');
        }
    };


    if (loading) return <div className="text-center mt-8">Loading...</div>;
    if (error) return <div className="text-center mt-8 text-red-600">{error}</div>;
    if (!order) return <div className="text-center mt-8">Order not found.</div>;

    const canCancel = new Date(order.orderDate).getTime() + 5 * 24 * 60 * 60 * 1000 > Date.now();
    const confirmationNumber = `ORD-${order.id.toString().padStart(6, '0')}`;

    const formatDate = (timestamp) => {
        return new Date(timestamp).toLocaleDateString();
    };

    const handleGoBack = () => {
        navigate(-1); // This will take the user back to the previous page
    };

    return (
        <div className="container mx-auto px-4 py-8">
            <button
                onClick={handleGoBack}
                className="mb-4 bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-2 px-4 rounded inline-flex items-center"
            >
                <svg className="fill-current w-4 h-4 mr-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
                    <path d="M10 8.586L2.929 1.515 1.515 2.929 8.586 10l-7.071 7.071 1.414 1.414L10 11.414l7.071 7.071 1.414-1.414L11.414 10l7.071-7.071-1.414-1.414L10 8.586z" />
                </svg>
                <span>Back to Order History</span>
            </button>
            <h1 className="text-3xl font-bold mb-8">Order Details</h1>
            <div className="bg-white shadow-md rounded-lg p-6">
                <h2 className="text-2xl font-semibold mb-4">Order #{confirmationNumber}</h2>
                <p className="mb-2"><strong>Status:</strong> {order.status}</p>
                <p className="mb-2"><strong>Order Date:</strong> {formatDate(order.orderDate)}</p>
                <p className="mb-2"><strong>
                    {order.deliveryMethod === 'STORE_PICKUP' ? 'Pickup' : 'Shipping'} Date:
                </strong> {formatDate(order.shipDate)}</p>
                <p className="mb-4"><strong>Delivery Method:</strong> {order.deliveryMethod}</p>
                {order.deliveryMethod === 'HOME_DELIVERY' ? (
                    <p className="mb-4"><strong>Shipping Address:</strong> {order.shippingAddress}</p>
                ) : store && (
                    <div className="mb-4">
                        <p><strong>Pickup Store:</strong> {store.name}</p>
                        <p><strong>Address:</strong> {store.address}</p>
                        <p>{store.city}, {store.state} {store.zipCode}</p>
                    </div>
                )}
                <h3 className="text-xl font-semibold mb-2">Order Items</h3>
                <ul className="list-disc pl-5 mb-4">
                    {order.orderItems.map((item) => (
                        <li key={item.id} className="mb-2">
                            {products[item.productId]?.name || 'Product Name Not Available'} -
                            Quantity: {item.quantity}, Price: ${item.price.toFixed(2)}
                        </li>
                    ))}
                </ul>
                <p className="mb-2"><strong>Subtotal:</strong> ${order.totalAmount.toFixed(2)}</p>
                <p className="mb-2"><strong>Shipping Cost:</strong> ${order.shippingCost.toFixed(2)}</p>
                <p className="mb-2"><strong>Discount:</strong> ${order.discountAmount.toFixed(2)}</p>
                <p className="text-xl font-bold mb-4"><strong>Total:</strong> ${order.finalSaleAmount.toFixed(2)}</p>
                {canCancel && (
                    <button
                        onClick={handleCancelOrder}
                        className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700 transition duration-300"
                    >
                        Cancel Order
                    </button>
                )}
                {!canCancel && (
                    <p className="text-red-600">Orders can only be cancelled within 5 days of placing the order.</p>
                )}
            </div>
        </div>
    );
}

export default OrderDetails;