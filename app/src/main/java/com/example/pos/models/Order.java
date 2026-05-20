package com.example.pos.models;

public class Order {
    private String orderId;
    private String date;
    private String customer;
    private String staff;
    private double total;
    private String status;
    private double discount;

    public Order(String orderId, String date, String customer, String staff, double total, String status) {
        this.orderId = orderId;
        this.date = date;
        this.customer = customer;
        this.staff = staff;
        this.total = total;
        this.status = status;
        this.discount = 0;
    }

    public Order(String orderId, String date, String customer, String staff, double total, String status, double discount) {
        this.orderId = orderId;
        this.date = date;
        this.customer = customer;
        this.staff = staff;
        this.total = total;
        this.status = status;
        this.discount = discount;
    }

    public String getOrderId() { return orderId; }
    public String getDate() { return date; }
    public String getCustomer() { return customer; }
    public String getStaff() { return staff; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public double getDiscount() { return discount; }
}