import React, { useState, useEffect } from 'react';
import api from '../../services/api';

function WarrantyManagement() {
    const [warranties, setWarranties] = useState([]);
    const [formData, setFormData] = useState({
        productId: '',
        duration: '',
        description: '',
        price: ''
    });
    const [editingId, setEditingId] = useState(null);
    const [products, setProducts] = useState([]);

    useEffect(() => {
        fetchWarranties();
        fetchProducts();
    }, []);

    const fetchWarranties = async () => {
        try {
            const response = await api.get('/warranties');
            setWarranties(response.data);
        } catch (error) {
            console.error('Error fetching warranties:', error);
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
                await api.put(`/warranties/${editingId}`, formData);
            } else {
                await api.post('/warranties', formData);
            }
            fetchWarranties();
            setFormData({ productId: '', duration: '', description: '', price: '' });
            setEditingId(null);
        } catch (error) {
            console.error('Error saving warranty:', error);
        }
    };

    const handleEdit = (warranty) => {
        setFormData({
            productId: warranty.productId,
            duration: warranty.duration,
            description: warranty.description,
            price: warranty.price
        });
        setEditingId(warranty.id);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this warranty?')) {
            try {
                await api.delete(`/warranties/${id}`);
                fetchWarranties();
            } catch (error) {
                console.error('Error deleting warranty:', error);
            }
        }
    };

    return (
        <div>
            <h2 className="text-2xl font-bold mb-4">Warranty Management</h2>
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
                    <label htmlFor="duration" className="block text-gray-700 text-sm font-bold mb-2">
                        Duration (e.g., 1 year, 6 months)
                    </label>
                    <input
                        type="text"
                        id="duration"
                        name="duration"
                        value={formData.duration}
                        onChange={handleInputChange}
                        placeholder="Enter warranty duration"
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="mb-4">
                    <label htmlFor="description" className="block text-gray-700 text-sm font-bold mb-2">
                        Warranty Description
                    </label>
                    <textarea
                        id="description"
                        name="description"
                        value={formData.description}
                        onChange={handleInputChange}
                        placeholder="Enter warranty description"
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    ></textarea>
                </div>

                <div className="mb-4">
                    <label htmlFor="price" className="block text-gray-700 text-sm font-bold mb-2">
                        Warranty Price
                    </label>
                    <input
                        type="number"
                        id="price"
                        name="price"
                        value={formData.price}
                        onChange={handleInputChange}
                        placeholder="Enter price"
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="flex items-center justify-between">
                    <button
                        type="submit"
                        className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                    >
                        {editingId ? 'Update Warranty' : 'Add Warranty'}
                    </button>
                </div>
            </form>

            <div>
                {warranties.map(warranty => (
                    <div key={warranty.id} className="border p-4 mb-4 rounded">
                        <h3 className="font-bold">{products.find(p => p.id === warranty.productId)?.name}</h3>
                        <p>Duration: {warranty.duration}</p>
                        <p>Description: {warranty.description}</p>
                        <p>Price: ${warranty.price}</p>
                        <button
                            onClick={() => handleEdit(warranty)}
                            className="bg-yellow-500 text-white px-2 py-1 rounded mr-2"
                        >
                            Edit
                        </button>
                        <button
                            onClick={() => handleDelete(warranty.id)}
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

export default WarrantyManagement;