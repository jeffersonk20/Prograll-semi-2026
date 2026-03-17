package com.example.miprimeraapp;

public class Product {
    private String name;
    private String category;
    private double price;
    private String description;
    private String specs; 
    private int imageResource;
    private String imageUri; // Added to support custom images

    public Product(String name, String category, double price, String description, String specs, int imageResource) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.specs = specs;
        this.imageResource = imageResource;
        this.imageUri = null;
    }

    public Product(String name, String category, double price, String description, String specs, String imageUri) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.specs = specs;
        this.imageResource = 0;
        this.imageUri = imageUri;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public String getSpecs() { return specs; }
    public int getImageResource() { return imageResource; }
    public String getImageUri() { return imageUri; }
}
