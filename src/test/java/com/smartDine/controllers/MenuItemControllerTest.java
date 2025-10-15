package com.smartDine.controllers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartDine.dto.DishDTO;
import com.smartDine.dto.DrinkDTO;
import com.smartDine.entity.CourseType;
import com.smartDine.entity.DrinkType;
import com.smartDine.entity.Element;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class MenuItemControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private DishDTO dishDTO;
    private DrinkDTO drinkDTO;
    
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
}