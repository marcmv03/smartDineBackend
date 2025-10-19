package com.smartDine.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
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

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    public Customer() {
        super();
        this.setRole("customer");
    }
    public Customer(String name, String email, String password, Long phoneNumber) {
        super(name, email, password, phoneNumber);
        this.setRole("customer");
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}
