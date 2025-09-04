package com.smartDine.services;
import com.smartDine.entity.Customer ;
import com.smartDine.repository.CustomersRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomersServiceTest {

    @Mock
    private CustomersRepository clientsRepository;

    @InjectMocks
    private CustomersService clientsService;

    private Customer client1;
    private Customer client2;

    /**
     * Set up method to initialize common objects before each test.
     */
    @BeforeEach
    void setUp() {
        client1 = new Customer("John Doe", "john.doe@test.com", "password123", 123456789L);
        client1.setId(1L);

        client2 = new Customer("Jane Smith", "jane.smith@test.com", "password456", 987654321L);
        client2.setId(2L);
    }

    /**
     * Tests the correct client registration scenario.
     */
    @Test
    void createClient_CorrectRegistration_ReturnsClient() {
        // Arrange: Prepare the mock behavior for a successful scenario.
        when(clientsRepository.findByEmailOrNumber(anyString(), anyLong())).thenReturn(Optional.empty());
        when(clientsRepository.save(any(Customer.class))).thenReturn(client1);

        // Act: Call the service method to be tested.
        Customer createdClient = clientsService.createClient("John Doe", "john.doe@test.com","12345",123456789L);

        // Assert: Verify the results and the interactions with the mock repository.
        assertNotNull(createdClient);
        assertEquals("John Doe", createdClient.getName());
        verify(clientsRepository, times(1)).findByEmailOrNumber("john.doe@test.com", 123456789L);
        verify(clientsRepository, times(1)).save(any(Customer.class));
    }

    /**
     * Tests the scenario where an existing email is used during registration.
     */
    @Test
    void createClient_ExistingEmail_ThrowsException() {
        // Arrange
        when(clientsRepository.findByEmailOrNumber(anyString(), anyLong())).thenReturn(Optional.of(client1));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                clientsService.createClient("Another User", "john.doe@test.com", "12345", 111222333L));

        assertEquals("Ya existe un cliente con este correo electrónico o número de teléfono.", exception.getMessage());
        verify(clientsRepository, times(1)).findByEmailOrNumber("john.doe@test.com", 111222333L);
        verify(clientsRepository, never()).save(any(Customer.class));
    }

    /**
     * Tests the correct client update scenario.
     */
    @Test
    void updateClient_CorrectUpdate_ReturnsUpdatedClient() {
        // Arrange
        Customer updatedInfo = new Customer("John D.", "john.d@test.com", "newpassword", 123456789L);
        updatedInfo.setId(1L);

        when(clientsRepository.findById(1L)).thenReturn(Optional.of(client1));
        when(clientsRepository.findByEmail("john.d@test.com")).thenReturn(Optional.empty());
        when(clientsRepository.findByNumber(123456789L)).thenReturn(Optional.of(client1));
        when(clientsRepository.save(any(Customer.class))).thenReturn(updatedInfo);

        // Act
        Customer result = clientsService.updateClient(1L, "John D.","john.d@test.com", 123456789L);

        // Assert
        assertNotNull(result);
        assertEquals("John D.", result.getName());
        assertEquals("john.d@test.com", result.getEmail());
        verify(clientsRepository, times(1)).findById(1L);
        verify(clientsRepository, times(1)).save(any(Customer.class));
    }

    /**
     * Tests the scenario where the client to update is not found.
     */
    @Test
    void updateClient_ClientNotFound_ThrowsException() {
        // Arrange
        when(clientsRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                clientsService.updateClient(99L, "Test Name","test@mail.com", 123L));

        assertEquals("Cliente no encontrado.", exception.getMessage());
        verify(clientsRepository, times(1)).findById(99L);
        verify(clientsRepository, never()).save(any(Customer.class));
    }

    /**
     * Tests the scenario where the updated email is already in use by another client.
     */
    @Test
    void updateClient_EmailAlreadyInUse_ThrowsException() {
        // Arrange
        when(clientsRepository.findById(1L)).thenReturn(Optional.of(client1));
        when(clientsRepository.findByEmail("jane.smith@test.com")).thenReturn(Optional.of(client2));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                clientsService.updateClient(1L, "John D.","jane.smith@test.com", 123456789L));

        assertEquals("El correo electrónico ya está en uso.", exception.getMessage());
        verify(clientsRepository, times(1)).findById(1L);
        verify(clientsRepository, never()).save(any(Customer.class));
    }

    /**
     * Tests the scenario where the updated phone number is already in use by another client.
     */
    @Test
    void updateClient_PhoneNumberAlreadyInUse_ThrowsException() {
        // Arrange
        when(clientsRepository.findById(1L)).thenReturn(Optional.of(client1));
        when(clientsRepository.findByEmail("john.d@test.com")).thenReturn(Optional.empty());
        when(clientsRepository.findByNumber(987654321L)).thenReturn(Optional.of(client2));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                clientsService.updateClient(1L, "John D.","john.d@test.com", 987654321L));

        assertEquals("El número de teléfono ya está en uso.", exception.getMessage());
        verify(clientsRepository, times(1)).findById(1L);
        verify(clientsRepository, never()).save(any(Customer.class));
    }

    /**
     * Tests the correct client deletion scenario.
     */
    @Test
    void deleteClient_CorrectDeletion_NoExceptionThrown() {
        // Arrange
        when(clientsRepository.findById(1L)).thenReturn(Optional.of(client1));
        doNothing().when(clientsRepository).delete(client1);

        // Act
        clientsService.deleteClient(1L);

        // Assert
        verify(clientsRepository, times(1)).findById(1L);
        verify(clientsRepository, times(1)).delete(client1);
    }

    /**
     * Tests the scenario where the client to delete is not found.
     */
    @Test
    void deleteClient_ClientNotFound_ThrowsException() {
        // Arrange
        when(clientsRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                clientsService.deleteClient(99L));

        assertEquals("Client not found.", exception.getMessage());
        verify(clientsRepository, times(1)).findById(99L);
        verify(clientsRepository, never()).delete(any(Customer.class));
    }

    /**
     * Tests the scenario where a null search parameter is provided.
     */
    @Test
    void findClients_SearchIsNull_ReturnsAllClients() {
        // Arrange
        List<Customer> allClients = Arrays.asList(client1, client2);
        when(clientsRepository.findAll()).thenReturn(allClients);

        // Act
        List<Customer> result = clientsService.findClients(null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(clientsRepository, times(1)).findAll();
        verify(clientsRepository, never()).findByNameContainingIgnoreCase(anyString());
    }

    /**
     * Tests the scenario where a valid search parameter is provided.
     */
    @Test
    void findClients_SearchIsNotEmpty_ReturnsFilteredClients() {
        // Arrange
        List<Customer> filteredClients = Arrays.asList(client1);
        when(clientsRepository.findByNameContainingIgnoreCase("John")).thenReturn(filteredClients);

        // Act
        List<Customer> result = clientsService.findClients("John");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
        verify(clientsRepository, times(1)).findByNameContainingIgnoreCase("John");
        verify(clientsRepository, never()).findAll();
    }
}