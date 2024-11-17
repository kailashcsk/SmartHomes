// pages/Trending.jsx
import React, { useState, useEffect } from 'react';

function Trending() {
    const [trendingData, setTrendingData] = useState({
        mostLikedProducts: [],
        topZipCodes: [],
        mostSoldProducts: [],
    });

    useEffect(() => {
        // TODO: Fetch trending data from API
        // For now, we'll use dummy data
        setTrendingData({
            mostLikedProducts: [
                { id: 1, name: 'Smart Doorbell Pro', likes: 150 },
                { id: 2, name: 'EchoSphere', likes: 120 },
                { id: 3, name: 'SmartLight Bulb', likes: 100 },
                { id: 4, name: 'SecureLock 2000', likes: 90 },
                { id: 5, name: 'ThermoControl X', likes: 80 },
            ],
            topZipCodes: [
                { zipCode: '90210', sales: 500 },
                { zipCode: '10001', sales: 450 },
                { zipCode: '60601', sales: 400 },
                { zipCode: '02108', sales: 350 },
                { zipCode: '75201', sales: 300 },
            ],
            mostSoldProducts: [
                { id: 1, name: 'SmartLight Bulb', sales: 1000 },
                { id: 2, name: 'EchoSphere', sales: 950 },
                { id: 3, name: 'Smart Doorbell Pro', sales: 900 },
                { id: 4, name: 'ThermoControl X', sales: 850 },
                { id: 5, name: 'SecureLock 2000', sales: 800 },
            ],
        });
    }, []);

    return (
        <div className="py-8">
            <h1 className="text-4xl font-bold mb-8">Trending</h1>

            <section className="mb-8">
                <h2 className="text-2xl font-semibold mb-4">Top 5 Most Liked Products</h2>
                <ul className="list-disc pl-5">
                    {trendingData.mostLikedProducts.map((product) => (
                        <li key={product.id}>{product.name} - {product.likes} likes</li>
                    ))}
                </ul>
            </section>

            <section className="mb-8">
                <h2 className="text-2xl font-semibold mb-4">Top 5 Zip Codes with Maximum Products Sold</h2>
                <ul className="list-disc pl-5">
                    {trendingData.topZipCodes.map((zipCode) => (
                        <li key={zipCode.zipCode}>{zipCode.zipCode} - {zipCode.sales} sales</li>
                    ))}
                </ul>
            </section>

            <section>
                <h2 className="text-2xl font-semibold mb-4">Top 5 Most Sold Products</h2>
                <ul className="list-disc pl-5">
                    {trendingData.mostSoldProducts.map((product) => (
                        <li key={product.id}>{product.name} - {product.sales} sales</li>
                    ))}
                </ul>
            </section>
        </div>
    );
}

export default Trending;