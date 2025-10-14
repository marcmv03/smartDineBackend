package com.smartDine.entity;


import jakarta.persistence.Table;
import jakarta.persistence.Id ;
import jakarta.persistence.Column ;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn ;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType ;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Entity
@Table(name = "restaurants")
public class Restaurant {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Getter
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String address;
    @Column(name = "description", length = 1000)
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Business owner;
    @OneToMany
    private List<MenuItem> menu;
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeSlot> timeSlots = new ArrayList<>();
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.smartDine.entity.Table> tables = new ArrayList<>();
    // Constructors
    public Restaurant() {}
    public Restaurant(String name, String address, String description) {
        this.name = name;
        this.address = address;
        this.description = description;
    }


}
