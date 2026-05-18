package com.warehouse.model;

public class Product {
    private final int id;
    private final String name;
    private final String category;
    private final int quantity;
    private final String unit;
    private final double price;
    private final String arrivalDate;
    private final String expirationDate;
    private final String location;

    public Product(int id, String name, String category, int quantity, String unit, double price,
                   String arrivalDate, String expirationDate, String location) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.price = price;
        this.arrivalDate = arrivalDate;
        this.expirationDate = expirationDate;
        this.location = location;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public double getPrice() { return price; }
    public String getArrivalDate() { return arrivalDate; }
    public String getExpirationDate() { return expirationDate; }
    public String getLocation() { return location; }
}