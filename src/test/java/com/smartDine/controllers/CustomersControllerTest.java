package com.smartDine.controllers;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.smartDine.dto.CustomerDTO;
import com.smartDine.entity.Customer;
import com.smartDine.services.CustomersService;

@ExtendWith(MockitoExtension.class)
class CustomersControllerTest {

    @Mock
    private CustomersService customersService;

    @InjectMocks
    private CustomersController customersController;

    private CustomerDTO customerDTO;
    private Customer client;

    @BeforeEach
    void setUp() {
        customerDTO = new CustomerDTO();
        customerDTO.setName("Test User");
        customerDTO.setEmail("test.user@example.com");
        customerDTO.setPassword("Password12345");
        customerDTO.setPhone(123456789L);

        client = new Customer();
        client.setId(1L);
        client.setName("Test User");
        client.setEmail("test.user@example.com");
        client.setNumber(123456789L);
    }

    @Test
    void whenRegisterClient_thenReturnCreatedStatusAndClient() {
        // GIVEN: The service returns a client when creating it
        when(customersService.createClient(any(), any(), any(), anyLong())).thenReturn(client);

        // WHEN: We call the controller method
        ResponseEntity<Customer> response = customersController.registerClient(customerDTO);

        // THEN: We verify the response status and body
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(client, response.getBody());
    }

    @Test
    void whenGetClients_thenReturnOkStatusAndList() {
        // GIVEN: The service returns a list of clients
        when(customersService.findClients(any())).thenReturn(Collections.singletonList(client));

        // WHEN: We call the controller method
        ResponseEntity<List<Customer>> response = customersController.getClients(null);

        // THEN: We verify the response status and body
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(client.getName(), response.getBody().get(0).getName());
    }

    @Test
    void whenDeleteClient_thenReturnOkStatus() {
        // GIVEN: The service performs the deletion without an error
        doNothing().when(customersService).deleteClient(anyLong());

        // WHEN: We call the controller method
        ResponseEntity<Void> response = customersController.deleteClient(1L);

        // THEN: We verify the response status
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(customersService, times(1)).deleteClient(1L);
    }
}