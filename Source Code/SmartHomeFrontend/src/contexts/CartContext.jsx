import React, { createContext, useState, useContext, useEffect } from 'react';

const CartContext = createContext();

export const useCart = () => useContext(CartContext);

export const CartProvider = ({ children }) => {
    const [cart, setCart] = useState(() => {
        const savedCart = localStorage.getItem('cart');
        return savedCart ? JSON.parse(savedCart) : [];
    });

    useEffect(() => {
        localStorage.setItem('cart', JSON.stringify(cart));
    }, [cart]);

    const addToCart = (item, quantity = 1) => {
        setCart(prevCart => {
            const existingItem = prevCart.find(cartItem =>
                cartItem.id === item.id && cartItem.type === item.type
            );

            if (existingItem) {
                return prevCart.map(cartItem =>
                    cartItem.id === item.id && cartItem.type === item.type
                        ? { ...cartItem, quantity: cartItem.quantity + quantity }
                        : cartItem
                );
            } else {
                return [...prevCart, { ...item, quantity }];
            }
        });
    };

    const removeFromCart = (itemId, itemType) => {
        setCart(prevCart => prevCart.filter(item => !(item.id === itemId && item.type === itemType)));
    };

    const updateQuantity = (itemId, itemType, newQuantity) => {
        setCart(prevCart =>
            prevCart.map(item =>
                item.id === itemId && item.type === itemType
                    ? { ...item, quantity: newQuantity }
                    : item
            )
        );
    };

    const getCartItemsCount = () => {
        return cart.reduce((total, item) => total + item.quantity, 0);
    };

    const removeWarranty = (productId) => {
        setCart(prevCart =>
            prevCart.map(item =>
                item.id === productId
                    ? { ...item, warranty: null }
                    : item
            )
        );
    };

    const removeAccessory = (productId, accessoryId) => {
        setCart(prevCart =>
            prevCart.map(item =>
                item.id === productId
                    ? { ...item, accessories: item.accessories.filter(acc => acc.id !== accessoryId) }
                    : item
            )
        );
    };

    // Update the getTotalPrice method to include warranties and accessories
    const getTotalPrice = () => {
        return cart.reduce((total, item) => {
            let itemTotal = item.price * item.quantity;
            if (item.warranty) {
                itemTotal += parseFloat(item.warranty.price.replace('$', ''));
            }
            if (item.accessories) {
                itemTotal += item.accessories.reduce((sum, acc) => sum + acc.price, 0);
            }
            return total + itemTotal;
        }, 0);
    };

    const clearCart = () => {
        setCart([]);
        localStorage.removeItem('cart');
    };


    return (
        <CartContext.Provider value={{
            cart,
            addToCart,
            removeFromCart,
            updateQuantity,
            getTotalPrice,
            getCartItemsCount,
            removeWarranty,
            removeAccessory,
            clearCart
        }}>
            {children}
        </CartContext.Provider>
    );
};