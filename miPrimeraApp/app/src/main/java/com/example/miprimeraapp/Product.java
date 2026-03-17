package com.example.miprimeraapp;

public class Product {
    private String name;
    private String category;
    private double price;
    private String description;
    private String specs; // Added for more specifications
    private int imageResource;

    public Product(String name, String category, double price, String description, String specs, int imageResource) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.specs = specs;
        this.imageResource = imageResource;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public String getSpecs() { return specs; }
    public int getImageResource() { return imageResource; }
}
