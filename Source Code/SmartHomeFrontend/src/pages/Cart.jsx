import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../contexts/CartContext';

function Cart() {
    const { cart, removeFromCart, updateQuantity, getTotalPrice, removeWarranty, removeAccessory } = useCart();
    const navigate = useNavigate();

    const groupedCart = cart.reduce((acc, item) => {
        if (item.type === 'product') {
            acc[item.id] = {
                ...item,
                subItems: cart.filter(subItem => subItem.parentProductId === item.id)
            };
        } else if (!acc[item.id]) {
            acc[item.id] = item;
        }
        return acc;
    }, {});

    const handleGoBack = () => {
        navigate(-1); // This will take the user back to the previous page
    };

    const handleProceedToCheckout = () => {
        navigate('/checkout');
    };


    const handleRemoveWarranty = (productId) => removeWarranty(productId);
    const handleRemoveAccessory = (productId, accessoryId) => removeAccessory(productId, accessoryId);

    const calculateItemTotal = (item) => {
        let total = item.price * item.quantity;
        if (item.warranty) {
            total += parseFloat(item.warranty.price.replace('$', ''));
        }
        if (item.accessories) {
            total += item.accessories.reduce((sum, acc) => sum + acc.price, 0);
        }
        return total.toFixed(2);
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
                <span>Back to Product</span>
            </button>
            <h1 className="text-3xl font-bold mb-8">Your Cart</h1>
            {cart.map((item) => (
                <div key={item.id} className="mb-6 border-b pb-4">
                    <div className="flex justify-between items-center mb-2">
                        <h2 className="text-xl font-semibold">{item.name}</h2>
                        <button
                            onClick={() => removeFromCart(item.id, item.type)}
                            className="text-red-600 hover:text-red-800"
                        >
                            Remove
                        </button>
                    </div>
                    <div className="flex justify-between items-center mb-2">
                        <div>
                            <p>Price: ${item.price.toFixed(2)}</p>
                            <div className="flex items-center mt-2">
                                <label htmlFor={`quantity-${item.id}`} className="mr-2">Quantity:</label>
                                <input
                                    id={`quantity-${item.id}`}
                                    type="number"
                                    min="1"
                                    value={item.quantity}
                                    onChange={(e) => updateQuantity(item.id, item.type, parseInt(e.target.value))}
                                    className="w-16 px-2 py-1 border rounded"
                                />
                            </div>
                        </div>
                        <p className="font-bold">Subtotal: ${(item.price * item.quantity).toFixed(2)}</p>
                    </div>
                    {item.warranty && (
                        <div className="ml-4 mt-2 flex justify-between items-center">
                            <p>Warranty: {item.warranty.duration} - {item.warranty.price}</p>
                            <button
                                onClick={() => handleRemoveWarranty(item.id)}
                                className="text-red-600 hover:text-red-800 text-sm"
                            >
                                Remove Warranty
                            </button>
                        </div>
                    )}
                    {item.accessories && item.accessories.length > 0 && (
                        <div className="ml-4 mt-2">
                            <p className="font-semibold">Accessories:</p>
                            {item.accessories.map((acc, index) => (
                                <div key={index} className="flex justify-between items-center">
                                    <p>{acc.name} - ${acc.price.toFixed(2)}</p>
                                    <button
                                        onClick={() => handleRemoveAccessory(item.id, acc.id)}
                                        className="text-red-600 hover:text-red-800 text-sm"
                                    >
                                        Remove
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                    <p className="text-right font-bold mt-2">Item Total: ${calculateItemTotal(item)}</p>
                </div>
            ))}
            <div className="mt-8">
                <div className="text-xl font-bold mb-4">
                    Cart Total: ${getTotalPrice().toFixed(2)}
                </div>
                <button
                    onClick={handleProceedToCheckout}
                    className="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
                >
                    Proceed to Checkout
                </button>
            </div>
        </div>
    );
}

export default Cart;