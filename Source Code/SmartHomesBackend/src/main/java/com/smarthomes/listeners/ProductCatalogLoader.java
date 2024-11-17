package com.smarthomes.listeners;

import com.smarthomes.models.Product;
import com.smarthomes.dao.ProductDAO;
import com.smarthomes.util.AjaxUtility;
import org.w3c.dom.*;
import javax.servlet.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;
import java.math.BigDecimal;
import java.sql.SQLException;

public class ProductCatalogLoader implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String catalogPath = sce.getServletContext().getRealPath("/WEB-INF/ProductCatalog.xml");
        List<Product> xmlProducts = parseProductCatalog(catalogPath);

        ProductDAO productDAO = new ProductDAO();
        try {
            // Load existing products from database
            List<Product> dbProducts = productDAO.getAllProducts();
            Map<String, Product> productMap = new HashMap<>();

            // Add existing DB products to map
            for (Product dbProduct : dbProducts) {
                productMap.put(dbProduct.getName().toLowerCase(), dbProduct);
            }

            // Process XML products
            for (Product xmlProduct : xmlProducts) {
                String key = xmlProduct.getName().toLowerCase();
                if (!productMap.containsKey(key)) {
                    // New product, add to DB
                    productDAO.createProduct(xmlProduct);
                    productMap.put(key, xmlProduct);
                }
            }

            // Load all products into AjaxUtility HashMap
            AjaxUtility.loadProductsToMap(new ArrayList<>(productMap.values()));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Product> parseProductCatalog(String filePath) {
        List<Product> products = new ArrayList<>();
        try {
            File file = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("product");
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    Product product = new Product();
                    product.setName(eElement.getElementsByTagName("name").item(0).getTextContent());
                    product.setPrice(new BigDecimal(eElement.getElementsByTagName("price").item(0).getTextContent()));
                    product.setDescription(eElement.getElementsByTagName("description").item(0).getTextContent());
                    product.setManufacturerName(eElement.getElementsByTagName("manufacturer").item(0).getTextContent());
                    // You might need to handle category mapping here
                    products.add(product);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup code if needed
    }
}