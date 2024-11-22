import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const AdvancedSearch = () => {
    const [activeTab, setActiveTab] = useState('reviews');
    const [reviewQuery, setReviewQuery] = useState('');
    const [productQuery, setProductQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [recommendation, setRecommendation] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const handleReviewSearch = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await api.post('/search/reviews', {
                query: reviewQuery
            });
            const data = response.data;

            // Fetch product details for each review
            const reviewsWithProducts = await Promise.all(
                data.map(async (result) => {
                    try {
                        const productResponse = await api.get(`/products/${result.review.productId}`);
                        return {
                            id: result.review.id,
                            productId: result.review.productId,
                            productName: productResponse.data.name,
                            text: result.review.reviewText,
                            rating: result.review.rating,
                            similarity: result.similarity,
                            userName: result.review.userName
                        };
                    } catch (error) {
                        console.error('Error fetching product details:', error);
                        return {
                            id: result.review.id,
                            productId: result.review.productId,
                            productName: 'Product Name Not Found',
                            text: result.review.reviewText,
                            rating: result.review.rating,
                            similarity: result.similarity
                        };
                    }
                })
            );
            setSearchResults(reviewsWithProducts);
        } catch (error) {
            console.error('Error searching reviews:', error);
            setError(error.response?.data?.error || 'An error occurred while searching reviews');
        } finally {
            setLoading(false);
        }
    };

    const handleProductRecommendation = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await api.post('/search/products', {
                query: productQuery
            });
            const data = response.data;

            if (data && data.length > 0) {
                // Sort by similarity score
                const sortedResults = data.sort((a, b) => b.similarity - a.similarity);

                // Map and set all recommendations
                const recommendations = sortedResults.map(result => ({
                    id: result.product.id,
                    name: result.product.name,
                    description: result.product.description,
                    price: result.product.price,
                    similarity: result.similarity
                }));

                setRecommendation(recommendations);
            }
        } catch (error) {
            console.error('Error getting recommendation:', error);
            setError(error.response?.data?.error || 'An error occurred while getting recommendations');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold mb-6">Advanced Search</h1>

            {error && (
                <div className="mb-6 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
                    {error}
                </div>
            )}

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
                                            <div
                                                key={index}
                                                className="bg-gray-50 border rounded-lg p-4 cursor-pointer hover:bg-gray-100 transition-colors"
                                                onClick={() => navigate(`/products/${review.productId}`, {
                                                    state: { highlightReviewId: review.id }
                                                })}
                                            >
                                                <div className="flex justify-between items-start">
                                                    <h4 className="font-medium text-lg text-blue-600">{review.userName}</h4>
                                                    <span className="text-sm text-blue-600">
                                                        Match: {(review.similarity * 100).toFixed(1)}%
                                                    </span>
                                                </div>
                                                <p className="text-gray-600 mt-2">{review.text}</p>
                                                <div className="mt-2 flex justify-between items-center">
                                                    <span className="text-sm text-gray-500">
                                                        Rating: {review.rating} {'⭐'.repeat(review.rating)}
                                                    </span>
                                                    <button
                                                        className="text-sm text-blue-600 hover:underline"
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            navigate(`/products/${review.productId}`, {
                                                                state: { highlightReviewId: review.id }
                                                            });
                                                        }}
                                                    >
                                                        View Product →
                                                    </button>
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
                                <div className="mt-6 space-y-4">
                                    <h3 className="font-medium text-lg mb-4">Recommended Products:</h3>
                                    {recommendation.map((product, index) => (
                                        <div
                                            key={product.id}
                                            className={`bg-blue-50 border border-blue-200 rounded-lg p-6 ${index === 0 ? 'ring-2 ring-blue-500' : ''
                                                }`}
                                        >
                                            <div className="flex justify-between items-start">
                                                <h4 className="text-xl font-semibold">
                                                    {product.name}
                                                    {index === 0 && (
                                                        <span className="ml-2 text-sm bg-blue-600 text-white px-2 py-1 rounded">
                                                            Best Match
                                                        </span>
                                                    )}
                                                </h4>
                                                <span className="text-sm text-blue-600">
                                                    Match: {(product.similarity * 100).toFixed(1)}%
                                                </span>
                                            </div>
                                            <p className="text-gray-600 mt-2">{product.description}</p>
                                            <div className="flex justify-between items-center mt-4">
                                                <span className="font-medium text-lg">
                                                    ${product.price}
                                                </span>
                                                <button
                                                    onClick={() => navigate(`/products/${product.id}`)}
                                                    className="px-4 py-2 border border-blue-600 text-blue-600 rounded-lg hover:bg-blue-50"
                                                >
                                                    View Product
                                                </button>
                                            </div>
                                        </div>
                                    ))}
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