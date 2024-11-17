import React, { useState, useEffect, useRef } from 'react';
import { getProductSales, getDailySales } from '../../services/api';
import dayjs from 'dayjs'; // Import dayjs

function SalesReport() {
    const [productSales, setProductSales] = useState([]);
    const [dailySales, setDailySales] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const chartRef = useRef(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                setError(null);

                await loadGoogleCharts();
                const [productSalesResponse, dailySalesResponse] = await Promise.all([
                    getProductSales(),
                    getDailySales()
                ]);
                setProductSales(productSalesResponse);
                setDailySales(dailySalesResponse);
                setLoading(false);
            } catch (err) {
                console.error('Error fetching sales data:', err);
                setError('Failed to load sales data. Please try again later.');
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    useEffect(() => {
        if (!loading && productSales.length > 0) {
            drawChart();
        }
    }, [loading, productSales]);

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
        if (typeof google !== 'undefined' && google.charts) {
            const data = new google.visualization.DataTable();
            data.addColumn('string', 'Product');
            data.addColumn('number', 'Total Sales ($)');

            productSales.forEach(item => {
                data.addRow([item.productName, parseFloat(item.totalSales)]);
            });

            const options = {
                title: 'Product Sales',
                chartArea: { width: '50%' },
                hAxis: {
                    title: 'Total Sales ($)',
                    minValue: 0
                },
                vAxis: {
                    title: 'Product'
                }
            };

            const chart = new google.visualization.BarChart(chartRef.current);
            chart.draw(data, options);
        }
    };

    if (loading) return <div>Loading sales data...</div>;
    if (error) return <div className="text-red-500">{error}</div>;

    return (
        <div>
            <h2 className="text-2xl font-bold mb-4">Sales Report</h2>

            <div className="mb-8">
                <h3 className="text-xl font-semibold mb-2">Product Sales Summary</h3>
                <table className="w-full border-collapse border border-gray-300">
                    <thead>
                        <tr className="bg-gray-100">
                            <th className="border border-gray-300 p-2">Product Name</th>
                            <th className="border border-gray-300 p-2">Price</th>
                            <th className="border border-gray-300 p-2">Items Sold</th>
                            <th className="border border-gray-300 p-2">Total Sales</th>
                        </tr>
                    </thead>
                    <tbody>
                        {productSales.map((item) => (
                            <tr key={item.productId}>
                                <td className="border border-gray-300 p-2">{item.productName}</td>
                                <td className="border border-gray-300 p-2">${parseFloat(item.productPrice).toFixed(2)}</td>
                                <td className="border border-gray-300 p-2">{item.itemsSold}</td>
                                <td className="border border-gray-300 p-2">${parseFloat(item.totalSales).toFixed(2)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            <div className="mb-8">
                <h3 className="text-xl font-semibold mb-2">Product Sales Bar Chart</h3>
                <div ref={chartRef} style={{ width: '100%', height: '400px' }}></div>
            </div>

            <div className="mb-8">
                <h3 className="text-xl font-semibold mb-2">Daily Sales Transactions</h3>
                <table className="w-full border-collapse border border-gray-300">
                    <thead>
                        <tr className="bg-gray-100">
                            <th className="border border-gray-300 p-2">Date</th>
                            <th className="border border-gray-300 p-2">Product Name</th>
                            <th className="border border-gray-300 p-2">Items Sold</th>
                            <th className="border border-gray-300 p-2">Total Sales</th>
                        </tr>
                    </thead>
                    <tbody>
                        {dailySales.map((item, index) => (
                            <tr key={`${item.saleDate}-${item.productId}-${index}`}>
                                <td className="border border-gray-300 p-2">{dayjs(item.saleDate).format('D MMM YYYY')}</td>
                                <td className="border border-gray-300 p-2">{item.productName}</td>
                                <td className="border border-gray-300 p-2">{item.itemsSold}</td>
                                <td className="border border-gray-300 p-2">${parseFloat(item.totalSales).toFixed(2)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default SalesReport;
