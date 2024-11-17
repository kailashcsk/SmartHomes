import React, { useState, useEffect } from 'react';
import api from '../../services/api';

function StoreManagement() {
    const [stores, setStores] = useState([]);
    const [formData, setFormData] = useState({
        name: '',
        street: '',
        city: '',
        state: '',
        zipCode: ''
    });
    const [editingId, setEditingId] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchStores();
    }, []);

    const fetchStores = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const response = await api.get('/stores');
            setStores(response.data);
        } catch (err) {
            setError('Error fetching stores');
            console.error('Error fetching stores:', err);
        } finally {
            setIsLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setError(null);
        try {
            if (editingId) {
                await api.put(`/stores/${editingId}`, formData);
            } else {
                await api.post('/stores', formData);
            }
            fetchStores();
            resetForm();
        } catch (err) {
            setError('Error saving store');
            console.error('Error saving store:', err);
        } finally {
            setIsLoading(false);
        }
    };

    const handleEdit = (store) => {
        setFormData(store);
        setEditingId(store.id);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this store?')) {
            setIsLoading(true);
            setError(null);
            try {
                await api.delete(`/stores/${id}`);
                fetchStores();
            } catch (err) {
                setError('Error deleting store');
                console.error('Error deleting store:', err);
            } finally {
                setIsLoading(false);
            }
        }
    };

    const resetForm = () => {
        setFormData({
            name: '',
            street: '',
            city: '',
            state: '',
            zipCode: ''
        });
        setEditingId(null);
    };

    return (
        <div className="container mx-auto">
            <h2 className="text-2xl font-bold mb-4">Store Management</h2>

            {error && <p className="text-red-500 mb-4">{error}</p>}
            {isLoading && <p className="text-gray-500 mb-4">Loading...</p>}

            <form onSubmit={handleSubmit} className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-8">
                <div className="mb-4">
                    <label htmlFor="name" className="block text-gray-700 text-sm font-bold mb-2">Store Name</label>
                    <input
                        type="text"
                        name="name"
                        value={formData.name}
                        onChange={handleInputChange}
                        placeholder="Enter store name"
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>
                <div className="mb-4">
                    <label htmlFor="street" className="block text-gray-700 text-sm font-bold mb-2">Street</label>
                    <input
                        type="text"
                        name="street"
                        value={formData.street}
                        onChange={handleInputChange}
                        placeholder="Enter street"
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    <div>
                        <label htmlFor="city" className="block text-gray-700 text-sm font-bold mb-2">City</label>
                        <input
                            type="text"
                            name="city"
                            value={formData.city}
                            onChange={handleInputChange}
                            placeholder="Enter city"
                            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                            required
                        />
                    </div>
                    <div>
                        <label htmlFor="state" className="block text-gray-700 text-sm font-bold mb-2">State</label>
                        <input
                            type="text"
                            name="state"
                            value={formData.state}
                            onChange={handleInputChange}
                            placeholder="Enter state"
                            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                            required
                        />
                    </div>
                </div>
                <div className="mb-4">
                    <label htmlFor="zipCode" className="block text-gray-700 text-sm font-bold mb-2">Zip Code</label>
                    <input
                        type="text"
                        name="zipCode"
                        value={formData.zipCode}
                        onChange={handleInputChange}
                        placeholder="Enter zip code"
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>
                <div className="flex items-center justify-between">
                    <button
                        type="submit"
                        className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                    >
                        {editingId ? 'Update Store' : 'Add Store'}
                    </button>
                </div>
            </form>

            <div>
                {stores.length === 0 && !isLoading && <p className="text-gray-500">No stores available.</p>}
                {stores.map(store => (
                    <div key={store.id} className="border p-4 mb-4 rounded shadow-md">
                        <h3 className="font-bold text-lg mb-2">{store.name}</h3>
                        <p className="mb-2">{store.street}, {store.city}, {store.state} {store.zipCode}</p>
                        <div className="space-x-2">
                            <button
                                onClick={() => handleEdit(store)}
                                className="bg-yellow-500 text-white px-3 py-1 rounded"
                            >
                                Edit
                            </button>
                            <button
                                onClick={() => handleDelete(store.id)}
                                className="bg-red-500 text-white px-3 py-1 rounded"
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

export default StoreManagement;
