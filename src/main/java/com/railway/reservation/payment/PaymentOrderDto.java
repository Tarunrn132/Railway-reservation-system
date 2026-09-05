package com.railway.reservation.payment;

public class PaymentOrderDto {
    private String orderId;
    private Double amount;
    private String currency;
    private String receiptId;
    private String status;
    private String gateway;
    private String keyId;

    public PaymentOrderDto() {}

    public PaymentOrderDto(String orderId, Double amount, String currency, String receiptId, String status, String gateway, String keyId) {
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.receiptId = receiptId;
        this.status = status;
        this.gateway = gateway;
        this.keyId = keyId;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }

    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }
}
