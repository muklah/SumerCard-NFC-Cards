package com.example.myapplication;

class Customer {

    private String customerId;
    private String customerBalance;

    public Customer() {}

    public Customer(String customerId, String customerBalance) {
        this.customerId = customerId;
        this.customerBalance = customerBalance;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerBalance() {
        return customerBalance;
    }

    public void setCustomerBalance(String customerBalance) {
        this.customerBalance = customerBalance;
    }
}
