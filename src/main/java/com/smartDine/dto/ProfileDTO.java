package com.smartDine.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.smartDine.entity.Customer;
import com.smartDine.entity.Role;
import com.smartDine.entity.User;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private Role role ;
    public ProfileDTO() {}


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }
    
    public static Customer toEntity(ProfileDTO dto) {
        Customer customer = new Customer();
        if (dto.getId() != null) {
            // Note: id is managed by JPA for User entities
        }
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) {
            customer.setPhoneNumber(Long.parseLong(dto.getPhoneNumber()));
        }
        return customer;
    }
    
    public static ProfileDTO fromEntity(User user) {
        ProfileDTO dto = new ProfileDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        if (user.getPhoneNumber() != null) {
            dto.setPhoneNumber(user.getPhoneNumber().toString());
        }
        return dto;
    }
    
    public static List<ProfileDTO> fromEntity(List<User> users) {
        return users.stream()
            .map(ProfileDTO::fromEntity)
            .collect(Collectors.toList());
    }
    public static List<Customer> toEntity(List<ProfileDTO> dtos) {
        return dtos.stream()
            .map(ProfileDTO::toEntity)
            .collect(Collectors.toList());
    }
    
}
