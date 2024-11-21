import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const AdvancedSearch = () => {
    const [activeTab, setActiveTab] = useState('reviews');
    const [reviewQuery, setReviewQuery] = useState('');
    const [productQuery, setProductQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [recommendation, setRecommendation] = useState(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleReviewSearch = async () => {
        setLoading(true);
        try {
            // Implementation: Call your backend API to search reviews
            const response = await fetch('/api/search-reviews', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ query: reviewQuery })
            });
            const data = await response.json();
            setSearchResults(data.results);
        } catch (error) {
            console.error('Error searching reviews:', error);
        }
        setLoading(false);
    };

    const handleProductRecommendation = async () => {
        setLoading(true);
        try {
            // Implementation: Call your backend API to get product recommendations
            const response = await fetch('/api/recommend-product', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ query: productQuery })
            });
            const data = await response.json();
            setRecommendation(data.recommendation);
        } catch (error) {
            console.error('Error getting recommendation:', error);
        }
        setLoading(false);
    };

    return (
        <div className="container mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold mb-6">Advanced Search</h1>

            <div className="bg-white rounded-lg shadow-md overflow-hidden">
                {/* Tabs */}
                <div className="flex border-b">
                    <button
                        className={`flex-1 py-4 text-center font-medium ${activeTab === 'reviews'
                                ? 'bg-blue-600 text-white'
                                : 'hover:bg-gray-100'
                            }`}
                        onClick={() => setActiveTab('reviews')}
                    >
                        Search Reviews
                    </button>
                    <button
                        className={`flex-1 py-4 text-center font-medium ${activeTab === 'recommendations'
                                ? 'bg-blue-600 text-white'
                                : 'hover:bg-gray-100'
                            }`}
                        onClick={() => setActiveTab('recommendations')}
                    >
                        Product Recommendations
                    </button>
                </div>

                <div className="p-6">
                    {/* Reviews Search Tab */}
                    {activeTab === 'reviews' && (
                        <div className="space-y-6">
                            <div>
                                <h2 className="text-xl font-semibold mb-2">Search Product Reviews</h2>
                                <p className="text-gray-600 mb-4">Search for reviews based on content similarity</p>

                                <div className="flex gap-4">
                                    <input
                                        type="text"
                                        placeholder="Enter search terms for reviews..."
                                        value={reviewQuery}
                                        onChange={(e) => setReviewQuery(e.target.value)}
                                        className="flex-1 px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    />
                                    <button
                                        onClick={handleReviewSearch}
                                        disabled={loading || !reviewQuery.trim()}
                                        className={`px-6 py-2 rounded-lg font-medium ${loading || !reviewQuery.trim()
                                                ? 'bg-gray-300 cursor-not-allowed'
                                                : 'bg-blue-600 text-white hover:bg-blue-700'
                                            }`}
                                    >
                                        {loading ? 'Searching...' : 'Search'}
                                    </button>
                                </div>
                            </div>

                            {/* Review Results */}
                            {searchResults.length > 0 && (
                                <div className="space-y-4">
                                    <h3 className="font-medium text-lg">Search Results:</h3>
                                    <div className="space-y-4">
                                        {searchResults.map((review, index) => (
                                            <div key={index} className="bg-gray-50 border rounded-lg p-4">
                                                <h4 className="font-medium text-lg">{review.productName}</h4>
                                                <p className="text-gray-600 mt-2">{review.text}</p>
                                                <div className="mt-2 text-sm text-gray-500">
                                                    Rating: {review.rating} ⭐
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </div>
                    )}

                    {/* Product Recommendations Tab */}
                    {activeTab === 'recommendations' && (
                        <div className="space-y-6">
                            <div>
                                <h2 className="text-xl font-semibold mb-2">Get Product Recommendations</h2>
                                <p className="text-gray-600 mb-4">Describe what you're looking for to get personalized recommendations</p>

                                <div className="flex gap-4">
                                    <input
                                        type="text"
                                        placeholder="Describe the product you're looking for..."
                                        value={productQuery}
                                        onChange={(e) => setProductQuery(e.target.value)}
                                        className="flex-1 px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    />
                                    <button
                                        onClick={handleProductRecommendation}
                                        disabled={loading || !productQuery.trim()}
                                        className={`px-6 py-2 rounded-lg font-medium ${loading || !productQuery.trim()
                                                ? 'bg-gray-300 cursor-not-allowed'
                                                : 'bg-blue-600 text-white hover:bg-blue-700'
                                            }`}
                                    >
                                        {loading ? 'Searching...' : 'Recommend'}
                                    </button>
                                </div>
                            </div>

                            {/* Recommendation Results */}
                            {recommendation && (
                                <div className="mt-6">
                                    <h3 className="font-medium text-lg mb-4">Recommended Product:</h3>
                                    <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
                                        <h4 className="text-xl font-semibold">{recommendation.name}</h4>
                                        <p className="text-gray-600 mt-2">{recommendation.description}</p>
                                        <div className="flex justify-between items-center mt-4">
                                            <span className="font-medium text-lg">
                                                ${recommendation.price}
                                            </span>
                                            <button
                                                onClick={() => navigate(`/products/${recommendation.id}`)}
                                                className="px-4 py-2 border border-blue-600 text-blue-600 rounded-lg hover:bg-blue-50"
                                            >
                                                View Product
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AdvancedSearch;