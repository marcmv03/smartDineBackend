package com.smartDine.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * Entity representing a client in the database.
 * This class maps to the "Clients" table.
 */
@Entity
@Table(name = "Customers")
@PrimaryKeyJoinColumn(name = "user_id") // <-- Links this table's PK to the User table's PK
public class Customer extends User  {
    public Customer() {
        super();
        this.setRole("customer");
    }
    public Customer(String name, String email, String password, Long phoneNumber) {
        super(name, email, password, phoneNumber);
        this.setRole("customer");
    }
}