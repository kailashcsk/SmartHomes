import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAutocompleteSuggestions } from '../../services/api';

function SearchBar() {
    const [searchTerm, setSearchTerm] = useState('');
    const [suggestions, setSuggestions] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchSuggestions = async () => {
            if (searchTerm.length > 2) {
                try {
                    const data = await getAutocompleteSuggestions(searchTerm);
                    setSuggestions(data);
                } catch (error) {
                    console.error('Error fetching suggestions:', error);
                }
            } else {
                setSuggestions([]);
            }
        };

        const timeoutId = setTimeout(fetchSuggestions, 300);
        return () => clearTimeout(timeoutId);
    }, [searchTerm]);

    const handleSearch = (product) => {
        navigate(`/products/${product.id}`);
        setSearchTerm('');
        setSuggestions([]);
    };

    return (
        <div className="relative">
            <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Search products..."
                className="w-full px-4 py-2 border rounded-md"
            />
            {suggestions.length > 0 && (
                <ul className="absolute z-10 w-full bg-white border rounded-md mt-1 max-h-60 overflow-auto">
                    {suggestions.map((product) => (
                        <li
                            key={product.id}
                            onClick={() => handleSearch(product)}
                            className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                        >
                            {product.name} - ${product.price.toFixed(2)}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}

export default SearchBar;