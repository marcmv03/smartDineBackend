package com.smartDine.dto;

import com.smartDine.entity.Customer;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerDTO {
    private Long id;
    private String name;
    private String password;
    private String email;
    private String phoneNumber;

    public CustomerDTO() {}

    public CustomerDTO(String name, String password, String email, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public static Customer toEntity(CustomerDTO dto) {
        Customer customer = new Customer();
        if (dto.getId() != null) {
            // Note: id is managed by JPA for User entities
        }
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPassword(dto.getPassword());
        if (dto.getPhoneNumber() != null) {
            customer.setPhoneNumber(Long.parseLong(dto.getPhoneNumber()));
        }
        return customer;
    }
    
    public static CustomerDTO fromEntity(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPassword(customer.getPassword());
        if (customer.getPhoneNumber() != null) {
            dto.setPhoneNumber(customer.getPhoneNumber().toString());
        }
        return dto;
    }
    
    public static List<CustomerDTO> fromEntity(List<Customer> customers) {
        return customers.stream()
            .map(CustomerDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
