import React, { useState, useEffect } from 'react';
import { getInventory, addInventory, updateInventory, deleteInventory } from '../../services/api';
import InventoryReports from './InventoryReports';

function InventoryManagement() {
    const [inventory, setInventory] = useState([]);
    const [formData, setFormData] = useState({
        id: '',
        inventoryCount: ''
    });
    const [message, setMessage] = useState('');
    const [showReport, setShowReport] = useState(false);
    const [reportError, setReportError] = useState(null);
    const [isEditing, setIsEditing] = useState(false);

    useEffect(() => {
        fetchInventory();
    }, []);

    const fetchInventory = async () => {
        try {
            const response = await getInventory();
            setInventory(response);
        } catch (error) {
            console.error('Error fetching inventory:', error);
            setMessage('Failed to fetch inventory. Please try again.');
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');
        try {
            const payload = {
                id: parseInt(formData.id),
                inventoryCount: parseInt(formData.inventoryCount)
            };

            if (isEditing) {
                // Update inventory (PUT request)
                await updateInventory(payload);
                setMessage('Inventory updated successfully');
            } else {
                // Add inventory (POST request)
                await addInventory(payload);
                setMessage('Inventory added successfully');
            }

            fetchInventory();
            setFormData({ id: '', inventoryCount: '' });
            setIsEditing(false);
        } catch (error) {
            console.error('Error saving inventory:', error);
            setMessage('Failed to update inventory. Please try again.');
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this inventory item?')) {
            try {
                await deleteInventory(id);
                setMessage('Inventory deleted successfully');
                fetchInventory();
            } catch (error) {
                console.error('Error deleting inventory:', error);
                setMessage('Failed to delete inventory. Please try again.');
            }
        }
    };

    return (
        <div>
            <h2 className="text-2xl font-bold mb-4">Inventory Management</h2>

            {message && <p className="text-red-500 mb-4">{message}</p>}

            <button
                onClick={() => {
                    setShowReport(!showReport);
                    setReportError(null);
                }}
                className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded mb-4"
            >
                {showReport ? 'Hide Inventory Report' : 'Generate Inventory Report'}
            </button>

            {showReport && (
                <ErrorBoundary
                    fallback={<div className="text-red-500">Error loading inventory report. Please try again.</div>}
                    onError={(error) => setReportError(error)}
                >
                    <InventoryReports />
                </ErrorBoundary>
            )}
            {reportError && <p className="text-red-500 mb-4">Failed to load inventory report: {reportError.message}</p>}

            <form onSubmit={handleSubmit} className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-8">
                <div className="mb-4">
                    <label htmlFor="id" className="block text-gray-700 text-sm font-bold mb-2">
                        Select Product
                    </label>
                    <select
                        id="id"
                        name="id"
                        value={formData.id}
                        onChange={handleInputChange}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    >
                        <option value="">Select Product</option>
                        {inventory.map(item => (
                            <option key={item.id} value={item.id}>{item.name}</option>
                        ))}
                    </select>
                </div>

                <div className="mb-4">
                    <label htmlFor="inventoryCount" className="block text-gray-700 text-sm font-bold mb-2">
                        {isEditing ? 'New Quantity' : 'Quantity to Add'}
                    </label>
                    <input
                        type="number"
                        id="inventoryCount"
                        name="inventoryCount"
                        value={formData.inventoryCount}
                        onChange={handleInputChange}
                        placeholder={isEditing ? "Enter new quantity" : "Enter quantity to add"}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        required
                    />
                </div>

                <div className="flex items-center justify-between">
                    <button
                        type="submit"
                        className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                    >
                        {isEditing ? 'Update Inventory' : 'Add to Inventory'}
                    </button>
                    {isEditing && (
                        <button
                            type="button"
                            onClick={() => {
                                setIsEditing(false);
                                setFormData({ id: '', inventoryCount: '' });
                            }}
                            className="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                        >
                            Cancel Edit
                        </button>
                    )}
                </div>
            </form>

            <div>
                {inventory.map(item => (
                    <div key={item.id} className="border p-4 mb-4 rounded">
                        <h3 className="font-bold">{item.name}</h3>
                        <p>Price: ${item.price}</p>
                        <p>Quantity: {item.inventoryCount}</p>
                        <button
                            onClick={() => {
                                setFormData({
                                    id: item.id.toString(),
                                    inventoryCount: item.inventoryCount.toString()
                                });
                                setIsEditing(true);
                            }}
                            className="bg-yellow-500 text-white px-2 py-1 rounded mr-2"
                        >
                            Edit
                        </button>
                        <button
                            onClick={() => handleDelete(item.id)}
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


// Error Boundary Component
class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false };
    }

    static getDerivedStateFromError(error) {
        return { hasError: true };
    }

    componentDidCatch(error, errorInfo) {
        console.error("Caught an error:", error, errorInfo);
        this.props.onError(error);
    }

    render() {
        if (this.state.hasError) {
            return this.props.fallback;
        }

        return this.props.children;
    }
}

export default InventoryManagement;