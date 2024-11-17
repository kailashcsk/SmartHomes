package com.smarthomes.util;

import com.smarthomes.models.Product;
import com.smarthomes.models.Store;
import com.smarthomes.models.Ticket;
import com.smarthomes.models.User;
import com.smarthomes.models.Warranty;
import com.smarthomes.dto.InventoryDTO;
import com.smarthomes.dto.InventoryReportDTO;
import com.smarthomes.dto.SalesReportDTO;
import com.smarthomes.models.Accessory;
import com.smarthomes.models.Address;
import com.smarthomes.models.Category;
import com.smarthomes.models.Discount;
import com.smarthomes.models.Order;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLDataStoreUtilities {
    private static final Logger LOGGER = Logger.getLogger(MySQLDataStoreUtilities.class.getName());

    // Create a new user
    public static void createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (email, password, name, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, PasswordUtil.hashPassword(user.getPassword()));
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getRole().toString());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    // Get a user by email
    public static User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // Get a user by ID
    public static User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // Update a user's information
    public static void updateUser(User user) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE users SET ");
        List<Object> params = new ArrayList<>();

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            sql.append("email = ?, ");
            params.add(user.getEmail());
        }
        if (user.getName() != null && !user.getName().isEmpty()) {
            sql.append("name = ?, ");
            params.add(user.getName());
        }
        if (user.getRole() != null) {
            sql.append("role = ?, ");
            params.add(user.getRole().toString());
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            sql.append("password = ?, ");
            params.add(PasswordUtil.hashPassword(user.getPassword()));
        }

        // Remove the trailing comma and space
        if (params.isEmpty()) {
            throw new SQLException("No fields to update");
        }
        sql.setLength(sql.length() - 2);

        sql.append(" WHERE id = ?");
        params.add(user.getId());

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }
        }
    }

    // Delete a user
    public static void deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting user failed, no rows affected.");
            }
        }
    }

    // Get all users
    public static List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        }
        return users;
    }

    // Check if a user exists by email
    public static boolean userExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Authenticate a user
    public static User authenticate(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    if (PasswordUtil.checkPassword(password, hashedPassword)) {
                        return extractUserFromResultSet(rs);
                    }
                }
            }
        }
        return null;
    }

    // Helper method to extract a User from a ResultSet
    private static User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setName(rs.getString("name"));
        user.setRole(User.Role.valueOf(rs.getString("role")));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }

    // Product-related methods

    public static List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        }
        return products;
    }

    public static Product getProductById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractProductFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public static void createProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (category_id, name, price, description, manufacturer_name) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, product.getCategoryId());
            pstmt.setString(2, product.getName());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setString(4, product.getDescription());
            pstmt.setString(5, product.getManufacturerName());

            LOGGER.info("Executing SQL: " + sql);
            LOGGER.info("Product details: " + product);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating product failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating product failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error in createProduct: " + e.getMessage(), e);
            throw e; // Re-throw the exception to be handled by the servlet
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in createProduct: " + e.getMessage(), e);
            throw new SQLException("Unexpected error occurred while creating product", e);
        }
    }

    public static void updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET category_id = ?, name = ?, price = ?, description = ?, manufacturer_name = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, product.getCategoryId());
            pstmt.setString(2, product.getName());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setString(4, product.getDescription());
            pstmt.setString(5, product.getManufacturerName());
            pstmt.setInt(6, product.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating product failed, no rows affected.");
            }
        }
    }

    public static void deleteProduct(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting product failed, no rows affected.");
            }
        }
    }

    public static List<Product> getProductAccessories(int productId) throws SQLException {
        List<Product> accessories = new ArrayList<>();
        String sql = "SELECT * FROM accessories WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product accessory = new Product();
                    accessory.setId(rs.getInt("id"));
                    accessory.setName(rs.getString("name"));
                    accessory.setPrice(rs.getBigDecimal("price"));
                    accessory.setDescription(rs.getString("description"));
                    // Note: category_id and manufacturer_name might not be applicable for
                    // accessories
                    // If they are, you can set them here as well
                    accessories.add(accessory);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching accessories for product ID " + productId, e);
            throw e;
        }
        return accessories;
    }

    public static List<String> getProductWarranty(int productId) throws SQLException {
        List<String> warranties = new ArrayList<>();
        String sql = "SELECT * FROM warranties WHERE product_id = ?";
        LOGGER.info("Executing SQL: " + sql + " with productId: " + productId);
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    warranties.add(String.format("%d months - $%.2f: %s",
                            rs.getInt("duration"),
                            rs.getBigDecimal("price"),
                            rs.getString("description")));
                }
            }
        }
        LOGGER.info("Found " + warranties.size() + " warranties for product ID: " + productId);
        return warranties;
    }

    public static List<String> getProductDiscounts(int productId) throws SQLException {
        List<String> discounts = new ArrayList<>();
        String sql = "SELECT * FROM discounts WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    discounts.add(String.format("%s - $%.2f off (%s to %s)",
                            rs.getString("discount_type"),
                            rs.getBigDecimal("amount"),
                            rs.getDate("start_date"),
                            rs.getDate("end_date")));
                }
            }
        }
        return discounts;
    }

    private static Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setName(rs.getString("name"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setDescription(rs.getString("description"));
        product.setManufacturerName(rs.getString("manufacturer_name"));
        return product;
    }

    // Category-related methods

    public static List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM product_categories";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(extractCategoryFromResultSet(rs));
            }
        }
        return categories;
    }

    public static Category getCategoryById(int id) throws SQLException {
        String sql = "SELECT * FROM product_categories WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractCategoryFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public static void createCategory(Category category) throws SQLException {
        String sql = "INSERT INTO product_categories (name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, category.getName());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        }
    }

    public static void updateCategory(Category category) throws SQLException {
        String sql = "UPDATE product_categories SET name = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setInt(2, category.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating category failed, no rows affected.");
            }
        }
    }

    public static void deleteCategory(int id) throws SQLException {
        String sql = "DELETE FROM product_categories WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting category failed, no rows affected.");
            }
        }
    }

    public static boolean categoryExists(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM product_categories WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private static Category extractCategoryFromResultSet(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        return category;
    }

    // Store-related methods

    public static List<Store> getAllStores() throws SQLException {
        List<Store> stores = new ArrayList<>();
        String sql = "SELECT * FROM stores";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stores.add(extractStoreFromResultSet(rs));
            }
        }
        return stores;
    }

    public static Store getStoreById(int id) throws SQLException {
        String sql = "SELECT * FROM stores WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractStoreFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public static void createStore(Store store) throws SQLException {
        String sql = "INSERT INTO stores (name, street, city, state, zip_code) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, store.getName());
            pstmt.setString(2, store.getStreet());
            pstmt.setString(3, store.getCity());
            pstmt.setString(4, store.getState());
            pstmt.setString(5, store.getZipCode());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating store failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    store.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating store failed, no ID obtained.");
                }
            }
        }
    }

    public static void updateStore(Store store) throws SQLException {
        String sql = "UPDATE stores SET name = ?, street = ?, city = ?, state = ?, zip_code = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, store.getName());
            pstmt.setString(2, store.getStreet());
            pstmt.setString(3, store.getCity());
            pstmt.setString(4, store.getState());
            pstmt.setString(5, store.getZipCode());
            pstmt.setInt(6, store.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating store failed, no rows affected.");
            }
        }
    }

    public static void deleteStore(int id) throws SQLException {
        String sql = "DELETE FROM stores WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting store failed, no rows affected.");
            }
        }
    }

    public static boolean storeExists(int storeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM stores WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, storeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private static Store extractStoreFromResultSet(ResultSet rs) throws SQLException {
        Store store = new Store();
        store.setId(rs.getInt("id"));
        store.setName(rs.getString("name"));
        store.setStreet(rs.getString("street"));
        store.setCity(rs.getString("city"));
        store.setState(rs.getString("state"));
        store.setZipCode(rs.getString("zip_code"));
        return store;
    }

    // Discount-related methods

    public static List<Discount> getAllDiscounts() throws SQLException {
        List<Discount> discounts = new ArrayList<>();
        String sql = "SELECT * FROM discounts";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                discounts.add(extractDiscountFromResultSet(rs));
            }
        }
        return discounts;
    }

    public static Discount getDiscountById(int id) throws SQLException {
        String sql = "SELECT * FROM discounts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractDiscountFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public static void createDiscount(Discount discount) throws SQLException {
        String sql = "INSERT INTO discounts (product_id, discount_type, amount, start_date, end_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, discount.getProductId());
            pstmt.setString(2, discount.getDiscountType());
            pstmt.setBigDecimal(3, discount.getAmount());
            pstmt.setDate(4, new java.sql.Date(discount.getStartDate().getTime()));
            pstmt.setDate(5, new java.sql.Date(discount.getEndDate().getTime()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating discount failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    discount.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating discount failed, no ID obtained.");
                }
            }
        }
    }

    public static void updateDiscount(Discount discount) throws SQLException {
        String sql = "UPDATE discounts SET product_id = ?, discount_type = ?, amount = ?, start_date = ?, end_date = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, discount.getProductId());
            pstmt.setString(2, discount.getDiscountType());
            pstmt.setBigDecimal(3, discount.getAmount());
            pstmt.setDate(4, new java.sql.Date(discount.getStartDate().getTime()));
            pstmt.setDate(5, new java.sql.Date(discount.getEndDate().getTime()));
            pstmt.setInt(6, discount.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating discount failed, no rows affected.");
            }
        }
    }

    public static void deleteDiscount(int id) throws SQLException {
        String sql = "DELETE FROM discounts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting discount failed, no rows affected.");
            }
        }
    }

    public static boolean discountExists(int discountId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM discounts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, discountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private static Discount extractDiscountFromResultSet(ResultSet rs) throws SQLException {
        Discount discount = new Discount();
        discount.setId(rs.getInt("id"));
        discount.setProductId(rs.getInt("product_id"));
        discount.setDiscountType(rs.getString("discount_type"));
        discount.setAmount(rs.getBigDecimal("amount"));
        discount.setStartDate(rs.getDate("start_date"));
        discount.setEndDate(rs.getDate("end_date"));
        return discount;
    }

    // Warranty-related methods

    public static List<Warranty> getAllWarranties() throws SQLException {
        List<Warranty> warranties = new ArrayList<>();
        String sql = "SELECT * FROM warranties";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                warranties.add(extractWarrantyFromResultSet(rs));
            }
        }
        return warranties;
    }

    public static Warranty getWarrantyById(int id) throws SQLException {
        String sql = "SELECT * FROM warranties WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractWarrantyFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public static void createWarranty(Warranty warranty) throws SQLException {
        String sql = "INSERT INTO warranties (product_id, duration, price, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, warranty.getProductId());
            pstmt.setInt(2, warranty.getDuration());
            pstmt.setBigDecimal(3, warranty.getPrice());
            pstmt.setString(4, warranty.getDescription());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating warranty failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    warranty.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating warranty failed, no ID obtained.");
                }
            }
        }
    }

    public static void updateWarranty(Warranty warranty) throws SQLException {
        String sql = "UPDATE warranties SET product_id = ?, duration = ?, price = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, warranty.getProductId());
            pstmt.setInt(2, warranty.getDuration());
            pstmt.setBigDecimal(3, warranty.getPrice());
            pstmt.setString(4, warranty.getDescription());
            pstmt.setInt(5, warranty.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating warranty failed, no rows affected.");
            }
        }
    }

    public static void deleteWarranty(int id) throws SQLException {
        String sql = "DELETE FROM warranties WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting warranty failed, no rows affected.");
            }
        }
    }

    public static boolean warrantyExists(int warrantyId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM warranties WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, warrantyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private static Warranty extractWarrantyFromResultSet(ResultSet rs) throws SQLException {
        Warranty warranty = new Warranty();
        warranty.setId(rs.getInt("id"));
        warranty.setProductId(rs.getInt("product_id"));
        warranty.setDuration(rs.getInt("duration"));
        warranty.setPrice(rs.getBigDecimal("price"));
        warranty.setDescription(rs.getString("description"));
        return warranty;
    }

    // Accessory-related methods

    public static List<Accessory> getAllAccessories() throws SQLException {
        List<Accessory> accessories = new ArrayList<>();
        String sql = "SELECT * FROM accessories";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                accessories.add(extractAccessoryFromResultSet(rs));
            }
        }
        return accessories;
    }

    public static Accessory getAccessoryById(int id) throws SQLException {
        String sql = "SELECT * FROM accessories WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractAccessoryFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public static void createAccessory(Accessory accessory) throws SQLException {
        String sql = "INSERT INTO accessories (product_id, name, price, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, accessory.getProductId());
            pstmt.setString(2, accessory.getName());
            pstmt.setBigDecimal(3, accessory.getPrice());
            pstmt.setString(4, accessory.getDescription());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating accessory failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    accessory.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating accessory failed, no ID obtained.");
                }
            }
        }
    }

    public static void updateAccessory(Accessory accessory) throws SQLException {
        String sql = "UPDATE accessories SET product_id = ?, name = ?, price = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accessory.getProductId());
            pstmt.setString(2, accessory.getName());
            pstmt.setBigDecimal(3, accessory.getPrice());
            pstmt.setString(4, accessory.getDescription());
            pstmt.setInt(5, accessory.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating accessory failed, no rows affected.");
            }
        }
    }

    public static void deleteAccessory(int id) throws SQLException {
        String sql = "DELETE FROM accessories WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting accessory failed, no rows affected.");
            }
        }
    }

    public static boolean accessoryExists(int accessoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM accessories WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accessoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private static Accessory extractAccessoryFromResultSet(ResultSet rs) throws SQLException {
        Accessory accessory = new Accessory();
        accessory.setId(rs.getInt("id"));
        accessory.setProductId(rs.getInt("product_id"));
        accessory.setName(rs.getString("name"));
        accessory.setPrice(rs.getBigDecimal("price"));
        accessory.setDescription(rs.getString("description"));
        return accessory;
    }

    // Address-related methods

    public static List<Address> getAddressesByUserId(int userId) throws SQLException {
        List<Address> addresses = new ArrayList<>();
        String sql = "SELECT * FROM customer_addresses WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    addresses.add(extractAddressFromResultSet(rs));
                }
            }
        }

        return addresses;
    }

    public static Address getAddressByIdAndUserId(int addressId, int userId) throws SQLException {
        String sql = "SELECT * FROM customer_addresses WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, addressId);
            pstmt.setInt(2, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractAddressFromResultSet(rs);
                }
            }
        }

        return null;
    }

    public static void createAddress(Address address) throws SQLException {
        String sql = "INSERT INTO customer_addresses (user_id, street, city, state, zip_code) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, address.getUserId());
            pstmt.setString(2, address.getStreet());
            pstmt.setString(3, address.getCity());
            pstmt.setString(4, address.getState());
            pstmt.setString(5, address.getZipCode());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating address failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    address.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating address failed, no ID obtained.");
                }
            }
        }
    }

    public static void updateAddress(Address address) throws SQLException {
        String sql = "UPDATE customer_addresses SET street = ?, city = ?, state = ?, zip_code = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, address.getStreet());
            pstmt.setString(2, address.getCity());
            pstmt.setString(3, address.getState());
            pstmt.setString(4, address.getZipCode());
            pstmt.setInt(5, address.getId());
            pstmt.setInt(6, address.getUserId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating address failed, no rows affected.");
            }
        }
    }

    public static void deleteAddress(int addressId, int userId) throws SQLException {
        String sql = "DELETE FROM customer_addresses WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, addressId);
            pstmt.setInt(2, userId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting address failed, no rows affected.");
            }
        }
    }

    private static Address extractAddressFromResultSet(ResultSet rs) throws SQLException {
        Address address = new Address();
        address.setId(rs.getInt("id"));
        address.setUserId(rs.getInt("user_id"));
        address.setStreet(rs.getString("street"));
        address.setCity(rs.getString("city"));
        address.setState(rs.getString("state"));
        address.setZipCode(rs.getString("zip_code"));
        return address;
    }

    // User Order Management-related methods

    public static void createOrder(Order order) throws SQLException {

        // Check if user exists
        String checkUserSql = "SELECT id FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement checkUserStmt = conn.prepareStatement(checkUserSql)) {
            checkUserStmt.setInt(1, order.getUserId());
            try (ResultSet userRs = checkUserStmt.executeQuery()) {
                if (!userRs.next()) {
                    throw new SQLException("User with ID " + order.getUserId() + " does not exist.");
                }
            }
        }

        Connection conn = null;
        PreparedStatement orderStmt = null;
        PreparedStatement itemStmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Calculate ship_date based on delivery method
            LocalDate orderDate = LocalDate.now();
            LocalDate shipDate;
            if ("HOME_DELIVERY".equals(order.getDeliveryMethod())) {
                shipDate = orderDate.plusDays(15);
            } else {
                shipDate = orderDate.plusDays(5);
            }

            // Insert into orders table
            String orderSql = "INSERT INTO orders (user_id, store_id, total_amount, shipping_cost, discount_amount, final_sale_amount, status, delivery_method, shipping_address, credit_card_number, ship_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, order.getUserId());
            orderStmt.setInt(2, order.getStoreId());
            orderStmt.setBigDecimal(3, order.getTotalAmount());
            orderStmt.setBigDecimal(4, order.getShippingCost());
            orderStmt.setBigDecimal(5, order.getDiscountAmount());
            orderStmt.setBigDecimal(6, order.getFinalSaleAmount());
            orderStmt.setString(7, order.getStatus());
            orderStmt.setString(8, order.getDeliveryMethod());
            orderStmt.setString(9, order.getShippingAddress());
            orderStmt.setString(10, order.getCreditCardNumber());
            orderStmt.setDate(11, java.sql.Date.valueOf(shipDate));

            int affectedRows = orderStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            rs = orderStmt.getGeneratedKeys();
            int orderId;
            if (rs.next()) {
                orderId = rs.getInt(1);
                order.setId(orderId);
            } else {
                throw new SQLException("Creating order failed, no ID obtained.");
            }

            // Insert into order_items table
            String itemSql = "INSERT INTO order_items (order_id, product_id, category, quantity, price) VALUES (?, ?, ?, ?, ?)";
            itemStmt = conn.prepareStatement(itemSql);
            for (Order.OrderItem item : order.getOrderItems()) {
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setString(3, item.getCategory());
                itemStmt.setInt(4, item.getQuantity());
                itemStmt.setBigDecimal(5, item.getPrice());
                itemStmt.addBatch();
            }
            itemStmt.executeBatch();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new SQLException("Error rolling back transaction", ex);
                }
            }
            throw e;
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    /* ignored */ }
            if (orderStmt != null)
                try {
                    orderStmt.close();
                } catch (SQLException e) {
                    /* ignored */ }
            if (itemStmt != null)
                try {
                    itemStmt.close();
                } catch (SQLException e) {
                    /* ignored */ }
            if (conn != null) {
                conn.setAutoCommit(true);
                try {
                    conn.close();
                } catch (SQLException e) {
                    /* ignored */ }
            }
        }
    }

    public static List<Order> getOrdersByUserId(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = extractOrderFromResultSet(rs);
                    order.setOrderItems(getOrderItemsByOrderId(order.getId()));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    public static Order getOrderByUserIdAndOrderId(int userId, int orderId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE user_id = ? AND id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = extractOrderFromResultSet(rs);
                    order.setOrderItems(getOrderItemsByOrderId(order.getId()));
                    return order;
                }
            }
        }
        return null;
    }

    public static boolean cancelOrder(int userId, int orderId) throws SQLException {
        String sql = "UPDATE orders SET status = 'CANCELLED' WHERE user_id = ? AND id = ? AND DATEDIFF(CURDATE(), order_date) <= 5 AND status != 'CANCELLED'";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, orderId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    private static Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setStoreId(rs.getInt("store_id"));
        order.setOrderDate(rs.getTimestamp("order_date"));
        order.setShipDate(rs.getDate("ship_date"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setShippingCost(rs.getBigDecimal("shipping_cost"));
        order.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        order.setFinalSaleAmount(rs.getBigDecimal("final_sale_amount"));
        order.setStatus(rs.getString("status"));
        order.setDeliveryMethod(rs.getString("delivery_method"));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setCreditCardNumber(rs.getString("credit_card_number"));
        return order;
    }

    private static List<Order.OrderItem> getOrderItemsByOrderId(int orderId) throws SQLException {
        List<Order.OrderItem> orderItems = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order.OrderItem item = new Order.OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setCategory(rs.getString("category"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getBigDecimal("price"));
                    orderItems.add(item);
                }
            }
        }
        return orderItems;
    }

    // You might want to add these utility methods as well

    public static boolean orderExists(int orderId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static boolean isUserOrder(int userId, int orderId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Salesman Order Management Methods
    public static List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                order.setOrderItems(getOrderItemsByOrderId(order.getId()));
                orders.add(order);
            }
        }
        return orders;
    }

    public static Order getOrderById(int orderId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = extractOrderFromResultSet(rs);
                    order.setOrderItems(getOrderItemsByOrderId(order.getId()));
                    return order;
                }
            }
        }
        return null;
    }

    public static void updateOrder(Order updatedFields) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE orders SET ");
        List<Object> params = new ArrayList<>();

        if (updatedFields.getUserId() != 0) {
            sql.append("user_id = ?, ");
            params.add(updatedFields.getUserId());
        }
        if (updatedFields.getStoreId() != 0) {
            sql.append("store_id = ?, ");
            params.add(updatedFields.getStoreId());
        }
        if (updatedFields.getTotalAmount() != null) {
            sql.append("total_amount = ?, ");
            params.add(updatedFields.getTotalAmount());
        }
        if (updatedFields.getShippingCost() != null) {
            sql.append("shipping_cost = ?, ");
            params.add(updatedFields.getShippingCost());
        }
        if (updatedFields.getDiscountAmount() != null) {
            sql.append("discount_amount = ?, ");
            params.add(updatedFields.getDiscountAmount());
        }
        if (updatedFields.getFinalSaleAmount() != null) {
            sql.append("final_sale_amount = ?, ");
            params.add(updatedFields.getFinalSaleAmount());
        }
        if (updatedFields.getStatus() != null) {
            sql.append("status = ?, ");
            params.add(updatedFields.getStatus());
        }
        if (updatedFields.getDeliveryMethod() != null) {
            sql.append("delivery_method = ?, ");
            params.add(updatedFields.getDeliveryMethod());
        }
        if (updatedFields.getShippingAddress() != null) {
            sql.append("shipping_address = ?, ");
            params.add(updatedFields.getShippingAddress());
        }
        if (updatedFields.getCreditCardNumber() != null) {
            sql.append("credit_card_number = ?, ");
            params.add(updatedFields.getCreditCardNumber());
        }
        if (updatedFields.getShipDate() != null) {
            sql.append("ship_date = ?, ");
            params.add(new java.sql.Date(updatedFields.getShipDate().getTime()));
        }

        // Remove the trailing comma and space
        sql.setLength(sql.length() - 2);

        sql.append(" WHERE id = ?");
        params.add(updatedFields.getId());

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating order failed, no rows affected.");
            }

            // Update order items if provided
            if (updatedFields.getOrderItems() != null && !updatedFields.getOrderItems().isEmpty()) {
                updateOrderItems(updatedFields);
            }
        }
    }

    private static void updateOrderItems(Order order) throws SQLException {
        // First, delete existing order items
        String deleteSql = "DELETE FROM order_items WHERE order_id = ?";

        // Then, insert new order items
        String insertSql = "INSERT INTO order_items (order_id, product_id, category, quantity, price) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                    PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

                deleteStmt.setInt(1, order.getId());
                deleteStmt.executeUpdate();

                for (Order.OrderItem item : order.getOrderItems()) {
                    insertStmt.setInt(1, order.getId());
                    insertStmt.setInt(2, item.getProductId());
                    insertStmt.setString(3, item.getCategory());
                    insertStmt.setInt(4, item.getQuantity());
                    insertStmt.setBigDecimal(5, item.getPrice());
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static void deleteOrder(int orderId) throws SQLException {
        String deleteOrderItemsSql = "DELETE FROM order_items WHERE order_id = ?";
        String deleteOrderSql = "DELETE FROM orders WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteItemsStmt = conn.prepareStatement(deleteOrderItemsSql);
                    PreparedStatement deleteOrderStmt = conn.prepareStatement(deleteOrderSql)) {

                deleteItemsStmt.setInt(1, orderId);
                deleteItemsStmt.executeUpdate();

                deleteOrderStmt.setInt(1, orderId);
                int affectedRows = deleteOrderStmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Deleting order failed, no rows affected.");
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static boolean userExistsById(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Analytics-related methods

    public static List<Product> getTopLikedProducts(int limit) throws SQLException {
        // First, get the average ratings from MongoDB
        Map<String, Double> productRatings = MongoDBDataStoreUtilities.getAverageProductRatings();

        // Now, fetch the products from MySQL and combine with ratings
        String sql = "SELECT * FROM products";
        List<Product> products = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Product product = extractProductFromResultSet(rs);
                String productId = String.valueOf(product.getId());
                if (productRatings.containsKey(productId)) {
                    product.setAverageRating(productRatings.get(productId));
                }
                products.add(product);
            }
        }

        // Sort products by average rating and limit to top 5
        products.sort((p1, p2) -> Double.compare(p2.getAverageRating(), p1.getAverageRating()));
        return products.subList(0, Math.min(limit, products.size()));
    }

    public static List<Map<String, Object>> getTopSellingZipCodes(int limit) throws SQLException {
        String sql = "SELECT s.zip_code, COUNT(o.id) as total_orders, SUM(o.final_sale_amount) as total_sales " +
                "FROM stores s " +
                "JOIN orders o ON s.id = o.store_id " +
                "WHERE o.status != 'CANCELLED' " +
                "GROUP BY s.zip_code " +
                "ORDER BY total_sales DESC " +
                "LIMIT ?";

        List<Map<String, Object>> zipCodes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> zipCodeInfo = new HashMap<>();
                    zipCodeInfo.put("zipCode", rs.getString("zip_code"));
                    zipCodeInfo.put("totalOrders", rs.getInt("total_orders"));
                    zipCodeInfo.put("totalSales", rs.getBigDecimal("total_sales"));
                    zipCodes.add(zipCodeInfo);
                }
            }
        }

        return zipCodes;
    }

    public static List<Product> getBestSellingProducts(int limit) throws SQLException {
        String sql = "SELECT p.*, SUM(oi.quantity) as total_sold " +
                "FROM products p " +
                "JOIN order_items oi ON p.id = oi.product_id " +
                "JOIN orders o ON oi.order_id = o.id " +
                "WHERE o.status != 'CANCELLED' " +
                "GROUP BY p.id " +
                "ORDER BY total_sold DESC " +
                "LIMIT ?";

        List<Product> products = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(extractProductFromResultSet(rs));
                }
            }
        }

        return products;
    }

    // Inventory Report

    public static List<InventoryReportDTO> getAllProductsWithInventory() throws SQLException {
        List<InventoryReportDTO> products = new ArrayList<>();
        String sql = "SELECT p.*, COALESCE(i.quantity, 0) as inventory_count, " +
                "d1.amount as discount_amount, d2.amount as rebate_amount " +
                "FROM products p " +
                "LEFT JOIN inventory i ON p.id = i.product_id " +
                "LEFT JOIN (SELECT product_id, amount FROM discounts WHERE discount_type = 'RETAILER_SPECIAL' AND CURDATE() BETWEEN start_date AND end_date) d1 ON p.id = d1.product_id "
                +
                "LEFT JOIN (SELECT product_id, amount FROM discounts WHERE discount_type = 'MANUFACTURER_REBATE' AND CURDATE() BETWEEN start_date AND end_date) d2 ON p.id = d2.product_id";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                InventoryReportDTO product = new InventoryReportDTO();
                product.setId(rs.getInt("id"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setDescription(rs.getString("description"));
                product.setManufacturerName(rs.getString("manufacturer_name"));
                product.setInventoryCount(rs.getInt("inventory_count"));
                product.setDiscountAmount(rs.getBigDecimal("discount_amount"));
                product.setRebateAmount(rs.getBigDecimal("rebate_amount"));
                // Set average rating if you have it, otherwise leave it as 0.0

                products.add(product);
            }
        }
        return products;
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static List<Map<String, Object>> getProductsOnSale() throws SQLException {
        List<Map<String, Object>> productsOnSale = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.price, d.amount as discount_amount, d.start_date, d.end_date " +
                "FROM products p " +
                "JOIN discounts d ON p.id = d.product_id " +
                "WHERE d.discount_type = 'RETAILER_SPECIAL' AND CURDATE() BETWEEN d.start_date AND d.end_date";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> product = new LinkedHashMap<>();
                product.put("id", rs.getInt("id"));
                product.put("name", rs.getString("name"));
                product.put("price", rs.getBigDecimal("price"));
                product.put("discount_amount", rs.getBigDecimal("discount_amount"));
                product.put("start_date", DATE_FORMAT.format(rs.getDate("start_date")));
                product.put("end_date", DATE_FORMAT.format(rs.getDate("end_date")));
                productsOnSale.add(product);
            }
        }
        return productsOnSale;
    }

    public static List<Map<String, Object>> getProductsWithRebates() throws SQLException {
        List<Map<String, Object>> productsWithRebates = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.price, d.amount as rebate_amount, d.start_date, d.end_date " +
                "FROM products p " +
                "JOIN discounts d ON p.id = d.product_id " +
                "WHERE d.discount_type = 'MANUFACTURER_REBATE' AND CURDATE() BETWEEN d.start_date AND d.end_date";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> product = new LinkedHashMap<>();
                product.put("id", rs.getInt("id"));
                product.put("name", rs.getString("name"));
                product.put("price", rs.getBigDecimal("price"));
                product.put("rebate_amount", rs.getBigDecimal("rebate_amount"));
                product.put("start_date", DATE_FORMAT.format(rs.getDate("start_date")));
                product.put("end_date", DATE_FORMAT.format(rs.getDate("end_date")));
                productsWithRebates.add(product);
            }
        }
        return productsWithRebates;
    }

    public static List<Map<String, Object>> getProductInventoryCounts() throws SQLException {
        List<Map<String, Object>> inventoryCounts = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.price, COALESCE(i.quantity, 0) as inventory_count " +
                "FROM products p " +
                "LEFT JOIN inventory i ON p.id = i.product_id";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> product = new LinkedHashMap<>();
                product.put("id", rs.getInt("id"));
                product.put("name", rs.getString("name"));
                product.put("price", rs.getBigDecimal("price"));
                product.put("inventory_count", rs.getInt("inventory_count"));
                inventoryCounts.add(product);
            }
        }
        return inventoryCounts;
    }

    private static InventoryReportDTO extractInventoryReportDTOFromResultSet(ResultSet rs) throws SQLException {
        InventoryReportDTO dto = new InventoryReportDTO();
        dto.setId(rs.getInt("id"));
        dto.setCategoryId(rs.getInt("category_id"));
        dto.setName(rs.getString("name"));
        dto.setPrice(rs.getBigDecimal("price"));
        dto.setDescription(rs.getString("description"));
        dto.setManufacturerName(rs.getString("manufacturer_name"));
        dto.setInventoryCount(rs.getInt("inventory_count"));
        dto.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        dto.setRebateAmount(rs.getBigDecimal("rebate_amount"));
        return dto;
    }

    public static InventoryDTO getInventoryById(int id) throws SQLException {
        String sql = "SELECT product_id, quantity FROM inventory WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new InventoryDTO(rs.getInt("product_id"), rs.getInt("quantity"));
                }
            }
        }
        return null;
    }

    public static void addInventory(InventoryDTO inventoryItem) throws SQLException {
        String sql = "INSERT INTO inventory (product_id, quantity) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, inventoryItem.getId());
            pstmt.setInt(2, inventoryItem.getInventoryCount());
            pstmt.executeUpdate();
        }
    }

    public static void updateInventory(InventoryDTO inventoryItem) throws SQLException {
        String sql = "INSERT INTO inventory (product_id, quantity) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = VALUES(quantity)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, inventoryItem.getId());
            pstmt.setInt(2, inventoryItem.getInventoryCount());
            pstmt.executeUpdate();
        }
    }

    public static void deleteInventory(int productId) throws SQLException {
        String sql = "DELETE FROM inventory WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting inventory failed, no rows affected.");
            }
        }
    }

    // Sales Report Methods
    public static List<SalesReportDTO.ProductSalesSummary> getProductSalesSummary() throws SQLException {
        List<SalesReportDTO.ProductSalesSummary> summary = new ArrayList<>();
        String sql = "SELECT * FROM product_sales_summary";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                SalesReportDTO.ProductSalesSummary item = new SalesReportDTO.ProductSalesSummary();
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setProductPrice(rs.getBigDecimal("product_price"));
                item.setItemsSold(rs.getInt("items_sold"));
                item.setTotalSales(rs.getBigDecimal("total_sales"));
                summary.add(item);
            }
        }
        return summary;
    }

    public static List<SalesReportDTO.DailySalesTransaction> getDailySalesTransactions() throws SQLException {
        List<SalesReportDTO.DailySalesTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM daily_sales_transactions";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            while (rs.next()) {
                SalesReportDTO.DailySalesTransaction transaction = new SalesReportDTO.DailySalesTransaction();
                transaction.setSaleDate(rs.getDate("sale_date").toLocalDate().format(formatter));
                transaction.setProductId(rs.getInt("product_id"));
                transaction.setProductName(rs.getString("product_name"));
                transaction.setItemsSold(rs.getInt("items_sold"));
                transaction.setTotalSales(rs.getBigDecimal("total_sales"));
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    // Add these methods to your existing MySQLDataStoreUtilities class

    public static Ticket createTicket(Ticket ticket) throws SQLException {
        String sql = "INSERT INTO tickets (ticket_number, user_id, order_id, description, image_path, status, decision) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, ticket.getTicketNumber());
            pstmt.setInt(2, ticket.getUserId());

            if (ticket.getOrderId() != null) {
                pstmt.setInt(3, ticket.getOrderId());
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }

            pstmt.setString(4, ticket.getDescription());
            pstmt.setString(5, ticket.getImagePath());
            pstmt.setString(6, ticket.getStatus().toString());

            // Ensure decision is set
            if (ticket.getDecision() != null) {
                pstmt.setString(7, ticket.getDecision().toString());
            } else {
                pstmt.setString(7, Ticket.Decision.ESCALATE_TO_HUMAN.toString());
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating ticket failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ticket.setId(generatedKeys.getInt(1));
                    return ticket;
                } else {
                    throw new SQLException("Creating ticket failed, no ID obtained.");
                }
            }
        }
    }

    
    public static Ticket getTicketByNumber(String ticketNumber) throws SQLException {
        String sql = "SELECT * FROM tickets WHERE ticket_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ticketNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractTicketFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public static void updateTicketDecision(String ticketNumber, Ticket.Decision decision) throws SQLException {
        String sql = "UPDATE tickets SET decision = ?, status = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE ticket_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, decision.toString());
            pstmt.setString(2, Ticket.Status.RESOLVED.toString());
            pstmt.setString(3, ticketNumber);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating ticket failed, no rows affected.");
            }
        }
    }

    public static List<Ticket> getUserTickets(int userId) throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM tickets WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(extractTicketFromResultSet(rs));
                }
            }
        }
        return tickets;
    }

    private static Ticket extractTicketFromResultSet(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getInt("id"));
        ticket.setTicketNumber(rs.getString("ticket_number"));
        ticket.setUserId(rs.getInt("user_id"));

        int orderId = rs.getInt("order_id");
        if (!rs.wasNull()) {
            ticket.setOrderId(orderId);
        }

        ticket.setDescription(rs.getString("description"));
        ticket.setImagePath(rs.getString("image_path"));
        ticket.setStatus(Ticket.Status.valueOf(rs.getString("status")));

        String decision = rs.getString("decision");
        if (decision != null) {
            try {
                ticket.setDecision(Ticket.Decision.valueOf(decision));
            } catch (IllegalArgumentException e) {
                LOGGER.warning("Invalid decision value in database: " + decision);
                ticket.setDecision(Ticket.Decision.ESCALATE_TO_HUMAN);
            }
        } else {
            ticket.setDecision(Ticket.Decision.ESCALATE_TO_HUMAN);
        }

        ticket.setCreatedAt(rs.getTimestamp("created_at"));
        ticket.setUpdatedAt(rs.getTimestamp("updated_at"));
        return ticket;
    }

}