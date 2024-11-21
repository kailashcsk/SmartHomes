import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getProductDetails, getProductDiscounts, getProductWarranty, getProductAccessories, getCategoryById } from '../services/api';
import { useCart } from '../contexts/CartContext';
import ReviewSection from '../components/common/ReviewSection';

function ProductDetails() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [product, setProduct] = useState(null);
    const [category, setCategory] = useState(null);
    const [discounts, setDiscounts] = useState([]);
    const [warranty, setWarranty] = useState([]);
    const [accessories, setAccessories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedWarranty, setSelectedWarranty] = useState(null);
    const [selectedAccessories, setSelectedAccessories] = useState([]);

    const { addToCart } = useCart();

    const handleAddToCart = () => {
        if (product) {
            const item = {
                id: product.id,
                name: product.name,
                price: parseFloat(product.price),
                type: 'product',
                warranty: selectedWarranty,
                accessories: selectedAccessories
            };
            addToCart(item);
            navigate('/cart');
        }
    };

    const handleToggleWarranty = (warrantyOption) => {
        setSelectedWarranty(prev => prev && prev.duration === warrantyOption.duration ? null : warrantyOption);
    };

    const handleToggleAccessory = (accessory) => {
        setSelectedAccessories(prev =>
            prev.some(acc => acc.id === accessory.id)
                ? prev.filter(acc => acc.id !== accessory.id)
                : [...prev, accessory]
        );
    };

    const handleAddWarranty = (warrantyOption) => {
        setSelectedWarranty(warrantyOption);
    };

    const handleAddAccessory = (accessory) => {
        setSelectedAccessories(prev => [...prev, accessory]);
    };

    const handleRemoveAccessory = (accessoryId) => {
        setSelectedAccessories(prev => prev.filter(acc => acc.id !== accessoryId));
    };


    useEffect(() => {
        const fetchProductData = async () => {
            try {
                const productData = await getProductDetails(id);
                setProduct(productData);

                const categoryData = await getCategoryById(productData.categoryId);
                setCategory(categoryData);

                const [discountsData, warrantyData, accessoriesData] = await Promise.all([
                    getProductDiscounts(id),
                    getProductWarranty(id),
                    getProductAccessories(id)
                ]);

                setDiscounts(parseDiscounts(discountsData));
                setWarranty(parseWarranty(warrantyData));
                setAccessories(accessoriesData);

                // Load saved selections from localStorage
                const savedWarranty = localStorage.getItem(`selectedWarranty_${id}`);
                if (savedWarranty) {
                    setSelectedWarranty(JSON.parse(savedWarranty));
                }

                const savedAccessories = localStorage.getItem(`selectedAccessories_${id}`);
                if (savedAccessories) {
                    setSelectedAccessories(JSON.parse(savedAccessories));
                }

                setLoading(false);
            } catch (error) {
                console.error('Error fetching product data:', error);
                setError('Failed to load product details. Please try again later.');
                setLoading(false);
            }
        };

        fetchProductData();
    }, [id]);

    useEffect(() => {
        // Save selections to localStorage whenever they change
        if (selectedWarranty) {
            localStorage.setItem(`selectedWarranty_${id}`, JSON.stringify(selectedWarranty));
        } else {
            localStorage.removeItem(`selectedWarranty_${id}`);
        }

        localStorage.setItem(`selectedAccessories_${id}`, JSON.stringify(selectedAccessories));
    }, [id, selectedWarranty, selectedAccessories]);

    const parseDiscounts = (discountStrings) => {
        return discountStrings.map(str => {
            const [type, rest] = str.split(' - ');
            const [amount, dateRange] = rest.split(' (');
            return {
                type: type === 'RETAILER_SPECIAL' ? 'Retailer Special' : 'Manufacturer Rebate',
                amount: amount.trim(),
                dateRange: dateRange.slice(0, -1) // Remove the closing parenthesis
            };
        });
    };

    const parseWarranty = (warrantyStrings) => {
        return warrantyStrings.map(str => {
            const [duration, rest] = str.split(' - ');
            const [price, description] = rest.split(': ');
            return { duration, price, description };
        });
    };

    if (loading) return <div className="text-center mt-8">Loading...</div>;
    if (error) return <div className="text-center mt-8 text-red-600">{error}</div>;
    if (!product) return <div className="text-center mt-8">Product not found.</div>;

    const DiscountBadge = ({ discount }) => (
        <div className="bg-green-100 text-green-800 text-sm font-medium mr-2 px-2.5 py-0.5 rounded">
            <p>{discount.type}: {discount.amount}</p>
            <p className="text-xs">{discount.dateRange}</p>
        </div>
    );

    const calculateTotalPrice = () => {
        let total = product ? product.price : 0;
        if (selectedWarranty) {
            total += parseFloat(selectedWarranty.price.replace('$', ''));
        }
        selectedAccessories.forEach(acc => {
            total += acc.price;
        });
        return total.toFixed(2);
    };

    const handleGoBack = () => {
        navigate('/');
    };


    return (
        <div className="py-8 max-w-4xl mx-auto">
            <button
                onClick={handleGoBack}
                className="mb-4 bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-2 px-4 rounded inline-flex items-center"
            >
                <svg className="fill-current w-4 h-4 mr-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
                    <path d="M10 8.586L2.929 1.515 1.515 2.929 8.586 10l-7.071 7.071 1.414 1.414L10 11.414l7.071 7.071 1.414-1.414L11.414 10l7.071-7.071-1.414-1.414L10 8.586z" />
                </svg>
                <span>Back to Home</span>
            </button>
            <h1 className="text-3xl font-bold mb-4">{product.name}</h1>
            <p className="text-xl mb-2">{category ? category.name : 'Loading category...'}</p>
            <p className="text-gray-600 mb-4">Manufacturer: {product.manufacturerName}</p>
            <div className="flex items-center mb-4">
                <p className="text-2xl font-bold mr-4">${calculateTotalPrice()}</p>
                <div className="flex flex-wrap">
                    {discounts.map((discount, index) => (
                        <span key={index} className="bg-green-100 text-green-800 text-sm font-medium mr-2 px-2.5 py-0.5 rounded">
                            {discount.type}: {discount.amount} ({discount.dateRange})
                        </span>
                    ))}
                </div>
            </div>
            <p className="text-gray-700 mb-8">{product.description}</p>

            <h2 className="text-2xl font-semibold mb-4">Warranty Options</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
                {warranty.map((option, index) => (
                    <div key={index} className={`bg-gray-100 p-4 rounded-lg flex justify-between items-center ${selectedWarranty === option ? 'border-2 border-blue-500' : ''}`}>
                        <div>
                            <h3 className="font-semibold mb-2">{option.duration}</h3>
                            <p className="text-lg mb-1">{option.price}</p>
                            <p className="text-sm text-gray-600">{option.description}</p>
                        </div>
                        <button
                            onClick={() => handleToggleWarranty(option)}
                            className={`px-4 py-2 rounded ${selectedWarranty === option ? 'bg-blue-600 text-white' : 'bg-gray-300 text-gray-800'}`}
                        >
                            {selectedWarranty === option ? 'Deselect' : 'Select'}
                        </button>
                    </div>
                ))}
            </div>

            <h2 className="text-2xl font-semibold mb-4">Accessories</h2>
            <div className="overflow-x-auto whitespace-nowrap pb-4 mb-8">
                <div className="inline-flex space-x-4">
                    {accessories.map((accessory, index) => (
                        <div key={index} className={`bg-white shadow-md rounded-lg p-4 w-80 ${selectedAccessories.some(acc => acc.id === accessory.id) ? 'border-2 border-blue-500' : ''}`}>
                            <h3 className="font-semibold mb-2">{accessory.name}</h3>
                            <p className="text-gray-600 mb-2">${accessory.price.toFixed(2)}</p>
                            <p className="text-sm mb-4">{accessory.description}</p>
                            <button
                                onClick={() => handleToggleAccessory(accessory)}
                                className={`px-4 py-2 rounded ${selectedAccessories.some(acc => acc.id === accessory.id) ? 'bg-red-600 text-white' : 'bg-blue-600 text-white'}`}
                            >
                                {selectedAccessories.some(acc => acc.id === accessory.id) ? 'Remove' : 'Add'}
                            </button>
                        </div>
                    ))}
                </div>
            </div>

            <div className="mt-8">
                <h2 className="text-2xl font-semibold mb-4">Selected Items</h2>
                <p>Base Product: ${product.price.toFixed(2)}</p>
                {selectedWarranty && (
                    <p>Warranty: {selectedWarranty.duration} - {selectedWarranty.price}</p>
                )}
                {selectedAccessories.length > 0 && (
                    <div>
                        <p>Accessories:</p>
                        <ul className="list-disc list-inside">
                            {selectedAccessories.map((acc, index) => (
                                <li key={index}>{acc.name} - ${acc.price.toFixed(2)}</li>
                            ))}
                        </ul>
                    </div>
                )}
                <p className="text-xl font-bold mt-4">Total: ${calculateTotalPrice()}</p>
            </div>

            <div className="flex space-x-4 mt-6">
                <button
                    onClick={handleAddToCart}
                    className="bg-green-600 text-white px-6 py-2 rounded hover:bg-green-700 transition duration-300"
                >
                    Add to Cart
                </button>
                
            </div>
            <ReviewSection productId={id} />
        </div>
    );
}

export default ProductDetails;