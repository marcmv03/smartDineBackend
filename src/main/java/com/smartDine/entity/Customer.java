package com.smartDine.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a client in the database.
 * This class maps to the "Clients" table.
 */
@Entity
@Table(name = "Customers")
@Data // Lombok annotation that generates getters, setters, toString, equals, etc.
@Getter
@Setter
@Builder // Implements the Builder design pattern.
@AllArgsConstructor // Generates a constructor with all arguments.
@NoArgsConstructor // Generates an empty constructor.
public class Customer implements Serializable {

    /**
     * Unique identifier for the client.
     * It is the primary key (PK) and is automatically generated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Full name of the client.
     * Cannot be null.
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Email address of the client.
     * Must be unique and cannot be null.
     */
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Client's password.
     * It is recommended to store it hashed.
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * Client's phone number.
     * It is optional but must be unique if specified.
     */
    @Column(name = "number", unique = true, length = 20)
    private Long  number;

    public Customer(String name, String email, String password, Long number) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.number = number;
    }
}