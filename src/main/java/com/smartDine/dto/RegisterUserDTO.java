package com.smartDine.dto;

import jakarta.validation.constraints.Email;
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
    private Long phone;

    // Constructors
    public RegisterUserDTO() {}

    public RegisterUserDTO(String name, String email, String password, Long phone) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public Long getPhone() { return phone; }
    public void setPhone(Long phone) { this.phone = phone; }

}
