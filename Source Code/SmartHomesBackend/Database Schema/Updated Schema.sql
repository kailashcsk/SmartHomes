CREATE SCHEMA IF NOT EXISTS smarthomes_db;
USE smarthomes_db;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role ENUM('STOREMANAGER', 'SALESMAN', 'CUSTOMER') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Stores Table (No changes needed)
CREATE TABLE stores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip_code VARCHAR(20) NOT NULL
);

-- Product Categories Table (No changes needed)
CREATE TABLE product_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Products Table (Added manufacturer_name)
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    manufacturer_name VARCHAR(255),
    FOREIGN KEY (category_id) REFERENCES product_categories(id)
);

-- Accessories Table (No changes needed)
CREATE TABLE accessories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Warranties Table (No changes needed)
CREATE TABLE warranties (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT,
    duration INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Orders Table (Added additional fields as per requirements)
CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    store_id INT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ship_date DATE,
    total_amount DECIMAL(10, 2) NOT NULL,
    shipping_cost DECIMAL(10, 2),
    discount_amount DECIMAL(10, 2),
    final_sale_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED') NOT NULL,
    delivery_method ENUM('STORE_PICKUP', 'HOME_DELIVERY') NOT NULL,
    shipping_address TEXT,
    credit_card_number VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (store_id) REFERENCES stores(id)
);

-- Order Items Table (Added category field)
CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    product_id INT,
    category VARCHAR(100),
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Discounts Table (No changes needed)
CREATE TABLE discounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT,
    discount_type ENUM('RETAILER_SPECIAL', 'MANUFACTURER_REBATE') NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    start_date DATE,
    end_date DATE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Customer Addresses Table (New table to store customer addresses)
CREATE TABLE customer_addresses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- New inventory table
CREATE TABLE inventory (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id)
);


-- Trending Products View (New view for analytics)
CREATE VIEW trending_products AS
SELECT 
    p.id,
    p.name,
    p.category_id,
    COUNT(oi.id) AS total_sales,
    AVG(r.rating) AS avg_rating
FROM 
    products p
    LEFT JOIN order_items oi ON p.id = oi.product_id
    LEFT JOIN reviews r ON p.id = r.product_id
GROUP BY 
    p.id, p.name, p.category_id
ORDER BY 
    total_sales DESC, avg_rating DESC;

-- Top Selling Zip Codes View (New view for analytics)
CREATE VIEW top_selling_zip_codes AS
SELECT 
    s.zip_code,
    COUNT(o.id) AS total_orders,
    SUM(o.final_sale_amount) AS total_sales
FROM 
    orders o
    JOIN stores s ON o.store_id = s.id
GROUP BY 
    s.zip_code
ORDER BY 
    total_sales DESC;

-- New views for inventory report
CREATE OR REPLACE VIEW products_on_sale AS
SELECT p.id, p.name, p.price, d.amount AS discount_amount, d.start_date, d.end_date
FROM products p
JOIN discounts d ON p.id = d.product_id
WHERE d.discount_type = 'RETAILER_SPECIAL' AND CURDATE() BETWEEN d.start_date AND d.end_date;

CREATE OR REPLACE VIEW products_with_rebates AS
SELECT p.id, p.name, p.price, d.amount AS rebate_amount, d.start_date, d.end_date
FROM products p
JOIN discounts d ON p.id = d.product_id
WHERE d.discount_type = 'MANUFACTURER_REBATE' AND CURDATE() BETWEEN d.start_date AND d.end_date;

CREATE OR REPLACE VIEW inventory_report AS
SELECT p.id, p.name, p.price, COALESCE(i.quantity, 0) AS available_quantity
FROM products p
LEFT JOIN inventory i ON p.id = i.product_id;

-- Indexes for improved performance
CREATE INDEX idx_product_inventory ON inventory (product_id);
CREATE INDEX idx_product_discounts ON discounts (product_id, discount_type, start_date, end_date);

-- Verify the changes
SHOW TABLES;
DESCRIBE inventory;
DESCRIBE discounts;
SHOW CREATE VIEW products_on_sale;
SHOW CREATE VIEW products_with_rebates;
SHOW CREATE VIEW inventory_report;
SHOW INDEX FROM inventory;
SHOW INDEX FROM discounts;

-- View for product sales summary
CREATE OR REPLACE VIEW product_sales_summary AS
SELECT 
    p.id AS product_id,
    p.name AS product_name,
    p.price AS product_price,
    SUM(oi.quantity) AS items_sold,
    SUM(oi.quantity * oi.price) AS total_sales
FROM 
    products p
    LEFT JOIN order_items oi ON p.id = oi.product_id
    LEFT JOIN orders o ON oi.order_id = o.id
WHERE 
    o.status != 'CANCELLED'
GROUP BY 
    p.id, p.name, p.price;

-- View for daily sales transactions
CREATE OR REPLACE VIEW daily_sales_transactions AS
SELECT 
    DATE(o.order_date) AS sale_date,
    SUM(o.final_sale_amount) AS total_sales
FROM 
    orders o
WHERE 
    o.status != 'CANCELLED'
GROUP BY 
    DATE(o.order_date)
ORDER BY 
    sale_date;


-- Create the tickets table if not already created
CREATE TABLE tickets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_number VARCHAR(20) UNIQUE NOT NULL,
    user_id INT,
    order_id INT,
    description TEXT NOT NULL,
    image_path VARCHAR(255),
    status ENUM('PENDING', 'IN_REVIEW', 'RESOLVED') DEFAULT 'PENDING',
    decision ENUM('REFUND_ORDER', 'REPLACE_ORDER', 'ESCALATE_TO_HUMAN') NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);


CREATE TABLE reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    store_id INT NOT NULL,
    review_text TEXT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE
);