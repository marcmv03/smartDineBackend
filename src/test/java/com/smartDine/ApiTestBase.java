package com.smartDine;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.junit.jupiter.api.BeforeAll;

public abstract class ApiTestBase {

    @BeforeAll
    public static void setup() {
        // Lee la URL del entorno de Staging desde las propiedades del sistema o un archivo
        // Esto evita tener la URL hardcodeada en el código
        String baseUri = System.getProperty("api.url", "http://localhost:8080");
        RestAssured.baseURI = baseUri;
        RestAssured.basePath = "/api"; // Prefijo común para todos los endpoints

        // Habilita logs para ver las peticiones y respuestas en la consola.
        // Es extremadamente útil para depurar pruebas fallidas.
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }
}