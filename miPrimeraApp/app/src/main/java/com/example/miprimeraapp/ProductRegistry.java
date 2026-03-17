package com.example.miprimeraapp;

import java.util.ArrayList;
import java.util.List;

public class ProductRegistry {
    private static List<Product> allProducts = new ArrayList<>();

    static {
        int icon = R.mipmap.ic_launcher;
        allProducts.add(new Product("iPhone 15 Pro", "Alta", 1199.0, "Titanio aeroespacial.", "• Pantalla: 6.1\" OLED\n• Chip: A17 Pro\n• Cámara: 48MP Principal", icon));
        allProducts.add(new Product("S24 Ultra", "Alta", 1299.0, "Galaxy AI Integrada.", "• Pantalla: 6.8\" Dynamic AMOLED\n• Cámara: 200MP\n• S-Pen incluido", icon));
        allProducts.add(new Product("Galaxy A54", "Media", 450.0, "Pantalla Super AMOLED.", "• Pantalla: 6.4\"\n• Batería: 5000 mAh", icon));
        allProducts.add(new Product("Redmi Note 13", "Media", 300.0, "Cámara 108MP.", "• Carga rápida 67W", icon));
    }

    public static Product getProductByName(String name) {
        for (Product p : allProducts) {
            if (p.getName().equals(name)) return p;
        }
        return null;
    }

    public static List<Product> getAllProducts() {
        return allProducts;
    }
}
