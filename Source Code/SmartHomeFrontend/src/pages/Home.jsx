import React, { useState, useEffect, useMemo } from 'react';
import { getAllProducts, getCurrentUser, getTrendingProducts, getTopZipcodes, getBestSellingProducts } from '../services/api';
import { useNavigate } from 'react-router-dom';
import SearchBar from '../components/common/SearchBar';

function Home() {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [selectedCategory, setSelectedCategory] = useState('all');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [trendingProducts, setTrendingProducts] = useState([]);
    const [topZipcodes, setTopZipcodes] = useState([]);
    const [bestSellingProducts, setBestSellingProducts] = useState([]);
    const [showTrending, setShowTrending] = useState(false);
    const navigate = useNavigate();

    // Fixed categories
    const fixedCategories = [
        { id: 'all', name: 'All' },
        { id: 1, name: 'Smart Doorbells' },
        { id: 2, name: 'Smart Doorlocks' },
        { id: 3, name: 'Smart Speakers' },
        { id: 4, name: 'Smart Lightings' },
        { id: 5, name: 'Smart Thermostats' },
        { id: 'trending', name: 'Trending', icon: '📈' }
    ];

    useEffect(() => {
        const fetchData = async () => {
            try {
                const user = getCurrentUser();
                if (!user) {
                    navigate('/login');
                    return;
                }
                const productsData = await getAllProducts();
                setProducts(productsData);
                setCategories(fixedCategories);
                setLoading(false);
            } catch (error) {
                console.error('Error fetching data:', error);
                setError('Failed to load products. Please try again later.');
                setLoading(false);
            }
        };
        fetchData();
    }, [navigate]);

    useEffect(() => {
        const fetchTrendingData = async () => {
            if (showTrending) {
                try {
                    const [trendingData, zipcodesData, bestSellingData] = await Promise.all([
                        getTrendingProducts(),
                        getTopZipcodes(),
                        getBestSellingProducts()
                    ]);
                    setTrendingProducts(trendingData);
                    setTopZipcodes(zipcodesData);
                    setBestSellingProducts(bestSellingData);
                } catch (error) {
                    console.error('Error fetching trending data:', error);
                    setError('Failed to load trending data. Please try again later.');
                }
            }
        };
        fetchTrendingData();
    }, [showTrending]);

    // Function to get 5 random products
    const getRandomProducts = (productList, count) => {
        const shuffled = [...productList].sort(() => 0.5 - Math.random());
        return shuffled.slice(0, count);
    };

    // Memoize the random products to keep them consistent between renders
    const randomProducts = useMemo(() => getRandomProducts(products, 5), [products]);

    const filteredProducts = selectedCategory === 'all'
        ? products
        : products.filter(product => product.categoryId === parseInt(selectedCategory));

    const ProductCard = ({ product }) => {
        const category = fixedCategories.find(cat => cat.id === product.categoryId);
        return (
            <div className="border p-4 rounded-lg shadow-md">
                <h3 className="text-lg font-semibold">{product.name}</h3>
                <p className="text-sm text-gray-600">{category ? category.name : 'Unknown Category'}</p>
                <p className="text-lg font-bold mt-2">${product.price.toFixed(2)}</p>
                <button onClick={() => navigate(`/products/${product.id}`)} className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition duration-300">
                    Buy Now
                </button>
            </div>
        );
    };

    const TrendingSection = () => (
        <div className="mt-8">
            <h2 className="text-2xl font-bold mb-4">Trending</h2>
            <div className="space-y-8">
                <div>
                    <h3 className="text-xl font-semibold mb-2">Top 5 Most Liked Products</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        {trendingProducts.map(product => <ProductCard key={product.id} product={product} />)}
                    </div>
                </div>
                <div>
                    <h3 className="text-xl font-semibold mb-2">Top 5 Best-Selling Products</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        {bestSellingProducts.map(product => <ProductCard key={product.id} product={product} />)}
                    </div>
                </div>
                <div>
                    <h3 className="text-xl font-semibold mb-2">Top 5 Zip Codes with Maximum Products Sold</h3>
                    <ul className="list-disc pl-5 mb-4">
                        {topZipcodes.map((zipcode, index) => (
                            <li key={index}>{zipcode.zipCode} - Total Sales: ${zipcode.totalSales.toFixed(2)}, Orders: {zipcode.totalOrders}</li>
                        ))}
                    </ul>
                    <h4 className="text-lg font-semibold mb-2">Featured Products for These Areas</h4>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        {randomProducts.map(product => <ProductCard key={product.id} product={product} />)}
                    </div>
                </div>
            </div>
        </div>
    );

    if (loading) {
        return <div className="text-center mt-8">Loading...</div>;
    }

    if (error) {
        return <div className="text-center mt-8 text-red-600">{error}</div>;
    }

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="mb-8">
                <SearchBar />  {/* Add the SearchBar component here */}
                
            
            <div className="mt-4">
                <button
                    onClick={() => navigate('/advanced-search')}
                    className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition duration-300 flex items-center gap-2"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clipRule="evenodd" />
                    </svg>
                    Advanced Search
                </button>
            </div>

            </div>
            <div className="mb-8">
                <div className="flex flex-wrap gap-2">
                    {fixedCategories.map(category => (
                        <button
                            key={category.id}
                            onClick={() => {
                                setSelectedCategory(category.id.toString());
                                setShowTrending(category.id === 'trending');
                            }}
                            className={`px-4 py-2 rounded flex items-center ${selectedCategory === category.id.toString() ? 'bg-blue-600 text-white' : 'bg-gray-200'}`}
                        >
                            {category.icon && <span className="mr-2">{category.icon}</span>}
                            {category.name}
                        </button>
                    ))}
                </div>
            </div>

            {showTrending ? (
                <TrendingSection />
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {filteredProducts.map(product => (
                        <ProductCard key={product.id} product={product} />
                    ))}
                </div>
            )}
        </div>
    );
}

export default Home;