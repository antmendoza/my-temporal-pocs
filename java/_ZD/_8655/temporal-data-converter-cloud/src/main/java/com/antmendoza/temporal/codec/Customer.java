package com.antmendoza.temporal.codec;

public record Customer(String customerId, String customerName) {


    public String wfId() {
        return "Custom-wid- "+customerId + '-' +customerName.replaceAll(" ", "_");
    }

}
