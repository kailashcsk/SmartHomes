package com.smarthomes.dao;

import com.smarthomes.models.Category;
import com.smarthomes.util.MySQLDataStoreUtilities;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CategoryDAO {
    private static final Logger LOGGER = Logger.getLogger(CategoryDAO.class.getName());

    public List<Category> getAllCategories() throws SQLException {
        return MySQLDataStoreUtilities.getAllCategories();
    }

    public Category getCategoryById(int id) throws SQLException {
        return MySQLDataStoreUtilities.getCategoryById(id);
    }

    public void createCategory(Category category) throws SQLException {
        MySQLDataStoreUtilities.createCategory(category);
    }

    public void updateCategory(Category category) throws SQLException {
        MySQLDataStoreUtilities.updateCategory(category);
    }

    public void deleteCategory(int id) throws SQLException {
        MySQLDataStoreUtilities.deleteCategory(id);
    }

    public boolean categoryExists(int categoryId) throws SQLException {
        return MySQLDataStoreUtilities.categoryExists(categoryId);
    }
}