import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';

function ProductList() {
    const [products, setProducts] = useState([]);
    const location = useLocation();
    const category = new URLSearchParams(location.search).get('category');

    useEffect(() => {
        // TODO: Fetch products from API based on category
        // For now, we'll use dummy data
        setProducts([
            { id: 1, name: 'Smart Doorbell Pro', price: 199.99, category: 'Smart Doorbells' },
            { id: 2, name: 'SecureLock 2000', price: 149.99, category: 'Smart Doorlocks' },
            { id: 3, name: 'EchoSphere', price: 99.99, category: 'Smart Speakers' },
        ]);
    }, [category]);

    return (
        <div className="py-8">
            <h1 className="text-4xl font-bold mb-8">
                {category ? `${category}` : 'All Products'}
            </h1>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {products.map((product) => (
                    <div key={product.id} className="bg-white shadow-md rounded-lg p-6">
                        <h2 className="text-2xl font-semibold mb-2">{product.name}</h2>
                        <p className="text-gray-600 mb-4">{product.category}</p>
                        <p className="text-lg font-bold">${product.price.toFixed(2)}</p>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default ProductList;