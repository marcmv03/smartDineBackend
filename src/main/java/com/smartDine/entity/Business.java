// src/main/java/com/smartdine/entity/Business.java
package com.smartDine.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "businesses")
@PrimaryKeyJoinColumn(name = "user_id") // <-- Links this table's PK to the User table's PK
public class Business extends User {
    // Constructors, Getters, Setters...
    public Business() {
        super();
        this.setRole("business");
    }
    public Business(String name, String email, String password, Long phoneNumber) {
        super(name, email, password, phoneNumber);
        this.setRole("business");
    }
}