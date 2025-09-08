package com.smartDine.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;


/**
 * Entity representing a client in the database.
 * This class maps to the "Clients" table.
 */
@Entity
@Table(name = "Customers")
public class Customer extends User {
    public Customer(String name, String email, String password, Long number) {
        super(name, email, password, number);
    }
}
