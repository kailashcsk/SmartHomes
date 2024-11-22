import React, { useState, useEffect } from 'react';
import { getCurrentUser, getProductReviews, addReview, updateReview, deleteReview, getAllStores } from '../../services/api';

function ReviewSection({ productId, highlightedReviewId, highlightRef }) {
    const [reviews, setReviews] = useState([]);
    const [stores, setStores] = useState([]);
    const [newReview, setNewReview] = useState({
        rating: 5,
        reviewText: '',
        storeId: ''
    });
    const [editingReview, setEditingReview] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const currentUser = getCurrentUser()?.user;
    
    const fetchReviews = async () => {
        try {
            setLoading(true);
            const data = await getProductReviews(productId);
            setReviews(data || []);
            setError(null);
        } catch (err) {
            console.error('Error fetching reviews:', err);
            setError('Failed to load reviews');
        } finally {
            setLoading(false);
        }
    };

    const fetchStores = async () => {
        try {
            const storesData = await getAllStores();
            setStores(storesData);
            if (storesData.length > 0) {
                setNewReview(prev => ({ ...prev, storeId: storesData[0].id }));
            }
        } catch (err) {
            console.error('Error fetching stores:', err);
            setError('Failed to load stores');
        }
    };

    useEffect(() => {
        fetchReviews();
        fetchStores();
    }, [productId]);

    const handleSubmitReview = async (e) => {
        e.preventDefault();
        if (!newReview.storeId) {
            setError('Please select a store');
            return;
        }
        try {
            setLoading(true);
            const reviewData = {
                productId: parseInt(productId),
                storeId: parseInt(newReview.storeId),
                rating: parseInt(newReview.rating),
                reviewText: newReview.reviewText
            };
            await addReview(reviewData);
            setNewReview({ rating: 5, reviewText: '', storeId: stores[0]?.id || '' });
            await fetchReviews();
            setError(null);
        } catch (err) {
            console.error('Error submitting review:', err);
            setError('Failed to submit review');
        } finally {
            setLoading(false);
        }
    };

    const handleUpdateReview = async (e) => {
        e.preventDefault();
        if (!editingReview) return;

        try {
            setLoading(true);
            await updateReview({
                id: editingReview.id,
                productId: parseInt(productId),
                storeId: editingReview.storeId,
                rating: parseInt(editingReview.rating),
                reviewText: editingReview.reviewText
            });
            setEditingReview(null);
            await fetchReviews();
            setError(null);
        } catch (err) {
            console.error('Error updating review:', err);
            setError('Failed to update review');
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteReview = async (reviewId) => {
        if (!window.confirm('Are you sure you want to delete this review?')) return;

        try {
            setLoading(true);
            await deleteReview(reviewId);
            await fetchReviews();
            setError(null);
        } catch (err) {
            console.error('Error deleting review:', err);
            setError('Failed to delete review');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="mt-8 max-w-4xl mx-auto">
            <h2 className="text-2xl font-bold mb-6">Customer Reviews</h2>

            {error && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                    {error}
                </div>
            )}

            {loading && (
                <div className="text-center py-4">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto"></div>
                </div>
            )}

            {currentUser && !editingReview && (
                <div className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-6">
                    <h3 className="text-xl font-semibold mb-4">Write a Review</h3>
                    <form onSubmit={handleSubmitReview}>
                        <div className="mb-4">
                            <label className="block text-gray-700 text-sm font-bold mb-2">
                                Select Store
                            </label>
                            <select
                                value={newReview.storeId}
                                onChange={(e) => setNewReview({ ...newReview, storeId: e.target.value })}
                                className="shadow border rounded w-full py-2 px-3 text-gray-700"
                                required
                                disabled={loading}
                            >
                                <option value="">Select a store</option>
                                {stores.map((store) => (
                                    <option key={store.id} value={store.id}>
                                        {store.name} - {store.city}, {store.state}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="mb-4">
                            <label className="block text-gray-700 text-sm font-bold mb-2">
                                Rating
                            </label>
                            <select
                                value={newReview.rating}
                                onChange={(e) => setNewReview({ ...newReview, rating: e.target.value })}
                                className="shadow border rounded w-full py-2 px-3 text-gray-700"
                                disabled={loading}
                            >
                                {[5, 4, 3, 2, 1].map((num) => (
                                    <option key={num} value={num}>
                                        {num} {num === 1 ? 'Star' : 'Stars'}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="mb-6">
                            <label className="block text-gray-700 text-sm font-bold mb-2">
                                Review
                            </label>
                            <textarea
                                value={newReview.reviewText}
                                onChange={(e) => setNewReview({ ...newReview, reviewText: e.target.value })}
                                className="shadow border rounded w-full py-2 px-3 text-gray-700"
                                rows="4"
                                required
                                disabled={loading}
                            />
                        </div>

                        <button
                            type="submit"
                            className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                            disabled={loading}
                        >
                            Submit Review
                        </button>
                    </form>
                </div>
            )}

            {editingReview && (
                <div className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-6">
                    <h3 className="text-xl font-semibold mb-4">Edit Review</h3>
                    <form onSubmit={handleUpdateReview}>
                        <div className="mb-4">
                            <label className="block text-gray-700 text-sm font-bold mb-2">
                                Store
                            </label>
                            <select
                                value={editingReview.storeId}
                                onChange={(e) => setEditingReview({ ...editingReview, storeId: e.target.value })}
                                className="shadow border rounded w-full py-2 px-3 text-gray-700"
                                required
                                disabled={loading}
                            >
                                {stores.map((store) => (
                                    <option key={store.id} value={store.id}>
                                        {store.name} - {store.city}, {store.state}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="mb-4">
                            <label className="block text-gray-700 text-sm font-bold mb-2">
                                Rating
                            </label>
                            <select
                                value={editingReview.rating}
                                onChange={(e) => setEditingReview({ ...editingReview, rating: e.target.value })}
                                className="shadow border rounded w-full py-2 px-3 text-gray-700"
                                disabled={loading}
                            >
                                {[5, 4, 3, 2, 1].map((num) => (
                                    <option key={num} value={num}>
                                        {num} {num === 1 ? 'Star' : 'Stars'}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="mb-6">
                            <label className="block text-gray-700 text-sm font-bold mb-2">
                                Review
                            </label>
                            <textarea
                                value={editingReview.reviewText}
                                onChange={(e) => setEditingReview({ ...editingReview, reviewText: e.target.value })}
                                className="shadow border rounded w-full py-2 px-3 text-gray-700"
                                rows="4"
                                required
                                disabled={loading}
                            />
                        </div>

                        <div className="flex gap-4">
                            <button
                                type="submit"
                                className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                                disabled={loading}
                            >
                                Update Review
                            </button>
                            <button
                                type="button"
                                onClick={() => setEditingReview(null)}
                                className="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                                disabled={loading}
                            >
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            )}

            <div className="space-y-4">
                {reviews.map((review) => (
                    <div key={review.id} className="bg-white shadow-md rounded-lg p-6">
                        <div className="flex justify-between items-start mb-4">
                            <div>
                                <p className="font-semibold text-lg">{review.userName}</p>
                                <p className="text-sm text-gray-600">
                                    {stores.find(s => s.id === review.storeId)?.name || 'Store Not Found'}
                                </p>
                                <div className="text-xl text-yellow-400">
                                    {[...Array(5)].map((_, index) => (
                                        <span key={index} className={index < review.rating ? "text-yellow-400" : "text-gray-300"}>
                                            ★
                                        </span>
                                    ))}
                                </div>
                            </div>
                            <div className="text-sm text-gray-500">
                                {new Date(review.reviewDate).toLocaleDateString()}
                            </div>
                        </div>
                        <p className="text-gray-700 mb-4">{review.reviewText}</p>

                        {currentUser && (parseInt(review.userId) === parseInt(currentUser.id)) && (
                            <div className="flex gap-2 mt-4">
                                <button
                                    onClick={() => setEditingReview(review)}
                                    className="text-blue-500 hover:text-blue-700"
                                    disabled={loading}
                                >
                                    Edit
                                </button>
                                <button
                                    onClick={() => handleDeleteReview(review.id)}
                                    className="text-red-500 hover:text-red-700"
                                    disabled={loading}
                                >
                                    Delete
                                </button>
                            </div>
                        )}
                    </div>
                ))}

                {reviews.length === 0 && !loading && (
                    <p className="text-gray-500 text-center">No reviews yet. Be the first to review!</p>
                )}
            </div>
        </div>
    );
}

export default ReviewSection;