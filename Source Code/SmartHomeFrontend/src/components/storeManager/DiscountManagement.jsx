import React, { useState, useEffect } from 'react';
import api from '../../services/api';

function DiscountManagement() {
    const [discounts, setDiscounts] = useState([]);
    const [formData, setFormData] = useState({
        productId: '',
        discountType: '',
        amount: '',
        startDate: '',
        endDate: ''
    });
    const [editingId, setEditingId] = useState(null);
    const [products, setProducts] = useState([]);

    useEffect(() => {
        fetchDiscounts();
        fetchProducts();
    }, []);

    const fetchDiscounts = async () => {
        try {
            const response = await api.get('/discounts');
            setDiscounts(response.data);
        } catch (error) {
            console.error('Error fetching discounts:', error);
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

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editingId) {
                await api.put(`/discounts/${editingId}`, formData);
            } else {
                await api.post('/discounts', formData);
            }
            fetchDiscounts();
            setFormData({ productId: '', discountType: '', amount: '', startDate: '', endDate: '' });
            setEditingId(null);
        } catch (error) {
            console.error('Error saving discount:', error);
        }
    };

    const handleEdit = (discount) => {
        setFormData({
            productId: discount.productId,
            discountType: discount.discountType,
            amount: discount.amount,
            startDate: discount.startDate,
            endDate: discount.endDate
        });
        setEditingId(discount.id);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this discount?')) {
            try {
                await api.delete(`/discounts/${id}`);
                fetchDiscounts();
            } catch (error) {
                console.error('Error deleting discount:', error);
            }
        }
    };

    return (
        <div>
            <h2 className="text-2xl font-bold mb-4">Discount Management</h2>
            <form onSubmit={handleSubmit} className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-8">
                <div className="mb-4">
                    <label htmlFor="productId" className="block text-gray-700 text-sm font-bold mb-2">
                        Select Product
                    </label>
                    <select
                        id="productId"
                        name="productId"
                        value={formData.productId}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    >
                        <option value="">Select Product</option>
                        {products.map(product => (
                            <option key={product.id} value={product.id}>{product.name}</option>
                        ))}
                    </select>
                </div>

                <div className="mb-4">
                    <label htmlFor="discountType" className="block text-gray-700 text-sm font-bold mb-2">
                        Discount Type
                    </label>
                    <select
                        id="discountType"
                        name="discountType"
                        value={formData.discountType}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    >
                        <option value="">Select Discount Type</option>
                        <option value="RETAILER_SPECIAL">Retailer Discount</option>
                        <option value="MANUFACTURER_REBATE">Manufacturer Rebate</option>
                    </select>
                </div>

                <div className="mb-4">
                    <label htmlFor="amount" className="block text-gray-700 text-sm font-bold mb-2">
                        Discount Amount
                    </label>
                    <input
                        type="number"
                        id="amount"
                        name="amount"
                        value={formData.amount}
                        onChange={handleInputChange}
                        placeholder="Enter discount amount"
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="mb-4">
                    <label htmlFor="startDate" className="block text-gray-700 text-sm font-bold mb-2">
                        Start Date
                    </label>
                    <input
                        type="date"
                        id="startDate"
                        name="startDate"
                        value={formData.startDate}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="mb-4">
                    <label htmlFor="endDate" className="block text-gray-700 text-sm font-bold mb-2">
                        End Date
                    </label>
                    <input
                        type="date"
                        id="endDate"
                        name="endDate"
                        value={formData.endDate}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="flex items-center justify-between">
                    <button
                        type="submit"
                        className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                    >
                        {editingId ? 'Update Discount' : 'Add Discount'}
                    </button>
                </div>
            </form>

            <div>
                {discounts.map(discount => (
                    <div key={discount.id} className="border p-4 mb-4 rounded">
                        <h3 className="font-bold">{products.find(p => p.id === discount.productId)?.name}</h3>
                        <p>Type: {discount.discountType}</p>
                        <p>Amount: {discount.amount}</p>
                        <p>Start Date: {new Date(discount.startDate).toLocaleDateString()}</p>
                        <p>End Date: {new Date(discount.endDate).toLocaleDateString()}</p>
                        <button
                            onClick={() => handleEdit(discount)}
                            className="bg-yellow-500 text-white px-2 py-1 rounded mr-2"
                        >
                            Edit
                        </button>
                        <button
                            onClick={() => handleDelete(discount.id)}
                            className="bg-red-500 text-white px-2 py-1 rounded"
                        >
                            Delete
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default DiscountManagement;
