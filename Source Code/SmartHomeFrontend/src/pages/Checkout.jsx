import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../contexts/CartContext';
import api from '../services/api';

function Checkout() {
    const navigate = useNavigate();
    const { cart, getTotalPrice, clearCart } = useCart();
    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        address: '',
        city: '',
        state: '',
        zipCode: '',
        cardNumber: '',
        deliveryMethod: 'HOME_DELIVERY',
        storeId: ''
    });
    const [stores, setStores] = useState([]);
    const [discounts, setDiscounts] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const getShippingCost = () => {
        return formData.deliveryMethod === 'STORE_PICKUP' ? 0 : 10.00;
    };

    useEffect(() => {
        const fetchInitialData = async () => {
            try {
                const [storesResponse, discountsResponse] = await Promise.all([
                    api.get('/stores'),
                    api.get('/discounts')
                ]);
                setStores(storesResponse.data);
                setDiscounts(discountsResponse.data);
            } catch (err) {
                console.error('Error fetching initial data:', err);
                setError('Failed to load necessary data. Please try again.');
            }
        };

        fetchInitialData();
    }, []);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const getProductCategory = async (productId) => {
        try {
            const productResponse = await api.get(`/products/${productId}`);
            const categoryResponse = await api.get(`/categories/${productResponse.data.categoryId}`);
            return categoryResponse.data.name;
        } catch (err) {
            console.error('Error fetching product category:', err);
            return null;
        }
    };

    const calculateDiscountAmount = () => {
        return cart.reduce((total, item) => {
            const itemDiscounts = discounts.filter(discount => discount.productId === item.id);
            const itemDiscountAmount = itemDiscounts.reduce((sum, discount) => sum + discount.amount, 0);
            return total + (itemDiscountAmount * item.quantity);
        }, 0);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        if (!formData.storeId) {
            setError('Please select a store.');
            setLoading(false);
            return;
        }

        const totalAmount = getTotalPrice();
        const shippingCost = getShippingCost();
        const discountAmount = calculateDiscountAmount();
        const finalSaleAmount = totalAmount + shippingCost - discountAmount;

        try {
            // Fetch categories for all products in the cart
            const orderItems = await Promise.all(cart.map(async item => ({
                productId: item.id,
                category: await getProductCategory(item.id),
                quantity: item.quantity,
                price: item.price
            })));

            const orderData = {
                storeId: parseInt(formData.storeId),
                totalAmount,
                shippingCost,
                discountAmount,
                finalSaleAmount,
                status: 'PENDING',
                deliveryMethod: formData.deliveryMethod,
                shippingAddress: formData.deliveryMethod === 'HOME_DELIVERY'
                    ? `${formData.address}, ${formData.city}, ${formData.state} ${formData.zipCode}`
                    : null,
                creditCardNumber: formData.cardNumber,
                orderItems
            };

            // Place the order
            const orderResponse = await api.post('/orders', orderData);

            // If it's a shipping order, add the address
            if (formData.deliveryMethod === 'HOME_DELIVERY') {
                const addressData = {
                    street: formData.address,
                    city: formData.city,
                    state: formData.state,
                    zipCode: formData.zipCode
                };
                await api.post('/address', addressData);
            }

            console.log('Order placed successfully:', orderResponse.data);
            // Navigate to a confirmation page or clear cart and show success message
            navigate('/orders', { state: { orderId: orderResponse.data.id } });
            // Clear the cart after successful order placement
            clearCart();
        } catch (err) {
            console.error('Error placing order:', err);
            setError('Failed to place order. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleGoBack = () => {
        navigate(-1);
    };

    return (
        <div className="py-8 max-w-4xl mx-auto">
            <button
                onClick={handleGoBack}
                className="mb-4 bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-2 px-4 rounded inline-flex items-center"
            >
                <svg className="fill-current w-4 h-4 mr-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
                    <path d="M10 8.586L2.929 1.515 1.515 2.929 8.586 10l-7.071 7.071 1.414 1.414L10 11.414l7.071 7.071 1.414-1.414L11.414 10l7.071-7.071-1.414-1.414L10 8.586z" />
                </svg>
                <span>Back to Cart</span>
            </button>

            <h1 className="text-3xl font-bold mb-8">Checkout</h1>

            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label htmlFor="fullName" className="block mb-1">Full Name</label>
                    <input
                        type="text"
                        id="fullName"
                        name="fullName"
                        value={formData.fullName}
                        onChange={handleInputChange}
                        required
                        className="w-full px-3 py-2 border rounded"
                    />
                </div>

                <div>
                    <label htmlFor="email" className="block mb-1">Email</label>
                    <input
                        type="email"
                        id="email"
                        name="email"
                        value={formData.email}
                        onChange={handleInputChange}
                        required
                        className="w-full px-3 py-2 border rounded"
                    />
                </div>

                <div>
                    <label htmlFor="storeId" className="block mb-1">Select Store</label>
                    <select
                        id="storeId"
                        name="storeId"
                        value={formData.storeId}
                        onChange={handleInputChange}
                        required
                        className="w-full px-3 py-2 border rounded"
                    >
                        <option value="">Select a store</option>
                        {stores.map(store => (
                            <option key={store.id} value={store.id}>
                                {store.name} - {store.address}
                            </option>
                        ))}
                    </select>
                </div>

                <div>
                    <label htmlFor="deliveryMethod" className="block mb-1">Delivery Method</label>
                    <select
                        id="deliveryMethod"
                        name="deliveryMethod"
                        value={formData.deliveryMethod}
                        onChange={handleInputChange}
                        required
                        className="w-full px-3 py-2 border rounded"
                    >
                        <option value="HOME_DELIVERY">Home Delivery</option>
                        <option value="STORE_PICKUP">Store Pickup</option>
                    </select>
                </div>

                {formData.deliveryMethod === 'HOME_DELIVERY' && (
                    <>
                        <div>
                            <label htmlFor="address" className="block mb-1">Address</label>
                            <input
                                type="text"
                                id="address"
                                name="address"
                                value={formData.address}
                                onChange={handleInputChange}
                                required
                                className="w-full px-3 py-2 border rounded"
                            />
                        </div>

                        <div className="grid grid-cols-3 gap-4">
                            <div>
                                <label htmlFor="city" className="block mb-1">City</label>
                                <input
                                    type="text"
                                    id="city"
                                    name="city"
                                    value={formData.city}
                                    onChange={handleInputChange}
                                    required
                                    className="w-full px-3 py-2 border rounded"
                                />
                            </div>
                            <div>
                                <label htmlFor="state" className="block mb-1">State</label>
                                <input
                                    type="text"
                                    id="state"
                                    name="state"
                                    value={formData.state}
                                    onChange={handleInputChange}
                                    required
                                    className="w-full px-3 py-2 border rounded"
                                />
                            </div>
                            <div>
                                <label htmlFor="zipCode" className="block mb-1">Zip Code</label>
                                <input
                                    type="text"
                                    id="zipCode"
                                    name="zipCode"
                                    value={formData.zipCode}
                                    onChange={handleInputChange}
                                    required
                                    className="w-full px-3 py-2 border rounded"
                                />
                            </div>
                        </div>
                    </>
                )}

                {formData.deliveryMethod === 'STORE_PICKUP' && (
                    <div>
                        <label htmlFor="storeId" className="block mb-1">Select Store</label>
                        <select
                            id="storeId"
                            name="storeId"
                            value={formData.storeId}
                            onChange={handleInputChange}
                            required
                            className="w-full px-3 py-2 border rounded"
                        >
                            <option value="">Select a store</option>
                            {stores.map(store => (
                                <option key={store.id} value={store.id}>
                                    {store.name} - {store.address}
                                </option>
                            ))}
                        </select>
                    </div>
                )}

                <div>
                    <label htmlFor="cardNumber" className="block mb-1">Credit Card Number</label>
                    <input
                        type="text"
                        id="cardNumber"
                        name="cardNumber"
                        value={formData.cardNumber}
                        onChange={handleInputChange}
                        required
                        className="w-full px-3 py-2 border rounded"
                    />
                </div>

                <div className="mt-8">
                    <h2 className="text-xl font-semibold mb-2">Order Summary</h2>
                    <p>Subtotal: ${getTotalPrice().toFixed(2)}</p>
                    <p>Shipping: ${getShippingCost().toFixed(2)}</p>
                    <p>Discount: ${calculateDiscountAmount().toFixed(2)}</p>
                    <p className="font-bold">Total: ${(getTotalPrice() + 10 - calculateDiscountAmount()).toFixed(2)}</p>
                </div>

                {error && <p className="text-red-600">{error}</p>}

                <button
                    type="submit"
                    className="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
                    disabled={loading}
                >
                    {loading ? 'Placing Order...' : 'Place Order'}
                </button>
            </form>
        </div>
    );
}

export default Checkout;