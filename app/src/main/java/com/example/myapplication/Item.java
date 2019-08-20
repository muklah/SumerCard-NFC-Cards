package com.example.myapplication;

class Item {
    private String customerId;
    private String paid;

    public Item() {}

    public Item(String customerId, String paid) {
        this.customerId = customerId;
        this.paid = paid;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getPaid() {
        return paid;
    }

    public void setPaid(String paid) {
        this.paid = paid;
    }
}
