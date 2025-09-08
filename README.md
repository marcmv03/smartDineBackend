# SmartDine API

SmartDine is the backend REST API for a comprehensive restaurant reservation and management application. It is designed to provide a complete solution for small and medium-sized restaurants, helping them digitize and streamline their operations. The API handles user authentication, restaurant management, reservations, and more.

## Architecture

The application is built using **Spring Boot** and follows a classic **layered architecture** to ensure a clean separation of concerns, maintainability, and scalability.

1.  **Controller Layer (`@RestController`)**: This is the entry point for all incoming HTTP requests. Its primary role is to handle the request, validate the input (DTOs - Data Transfer Objects), and delegate the business logic to the Service Layer. It is responsible for serializing the response back to the client (usually in JSON format).

2.  **Service Layer (`@Service`)**: This layer contains the core business logic of the application. It orchestrates operations by coordinating with the Repository Layer to access and persist data. All complex calculations, business rules, and validations are implemented here.

3.  **Repository Layer (`@Repository`)**: This layer is responsible for data access. It uses **Spring Data JPA** to interact with the database. The interfaces extend `JpaRepository`, which provides standard CRUD (Create, Read, Update, Delete) operations out of the box, abstracting away the boilerplate code for database interaction.

4.  **Security (`Spring Security`)**: Authentication and authorization are handled using Spring Security. The current implementation uses **JSON Web Tokens (JWT)** for stateless, token-based authentication. A `JwtAuthenticationFilter` intercepts incoming requests, validates the JWT, and sets the security context for authenticated users.

This architecture ensures that each part of the application has a specific responsibility, making the codebase easier to develop, test, and maintain.

### Technologies Used

* **Java 17+**
* **Spring Boot 3**
* **Spring Security**: For authentication and authorization.
* **JWT (Java Web Token)**: For stateless session management.
* **Spring Data JPA / Hibernate**: For data persistence.
* **PostgreSQL / H2**: Database for development/testing environments.
* **Maven**: Build automation and dependency management tool.

## Getting Started

Follow these instructions to get a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

* **JDK 17** or later.
* **Apache Maven** 3.6 or later.
* A running instance of **PostgreSQL** (for the `dev` profile).

### Running the Application

1.  **Clone the repository**
    ```sh
    git clone <your-repository-url>
    cd smartdine-api
    ```

2.  **Configure the database**
    Make sure your `src/main/resources/application-dev.properties` file is configured with the correct credentials for your local PostgreSQL database.

3.  **Build and Run with Maven**
    The project uses Maven for building and managing dependencies. Here are the most common commands you will use from the root of the project.

    * **Clean the project**: This command removes the `target` directory, deleting all previously compiled code and packaged artifacts. It's useful for ensuring a fresh build.
        ```sh
        mvn clean
        ```

    * **Compile the project**: This compiles the source code of the project.
        ```sh
        mvn compile
        ```

    * **Run tests**: This command will compile the code and run all the unit and integration tests in the project.
        ```sh
        mvn test
        ```

    * **Package the application**: This command compiles the code, runs the tests, and packages everything into a distributable format, such as a `.jar` file. The final artifact will be located in the `target` directory.
        ```sh
        mvn package
        ```
        *Note: `mvn package` executes the `clean`, `compile`, and `test` phases before packaging.*

    * **Run the application**: The simplest way to run the application during development is by using the Spring Boot Maven plugin. This command builds the project and starts the application on the embedded web server (Tomcat).
        ```sh
        mvn spring-boot:run
        ```
        The API will be available at `http://localhost:8080`.

    * **Install the package**: This command compiles, tests, packages the project, and installs the artifact into your local Maven repository, making it available to other local projects.
        ```sh
        mvn install
        ```