package com.smartDine.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartDine.entity.Customer;
import com.smartDine.repository.CustomerRepository;

@Service
public class CustomerService  {

    @Autowired
    private CustomerRepository clientsRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


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
    public Customer getCustomerById(Long id) {
        return clientsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + id));
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

        Optional<Customer> existingNumber = clientsRepository.findByPhoneNumber(number);
        if (existingNumber.isPresent() && !existingNumber.get().getId().equals(clientId)) {
            throw new IllegalArgumentException("El número de teléfono ya está en uso.");
        }

        // Update client data.
        if (name != null) clientToUpdate.setName(name);
        if (number != null) clientToUpdate.setPhoneNumber(number);
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