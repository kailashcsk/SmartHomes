import axios from 'axios';

const API_URL = 'http://localhost:8080/smarthomes-backend/api';

const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

export const login = async (email, password) => {
    const response = await api.post('/login', { email, password });
    if (response.data.token) {
        localStorage.setItem('user', JSON.stringify(response.data));
    }
    return response.data;
};

export const signup = async (name, email, password, role) => {
    return await api.post('/register', { name, email, password, role });
};

export const logout = () => {
    localStorage.removeItem('user');
};

export const getCurrentUser = () => {
    return JSON.parse(localStorage.getItem('user'));
};

export const getAllProducts = async () => {
    const response = await api.get('/products');
    return response.data;
};

export const getCategoryById = async (categoryId) => {
    const response = await api.get(`/categories/${categoryId}`);
    return response.data;
};

export const getProductDetails = async (productId) => {
    const response = await api.get(`/products/${productId}`);
    return response.data;
};

export const getProductDiscounts = async (productId) => {
    const response = await api.get(`/products/${productId}/discounts`);
    return response.data;
};

export const getProductWarranty = async (productId) => {
    const response = await api.get(`/products/${productId}/warranty`);
    return response.data;
};

export const getProductAccessories = async (productId) => {
    const response = await api.get(`/products/${productId}/accessories`);
    return response.data;
};

// New trending data endpoints
export const getTrendingProducts = async () => {
    const response = await api.get('/analytics/trending-products');
    return response.data;
};

export const getTopZipcodes = async () => {
    const response = await api.get('/analytics/top-zipcodes');
    return response.data;
};

export const getBestSellingProducts = async () => {
    const response = await api.get('/analytics/best-selling-products');
    return response.data;
};

export const getInventoryCounts = async () => {
    const response = await api.get('/inventory/inventory-counts');
    return response.data;
};

export const getProductsOnSale = async () => {
    const response = await api.get('/inventory/on-sale');
    return response.data;
};

export const getProductsWithRebates = async () => {
    const response = await api.get('/inventory/with-rebates');
    return response.data;
};

export const getInventory = async () => {
    const response = await api.get('/inventory');
    return response.data;
};

export const addInventory = async (inventoryData) => {
    const response = await api.post('/inventory', inventoryData);
    return response.data;
};

export const updateInventory = async (inventoryData) => {
    const response = await api.put('/inventory', inventoryData);
    return response.data;
};

export const deleteInventory = async (id) => {
    const response = await api.delete(`/inventory/${id}`);
    return response.data;
};

// New sales report endpoints
export const getProductSales = async () => {
    const response = await api.get('/sales-report/product-sales');
    return response.data;
};

export const getDailySales = async () => {
    const response = await api.get('/sales-report/daily-sales');
    return response.data;
};

export const getAutocompleteSuggestions = async (term) => {
    const response = await api.get(`/autocomplete?term=${term}`);
    return response.data;
};

export const getDeliveredOrders = async () => {
    const response = await api.get('/customer-service?delivered-orders=true');
    return response.data;
};

export const createTicket = async (formData) => {
    const response = await api.post('/customer-service', formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
    return response.data;
};

export const getTickets = async () => {
    const response = await api.get('/customer-service');
    return response.data;
};

export const getTicketStatus = async (ticketNumber) => {
    const response = await api.get(`/customer-service/${ticketNumber}`);
    return response.data;
};

// Add an interceptor to include the token in all requests
api.interceptors.request.use(
    (config) => {
        const user = getCurrentUser();
        if (user && user.token) {
            config.headers['Authorization'] = 'Bearer ' + user.token;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default api;