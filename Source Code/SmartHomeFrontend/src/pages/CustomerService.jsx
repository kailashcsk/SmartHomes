import React, { useState, useEffect } from 'react';
import { getDeliveredOrders, createTicket, getTickets, getTicketStatus } from '../services/api';

function CustomerService() {
    const [activeTab, setActiveTab] = useState('create');
    const [deliveredOrders, setDeliveredOrders] = useState([]);
    const [selectedOrder, setSelectedOrder] = useState(null);
    const [description, setDescription] = useState('');
    const [image, setImage] = useState(null);
    const [imagePreview, setImagePreview] = useState(null);
    const [tickets, setTickets] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [successMessage, setSuccessMessage] = useState('');
    const [ticketNumber, setTicketNumber] = useState('');
    const [lookupTicket, setLookupTicket] = useState(null);
    const [lookupError, setLookupError] = useState(null);
    const [showLookupForm, setShowLookupForm] = useState(true);
    const [aiProcessing, setAiProcessing] = useState(false);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [ordersData, ticketsData] = await Promise.all([
                    getDeliveredOrders(),
                    getTickets()
                ]);
                setDeliveredOrders(ordersData);
                setTickets(ticketsData);
            } catch (err) {
                setError('Failed to fetch data');
            }
        };
        fetchData();
    }, []);

    const handleImageChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            // Basic validation
            if (!file.type.startsWith('image/')) {
                setError('Please upload an image file');
                return;
            }
            if (file.size > 10 * 1024 * 1024) { // 10MB limit
                setError('Image size should be less than 10MB');
                return;
            }

            setImage(file);
            setImagePreview(URL.createObjectURL(file));
            setError(null);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!selectedOrder || !description || !image) {
            setError('Please fill all required fields');
            return;
        }

        setLoading(true);
        setError(null);
        setAiProcessing(true); // Start AI processing animation

        try {
            const formData = new FormData();
            formData.append('orderId', selectedOrder);
            formData.append('description', description);
            formData.append('image', image);

            const response = await createTicket(formData);
            setSuccessMessage(`Ticket created successfully! Ticket number: ${response.ticketNumber}`);

            // Refresh tickets list
            const updatedTickets = await getTickets();
            setTickets(updatedTickets);

            // Reset form
            setSelectedOrder(null);
            setDescription('');
            setImage(null);
            setImagePreview(null);
        } catch (err) {
            setError('Failed to create ticket');
        } finally {
            setLoading(false);
            setAiProcessing(false);
        }
    };

    const handleTicketLookup = async (e) => {
        e.preventDefault();
        if (!ticketNumber.trim()) {
            setLookupError('Please enter a ticket number');
            return;
        }

        setLookupError(null);
        setLookupTicket(null);
        setLoading(true);

        try {
            const ticket = await getTicketStatus(ticketNumber.trim());
            setLookupTicket(ticket);
            setShowLookupForm(false);
        } catch (err) {
            setLookupError('Failed to find ticket or you do not have permission to view this ticket');
        } finally {
            setLoading(false);
        }
    };

    const resetLookupForm = () => {
        setLookupTicket(null);
        setTicketNumber('');
        setLookupError(null);
        setShowLookupForm(true);
    };

    const AIProcessingAnimation = () => (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white p-8 rounded-lg shadow-xl max-w-md w-full">
                <div className="text-center">
                    <div className="mb-4">
                        <svg className="animate-spin h-12 w-12 text-blue-600 mx-auto" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                    </div>
                    <div className="space-y-3">
                        <h3 className="text-lg font-semibold text-gray-900">AI Analysis in Progress</h3>
                        <div className="relative h-2 bg-gray-200 rounded-full overflow-hidden">
                            <div className="absolute inset-0 bg-blue-600 animate-progress"></div>
                        </div>
                        <p className="text-sm text-gray-500">
                            Our AI is analyzing your image and determining the best resolution...
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );

    const TicketDetails = ({ ticket }) => (
        <div className="border p-4 rounded shadow-sm hover:shadow-md transition-shadow duration-200">
            <div className="flex justify-between">
                <div>
                    <h3 className="font-bold">Ticket #{ticket.ticketNumber}</h3>
                    <p className="text-gray-600">{ticket.description}</p>
                </div>
                <div className="text-right">
                    <p className={`font-bold ${ticket.decision === 'REFUND_ORDER' ? 'text-red-600' :
                        ticket.decision === 'REPLACE_ORDER' ? 'text-green-600' :
                            'text-yellow-600'
                        }`}>
                        {ticket.decision?.split('_').map(word =>
                            word.charAt(0) + word.slice(1).toLowerCase()
                        ).join(' ')}
                    </p>
                    <p className="text-gray-500">Status: {ticket.status}</p>
                </div>
            </div>
            {ticket.imagePath && (
                <div className="mt-4">
                    <img
                        src={ticket.imagePath}
                        alt="Ticket attachment"
                        className="max-w-xs rounded shadow-sm hover:shadow-md transition-shadow duration-200"
                    />
                </div>
            )}
            <div className="mt-4 text-sm text-gray-500">
                <p>Created: {new Date(ticket.createdAt).toLocaleString()}</p>
                <p>Last Updated: {new Date(ticket.updatedAt).toLocaleString()}</p>
            </div>
        </div>
    );

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="mb-8">
                <h1 className="text-3xl font-bold mb-4">Customer Service</h1>
                <div className="flex space-x-4">
                    <button
                        onClick={() => {
                            setActiveTab('create');
                            setError(null);
                            setSuccessMessage('');
                        }}
                        className={`px-4 py-2 rounded transition-colors duration-200 ${activeTab === 'create'
                            ? 'bg-blue-600 text-white'
                            : 'bg-gray-200 hover:bg-gray-300'
                            }`}
                    >
                        Open a Ticket
                    </button>
                    <button
                        onClick={() => {
                            setActiveTab('status');
                            setLookupError(null);
                            resetLookupForm();
                        }}
                        className={`px-4 py-2 rounded transition-colors duration-200 ${activeTab === 'status'
                            ? 'bg-blue-600 text-white'
                            : 'bg-gray-200 hover:bg-gray-300'
                            }`}
                    >
                        Ticket Status
                    </button>
                </div>
            </div>

            {activeTab === 'create' ? (
                <div className="bg-white p-6 rounded-lg shadow-md">
                    <h2 className="text-xl font-bold mb-4">Create New Ticket</h2>
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="block mb-2 font-medium">Select Order:</label>
                            <select
                                value={selectedOrder || ''}
                                onChange={(e) => setSelectedOrder(e.target.value)}
                                className="w-full p-2 border rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                required
                            >
                                <option value="">Select an order</option>
                                {deliveredOrders.map((order) => (
                                    <option key={order.orderId} value={order.orderId}>
                                        Order #{order.orderId} - {order.items.map(item =>
                                            item.productName
                                        ).join(', ')}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label className="block mb-2 font-medium">Description:</label>
                            <textarea
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                className="w-full p-2 border rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                rows="4"
                                placeholder="Please describe the issue with your order..."
                                required
                            />
                        </div>

                        <div className="space-y-4">
                            <div>
                                <label className="block mb-2 font-medium">Upload Image:</label>
                                <input
                                    type="file"
                                    accept="image/*"
                                    onChange={handleImageChange}
                                    className="w-full"
                                    required
                                />
                            </div>

                            {imagePreview && (
                                <div className="mt-4">
                                    <p className="text-sm text-gray-600 mb-2">Image Preview:</p>
                                    <div className="relative">
                                        <img
                                            src={imagePreview}
                                            alt="Preview"
                                            className="max-w-xs rounded-lg shadow-md"
                                        />
                                        <button
                                            type="button"
                                            onClick={() => {
                                                setImage(null);
                                                setImagePreview(null);
                                            }}
                                            className="absolute top-2 right-2 bg-red-500 text-white p-1 rounded-full hover:bg-red-600 transition-colors duration-200"
                                        >
                                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                                            </svg>
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>

                        {error && (
                            <div className="text-red-600 bg-red-50 p-3 rounded border border-red-200">
                                {error}
                            </div>
                        )}

                        {successMessage && (
                            <div className="text-green-600 bg-green-50 p-3 rounded border border-green-200">
                                {successMessage}
                            </div>
                        )}

                        <button
                            type="submit"
                            disabled={loading}
                            className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700 
                                     disabled:bg-blue-300 transition-colors duration-200"
                        >
                            {loading ? 'Creating...' : 'Submit Ticket'}
                        </button>
                    </form>
                </div>
            ) : (
                <div className="space-y-8">
                    {/* Ticket Lookup Section */}
                    <div className="bg-white p-6 rounded-lg shadow-md">
                        <h2 className="text-xl font-bold mb-4">Look Up Ticket Status</h2>

                        {(!lookupTicket || showLookupForm) ? (
                            <form onSubmit={handleTicketLookup} className="space-y-4">
                                <div>
                                    <label className="block mb-2 font-medium">Enter Ticket Number:</label>
                                    <input
                                        type="text"
                                        value={ticketNumber}
                                        onChange={(e) => setTicketNumber(e.target.value)}
                                        className="w-full p-2 border rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                        placeholder="Enter ticket number (e.g., CST-20241022-XXXXX)"
                                        required
                                    />
                                </div>

                                {lookupError && (
                                    <div className="text-red-600 bg-red-50 p-3 rounded border border-red-200">
                                        {lookupError}
                                    </div>
                                )}

                                <button
                                    type="submit"
                                    disabled={loading}
                                    className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700 
                                             disabled:bg-blue-300 transition-colors duration-200"
                                >
                                    {loading ? 'Looking up...' : 'Check Status'}
                                </button>
                            </form>
                        ) : null}

                        {lookupTicket && (
                            <div className="mt-6">
                                <div className="flex justify-between items-center mb-4">
                                    <h3 className="text-lg font-semibold">Ticket Details:</h3>
                                    <button
                                        onClick={resetLookupForm}
                                        className="text-blue-600 hover:text-blue-800 flex items-center 
                                                 transition-colors duration-200"
                                    >
                                        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-1" viewBox="0 0 20 20" fill="currentColor">
                                            <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                                        </svg>
                                        Check Another Ticket
                                    </button>
                                </div>
                                <TicketDetails ticket={lookupTicket} />
                            </div>
                        )}
                    </div>

                    {/* All Tickets List */}
                    <div className="bg-white p-6 rounded-lg shadow-md">
                        <h2 className="text-xl font-bold mb-4">Your Recent Tickets</h2>
                        <div className="space-y-4">
                            {tickets.length === 0 ? (
                                <p className="text-gray-600">No tickets found</p>
                            ) : (
                                tickets.map((ticket) => (
                                    <TicketDetails key={ticket.ticketNumber} ticket={ticket} />
                                ))
                            )}
                        </div>
                    </div>
                </div>
            )}

            {/* AI Processing Animation Overlay */}
            {aiProcessing && <AIProcessingAnimation />}
        </div>
    );
}

export default CustomerService;