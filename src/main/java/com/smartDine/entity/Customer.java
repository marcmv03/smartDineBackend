package com.smartDine.entity;
import jakarta.persistence.Entity;


/**
 * Entity representing a client in the database.
 * This class extends User and inherits the table mapping.
 */
@Entity
public class Customer extends User {
    
    public Customer() {
        super();
    }
    
    public Customer(String name, String email, String password, Long number) {
        super(name, email, password, number);
    }
}
