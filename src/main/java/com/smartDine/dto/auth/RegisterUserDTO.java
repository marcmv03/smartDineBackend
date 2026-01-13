package com.smartDine.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegisterUserDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    
    @Email(message = "Formato de email inválido")
    @NotBlank(message = "El email es obligatorio")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
    
    @NotNull(message = "El número de teléfono es obligatorio")
    @Min(value = 100000000L, message = "El número de teléfono debe tener al menos 9 dígitos")
    @Max(value = 999999999999L, message = "El número de teléfono no puede tener más de 12 dígitos")
    private Long phoneNumber;

    // Constructors
    public RegisterUserDTO() {}

    public RegisterUserDTO(String name, String email, String password, Long phoneNumber) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public Long getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(Long phone) { this.phoneNumber = phone; }

}
