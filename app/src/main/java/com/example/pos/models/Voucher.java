package com.example.pos.models;

public class Voucher {
    private long id;
    private String code;
    private double amount;
    private String status;

    public Voucher(long id, String code, double amount, String status) {
        this.id = id;
        this.code = code;
        this.amount = amount;
        this.status = status;
    }

    public long getId() { return id; }
    public String getCode() { return code; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
}