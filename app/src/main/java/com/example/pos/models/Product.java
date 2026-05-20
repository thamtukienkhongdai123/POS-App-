package com.example.pos.models;

import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    private long id;
    private String name;
    private String brand;
    private String gender;
    private String imageUrl;
    private List<Variant> variants;

    public Product(String name, String brand, String gender, String imageUrl, List<Variant> variants) {
        this.name = name;
        this.brand = brand;
        this.gender = gender;
        this.imageUrl = imageUrl;
        this.variants = variants;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getGender() { return gender; }
    public String getImageUrl() { return imageUrl; }
    public List<Variant> getVariants() { return variants; }

    public static class Variant implements Serializable {
        private String size;
        private String barcode;
        private double price;
        private int stock;

        public Variant(String size, String barcode, double price, int stock) {
            this.size = size;
            this.barcode = barcode;
            this.price = price;
            this.stock = stock;
        }

        public String getSize() { return size; }
        public String getBarcode() { return barcode; }
        public double getPrice() { return price; }
        public int getStock() { return stock; }
    }
}