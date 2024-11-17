import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';

function Reviews() {
    const navigate = useNavigate();

    const { productId } = useParams();
    const [product, setProduct] = useState(null);
    const [stores, setStores] = useState([]);
    const [reviews, setReviews] = useState([]);
    const [formData, setFormData] = useState({
        ProductModelName: '',
        ProductCategory: '',
        ProductPrice: '',
        StoreID: '',
        StoreZip: '',
        StoreCity: '',
        StoreState: '',
        ProductOnSale: '',
        ManufacturerName: '',
        ManufacturerRebate: '',
        UserID: '', // Replace with actual user ID
        UserAge: '',
        UserGender: '',
        UserOccupation: '',
        ReviewRating: '',
        ReviewDate: new Date().toISOString().split('T')[0],
        ReviewText: ''
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [productRes, storesRes, reviewsRes] = await Promise.all([
                    api.get(`/products/${productId}`),
                    api.get('/stores'),
                    api.get('/reviews')
                ]);

                setProduct(productRes.data);
                setStores(storesRes.data);
                setReviews(reviewsRes.data);

                // Pre-fill product-related fields
                setFormData(prev => ({
                    ...prev,
                    ProductModelName: productRes.data.name,
                    ProductCategory: productRes.data.category,
                    ProductPrice: productRes.data.price,
                    ProductOnSale: productRes.data.onSale,
                    ManufacturerName: productRes.data.manufacturerName,
                    ManufacturerRebate: productRes.data.hasRebate
                }));
                console.log('Product:', productRes.data.category);
                setLoading(false);
            } catch (err) {
                console.error('Error fetching data:', err);
                setError('Failed to load data. Please try again.');
                setLoading(false);
            }
        };

        fetchData();
    }, [productId]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleStoreChange = (e) => {
        const selectedStore = stores.find(store => store.name === e.target.value);
        if (selectedStore) {
            setFormData(prev => ({
                ...prev,
                StoreID: selectedStore.name,
                StoreZip: selectedStore.zipCode,
                StoreCity: selectedStore.city,
                StoreState: selectedStore.state
            }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await api.post('/reviews', formData);
            setReviews([...reviews, response.data]);
            setFormData(prev => ({
                ...prev,
                UserAge: '',
                UserGender: '',
                UserOccupation: '',
                ReviewRating: '',
                ReviewText: '',
                ReviewDate: new Date().toISOString().split('T')[0]
            }));

            // Redirect to the product details page
            navigate(`/products/${productId}`);
        } catch (err) {
            console.error('Error submitting review:', err);
            setError('Failed to submit review. Please try again.');
        }
    };

    const handleUpdateReview = async (reviewId) => {
        try {
            const updatedReview = reviews.find(review => review._id === reviewId);
            if (updatedReview) {
                await api.put(`/reviews/${reviewId}?userId=${formData.UserID}`, updatedReview);
                const updatedReviews = reviews.map(review =>
                    review._id === reviewId ? { ...review, ...updatedReview } : review
                );
                setReviews(updatedReviews);
            }
        } catch (err) {
            console.error('Error updating review:', err);
            setError('Failed to update review. Please try again.');
        }
    };

    const handleDeleteReview = async (reviewId) => {
        try {
            await api.delete(`/reviews/${reviewId}?userId=${formData.UserID}`);
            setReviews(reviews.filter(review => review._id !== reviewId));
        } catch (err) {
            console.error('Error deleting review:', err);
            setError('Failed to delete review. Please try again.');
        }
    };

    if (loading) return <div className="text-center mt-8">Loading...</div>;
    if (error) return <div className="text-center mt-8 text-red-600">{error}</div>;
    if (!product) return <div className="text-center mt-8">Product not found.</div>;

    return (
        <div className="container mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold mb-8">Reviews for {product.name}</h1>

            <form onSubmit={handleSubmit} className="space-y-4 mb-8">
                <div>
                    <label className="block mb-1">Product Model Name</label>
                    <input type="text" name="ProductModelName" value={formData.ProductModelName} readOnly className="w-full px-3 py-2 border rounded bg-gray-100" />
                </div>
                <div>
                    <label className="block mb-1">Product Category</label>
                    <input type="text" name="ProductCategory" value={formData.ProductCategory} readOnly className="w-full px-3 py-2 border rounded bg-gray-100" />
                </div>
                <div>
                    <label className="block mb-1">Product Price</label>
                    <input type="text" name="ProductPrice" value={`$${formData.ProductPrice}`} readOnly className="w-full px-3 py-2 border rounded bg-gray-100" />
                </div>
                <div>
                    <label className="block mb-1">Store</label>
                    <select name="StoreID" value={formData.StoreID} onChange={handleStoreChange} className="w-full px-3 py-2 border rounded" required>
                        <option value="">Select a store</option>
                        {stores.map(store => (
                            <option key={store._id} value={store.name}>{store.name}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <label className="block mb-1">Store Zip</label>
                    <input type="text" name="StoreZip" value={formData.StoreZip} readOnly className="w-full px-3 py-2 border rounded bg-gray-100" />
                </div>
                <div>
                    <label className="block mb-1">Store City</label>
                    <input type="text" name="StoreCity" value={formData.StoreCity} readOnly className="w-full px-3 py-2 border rounded bg-gray-100" />
                </div>
                <div>
                    <label className="block mb-1">Store State</label>
                    <input type="text" name="StoreState" value={formData.StoreState} readOnly className="w-full px-3 py-2 border rounded bg-gray-100" />
                </div>
                <div>
                    <label className="block mb-1">Product On Sale</label>
                    <input type="text" name="ProductOnSale" value={formData.ProductOnSale ? 'Yes' : 'No'} readOnly className="w-full px-3 py-2 border rounded bg-gray-100" />
                </div>
                <div>
                    <label className="block mb-1">Manufacturer Name</label>
                    <input type="text" name="ManufacturerName" value={formData.ManufacturerName} readOnly className="w-full px-3 py-2 border rounded bg-gray-100" />
                </div>
                <div>
                    <label className="block mb-1">Manufacturer Rebate</label>
                    <input type="text" name="ManufacturerRebate" value={formData.ManufacturerRebate ? 'Yes' : 'No'} readOnly className="w-full px-3 py-2 border rounded bg-gray-100" />
                </div>
                <div>
                    <label className="block mb-1">User ID</label>
                    <input type="text" name="UserID" value={formData.UserID} readOnly className="w-full px-3 py-2 border rounded bg-gray-100" />
                </div>
                <div>
                    <label className="block mb-1">User Age</label>
                    <input type="number" name="UserAge" value={formData.UserAge} onChange={handleInputChange} className="w-full px-3 py-2 border rounded" required />
                </div>
                <div>
                    <label className="block mb-1">User Gender</label>
                    <select name="UserGender" value={formData.UserGender} onChange={handleInputChange} className="w-full px-3 py-2 border rounded" required>
                        <option value="">Select gender</option>
                        <option value="Male">Male</option>
                        <option value="Female">Female</option>
                        <option value="Other">Other</option>
                    </select>
                </div>
                <div>
                    <label className="block mb-1">User Occupation</label>
                    <input type="text" name="UserOccupation" value={formData.UserOccupation} onChange={handleInputChange} className="w-full px-3 py-2 border rounded" required />
                </div>
                <div>
                    <label className="block mb-1">Review Rating</label>
                    <select name="ReviewRating" value={formData.ReviewRating} onChange={handleInputChange} className="w-full px-3 py-2 border rounded" required>
                        <option value="">Select rating</option>
                        {[1, 2, 3, 4, 5].map(rating => (
                            <option key={rating} value={rating}>{rating} Star{rating !== 1 ? 's' : ''}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <label className="block mb-1">Review Date</label>
                    <input type="date" name="ReviewDate" value={formData.ReviewDate} onChange={handleInputChange} className="w-full px-3 py-2 border rounded" required />
                </div>
                <div>
                    <label className="block mb-1">Review Text</label>
                    <textarea name="ReviewText" value={formData.ReviewText} onChange={handleInputChange} className="w-full px-3 py-2 border rounded" rows="4" required></textarea>
                </div>
                <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition duration-300">
                    Submit Review
                </button>
            </form>

            <h2 className="text-2xl font-semibold mb-4">All Reviews</h2>
            {reviews.length === 0 ? (
                <p>No reviews yet.</p>
            ) : (
                reviews.map((review, index) => (
                    <div key={`${review._id.timestamp}-${index}`} className="bg-white shadow-md rounded-lg p-6 mb-4">
                        <h3 className="text-xl font-semibold mb-2">{review.ProductModelName}</h3>
                        <p className="font-semibold">Rating: {review.ReviewRating} / 5</p>
                        <p className="mb-2">{review.ReviewText}</p>
                        <p className="text-sm text-gray-600">Reviewed by: {review.UserOccupation}, {review.UserAge} years old</p>
                        <p className="text-sm text-gray-600">Reviewed on: {new Date(review.ReviewDate).toLocaleDateString()}</p>
                        <p className="text-sm text-gray-600">Store: {review.StoreID}</p>
                        <p className="text-sm text-gray-600">Price: ${review.ProductPrice}</p>
                    </div>
                ))
            )}
        </div>
    );
}

export default Reviews;