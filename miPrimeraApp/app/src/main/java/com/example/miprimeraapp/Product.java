package com.example.miprimeraapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Product {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String category;
    private double price;
    private String description;
    private String specs; 
    private int imageResource;
    private String imageUri;

    public Product() {
    }

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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public String getSpecs() { return specs; }
    public int getImageResource() { return imageResource; }
    public String getImageUri() { return imageUri; }

    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(double price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setSpecs(String specs) { this.specs = specs; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}
