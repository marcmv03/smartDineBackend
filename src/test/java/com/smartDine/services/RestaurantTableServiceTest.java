package com.smartDine.services;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.smartDine.dto.RestaurantTableDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.RestaurantTable;
import com.smartDine.exceptions.RelatedEntityException;
import com.smartDine.repository.ReservationRepository;
import com.smartDine.repository.RestaurantTableRepository;

@ExtendWith(MockitoExtension.class)
class RestaurantTableServiceTest {

    @Mock
    private RestaurantTableRepository tableRepository;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private RestaurantTableService tableService;

    private Business owner;
    private Restaurant restaurant;
    private RestaurantTable table;

    @BeforeEach
    void setUp() {
        owner = new Business("Table Owner", "tableowner@test.com", "password", 800000001L);
        owner.setId(1L);
        owner.setRestaurants(new ArrayList<>());

        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Table Owner Restaurant");
        restaurant.setAddress("123 Table Street");
        restaurant.setDescription("Restaurant for table tests");
        restaurant.setOwner(owner);
        restaurant.setTables(new ArrayList<>());

        owner.getRestaurants().add(restaurant);

        table = new RestaurantTable();
        table.setId(1L);
        table.setNumber(1);
        table.setCapacity(4);
        table.setOutside(false);
        table.setRestaurant(restaurant);
    }

    @Test
    @DisplayName("Should create a table for the owner of the restaurant")
    void createTableForOwner() {
        RestaurantTableDTO tableDTO = new RestaurantTableDTO();
        tableDTO.setNumber(1);
        tableDTO.setCapacity(4);
        tableDTO.setOutside(false);
        tableDTO.setRestaurantId(restaurant.getId());

        when(restaurantService.getRestaurantById(restaurant.getId())).thenReturn(restaurant);
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(tableRepository.existsByRestaurantIdAndNumber(restaurant.getId(), 1)).thenReturn(false);
        when(tableRepository.save(any(RestaurantTable.class))).thenReturn(table);
        when(restaurantService.addTable(anyLong(), any(RestaurantTable.class))).thenReturn(true);

        RestaurantTable created = tableService.createTable(restaurant.getId(), tableDTO, owner);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(restaurant.getId(), created.getRestaurant().getId());
        assertEquals(1, created.getNumber());
        verify(tableRepository).save(any(RestaurantTable.class));
    }

    @Test
    @DisplayName("Should reject table creation when business is not the owner")
    void createTableWithNonOwner() {
        Business otherBusiness = new Business("Other Owner", "otherowner@test.com", "password", 800000002L);
        otherBusiness.setId(2L);
        otherBusiness.setRestaurants(new ArrayList<>());

        RestaurantTableDTO tableDTO = new RestaurantTableDTO();
        tableDTO.setNumber(2);
        tableDTO.setCapacity(2);
        tableDTO.setOutside(true);
        tableDTO.setRestaurantId(restaurant.getId());

        when(restaurantService.getRestaurantById(restaurant.getId())).thenReturn(restaurant);
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), otherBusiness)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> tableService.createTable(restaurant.getId(), tableDTO, otherBusiness)
        );

        assertEquals("Business is not the owner of the restaurant", exception.getMessage());
        verify(tableRepository, never()).save(any(RestaurantTable.class));
    }

    @Test
    @DisplayName("Should reject duplicated table numbers for the same restaurant")
    void createDuplicatedTableNumber() {
        RestaurantTableDTO tableDTO = new RestaurantTableDTO();
        tableDTO.setNumber(3);
        tableDTO.setCapacity(4);
        tableDTO.setOutside(false);
        tableDTO.setRestaurantId(restaurant.getId());

        when(restaurantService.getRestaurantById(restaurant.getId())).thenReturn(restaurant);
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(tableRepository.existsByRestaurantIdAndNumber(restaurant.getId(), 3)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> tableService.createTable(restaurant.getId(), tableDTO, owner)
        );

        assertEquals("A table with number 3 already exists in this restaurant", exception.getMessage());
        verify(tableRepository, never()).save(any(RestaurantTable.class));
    }

    @Test
    @DisplayName("Should delete table successfully")
    void deleteTableSuccess() {
        when(restaurantService.getRestaurantById(restaurant.getId())).thenReturn(restaurant);
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(tableRepository.findById(table.getId())).thenReturn(Optional.of(table));
        doNothing().when(tableRepository).delete(table);
        doNothing().when(tableRepository).flush();

        tableService.deleteTable(restaurant.getId(), table.getId(), owner);

        verify(tableRepository).delete(table);
        verify(tableRepository).flush();
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent table")
    void deleteNonExistentTable() {
        Long nonExistentId = 999L;

        when(restaurantService.getRestaurantById(restaurant.getId())).thenReturn(restaurant);
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(tableRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> tableService.deleteTable(restaurant.getId(), nonExistentId, owner)
        );

        assertEquals("Table not found with id: " + nonExistentId, exception.getMessage());
        verify(tableRepository, never()).delete(any(RestaurantTable.class));
    }

    @Test
    @DisplayName("Should throw RelatedEntityException when deleting table with reservations")
    void deleteTableWithReservations() {
        when(restaurantService.getRestaurantById(restaurant.getId())).thenReturn(restaurant);
        when(restaurantService.isOwnerOfRestaurant(restaurant.getId(), owner)).thenReturn(true);
        when(tableRepository.findById(table.getId())).thenReturn(Optional.of(table));
        doNothing().when(tableRepository).delete(table);
        doThrow(new DataIntegrityViolationException("Foreign key constraint violation"))
            .when(tableRepository).flush();

        RelatedEntityException exception = assertThrows(
            RelatedEntityException.class,
            () -> tableService.deleteTable(restaurant.getId(), table.getId(), owner)
        );

        assertEquals("No se puede eliminar la mesa porque tiene reservas asociadas.", exception.getMessage());
    }
}
