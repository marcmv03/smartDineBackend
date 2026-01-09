package com.smartDine.dto;

import com.smartDine.entity.Customer;

/**
 * DTO for customer search results.
 * Contains customer information plus relationship status with the authenticated user.
 */
public class CustomerSearchDTO {
    
    private Long id;
    private String name;
    private String email;
    private boolean isFriend;
    private boolean hasPendingRequest;

    public CustomerSearchDTO() {
    }

    public CustomerSearchDTO(Long id, String name, String email, boolean isFriend, boolean hasPendingRequest) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.isFriend = isFriend;
        this.hasPendingRequest = hasPendingRequest;
    }

    // Getters and Setters
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

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean isFriend) {
        this.isFriend = isFriend;
    }

    public boolean isHasPendingRequest() {
        return hasPendingRequest;
    }

    public void setHasPendingRequest(boolean hasPendingRequest) {
        this.hasPendingRequest = hasPendingRequest;
    }

    /**
     * Creates a CustomerSearchDTO from a Customer entity.
     * Note: isFriend and hasPendingRequest must be set separately after creation.
     * 
     * @param customer The customer entity
     * @return CustomerSearchDTO with basic info (isFriend and hasPendingRequest default to false)
     */
    public static CustomerSearchDTO fromEntity(Customer customer) {
        CustomerSearchDTO dto = new CustomerSearchDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setFriend(false);
        dto.setHasPendingRequest(false);
        return dto;
    }
}
