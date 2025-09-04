package com.smartDine.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.CustomerDTO;
import com.smartDine.entity.Customer;
import com.smartDine.services.CustomersService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/smartdine/api/users/customers")
public class CustomersController {

    // Dependency injection of the client service
    @Autowired
    private CustomersService clientsService;

    /**
     * GET /users/clients
     * Description: Gets a list of all clients or filters by name.
     *
     * @param search (query param, optional): name or part of the client's name to search for.
     * @return ResponseEntity
     */
    @GetMapping
    public ResponseEntity<List<Customer>> getClients(@RequestParam(required = false) String search) {
        // Business logic is delegated to the service
        List<Customer> result = clientsService.findClients(search);
        return ResponseEntity.ok().body(result);
    }

    /**
     * POST /users/clients
     * Description: Registers a new client.
     *
     * @param clientDTO A ClientDTO object that contains the client's data.
     * @return ResponseEntity
     */
    @PostMapping
    public ResponseEntity<Customer> registerClient(@Valid @RequestBody CustomerDTO clientDTO) {
        // The validation is performed automatically thanks to @Valid.
        // The business logic is delegated to the service.
        Customer response = clientsService.createClient(
                clientDTO.getName(),
                clientDTO.getEmail(),
                clientDTO.getPassword(),
                clientDTO.getPhone()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * PUT /users/clients/{clientId}
     * Description: Updates an existing client's information.
     *
     * @param clientId (path variable): ID of the client to update.
     * @param payload A Map with the data to update (name, email, phone).
     * @return ResponseEntity
     */
    @PutMapping("/{clientId}")
    public ResponseEntity<Customer> updateClient(@PathVariable Long clientId, @RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String email = payload.get("email");
        String numberString = payload.get("number");
        Long number = Long.parseLong(numberString);
        Customer result = clientsService.updateClient(clientId, name, email, number);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /users/clients/{clientId}
     * Description: Deletes a client.
     *
     * @param clientId (path variable): ID of the client to delete.
     * @return ResponseEntity
     */
    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long clientId) {
        // The deletion logic is delegated to the service
        clientsService.deleteClient(clientId);
        return ResponseEntity.ok().build();
    }
}