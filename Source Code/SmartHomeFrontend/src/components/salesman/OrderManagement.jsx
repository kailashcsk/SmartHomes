import React, { useState, useEffect } from 'react';
import api from '../../services/api';

function OrderManagement() {
    const [orders, setOrders] = useState([]);
    const [users, setUsers] = useState([]);
    const [stores, setStores] = useState([]);
    const [products, setProducts] = useState([]);
    const [formData, setFormData] = useState({
        userId: '',
        storeId: '',
        orderDate: '',
        shipDate: '',
        totalAmount: '',
        shippingCost: '',
        discountAmount: '',
        finalSaleAmount: '',
        status: '',
        deliveryMethod: '',
        shippingAddress: '',
        creditCardNumber: '',
        orderItems: []
    });
    const [editingId, setEditingId] = useState(null);
    const [discounts, setDiscounts] = useState([]);

    useEffect(() => {
        fetchOrders();
        fetchUsers();
        fetchStores();
        fetchProducts();
        fetchDiscounts();
    }, []);

    const fetchOrders = async () => {
        try {
            const response = await api.get('/allorders');
            setOrders(response.data);
        } catch (error) {
            console.error('Error fetching orders:', error);
        }
    };

    const fetchUsers = async () => {
        try {
            const response = await api.get('/users');
            setUsers(response.data);
        } catch (error) {
            console.error('Error fetching users:', error);
        }
    };

    const fetchStores = async () => {
        try {
            const response = await api.get('/stores');
            setStores(response.data);
        } catch (error) {
            console.error('Error fetching stores:', error);
        }
    };

    const fetchProducts = async () => {
        try {
            const response = await api.get('/products');
            setProducts(response.data);
        } catch (error) {
            console.error('Error fetching products:', error);
        }
    };

    const fetchDiscounts = async () => {
        try {
            const response = await api.get('/discounts');
            setDiscounts(response.data);
        } catch (error) {
            console.error('Error fetching discounts:', error);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        if (['totalAmount', 'shippingCost', 'discountAmount'].includes(name)) {
            calculateFinalAmount();
        }
    };

    const handleOrderItemChange = (index, field, value) => {
        const updatedOrderItems = [...formData.orderItems];
        updatedOrderItems[index] = { ...updatedOrderItems[index], [field]: value };
        setFormData(prev => ({ ...prev, orderItems: updatedOrderItems }));
        calculateTotalAmount();
    };

    const calculateTotalAmount = () => {
        const total = formData.orderItems.reduce((sum, item) => {
            return sum + (Number(item.price) * Number(item.quantity));
        }, 0);
        setFormData(prev => ({ ...prev, totalAmount: total.toFixed(2) }));
        calculateDiscountAmount(total);
    };

    const calculateDiscountAmount = (total) => {
        let discountAmount = 0;
        formData.orderItems.forEach(item => {
            const productDiscounts = discounts.filter(d => d.productId === item.productId);
            productDiscounts.forEach(discount => {
                    discountAmount += Number(discount.amount) * Number(item.quantity);
            });
        });
        setFormData(prev => ({ ...prev, discountAmount: discountAmount.toFixed(2) }));
        calculateFinalAmount(total, discountAmount);
    };

    const calculateFinalAmount = (total = formData.totalAmount, discount = formData.discountAmount) => {
        const finalAmount = Number(total) + Number(formData.shippingCost) - Number(discount);
        setFormData(prev => ({ ...prev, finalSaleAmount: finalAmount.toFixed(2) }));
    };


    const addOrderItem = () => {
        setFormData(prev => ({
            ...prev,
            orderItems: [...prev.orderItems, { productId: '', quantity: 1, price: '' }]
        }));
    };

    const removeOrderItem = (index) => {
        const updatedOrderItems = formData.orderItems.filter((_, i) => i !== index);
        setFormData(prev => ({ ...prev, orderItems: updatedOrderItems }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const orderData = {
                ...formData,
                orderDate: new Date(formData.orderDate).getTime(),
                shipDate: new Date(formData.shipDate).getTime()
            };
            if (editingId) {
                await api.put(`/allorders/${editingId}`, orderData);
            } else {
                await api.post('/allorders', orderData);
            }
            fetchOrders();
            resetForm();
        } catch (error) {
            console.error('Error saving order:', error);
            alert('Error saving order. Please try again.');
        }
    };

    const handleEdit = (order) => {
        setFormData({
            ...order,
            orderDate: new Date(order.orderDate).toISOString().split('T')[0],
            shipDate: new Date(order.shipDate).toISOString().split('T')[0]
        });
        setEditingId(order.id);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this order?')) {
            try {
                await api.delete(`/allorders/${id}`);
                fetchOrders();
            } catch (error) {
                console.error('Error deleting order:', error);
            }
        }
    };

    const resetForm = () => {
        setFormData({
            userId: '',
            storeId: '',
            orderDate: '',
            shipDate: '',
            totalAmount: '',
            shippingCost: '',
            discountAmount: '',
            finalSaleAmount: '',
            status: '',
            deliveryMethod: '',
            shippingAddress: '',
            creditCardNumber: '',
            orderItems: []
        });
        setEditingId(null);
    };


    return (
        <div>
            <h2 className="text-2xl font-bold mb-4">Order Management</h2>
            <form onSubmit={handleSubmit} className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-8">
                <div className="mb-4">
                    <label htmlFor="userId" className="block text-gray-700 text-sm font-bold mb-2">
                        User
                    </label>
                    <select
                        id="userId"
                        name="userId"
                        value={formData.userId}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    >
                        <option value="">Select User</option>
                        {users.map(user => (
                            <option key={user.id} value={user.id}>{user.name}</option>
                        ))}
                    </select>
                </div>

                <div className="mb-4">
                    <label htmlFor="storeId" className="block text-gray-700 text-sm font-bold mb-2">
                        Store
                    </label>
                    <select
                        id="storeId"
                        name="storeId"
                        value={formData.storeId}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    >
                        <option value="">Select Store</option>
                        {stores.map(store => (
                            <option key={store.id} value={store.id}>{store.name}</option>
                        ))}
                    </select>
                </div>

                <div className="mb-4">
                    <label htmlFor="orderDate" className="block text-gray-700 text-sm font-bold mb-2">
                        Order Date
                    </label>
                    <input
                        type="date"
                        id="orderDate"
                        name="orderDate"
                        value={formData.orderDate}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="mb-4">
                    <label htmlFor="shipDate" className="block text-gray-700 text-sm font-bold mb-2">
                        Ship Date
                    </label>
                    <input
                        type="date"
                        id="shipDate"
                        name="shipDate"
                        value={formData.shipDate}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="mb-4">
                    <label htmlFor="totalAmount" className="block text-gray-700 text-sm font-bold mb-2">
                        Total Amount
                    </label>
                    <input
                        type="number"
                        id="totalAmount"
                        name="totalAmount"
                        value={formData.totalAmount}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="mb-4">
                    <label htmlFor="shippingCost" className="block text-gray-700 text-sm font-bold mb-2">
                        Shipping Cost
                    </label>
                    <input
                        type="number"
                        id="shippingCost"
                        name="shippingCost"
                        value={formData.shippingCost}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="mb-4">
                    <label htmlFor="discountAmount" className="block text-gray-700 text-sm font-bold mb-2">
                        Discount Amount
                    </label>
                    <input
                        type="number"
                        id="discountAmount"
                        name="discountAmount"
                        value={formData.discountAmount}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="mb-4">
                    <label htmlFor="finalSaleAmount" className="block text-gray-700 text-sm font-bold mb-2">
                        Final Sale Amount
                    </label>
                    <input
                        type="number"
                        id="finalSaleAmount"
                        name="finalSaleAmount"
                        value={formData.finalSaleAmount}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="mb-4">
                    <label htmlFor="status" className="block text-gray-700 text-sm font-bold mb-2">
                        Status
                    </label>
                    <select
                        id="status"
                        name="status"
                        value={formData.status}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    >
                        <option value="">Select Status</option>
                        <option value="PENDING">Pending</option>
                        <option value="SHIPPED">Shipped</option>
                        <option value="DELIVERED">Delivered</option>
                        <option value="CANCELLED">Cancelled</option>
                    </select>
                </div>

                <div className="mb-4">
                    <label htmlFor="deliveryMethod" className="block text-gray-700 text-sm font-bold mb-2">
                        Delivery Method
                    </label>
                    <select
                        id="deliveryMethod"
                        name="deliveryMethod"
                        value={formData.deliveryMethod}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    >
                        <option value="">Select Delivery Method</option>
                        <option value="HOME_DELIVERY">Home Delivery</option>
                        <option value="STORE_PICKUP">Store Pickup</option>
                    </select>
                </div>

                <div className="mb-4">
                    <label htmlFor="shippingAddress" className="block text-gray-700 text-sm font-bold mb-2">
                        Shipping Address
                    </label>
                    <input
                        type="text"
                        id="shippingAddress"
                        name="shippingAddress"
                        value={formData.shippingAddress}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="mb-4">
                    <label htmlFor="creditCardNumber" className="block text-gray-700 text-sm font-bold mb-2">
                        Credit Card Number
                    </label>
                    <input
                        type="text"
                        id="creditCardNumber"
                        name="creditCardNumber"
                        value={formData.creditCardNumber}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="mb-4">
                    <h3 className="text-lg font-bold mb-2">Order Items</h3>
                    {formData.orderItems.map((item, index) => (
                        <div key={index} className="flex mb-2">
                            <select
                                value={item.productId}
                                onChange={(e) => handleOrderItemChange(index, 'productId', e.target.value)}
                                className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline mr-2"
                                required
                            >
                                <option value="">Select Product</option>
                                {products.map(product => (
                                    <option key={product.id} value={product.id}>{product.name}</option>
                                ))}
                            </select>
                            <input
                                type="number"
                                value={item.quantity}
                                onChange={(e) => handleOrderItemChange(index, 'quantity', e.target.value)}
                                placeholder="Quantity"
                                className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline mr-2"
                                required
                            />
                            <input
                                type="number"
                                value={item.price}
                                onChange={(e) => handleOrderItemChange(index, 'price', e.target.value)}
                                placeholder="Price"
                                className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline mr-2"
                                required
                            />
                            <button
                                type="button"
                                onClick={() => removeOrderItem(index)}
                                className="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                            >
                                Remove
                            </button>
                        </div>
                    ))}
                    <button
                        type="button"
                        onClick={addOrderItem}
                        className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                    >
                        Add Item
                    </button>
                </div>

                <div className="flex items-center justify-between">
                    <button
                        type="submit"
                        className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                    >
                        {editingId ? 'Update Order' : 'Add Order'}
                    </button>
                </div>
            </form>

            <div>
                {orders.map(order => (
                    <div key={order.id} className="border p-4 mb-4 rounded">
                        <h3 className="font-bold">Order #{order.id}</h3>
                        <p>Confirmation Number: {`ORD-${order.id.toString().padStart(6, '0')}`}</p>
                        <p>User: {users.find(u => u.id === order.userId)?.name}</p>
                        <p>Store: {stores.find(s => s.id === order.storeId)?.name}</p>
                        <p>Order Date: {new Date(order.orderDate).toLocaleDateString()}</p>
                        <p>Ship Date: {new Date(order.shipDate).toLocaleDateString()}</p>
                        <p>Total Amount: ${order.totalAmount.toFixed(2)}</p>
                        <p>Shipping Cost: ${order.shippingCost.toFixed(2)}</p>
                        <p>Discount Amount: ${order.discountAmount.toFixed(2)}</p>
                        <p>Final Sale Amount: ${order.finalSaleAmount.toFixed(2)}</p>
                        <p>Status: {order.status}</p>
                        <p>Delivery Method: {order.deliveryMethod}</p>
                        <p>Shipping Address: {order.shippingAddress}</p>
                        <p>Credit Card: **** **** **** {order.creditCardNumber.slice(-4)}</p>
                        <h4 className="font-semibold mt-2">Order Items:</h4>
                        <ul>
                            {order.orderItems.map((item, index) => (
                                <li key={index}>
                                    {products.find(p => p.id === item.productId)?.name} -
                                    Quantity: {item.quantity},
                                    Price: ${item.price.toFixed(2)}
                                </li>
                            ))}
                        </ul>
                        <div className="mt-4">
                            <button
                                onClick={() => handleEdit(order)}
                                className="bg-yellow-500 hover:bg-yellow-700 text-white font-bold py-2 px-4 rounded mr-2"
                            >
                                Edit
                            </button>
                            <button
                                onClick={() => handleDelete(order.id)}
                                className="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded"
                            >
                                Delete
                            </button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default OrderManagement;