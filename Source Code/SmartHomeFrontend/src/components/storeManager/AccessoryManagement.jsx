import React, { useState, useEffect } from 'react';
import api from '../../services/api';

function AccessoryManagement() {
    const [accessories, setAccessories] = useState([]);
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        price: '',
        productId: ''
    });
    const [editingId, setEditingId] = useState(null);
    const [products, setProducts] = useState([]);

    useEffect(() => {
        fetchAccessories();
        fetchProducts();
    }, []);

    const fetchAccessories = async () => {
        try {
            const response = await api.get('/accessories');
            setAccessories(response.data);
        } catch (error) {
            console.error('Error fetching accessories:', error);
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
                await api.put(`/accessories/${editingId}`, formData);
            } else {
                await api.post('/accessories', formData);
            }
            fetchAccessories();
            setFormData({ name: '', description: '', price: '', productId: '' });
            setEditingId(null);
        } catch (error) {
            console.error('Error saving accessory:', error);
        }
    };

    const handleEdit = (accessory) => {
        setFormData({
            name: accessory.name,
            description: accessory.description,
            price: accessory.price,
            productId: accessory.productId
        });
        setEditingId(accessory.id);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this accessory?')) {
            try {
                await api.delete(`/accessories/${id}`);
                fetchAccessories();
            } catch (error) {
                console.error('Error deleting accessory:', error);
            }
        }
    };

    return (
        <div>
            <h2 className="text-2xl font-bold mb-4">Accessory Management</h2>
            <form onSubmit={handleSubmit} className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-8">
                <div className="mb-4">
                    <label htmlFor="name" className="block text-gray-700 text-sm font-bold mb-2">
                        Accessory Name
                    </label>
                    <input
                        type="text"
                        id="name"
                        name="name"
                        value={formData.name}
                        onChange={handleInputChange}
                        placeholder="Enter accessory name"
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="mb-4">
                    <label htmlFor="description" className="block text-gray-700 text-sm font-bold mb-2">
                        Description
                    </label>
                    <textarea
                        id="description"
                        name="description"
                        value={formData.description}
                        onChange={handleInputChange}
                        placeholder="Enter accessory description"
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="mb-4">
                    <label htmlFor="price" className="block text-gray-700 text-sm font-bold mb-2">
                        Price ($)
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

                <div className="mb-4">
                    <label htmlFor="productId" className="block text-gray-700 text-sm font-bold mb-2">
                        Product
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

                <div className="flex items-center justify-between">
                    <button
                        type="submit"
                        className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                    >
                        {editingId ? 'Update Accessory' : 'Add Accessory'}
                    </button>
                </div>
            </form>

            <div>
                {accessories.map(accessory => (
                    <div key={accessory.id} className="border p-4 mb-4 rounded">
                        <h3 className="font-bold">{accessory.name}</h3>
                        <p>{accessory.description}</p>
                        <p>Price: ${accessory.price}</p>
                        <p>Product: {products.find(p => p.id === accessory.productId)?.name}</p>
                        <button
                            onClick={() => handleEdit(accessory)}
                            className="bg-yellow-500 text-white px-2 py-1 rounded mr-2"
                        >
                            Edit
                        </button>
                        <button
                            onClick={() => handleDelete(accessory.id)}
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

export default AccessoryManagement;
