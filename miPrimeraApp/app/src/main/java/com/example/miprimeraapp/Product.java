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
    private String imageUri2;
    private String imageUri3;

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
        this.imageUri2 = null;
        this.imageUri3 = null;
    }

    public Product(String name, String category, double price, String description, String specs, String imageUri) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.specs = specs;
        this.imageResource = 0;
        this.imageUri = imageUri;
        this.imageUri2 = null;
        this.imageUri3 = null;
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
    public String getImageUri2() { return imageUri2; }
    public String getImageUri3() { return imageUri3; }

    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(double price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setSpecs(String specs) { this.specs = specs; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public void setImageUri2(String imageUri2) { this.imageUri2 = imageUri2; }
    public void setImageUri3(String imageUri3) { this.imageUri3 = imageUri3; }
}
