package com.smartDine.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartDine.entity.Customer;
import com.smartDine.repository.CustomersRepository;

@Service
public class CustomersService {

    @Autowired
    private CustomersRepository clientsRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Registers a new client.
     *
     * @param name   The client's name.
     * @param number The client's phone number.
     * @param email  The client's email address.
     * @param password The client's password.
     * @return The created client object.
     * @throws IllegalArgumentException if a client with the same email or phone number already exists.
     */
    public Customer createClient(String name, String email, String password, Long number) {
        // Acceptance criteria: The system must verify that no user with the same name, email, or phone number exists.
        // Scenario 2 and 3: Error for already registered email or phone number.
        Optional<Customer> existingClient = clientsRepository.findByEmailOrNumber(email, number);
        if (existingClient.isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente con este correo electrónico o número de teléfono.");
        }
        
        // Encrypt password before saving
        String encryptedPassword = passwordEncoder.encode(password);
        Customer newClient = new Customer(name, email, encryptedPassword, number);
        return clientsRepository.save(newClient);
    }

    /**
     * Authenticates a client with email and password.
     *
     * @param email The client's email.
     * @param password The client's password.
     * @return The authenticated client.
     * @throws IllegalArgumentException if credentials are invalid.
     */
    public Customer authenticateClient(String email, String password) {
        Optional<Customer> clientOpt = clientsRepository.findByEmail(email);
        if (clientOpt.isEmpty()) {
            throw new IllegalArgumentException("Credenciales inválidas.");
        }

        Customer client = clientOpt.get();
        if (!passwordEncoder.matches(password, client.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas.");
        }

        return client;
    }

    /**
     * Updates a client's password.
     *
     * @param clientId The ID of the client.
     * @param oldPassword The current password.
     * @param newPassword The new password.
     * @throws IllegalArgumentException if the client is not found or old password is incorrect.
     */
    public void updatePassword(Long clientId, String oldPassword, String newPassword) {
        Customer client = clientsRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));

        if (!passwordEncoder.matches(oldPassword, client.getPassword())) {
            throw new IllegalArgumentException("Contraseña actual incorrecta.");
        }

        client.setPassword(passwordEncoder.encode(newPassword));
        clientsRepository.save(client);
    }

    /**
     * Updates a client's information.
     *
     * @param clientId The ID of the client to update.
     * @param name     The new name.
     * @param number   The new phone number.
     * @param email    The new email address.
     * @return The updated client.
     * @throws IllegalArgumentException if the client is not found or the data is already in use.
     */
    public Customer updateClient(long clientId, String name, String email , Long number) {
        // Acceptance criteria: Verify if the client exists.
        Customer clientToUpdate = clientsRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));

        // Acceptance criteria: The new email cannot be that of an existing client.
        Optional<Customer> existingEmail = clientsRepository.findByEmail(email);
        if (existingEmail.isPresent() && !existingEmail.get().getId().equals(clientId)) {
            throw new IllegalArgumentException("El correo electrónico ya está en uso.");
        }

        Optional<Customer> existingNumber = clientsRepository.findByNumber(number);
        if (existingNumber.isPresent() && !existingNumber.get().getId().equals(clientId)) {
            throw new IllegalArgumentException("El número de teléfono ya está en uso.");
        }

        // Update client data.
        if (name != null) clientToUpdate.setName(name);
        if (number != null) clientToUpdate.setNumber(number);
        if (email != null) clientToUpdate.setEmail(email);

        return clientsRepository.save(clientToUpdate);
    }

    /**
     * Deletes a client.
     *
     * @param clientId The ID of the client to delete.
     * @throws IllegalArgumentException if the client does not exist.
     */
    public void deleteClient(Long clientId) {
        // Acceptance criteria: The client must exist.
        // L'original feia servir findById(clientId) que retorna Optional<ObjectClients>
        // La comprovació amb un try-catch de NullPointerException no és correcta per al tipus de retorn
        Customer client = clientsRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found."));

        clientsRepository.delete(client);
    }

    /**
     * Finds a client by email.
     *
     * @param email The client's email.
     * @return Optional containing the client if found.
     */
    public Optional<Customer> findByEmail(String email) {
        return clientsRepository.findByEmail(email);
    }

    /**
     * Finds clients by name or returns all if no search parameter is provided.
     *
     * @param search The name or part of the name to search for.
     * @return A list of clients.
     */
    public List<Customer> findClients(String search) {
        if (search == null || search.isEmpty()) {
            return clientsRepository.findAll();
        }
        return clientsRepository.findByNameContainingIgnoreCase(search);
    }
}