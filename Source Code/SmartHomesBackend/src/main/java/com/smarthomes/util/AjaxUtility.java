package com.smarthomes.util;

import com.smarthomes.models.Product;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AjaxUtility {
    private static Map<Integer, Product> productMap = new HashMap<>();

    public static void loadProductsToMap(List<Product> products) {
        productMap.clear();
        for (Product product : products) {
            if (product.getId() != 0) {
                productMap.put(product.getId(), product);
            }
        }
    }

    public static List<Product> getAutoCompleteResults(String searchTerm) {
        return productMap.values().stream()
                .filter(product -> product.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                .limit(5)
                .collect(Collectors.toList());
    }

    public static void addOrUpdateProductInMap(Product product) {
        if (product.getId() != 0) {
            productMap.put(product.getId(), product);
        }
    }

    public static void removeProductFromMap(int productId) {
        productMap.remove(productId);
    }

    public static void refreshProductMap(List<Product> products) {
        loadProductsToMap(products);
    }
}