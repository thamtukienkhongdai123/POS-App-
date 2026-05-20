package com.example.pos.models;

public class Customer {
    private String name;
    private String phone;
    private String email;
    private String address;
    private int orderCount;
    private double totalSpent;

    public Customer(String name, String phone, double totalSpent) {
        this.name = name;
        this.phone = phone;
        this.totalSpent = totalSpent;
        this.email = "";
        this.address = "";
        this.orderCount = 0;
    }

    public Customer(String name, String phone, String email, String address, int orderCount, double totalSpent) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.orderCount = orderCount;
        this.totalSpent = totalSpent;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public int getOrderCount() { return orderCount; }
    public double getTotalSpent() { return totalSpent; }

    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
}