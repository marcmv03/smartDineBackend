package com.smartDine.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartDine.dto.DishDTO;
import com.smartDine.dto.DrinkDTO;
import com.smartDine.dto.UpdateDishDTO;
import com.smartDine.dto.UpdateDrinkDTO;
import com.smartDine.dto.UploadResponse;
import com.smartDine.entity.Business;
import com.smartDine.entity.CourseType;
import com.smartDine.entity.Dish;
import com.smartDine.entity.Drink;
import com.smartDine.entity.DrinkType;
import com.smartDine.entity.Element;
import com.smartDine.services.MenuItemService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Transactional
public class MenuItemControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private MenuItemService menuItemService;

    @InjectMocks
    private MenuItemController menuItemController;

    private String baseUrl;
    private DishDTO dishDTO;
    private DrinkDTO drinkDTO;
    private Business businessOwner;
    private MockMultipartFile testImageFile;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/smartdine/api";

        // Configurar DishDTO
        dishDTO = new DishDTO();
        dishDTO.setName("Pasta Carbonara")
                .setDescription("Delicious Italian pasta")
                .setPrice(15.99)
                .setItemType("DISH");
        
        List<Element> elements = new ArrayList<>();
        elements.add(Element.GLUTEN);
        elements.add(Element.DAIRY);
        dishDTO.setElements(elements);
        dishDTO.setCourseType(CourseType.MAIN_COURSE);

        // Configurar DrinkDTO
        drinkDTO = new DrinkDTO();
        drinkDTO.setName("Coca Cola")
                .setDescription("Fresh soft drink")
                .setPrice(2.50)
                .setItemType("DRINK");
        drinkDTO.setDrinkType(DrinkType.SOFT_DRINK);

        // Create a business owner for image upload tests
        businessOwner = new Business();
        businessOwner.setId(1L);
        businessOwner.setName("Test Owner");
        businessOwner.setEmail("owner@test.com");
        businessOwner.setPhoneNumber(123456789L);

        // Create a test image file
        testImageFile = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
    }

    @Test
    public void testCreateMenuItemDish_WithoutAuth_ShouldFail() throws Exception {
        // Test sin autenticación para ver el comportamiento del controlador
        Long restaurantId = 1L;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String jsonContent = "{\n" +
                "  \"name\": \"Pasta Carbonara\",\n" +
                "  \"description\": \"Delicious Italian pasta\",\n" +
                "  \"price\": 15.99,\n" +
                "  \"itemType\": \"DISH\",\n" +
                "  \"elements\": [\"GLUTEN\", \"DAIRY\"],\n" +
                "  \"courseType\": \"MAIN_COURSE\"\n" +
                "}";

        System.out.println("Testing JSON deserialization with: " + jsonContent);
        
        // Deserializar el JSON para ver si Jackson funciona correctamente
        DishDTO deserializedDto = objectMapper.readValue(jsonContent, DishDTO.class);
        System.out.println("Deserialized DTO name: " + deserializedDto.getName());
        System.out.println("Deserialized DTO description: " + deserializedDto.getDescription());
        System.out.println("Deserialized DTO price: " + deserializedDto.getPrice());
        System.out.println("Deserialized DTO itemType: " + deserializedDto.getItemType());
        System.out.println("Deserialized DTO elements: " + deserializedDto.getElements());
        System.out.println("Deserialized DTO courseType: " + deserializedDto.getCourseType());

        assertNotNull(deserializedDto.getName());
        assertNotNull(deserializedDto.getDescription());
        assertNotNull(deserializedDto.getPrice());
        assertNotNull(deserializedDto.getElements());
        assertNotNull(deserializedDto.getCourseType());
        
        HttpEntity<String> entity = new HttpEntity<>(jsonContent, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/restaurants/{restaurantId}/menu-items", 
                HttpMethod.POST, 
                entity, 
                String.class, 
                restaurantId
        );

        // Esperamos que falle por falta de autenticación, pero podemos ver el comportamiento
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        
        // Si falla por autenticación, es normal. El punto es ver que el JSON se deserializa bien
        assertTrue(response.getStatusCode().is4xxClientError(), "Debería fallar por falta de autenticación");
    }

    @Test
    public void testJsonDeserialization() throws Exception {
        // Test específico para verificar la deserialización JSON
        String dishJson = "{\n" +
                "  \"name\": \"Pasta Carbonara\",\n" +
                "  \"description\": \"Delicious Italian pasta\",\n" +
                "  \"price\": 15.99,\n" +
                "  \"itemType\": \"DISH\",\n" +
                "  \"elements\": [\"GLUTEN\", \"DAIRY\"],\n" +
                "  \"courseType\": \"MAIN_COURSE\"\n" +
                "}";

        String drinkJson = "{\n" +
                "  \"itemType\": \"DRINK\",\n" +
                "  \"name\": \"Coca Cola\",\n" +
                "  \"description\": \"Fresh soft drink\",\n" +
                "  \"price\": 2.50,\n" +

                "  \"drinkType\": \"SOFT_DRINK\"\n" +
                "}";

        // Test deserialización de plato
        DishDTO dish = objectMapper.readValue(dishJson, DishDTO.class);
        assertNotNull(dish);
        assertEquals("Pasta Carbonara", dish.getName());
        assertEquals("Delicious Italian pasta", dish.getDescription());
        assertEquals(15.99, dish.getPrice());
        assertEquals("DISH", dish.getItemType());
        assertNotNull(dish.getElements());
        assertEquals(2, dish.getElements().size());
        assertTrue(dish.getElements().contains(Element.GLUTEN));
        assertTrue(dish.getElements().contains(Element.DAIRY));
        assertEquals(CourseType.MAIN_COURSE, dish.getCourseType());

        System.out.println("✓ Deserialización de plato exitosa:");
        System.out.println("  - Nombre: " + dish.getName());
        System.out.println("  - Descripción: " + dish.getDescription());
        System.out.println("  - Precio: " + dish.getPrice());
        System.out.println("  - Elementos: " + dish.getElements());
        System.out.println("  - Tipo de curso: " + dish.getCourseType());

        // Test deserialización de bebida
        DrinkDTO drink = objectMapper.readValue(drinkJson, DrinkDTO.class);
        assertNotNull(drink);
        assertEquals("Coca Cola", drink.getName());
        assertEquals("Fresh soft drink", drink.getDescription());
        assertEquals(2.50, drink.getPrice());
        assertEquals("DRINK", drink.getItemType());
        assertEquals(DrinkType.SOFT_DRINK, drink.getDrinkType());

        System.out.println("✓ Deserialización de bebida exitosa:");
        System.out.println("  - Nombre: " + drink.getName());
        System.out.println("  - Descripción: " + drink.getDescription());
        System.out.println("  - Precio: " + drink.getPrice());
        System.out.println("  - Tipo de bebida: " + drink.getDrinkType());
    }

    @Test
    public void testMenuItemPolymorphism() throws Exception {
        // Test para verificar que el polimorfismo funciona correctamente
        String dishJson = "{\n" +
                "  \"name\": \"Test Dish\",\n" +
                "  \"description\": \"Test Description\",\n" +
                "  \"price\": 10.00,\n" +
                "  \"itemType\": \"DISH\",\n" +
                "  \"elements\": [\"GLUTEN\"],\n" +
                "  \"courseType\": \"MAIN_COURSE\"\n" +
                "}";

        // Deserializar como MenuItemDTO (clase padre)
        com.smartDine.dto.MenuItemDTO menuItem = objectMapper.readValue(dishJson, com.smartDine.dto.MenuItemDTO.class);
        
        assertNotNull(menuItem);
        assertTrue(menuItem instanceof DishDTO, "Debería ser una instancia de DishDTO");
        
        DishDTO dishFromPolymorphic = (DishDTO) menuItem;
        assertEquals("Test Dish", dishFromPolymorphic.getName());
        assertEquals("Test Description", dishFromPolymorphic.getDescription());
        assertEquals(10.00, dishFromPolymorphic.getPrice());
        assertEquals("DISH", dishFromPolymorphic.getItemType());
        assertNotNull(dishFromPolymorphic.getElements());
        assertEquals(CourseType.MAIN_COURSE, dishFromPolymorphic.getCourseType());

        System.out.println("✓ Polimorfismo JSON funcionando correctamente:");
        System.out.println("  - Tipo deserializado: " + menuItem.getClass().getSimpleName());
        System.out.println("  - Elementos específicos del plato preservados");
    }

    /**
     * Integration test for MenuItem image upload using Mockito.
     * Tests successful upload scenario with valid file and authenticated business owner.
     */
    @Test
    public void uploadMenuItemImage_Success() throws IOException {
        // Given
        Long restaurantId = 1L;
        Long menuItemId = 10L;
        String expectedKeyName = "restaurants/1/menu-items/10/images/test-uuid.jpg";
        String expectedUrl = "https://smartdine-s3-bucket.s3.amazonaws.com/" + expectedKeyName;

        // Mock uploadMenuItemImage to return [keyName, url]
        when(menuItemService.uploadMenuItemImage(eq(restaurantId), eq(menuItemId), any(MultipartFile.class), eq(businessOwner)))
            .thenReturn(new UploadResponse(
                expectedKeyName,
                expectedUrl,
                "image/jpeg",
                testImageFile.getSize()
            ));

        // When
        ResponseEntity<UploadResponse> response = menuItemController.uploadMenuItemImage(
            restaurantId,
            menuItemId,
            testImageFile,
            businessOwner
        );

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Should return CREATED status");
        
        UploadResponse body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals(expectedKeyName, body.getKey(), "Key should match expected");
        assertEquals(expectedUrl, body.getUrl(), "URL should match expected");
        assertEquals("image/jpeg", body.getContentType(), "Content type should match");
        assertEquals(testImageFile.getSize(), body.getSize(), "File size should match");

        // Verify service method was called
        verify(menuItemService, times(1)).uploadMenuItemImage(
            eq(restaurantId),
            eq(menuItemId),
            any(MultipartFile.class),
            eq(businessOwner)
        );

        System.out.println("✓ MenuItem image upload test passed:");
        System.out.println("  - Key: " + body.getKey());
        System.out.println("  - URL: " + body.getUrl());
        System.out.println("  - Content Type: " + body.getContentType());
        System.out.println("  - Size: " + body.getSize());
    }

    /**
     * Tests that uploading an empty file returns HTTP 400 Bad Request.
     */
    @Test
    public void uploadMenuItemImage_EmptyFile_ShouldReturnBadRequest() throws IOException {
        // Given
        Long restaurantId = 1L;
        Long menuItemId = 10L;
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.jpg",
            "image/jpeg",
            new byte[0]
        );

        // When
        ResponseEntity<UploadResponse> response = menuItemController.uploadMenuItemImage(
            restaurantId,
            menuItemId,
            emptyFile,
            businessOwner
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return BAD_REQUEST for empty file");

        // Verify service method was NOT called
        verify(menuItemService, times(0)).uploadMenuItemImage(anyLong(), anyLong(), any(MultipartFile.class), any());

        System.out.println("✓ Empty file validation test passed");
    }

    /**
     * Tests that service layer exceptions are properly propagated to the controller.
     */
    @Test
    public void uploadMenuItemImage_ServiceThrowsException_ShouldPropagate() throws IOException {
        // Given
        Long restaurantId = 1L;
        Long menuItemId = 10L;

        // Mock service to throw exception
        when(menuItemService.uploadMenuItemImage(eq(restaurantId), eq(menuItemId), any(MultipartFile.class), eq(businessOwner)))
            .thenThrow(new IllegalArgumentException("You do not own this restaurant"));

        // When/Then
        try {
            menuItemController.uploadMenuItemImage(restaurantId, menuItemId, testImageFile, businessOwner);
            assertTrue(false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("You do not own this restaurant", e.getMessage());
            System.out.println("✓ Exception propagation test passed: " + e.getMessage());
        }
    }

    /**
     * Tests successful dish update with valid data.
     */
    @Test
    public void updateDish_Success() {
        // Given
        Long restaurantId = 1L;
        Long menuItemId = 10L;
        
        UpdateDishDTO updateDishDTO = new UpdateDishDTO();
        updateDishDTO.setName("Updated Pasta");
        updateDishDTO.setDescription("Updated description");
        updateDishDTO.setCourseType(CourseType.APPETIZER);
        List<Element> elements = new ArrayList<>();
        elements.add(Element.GLUTEN);
        updateDishDTO.setElements(elements);

        Dish updatedDish = new Dish();
        updatedDish.setId(menuItemId);
        updatedDish.setName("Updated Pasta");
        updatedDish.setDescription("Updated description");
        updatedDish.setCourseType(CourseType.APPETIZER);
        updatedDish.setElements(elements);
        updatedDish.setPrice(15.99);

        // Mock service to return updated dish
        when(menuItemService.updateDish(eq(restaurantId), eq(menuItemId), any(UpdateDishDTO.class), eq(businessOwner)))
            .thenReturn(updatedDish);

        // When
        ResponseEntity<com.smartDine.dto.MenuItemDTO> response = menuItemController.updateDish(
            restaurantId, menuItemId, updateDishDTO, businessOwner);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return OK status");
        
        com.smartDine.dto.MenuItemDTO body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertTrue(body instanceof DishDTO, "Response should be DishDTO");
        
        DishDTO dishDTO = (DishDTO) body;
        assertEquals("Updated Pasta", dishDTO.getName());
        assertEquals("Updated description", dishDTO.getDescription());
        assertEquals(CourseType.APPETIZER, dishDTO.getCourseType());

        verify(menuItemService, times(1)).updateDish(
            eq(restaurantId), eq(menuItemId), any(UpdateDishDTO.class), eq(businessOwner));

        System.out.println("✓ Update dish test passed:");
        System.out.println("  - Name: " + dishDTO.getName());
        System.out.println("  - Course Type: " + dishDTO.getCourseType());
    }

    /**
     * Tests that unauthorized user cannot update a dish.
     */
    @Test
    public void updateDish_Unauthorized_ShouldReturnUnauthorized() {
        // Given
        Long restaurantId = 1L;
        Long menuItemId = 10L;
        
        UpdateDishDTO updateDishDTO = new UpdateDishDTO();
        updateDishDTO.setName("Test Dish");
        updateDishDTO.setDescription("Test");
        updateDishDTO.setCourseType(CourseType.MAIN_COURSE);

        // When
        ResponseEntity<com.smartDine.dto.MenuItemDTO> response = menuItemController.updateDish(
            restaurantId, menuItemId, updateDishDTO, null);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), 
            "Should return UNAUTHORIZED when user is null");

        verify(menuItemService, times(0)).updateDish(anyLong(), anyLong(), any(UpdateDishDTO.class), any());

        System.out.println("✓ Unauthorized update dish test passed");
    }

    /**
     * Tests that service exception for duplicate name is propagated.
     */
    @Test
    public void updateDish_DuplicateName_ShouldThrowException() {
        // Given
        Long restaurantId = 1L;
        Long menuItemId = 10L;
        
        UpdateDishDTO updateDishDTO = new UpdateDishDTO();
        updateDishDTO.setName("Existing Dish Name");
        updateDishDTO.setDescription("Test");
        updateDishDTO.setCourseType(CourseType.MAIN_COURSE);

        // Mock service to throw exception for duplicate name
        when(menuItemService.updateDish(eq(restaurantId), eq(menuItemId), any(UpdateDishDTO.class), eq(businessOwner)))
            .thenThrow(new IllegalArgumentException("A dish with name 'Existing Dish Name' already exists in this restaurant"));

        // When/Then
        try {
            menuItemController.updateDish(restaurantId, menuItemId, updateDishDTO, businessOwner);
            assertTrue(false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("already exists"));
            System.out.println("✓ Duplicate name validation test passed: " + e.getMessage());
        }
    }

    /**
     * Tests that updating wrong menu item type throws exception.
     */
    @Test
    public void updateDish_WrongType_ShouldThrowException() {
        // Given
        Long restaurantId = 1L;
        Long menuItemId = 10L; // This is actually a drink
        
        UpdateDishDTO updateDishDTO = new UpdateDishDTO();
        updateDishDTO.setName("Test Dish");
        updateDishDTO.setDescription("Test");
        updateDishDTO.setCourseType(CourseType.MAIN_COURSE);

        // Mock service to throw exception for wrong type
        when(menuItemService.updateDish(eq(restaurantId), eq(menuItemId), any(UpdateDishDTO.class), eq(businessOwner)))
            .thenThrow(new IllegalArgumentException("Menu item with ID " + menuItemId + " is not a dish"));

        // When/Then
        try {
            menuItemController.updateDish(restaurantId, menuItemId, updateDishDTO, businessOwner);
            assertTrue(false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("is not a dish"));
            System.out.println("✓ Wrong type validation test passed: " + e.getMessage());
        }
    }

    /**
     * Tests successful drink update with valid data.
     */
    @Test
    public void updateDrink_Success() {
        // Given
        Long restaurantId = 1L;
        Long menuItemId = 20L;
        
        UpdateDrinkDTO updateDrinkDTO = new UpdateDrinkDTO();
        updateDrinkDTO.setName("Updated Pepsi");
        updateDrinkDTO.setDescription("Updated description");
        updateDrinkDTO.setDrinkType(DrinkType.SOFT_DRINK);

        Drink updatedDrink = new Drink();
        updatedDrink.setId(menuItemId);
        updatedDrink.setName("Updated Pepsi");
        updatedDrink.setDescription("Updated description");
        updatedDrink.setDrinkType(DrinkType.SOFT_DRINK);
        updatedDrink.setPrice(3.50);

        // Mock service to return updated drink
        when(menuItemService.updateDrink(eq(restaurantId), eq(menuItemId), any(UpdateDrinkDTO.class), eq(businessOwner)))
            .thenReturn(updatedDrink);

        // When
        ResponseEntity<com.smartDine.dto.MenuItemDTO> response = menuItemController.updateDrink(
            restaurantId, menuItemId, updateDrinkDTO, businessOwner);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return OK status");
        
        com.smartDine.dto.MenuItemDTO body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertTrue(body instanceof DrinkDTO, "Response should be DrinkDTO");
        
        DrinkDTO drinkDTO = (DrinkDTO) body;
        assertEquals("Updated Pepsi", drinkDTO.getName());
        assertEquals("Updated description", drinkDTO.getDescription());
        assertEquals(DrinkType.SOFT_DRINK, drinkDTO.getDrinkType());

        verify(menuItemService, times(1)).updateDrink(
            eq(restaurantId), eq(menuItemId), any(UpdateDrinkDTO.class), eq(businessOwner));

        System.out.println("✓ Update drink test passed:");
        System.out.println("  - Name: " + drinkDTO.getName());
        System.out.println("  - Drink Type: " + drinkDTO.getDrinkType());
    }

    /**
     * Tests that unauthorized user cannot update a drink.
     */
    @Test
    public void updateDrink_Unauthorized_ShouldReturnUnauthorized() {
        // Given
        Long restaurantId = 1L;
        Long menuItemId = 20L;
        
        UpdateDrinkDTO updateDrinkDTO = new UpdateDrinkDTO();
        updateDrinkDTO.setName("Test Drink");
        updateDrinkDTO.setDescription("Test");
        updateDrinkDTO.setDrinkType(DrinkType.BEER);

        // When
        ResponseEntity<com.smartDine.dto.MenuItemDTO> response = menuItemController.updateDrink(
            restaurantId, menuItemId, updateDrinkDTO, null);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), 
            "Should return UNAUTHORIZED when user is null");

        verify(menuItemService, times(0)).updateDrink(anyLong(), anyLong(), any(UpdateDrinkDTO.class), any());

        System.out.println("✓ Unauthorized update drink test passed");
    }

    /**
     * Tests that service exception for duplicate name is propagated for drinks.
     */
    @Test
    public void updateDrink_DuplicateName_ShouldThrowException() {
        // Given
        Long restaurantId = 1L;
        Long menuItemId = 20L;
        
        UpdateDrinkDTO updateDrinkDTO = new UpdateDrinkDTO();
        updateDrinkDTO.setName("Existing Drink Name");
        updateDrinkDTO.setDescription("Test");
        updateDrinkDTO.setDrinkType(DrinkType.SOFT_DRINK);

        // Mock service to throw exception for duplicate name
        when(menuItemService.updateDrink(eq(restaurantId), eq(menuItemId), any(UpdateDrinkDTO.class), eq(businessOwner)))
            .thenThrow(new IllegalArgumentException("A drink with name 'Existing Drink Name' already exists in this restaurant"));

        // When/Then
        try {
            menuItemController.updateDrink(restaurantId, menuItemId, updateDrinkDTO, businessOwner);
            assertTrue(false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("already exists"));
            System.out.println("✓ Duplicate drink name validation test passed: " + e.getMessage());
        }
    }

    /**
     * Tests that updating wrong menu item type throws exception for drinks.
     */
    @Test
    public void updateDrink_WrongType_ShouldThrowException() {
        // Given
        Long restaurantId = 1L;
        Long menuItemId = 20L; // This is actually a dish
        
        UpdateDrinkDTO updateDrinkDTO = new UpdateDrinkDTO();
        updateDrinkDTO.setName("Test Drink");
        updateDrinkDTO.setDescription("Test");
        updateDrinkDTO.setDrinkType(DrinkType.SOFT_DRINK);

        // Mock service to throw exception for wrong type
        when(menuItemService.updateDrink(eq(restaurantId), eq(menuItemId), any(UpdateDrinkDTO.class), eq(businessOwner)))
            .thenThrow(new IllegalArgumentException("Menu item with ID " + menuItemId + " is not a drink"));

        // When/Then
        try {
            menuItemController.updateDrink(restaurantId, menuItemId, updateDrinkDTO, businessOwner);
            assertTrue(false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("is not a drink"));
            System.out.println("✓ Wrong type validation test for drink passed: " + e.getMessage());
        }
    }

    /**
     * Tests that non-owner business cannot update a dish.
     */
    @Test
    public void updateDish_NonOwner_ShouldThrowException() {
        // Given
        Long restaurantId = 1L;
        Long menuItemId = 10L;
        
        UpdateDishDTO updateDishDTO = new UpdateDishDTO();
        updateDishDTO.setName("Test Dish");
        updateDishDTO.setDescription("Test");
        updateDishDTO.setCourseType(CourseType.MAIN_COURSE);

        // Mock service to throw exception for non-owner
        when(menuItemService.updateDish(eq(restaurantId), eq(menuItemId), any(UpdateDishDTO.class), eq(businessOwner)))
            .thenThrow(new IllegalArgumentException("You do not own this restaurant"));

        // When/Then
        try {
            menuItemController.updateDish(restaurantId, menuItemId, updateDishDTO, businessOwner);
            assertTrue(false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("You do not own this restaurant", e.getMessage());
            System.out.println("✓ Non-owner validation test passed: " + e.getMessage());
        }
    }

    /**
     * Tests that non-owner business cannot update a drink.
     */
    @Test
    public void updateDrink_NonOwner_ShouldThrowException() {
        // Given
        Long restaurantId = 1L;
        Long menuItemId = 20L;
        
        UpdateDrinkDTO updateDrinkDTO = new UpdateDrinkDTO();
        updateDrinkDTO.setName("Test Drink");
        updateDrinkDTO.setDescription("Test");
        updateDrinkDTO.setDrinkType(DrinkType.SOFT_DRINK);

        // Mock service to throw exception for non-owner
        when(menuItemService.updateDrink(eq(restaurantId), eq(menuItemId), any(UpdateDrinkDTO.class), eq(businessOwner)))
            .thenThrow(new IllegalArgumentException("You do not own this restaurant"));

        // When/Then
        try {
            menuItemController.updateDrink(restaurantId, menuItemId, updateDrinkDTO, businessOwner);
            assertTrue(false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("You do not own this restaurant", e.getMessage());
            System.out.println("✓ Non-owner validation test for drink passed: " + e.getMessage());
        }
    }
}
