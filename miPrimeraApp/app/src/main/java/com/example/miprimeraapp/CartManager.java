package com.example.miprimeraapp;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<Product> cartItems;
    private List<Product> purchasedItems;

    private CartManager() {
        cartItems = new ArrayList<>();
        purchasedItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Product product) {
        cartItems.add(product);
    }

    public void removeFromCart(Product product) {
        cartItems.remove(product);
    }

    public void addToPurchased(Product product) {
        purchasedItems.add(product);
    }

    public List<Product> getCartItems() {
        return cartItems;
    }

    public List<Product> getPurchasedItems() {
        return purchasedItems;
    }

    public void clearCart() {
        cartItems.clear();
    }
}
