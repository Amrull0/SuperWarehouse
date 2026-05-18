package com.warehouse.model;

public class StockOperation {
    private final int id;
    private final String operationType;
    private final int productId;
    private final String productName;
    private final int quantity;
    private final String unit;
    private final double unitPrice;
    private final double totalPrice;
    private final String operationDate;
    private final String username;
    private final String note;

    public StockOperation(int id, String operationType, int productId, String productName, int quantity,
                          String unit, double unitPrice, double totalPrice, String operationDate,
                          String username, String note) {
        this.id = id;
        this.operationType = operationType;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.operationDate = operationDate;
        this.username = username;
        this.note = note;
    }

    public int getId() { return id; }
    public String getOperationType() { return operationType; }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return totalPrice; }
    public String getOperationDate() { return operationDate; }
    public String getUsername() { return username; }
    public String getNote() { return note; }
}