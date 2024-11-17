import React, { useState, useEffect, useRef } from 'react';
import { getInventoryCounts, getProductsOnSale, getProductsWithRebates } from '../../services/api';
import dayjs from 'dayjs';

function InventoryReports() {
    const [inventoryData, setInventoryData] = useState([]);
    const [onSaleProducts, setOnSaleProducts] = useState([]);
    const [rebateProducts, setRebateProducts] = useState([]);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const chartRef = useRef(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                setError(null);

                await loadGoogleCharts();

                const [inventoryResponse, onSaleResponse, rebatesResponse] = await Promise.all([
                    getInventoryCounts(),
                    getProductsOnSale(),
                    getProductsWithRebates()
                ]);

                setInventoryData(inventoryResponse);
                setOnSaleProducts(onSaleResponse);
                setRebateProducts(rebatesResponse);
            } catch (err) {
                console.error('Error fetching data:', err);
                setError('Failed to load inventory data. Please try again later.');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    useEffect(() => {
        if (!loading && inventoryData.length > 0 && chartRef.current) {
            drawChart();
        }
    }, [loading, inventoryData]);


    const loadGoogleCharts = () => {
        return new Promise((resolve, reject) => {
            if (window.google && window.google.charts) {
                resolve();
            } else {
                const script = document.createElement('script');
                script.src = 'https://www.gstatic.com/charts/loader.js';
                script.onload = () => {
                    if (window.google && window.google.charts) {
                        window.google.charts.load('current', { 'packages': ['corechart'] });
                        window.google.charts.setOnLoadCallback(resolve);
                    } else {
                        reject(new Error('Failed to load Google Charts'));
                    }
                };
                script.onerror = reject;
                document.body.appendChild(script);
            }
        });
    };

    const drawChart = () => {
        if (window.google && window.google.visualization && inventoryData.length > 0 && chartRef.current) {
            const chartData = new window.google.visualization.DataTable();
            chartData.addColumn('string', 'Product');
            chartData.addColumn('number', 'Inventory Count');

            inventoryData.forEach(item => {
                chartData.addRow([item.name, item.inventory_count]);
            });

            const options = {
                title: 'Product Inventory',
                chartArea: { width: '50%' },
                hAxis: {
                    title: 'Inventory Count',
                    minValue: 0
                },
                vAxis: {
                    title: 'Product'
                }
            };

            const chart = new window.google.visualization.BarChart(chartRef.current);
            chart.draw(chartData, options);
        }
    };

    const formatDate = (dateString) => {
        return dayjs(dateString).format('D MMM YYYY');
    };

    const formatDateRange = (startDate, endDate) => {
        return `${formatDate(startDate)} to ${formatDate(endDate)}`;
    };

    const calculateDiscountPercentage = (price, discountAmount) => {

        return discountAmount.toFixed(2) + '%';
    };

    if (loading) {
        return <div>Loading inventory data...</div>;
    }

    if (error) {
        return <div className="text-red-500">{error}</div>;
    }

    return (
        <div>
            <h2 className="text-2xl font-bold mb-4">Inventory Reports</h2>

            <div className="mb-8">
                <h3 className="text-xl font-semibold mb-2">All Products Inventory</h3>
                <table className="w-full border-collapse border border-gray-300">
                    <thead>
                        <tr className="bg-gray-100">
                            <th className="border border-gray-300 p-2">Product Name</th>
                            <th className="border border-gray-300 p-2">Price</th>
                            <th className="border border-gray-300 p-2">Available Items</th>
                        </tr>
                    </thead>
                    <tbody>
                        {inventoryData.map((item) => (
                            <tr key={item.id}>
                                <td className="border border-gray-300 p-2">{item.name}</td>
                                <td className="border border-gray-300 p-2">${item.price.toFixed(2)}</td>
                                <td className="border border-gray-300 p-2">{item.inventory_count}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            <div className="mb-8">
                <h3 className="text-xl font-semibold mb-2">Inventory Bar Chart</h3>
                <div ref={chartRef} style={{ width: '100%', height: '400px' }}></div>
            </div>

            <div className="mb-8">
                <h3 className="text-xl font-semibold mb-2">Products On Sale</h3>
                <table className="w-full border-collapse border border-gray-300">
                    <thead>
                        <tr className="bg-gray-100">
                            <th className="border border-gray-300 p-2">Product Name</th>
                            <th className="border border-gray-300 p-2">Price</th>
                            <th className="border border-gray-300 p-2">Discount</th>
                            <th className="border border-gray-300 p-2">Sale Period</th>
                        </tr>
                    </thead>
                    <tbody>
                        {onSaleProducts.map((item) => (
                            <tr key={item.id}>
                                <td className="border border-gray-300 p-2">{item.name}</td>
                                <td className="border border-gray-300 p-2">${item.price.toFixed(2)}</td>
                                <td className="border border-gray-300 p-2">
                                    {calculateDiscountPercentage(item.price, item.discount_amount)}
                                </td>
                                <td className="border border-gray-300 p-2">
                                    {formatDateRange(item.start_date, item.end_date)}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            <div className="mb-8">
                <h3 className="text-xl font-semibold mb-2">Products with Manufacturer Rebates</h3>
                <table className="w-full border-collapse border border-gray-300">
                    <thead>
                        <tr className="bg-gray-100">
                            <th className="border border-gray-300 p-2">Product Name</th>
                            <th className="border border-gray-300 p-2">Price</th>
                            <th className="border border-gray-300 p-2">Rebate Amount</th>
                            <th className="border border-gray-300 p-2">Rebate Period</th>
                        </tr>
                    </thead>
                    <tbody>
                        {rebateProducts.map((item) => (
                            <tr key={item.id}>
                                <td className="border border-gray-300 p-2">{item.name}</td>
                                <td className="border border-gray-300 p-2">${item.price.toFixed(2)}</td>
                                <td className="border border-gray-300 p-2">${item.rebate_amount.toFixed(2)}</td>
                                <td className="border border-gray-300 p-2">
                                    {formatDateRange(item.start_date, item.end_date)}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default InventoryReports;