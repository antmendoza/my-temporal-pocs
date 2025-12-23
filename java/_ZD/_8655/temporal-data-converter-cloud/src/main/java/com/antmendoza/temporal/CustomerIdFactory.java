package com.antmendoza.temporal;

import com.antmendoza.temporal.codec.Customer;

public class CustomerIdFactory {


    public static Customer get() {


        Customer firstname1_surname1_lastSurname = new Customer("12345", "Hello Temporal!!");

        return firstname1_surname1_lastSurname;
    }

}
