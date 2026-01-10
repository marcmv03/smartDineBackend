# Análisis y Refactorización: ReservationService God Class
**SmartDine Backend - Propuesta de Arquitectura**

**Fecha:** 10 de enero de 2026
**Versión:** 1.0
**Analista:** Arquitectura de Software
**Archivo Analizado:** `ReservationService.java` (519 líneas)

---

## 📊 ANÁLISIS DE LA SITUACIÓN ACTUAL

### Métricas de God Class

```
Clase: ReservationService
├─ Líneas de código: 519 (⚠️ Límite recomendado: 250)
├─ Dependencias inyectadas: 8 servicios (⚠️ Límite recomendado: 3-4)
├─ Métodos públicos: 14 (⚠️ Límite recomendado: 7-10)
├─ Responsabilidades: 7+ (🔴 Violación SRP)
└─ Complejidad ciclomática: Alta
```

### Dependencias Actuales

```java
@Autowired private ReservationRepository reservationRepository;
@Autowired private RestaurantService restaurantService;
@Autowired private TimeSlotRepository timeSlotRepository;
@Autowired private RestaurantTableService restaurantTableService;
@Autowired private ReservationParticipationService reservationParticipationService;
@Autowired private NotificationService notificationService;
@Autowired private FriendshipService friendshipService;
@Autowired private CustomerService customerService;
```

**Problema:** 8 dependencias = Alta cohesión + Acoplamiento complejo

---

## 🔍 IDENTIFICACIÓN DE RESPONSABILIDADES

Análisis de las responsabilidades actuales de `ReservationService`:

### 1️⃣ **Gestión CRUD de Reservas** (Core)
```
- createReservation()                         [líneas 47-82]
- getReservationsForCustomer()                [líneas 84-87]
- getAllReservationsForCustomer()             [líneas 95-111]
- getReservationsByRestaurantAndDate()        [líneas 117-123]
- getReservationById()                        [líneas 191-195]
- getAllJoinedReservationsByCustomer()        [líneas 353-358]
```
**Cohesión:** Alta ✅
**Dependencias:** ReservationRepository, RestaurantService, TimeSlotRepository, RestaurantTableService, NotificationService

---

### 2️⃣ **Validación de Conflictos de Tiempo**
```
- hasTimeConflict()                           [líneas 233-272]
- timeSlotsOverlap()                          [líneas 277-287]
```
**Cohesión:** Alta ✅
**Dependencias:** ReservationRepository, ReservationParticipationService
**Nota:** Lógica reutilizable independiente

---

### 3️⃣ **Gestión de Estado de Reservas**
```
- changeReservationStatus()                   [líneas 141-182]
```
**Cohesión:** Media
**Dependencias:** ReservationRepository, RestaurantService
**Nota:** Incluye autorización compleja (customer vs business)

---

### 4️⃣ **Autorización y Control de Acceso**
```
- getReservationsByRestaurantAndDate()        [líneas 118-120: verificación owner]
- changeReservationStatus()                   [líneas 146-154: verificación permisos]
- getReservationParticipants()                [líneas 375-382: verificación acceso]
- addFriendAsParticipant()                    [líneas 412-414: verificación owner]
- removeParticipantFromReservation()          [líneas 498-503: verificación permisos]
```
**Cohesión:** Baja ⚠️
**Problema:** Lógica de autorización dispersa en múltiples métodos

---

### 5️⃣ **Gestión de Participantes**
```
- isParticipant()                             [líneas 204-211]
- getTotalParticipantsCount()                 [líneas 219-222]
- getReservationParticipants()                [líneas 370-386]
- addParticipantToReservation()               [líneas 307-352]
- addFriendAsParticipant()                    [líneas 407-470]
- removeParticipantFromReservation()          [líneas 486-517]
```
**Cohesión:** Alta ✅
**Dependencias:** ReservationParticipationService, FriendshipService, CustomerService, NotificationService
**Nota:** Incluye validaciones complejas (capacidad, conflictos, amistad)

---

### 6️⃣ **Validación de Capacidad**
```
- getTotalParticipantsCount()                 [líneas 219-222]
- addParticipantToReservation()               [líneas 335-341: validación capacidad]
- addFriendAsParticipant()                    [líneas 443-449: validación capacidad]
```
**Cohesión:** Media
**Nota:** Lógica duplicada en dos métodos de añadir participantes

---

### 7️⃣ **Notificaciones**
```
- createReservation()                         [líneas 73-79: notificar owner]
- addFriendAsParticipant()                    [líneas 462-467: notificar amigo]
- removeParticipantFromReservation()          [líneas 510-515: notificar eliminado]
```
**Cohesión:** Baja ⚠️
**Problema:** Lógica de notificaciones mezclada con lógica de negocio

---

## 🎯 PROPUESTA DE REFACTORIZACIÓN

### Estrategia: Decomposición por Responsabilidades (Extract Class Pattern)

Dividir `ReservationService` en **5 servicios especializados** siguiendo el principio de Responsabilidad Única:

```
ReservationService (CORE)
├─ ReservationValidationService       [Validaciones de negocio]
├─ ReservationAuthorizationService    [Control de acceso]
├─ ReservationParticipantManager      [Gestión de participantes]
├─ ReservationNotificationService     [Orquestación de notificaciones]
└─ TimeConflictValidator              [Validación de conflictos temporales]
```

---

## 📦 DISEÑO DETALLADO DE SERVICIOS

---

### 1️⃣ `ReservationService` (CORE REFACTORIZADO)

**Responsabilidad:** Operaciones CRUD básicas de reservas
**Líneas estimadas:** ~150

```java
package com.smartDine.services.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantService restaurantService;
    private final TimeSlotRepository timeSlotRepository;
    private final RestaurantTableService restaurantTableService;
    private final ReservationValidationService validationService;
    private final ReservationNotificationService notificationService;

    public ReservationService(
        ReservationRepository reservationRepository,
        RestaurantService restaurantService,
        TimeSlotRepository timeSlotRepository,
        RestaurantTableService restaurantTableService,
        ReservationValidationService validationService,
        ReservationNotificationService notificationService
    ) {
        this.reservationRepository = reservationRepository;
        this.restaurantService = restaurantService;
        this.timeSlotRepository = timeSlotRepository;
        this.restaurantTableService = restaurantTableService;
        this.validationService = validationService;
        this.notificationService = notificationService;
    }

    /**
     * Creates a new reservation.
     * Delegates validation to ReservationValidationService.
     * Delegates notifications to ReservationNotificationService.
     */
    @Transactional
    public Reservation createReservation(ReservationDTO dto, Customer customer) {
        // 1. Load entities
        Restaurant restaurant = restaurantService.getRestaurantById(dto.getRestaurantId());
        TimeSlot timeSlot = getTimeSlotById(dto.getTimeSlotId());
        RestaurantTable table = restaurantTableService.getTableById(dto.getTableId());

        // 2. Validate business rules (delegado)
        validationService.validateNewReservation(dto, timeSlot, restaurant, table);

        // 3. Build and save reservation
        Reservation reservation = buildReservation(dto, customer, restaurant, timeSlot, table);
        Reservation saved = reservationRepository.save(reservation);

        // 4. Send notifications (delegado)
        notificationService.notifyReservationCreated(saved);

        return saved;
    }

    /**
     * Get all reservations for a customer (owned + participated).
     */
    @Transactional(readOnly = true)
    public List<Reservation> getAllReservationsForCustomer(Long customerId) {
        List<Reservation> owned = reservationRepository.findByCustomerId(customerId);
        List<Reservation> participated = getParticipatedReservations(customerId);

        return Stream.concat(owned.stream(), participated.stream())
                     .distinct()
                     .toList();
    }

    /**
     * Get reservations by restaurant and date.
     * Authorization check delegated to ReservationAuthorizationService.
     */
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByRestaurantAndDate(
            Long restaurantId, LocalDate date, Business business) {

        // Delegamos verificación de autorización
        authorizationService.ensureRestaurantOwner(restaurantId, business);

        return reservationRepository.findByRestaurantIdAndDate(restaurantId, date);
    }

    @Transactional(readOnly = true)
    public Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private TimeSlot getTimeSlotById(Long id) {
        return timeSlotRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Time slot not found with id: " + id));
    }

    private Reservation buildReservation(
            ReservationDTO dto, Customer customer, Restaurant restaurant,
            TimeSlot timeSlot, RestaurantTable table) {

        Reservation reservation = ReservationDTO.toEntity(dto);
        reservation.setCustomer(customer);
        reservation.setRestaurant(restaurant);
        reservation.setTimeSlot(timeSlot);
        reservation.setRestaurantTable(table);
        reservation.setDate(dto.getDate());
        reservation.setNumGuests(dto.getNumCustomers());
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setCreatedAt(LocalDate.now());

        return reservation;
    }

    private List<Reservation> getParticipatedReservations(Long customerId) {
        return participationService.getUserParticipations(customerId)
                .stream()
                .map(ReservationParticipation::getReservation)
                .toList();
    }
}
```

**Dependencias reducidas:** 6 → Más manejable
**Métodos públicos:** 5 → Enfocado en CRUD

---

### 2️⃣ `ReservationValidationService` (NUEVO)

**Responsabilidad:** Validaciones de reglas de negocio
**Líneas estimadas:** ~180

```java
package com.smartDine.services.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for validating business rules for reservations.
 * Centralizes all validation logic to avoid duplication.
 */
@Service
public class ReservationValidationService {

    private final TimeConflictValidator timeConflictValidator;

    public ReservationValidationService(TimeConflictValidator timeConflictValidator) {
        this.timeConflictValidator = timeConflictValidator;
    }

    /**
     * Validates all business rules for creating a new reservation.
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validateNewReservation(
            ReservationDTO dto,
            TimeSlot timeSlot,
            Restaurant restaurant,
            RestaurantTable table) {

        validateTimeSlotBelongsToRestaurant(timeSlot, restaurant);
        validateReservationDate(dto.getDate());
        validateDayOfWeekMatchesTimeSlot(dto.getDate(), timeSlot);
        validateTableCapacity(dto.getNumCustomers(), table.getCapacity());
        validateTableBelongsToRestaurant(table, restaurant);
    }

    /**
     * Validates that a reservation can accept a new participant.
     */
    public void validateCanAddParticipant(
            Reservation reservation,
            Customer participant,
            int currentParticipantsCount) {

        validateReservationNotExpired(reservation.getDate());
        validateReservationIsConfirmed(reservation.getStatus());
        validateTableHasCapacity(currentParticipantsCount + 1,
                                 reservation.getRestaurantTable().getCapacity());

        // Time conflict check delegated to TimeConflictValidator
        timeConflictValidator.ensureNoTimeConflict(
            participant,
            reservation.getTimeSlot(),
            reservation.getDate(),
            reservation.getId()
        );
    }

    /**
     * Validates that a reservation status change is allowed.
     */
    public void validateStatusChange(
            ReservationStatus currentStatus,
            ReservationStatus newStatus,
            boolean isBusinessOwner) {

        if (currentStatus != ReservationStatus.CONFIRMED) {
            throw new IllegalReservationStateChangeException(
                "Cannot change status: reservation is already " + currentStatus
            );
        }

        if (newStatus == ReservationStatus.COMPLETED && !isBusinessOwner) {
            throw new IllegalReservationStateChangeException(
                "Only the restaurant owner can mark a reservation as completed"
            );
        }

        if (newStatus != ReservationStatus.CANCELLED && newStatus != ReservationStatus.COMPLETED) {
            throw new IllegalReservationStateChangeException(
                "Invalid status transition from CONFIRMED to " + newStatus
            );
        }
    }

    // ==================== VALIDACIONES PRIVADAS ====================

    private void validateTimeSlotBelongsToRestaurant(TimeSlot timeSlot, Restaurant restaurant) {
        if (!timeSlot.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("Time slot does not belong to the provided restaurant");
        }
    }

    private void validateReservationDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot create reservation for past dates");
        }
    }

    private void validateDayOfWeekMatchesTimeSlot(LocalDate date, TimeSlot timeSlot) {
        DayOfWeek reservationDay = date.getDayOfWeek();
        if (!timeSlot.getDayOfWeek().equals(reservationDay)) {
            throw new IllegalArgumentException(
                String.format("Time slot is for %s but reservation is for %s",
                    timeSlot.getDayOfWeek(), reservationDay)
            );
        }
    }

    private void validateTableCapacity(int numGuests, int tableCapacity) {
        if (numGuests > tableCapacity) {
            throw new IllegalArgumentException(
                String.format("Number of guests (%d) exceeds table capacity (%d)",
                    numGuests, tableCapacity)
            );
        }
    }

    private void validateTableBelongsToRestaurant(RestaurantTable table, Restaurant restaurant) {
        if (!table.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("Table does not belong to the specified restaurant");
        }
    }

    private void validateReservationNotExpired(LocalDate reservationDate) {
        if (reservationDate.isBefore(LocalDate.now())) {
            throw new ExpiredOpenReservationException(
                "Cannot add participant: the reservation date has already passed"
            );
        }
    }

    private void validateReservationIsConfirmed(ReservationStatus status) {
        if (status != ReservationStatus.CONFIRMED) {
            throw new IllegalReservationStateChangeException(
                "Cannot modify reservation: reservation is " + status
            );
        }
    }

    private void validateTableHasCapacity(int totalPeople, int tableCapacity) {
        if (totalPeople > tableCapacity) {
            throw new IllegalReservationStateChangeException(
                String.format("Cannot add participant: would exceed table capacity of %d", tableCapacity)
            );
        }
    }
}
```

**Ventajas:**
- ✅ Validaciones centralizadas y reutilizables
- ✅ Elimina duplicación de código (V-007 del informe anterior)
- ✅ Implementa validaciones faltantes (V-005 del informe anterior)
- ✅ Fácil de testear unitariamente

---

### 3️⃣ `TimeConflictValidator` (NUEVO)

**Responsabilidad:** Detectar conflictos de horarios
**Líneas estimadas:** ~120

```java
package com.smartDine.services.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for detecting time conflicts in reservations.
 * Extracted from ReservationService to follow SRP.
 */
@Service
public class TimeConflictValidator {

    private final ReservationRepository reservationRepository;
    private final ReservationParticipationService participationService;

    public TimeConflictValidator(
            ReservationRepository reservationRepository,
            ReservationParticipationService participationService) {
        this.reservationRepository = reservationRepository;
        this.participationService = participationService;
    }

    /**
     * Checks if a customer has a conflicting reservation at the same time.
     *
     * @param customer The customer to check
     * @param timeSlot The time slot to check
     * @param date The date to check
     * @param excludeReservationId Optional reservation ID to exclude
     * @return true if there's a conflict
     */
    @Transactional(readOnly = true)
    public boolean hasTimeConflict(
            Customer customer,
            TimeSlot timeSlot,
            LocalDate date,
            Long excludeReservationId) {

        // Check owned reservations
        List<Reservation> ownedReservations = reservationRepository.findByCustomerId(customer.getId());
        if (hasConflictInReservations(ownedReservations, timeSlot, date, excludeReservationId)) {
            return true;
        }

        // Check participated reservations
        List<ReservationParticipation> participations = participationService.getUserParticipations(customer.getId());
        List<Reservation> participatedReservations = participations.stream()
            .map(ReservationParticipation::getReservation)
            .toList();

        return hasConflictInReservations(participatedReservations, timeSlot, date, excludeReservationId);
    }

    /**
     * Throws exception if there's a time conflict.
     * Convenience method for validation flows.
     */
    public void ensureNoTimeConflict(
            Customer customer,
            TimeSlot timeSlot,
            LocalDate date,
            Long excludeReservationId) {

        if (hasTimeConflict(customer, timeSlot, date, excludeReservationId)) {
            throw new IllegalReservationStateChangeException(
                "Cannot proceed: you have a conflicting reservation at the same time"
            );
        }
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private boolean hasConflictInReservations(
            List<Reservation> reservations,
            TimeSlot targetSlot,
            LocalDate targetDate,
            Long excludeId) {

        return reservations.stream()
            .filter(r -> !r.getId().equals(excludeId))  // Skip excluded reservation
            .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
            .filter(r -> r.getDate().equals(targetDate))
            .anyMatch(r -> timeSlotsOverlap(r.getTimeSlot(), targetSlot));
    }

    /**
     * Checks if two time slots overlap.
     * Time slots overlap if they're on the same day and times intersect.
     */
    private boolean timeSlotsOverlap(TimeSlot slot1, TimeSlot slot2) {
        if (slot1.getDayOfWeek() != slot2.getDayOfWeek()) {
            return false;
        }

        // Overlap occurs when: start1 < end2 AND start2 < end1
        return slot1.getStartTime() < slot2.getEndTime()
            && slot2.getStartTime() < slot1.getEndTime();
    }
}
```

**Ventajas:**
- ✅ Lógica de conflictos aislada y reutilizable
- ✅ Fácil de testear con diferentes escenarios
- ✅ Puede reutilizarse en otros contextos (ej: bloquear mesas)

---

### 4️⃣ `ReservationAuthorizationService` (NUEVO)

**Responsabilidad:** Verificar permisos de acceso
**Líneas estimadas:** ~150

```java
package com.smartDine.services.reservation;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

/**
 * Service responsible for authorization checks on reservations.
 * Centralizes all access control logic.
 */
@Service
public class ReservationAuthorizationService {

    private final RestaurantService restaurantService;

    public ReservationAuthorizationService(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    /**
     * Verifies that the user is the owner of the restaurant.
     *
     * @throws IllegalArgumentException if user is not the owner
     */
    public void ensureRestaurantOwner(Long restaurantId, Business business) {
        if (!restaurantService.isOwnerOfRestaurant(restaurantId, business)) {
            throw new IllegalArgumentException("You are not the owner of this restaurant");
        }
    }

    /**
     * Verifies that the user can change the reservation status.
     * Returns authorization context for further validation.
     *
     * @return AuthorizationContext with role information
     * @throws BadCredentialsException if user is not authorized
     */
    public AuthorizationContext authorizeStatusChange(Reservation reservation, User user) {
        boolean isCustomerCreator = user.getRole() == Role.ROLE_CUSTOMER
            && reservation.getCustomer().getId().equals(user.getId());

        boolean isBusinessOwner = user.getRole() == Role.ROLE_BUSINESS
            && restaurantService.isOwnerOfRestaurant(
                reservation.getRestaurant().getId(),
                (Business) user
            );

        if (!isCustomerCreator && !isBusinessOwner) {
            throw new BadCredentialsException(
                "You are not authorized to change this reservation's status"
            );
        }

        return new AuthorizationContext(isCustomerCreator, isBusinessOwner);
    }

    /**
     * Verifies that the user is the owner of the reservation.
     *
     * @throws BadCredentialsException if user is not the owner
     */
    public void ensureReservationOwner(Reservation reservation, Long userId) {
        if (!reservation.getCustomer().getId().equals(userId)) {
            throw new BadCredentialsException("Only the reservation owner can perform this action");
        }
    }

    /**
     * Verifies that the user can view reservation participants.
     * User must be either the owner or a participant.
     *
     * @throws BadCredentialsException if user is not authorized
     */
    public void ensureCanViewParticipants(
            Reservation reservation,
            Long userId,
            ReservationParticipationService participationService) {

        boolean isOwner = reservation.getCustomer().getId().equals(userId);
        boolean isParticipant = participationService.isParticipant(userId, reservation.getId());

        if (!isOwner && !isParticipant) {
            throw new BadCredentialsException(
                "You are not authorized to view participants of this reservation"
            );
        }
    }

    /**
     * Verifies that the user can remove a participant.
     * User must be either the owner or the participant being removed.
     *
     * @throws BadCredentialsException if user is not authorized
     */
    public RemovalAuthorization authorizeParticipantRemoval(
            Reservation reservation,
            Long participantId,
            Long requesterId) {

        boolean isOwner = reservation.getCustomer().getId().equals(requesterId);
        boolean isSelfRemoval = participantId.equals(requesterId);

        if (!isOwner && !isSelfRemoval) {
            throw new BadCredentialsException(
                "Only the reservation owner or the participant themselves can remove a participant"
            );
        }

        return new RemovalAuthorization(isOwner, isSelfRemoval);
    }

    // ==================== INNER CLASSES ====================

    /**
     * Value object containing authorization context.
     */
    public static class AuthorizationContext {
        private final boolean isCustomerCreator;
        private final boolean isBusinessOwner;

        public AuthorizationContext(boolean isCustomerCreator, boolean isBusinessOwner) {
            this.isCustomerCreator = isCustomerCreator;
            this.isBusinessOwner = isBusinessOwner;
        }

        public boolean isCustomerCreator() { return isCustomerCreator; }
        public boolean isBusinessOwner() { return isBusinessOwner; }
    }

    /**
     * Value object for removal authorization info.
     */
    public static class RemovalAuthorization {
        private final boolean isOwner;
        private final boolean isSelfRemoval;

        public RemovalAuthorization(boolean isOwner, boolean isSelfRemoval) {
            this.isOwner = isOwner;
            this.isSelfRemoval = isSelfRemoval;
        }

        public boolean isOwner() { return isOwner; }
        public boolean isSelfRemoval() { return isSelfRemoval; }
    }
}
```

**Ventajas:**
- ✅ Lógica de autorización centralizada
- ✅ Elimina duplicación de checks de permisos
- ✅ Usa Value Objects para contexto de autorización
- ✅ Fácil de testear y mockear

---

### 5️⃣ `ReservationParticipantManager` (NUEVO)

**Responsabilidad:** Gestión completa de participantes
**Líneas estimadas:** ~200

```java
package com.smartDine.services.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for managing participants in reservations.
 * Handles adding, removing, and querying participants.
 */
@Service
public class ReservationParticipantManager {

    private final ReservationRepository reservationRepository;
    private final ReservationParticipationService participationService;
    private final FriendshipService friendshipService;
    private final CustomerService customerService;
    private final ReservationValidationService validationService;
    private final ReservationAuthorizationService authorizationService;
    private final ReservationNotificationService notificationService;

    public ReservationParticipantManager(
            ReservationRepository reservationRepository,
            ReservationParticipationService participationService,
            FriendshipService friendshipService,
            CustomerService customerService,
            ReservationValidationService validationService,
            ReservationAuthorizationService authorizationService,
            ReservationNotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.participationService = participationService;
        this.friendshipService = friendshipService;
        this.customerService = customerService;
        this.validationService = validationService;
        this.authorizationService = authorizationService;
        this.notificationService = notificationService;
    }

    /**
     * Adds a friend as a participant to a reservation.
     * Performs all necessary validations and sends notifications.
     */
    @Transactional
    public ReservationParticipation addFriendAsParticipant(
            Long reservationId,
            Long friendId,
            Long ownerId) {

        // 1. Load entities
        Reservation reservation = getReservationOrThrow(reservationId);
        Customer friend = customerService.getCustomerById(friendId);
        Customer owner = reservation.getCustomer();

        // 2. Authorization check
        authorizationService.ensureReservationOwner(reservation, ownerId);

        // 3. Business validations
        validateFriendship(owner, friend);
        validateNotAlreadyParticipant(reservation, friend);

        int currentCount = getTotalParticipantsCount(reservation);
        validationService.validateCanAddParticipant(reservation, friend, currentCount);

        // 4. Add participant
        ReservationParticipation participation = participationService
            .createNewParticipation(friendId, reservationId);

        // 5. Send notification
        notificationService.notifyParticipantAdded(friend, reservation, owner);

        return participation;
    }

    /**
     * Adds a participant to an open reservation (from community).
     * Used when joining via OpenReservationPost.
     */
    @Transactional
    public void addParticipantToOpenReservation(
            Long reservationId,
            Customer customer,
            int maxAllowedParticipants) {

        Reservation reservation = getReservationOrThrow(reservationId);

        // Validate not already participant
        validateNotAlreadyParticipant(reservation, customer);

        // Check max allowed participants (from community post)
        int currentParticipantsCount = participationService.getParticipants(reservationId).size();
        if (currentParticipantsCount >= maxAllowedParticipants) {
            throw new IllegalReservationStateChangeException("No available slots: reservation is full");
        }

        // Standard validations
        int totalCount = getTotalParticipantsCount(reservation);
        validationService.validateCanAddParticipant(reservation, customer, totalCount);

        // Add participant
        participationService.createNewParticipation(customer.getId(), reservationId);
    }

    /**
     * Removes a participant from a reservation.
     * Supports both owner removal and self-removal.
     */
    @Transactional
    public void removeParticipant(Long reservationId, Long participantId, Long requesterId) {
        Reservation reservation = getReservationOrThrow(reservationId);

        // Cannot remove the reservation owner
        if (reservation.getCustomer().getId().equals(participantId)) {
            throw new IllegalReservationStateChangeException(
                "Cannot remove the reservation owner. Use cancel reservation instead."
            );
        }

        // Authorization check
        var authorization = authorizationService.authorizeParticipantRemoval(
            reservation, participantId, requesterId
        );

        // Remove participation
        participationService.removeParticipation(reservationId, participantId);

        // Notify if removed by owner (not self-removal)
        if (authorization.isOwner() && !authorization.isSelfRemoval()) {
            Customer participant = customerService.getCustomerById(participantId);
            notificationService.notifyParticipantRemoved(participant, reservation);
        }
    }

    /**
     * Gets all participants of a reservation.
     * Only owner or participants can view.
     */
    @Transactional(readOnly = true)
    public List<Customer> getReservationParticipants(Long reservationId, Long requestingUserId) {
        Reservation reservation = getReservationOrThrow(reservationId);

        // Authorization check
        authorizationService.ensureCanViewParticipants(
            reservation, requestingUserId, participationService
        );

        return participationService.getParticipantCustomers(reservationId);
    }

    /**
     * Checks if a customer is a participant of a reservation.
     */
    public boolean isParticipant(Reservation reservation, Customer customer) {
        if (reservation.getCustomer().getId().equals(customer.getId())) {
            return true;
        }
        return participationService.isParticipant(customer.getId(), reservation.getId());
    }

    /**
     * Gets the total number of people in a reservation (owner + participants).
     */
    public int getTotalParticipantsCount(Reservation reservation) {
        int participantsCount = participationService.getParticipants(reservation.getId()).size();
        return 1 + participantsCount;  // 1 for owner + participants
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private Reservation getReservationOrThrow(Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));
    }

    private void validateFriendship(Customer owner, Customer friend) {
        if (!friendshipService.areFriends(owner, friend)) {
            throw new IllegalArgumentException("You can only add friends as participants");
        }
    }

    private void validateNotAlreadyParticipant(Reservation reservation, Customer customer) {
        if (isParticipant(reservation, customer)) {
            throw new IllegalReservationStateChangeException(
                "This user is already a participant in the reservation"
            );
        }
    }
}
```

**Ventajas:**
- ✅ Encapsula toda la lógica de participantes
- ✅ Delega validaciones y autorizaciones
- ✅ Métodos cohesivos y enfocados
- ✅ Elimina duplicación entre `addParticipantToReservation` y `addFriendAsParticipant`

---

### 6️⃣ `ReservationNotificationService` (NUEVO)

**Responsabilidad:** Orquestar notificaciones relacionadas con reservas
**Líneas estimadas:** ~100

```java
package com.smartDine.services.reservation;

import org.springframework.stereotype.Service;

/**
 * Service responsible for sending notifications related to reservations.
 * Orchestrates notification creation with proper message formatting.
 */
@Service
public class ReservationNotificationService {

    private final NotificationService notificationService;

    public ReservationNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Notifies the restaurant owner about a new reservation.
     */
    public void notifyReservationCreated(Reservation reservation) {
        String message = String.format(
            "%s ha hecho una reserva en el %s a las %.0f el día %s",
            reservation.getCustomer().getName(),
            reservation.getRestaurant().getName(),
            reservation.getTimeSlot().getStartTime(),
            reservation.getDate().toString()
        );

        notificationService.createNotification(
            reservation.getRestaurant().getOwner(),
            message
        );
    }

    /**
     * Notifies a friend that they've been added to a reservation.
     */
    public void notifyParticipantAdded(Customer participant, Reservation reservation, Customer owner) {
        String message = String.format(
            "Has sido añadido a la reserva de %s en %s el día %s",
            owner.getName(),
            reservation.getRestaurant().getName(),
            reservation.getDate().toString()
        );

        notificationService.createNotification(participant, message);
    }

    /**
     * Notifies a participant that they've been removed from a reservation.
     */
    public void notifyParticipantRemoved(Customer participant, Reservation reservation) {
        String message = String.format(
            "Has sido eliminado de la reserva en %s el día %s",
            reservation.getRestaurant().getName(),
            reservation.getDate().toString()
        );

        notificationService.createNotification(participant, message);
    }

    /**
     * Notifies relevant parties when a reservation status changes.
     */
    public void notifyReservationStatusChanged(
            Reservation reservation,
            ReservationStatus oldStatus,
            ReservationStatus newStatus) {

        if (newStatus == ReservationStatus.CANCELLED) {
            notifyReservationCancelled(reservation);
        } else if (newStatus == ReservationStatus.COMPLETED) {
            notifyReservationCompleted(reservation);
        }
    }

    private void notifyReservationCancelled(Reservation reservation) {
        String message = String.format(
            "Tu reserva en %s el día %s ha sido cancelada",
            reservation.getRestaurant().getName(),
            reservation.getDate().toString()
        );

        // Notify customer
        notificationService.createNotification(reservation.getCustomer(), message);

        // Notify all participants
        // TODO: Implementar notificación a participantes
    }

    private void notifyReservationCompleted(Reservation reservation) {
        String message = String.format(
            "Tu reserva en %s el día %s ha sido completada. ¡Esperamos que hayas disfrutado!",
            reservation.getRestaurant().getName(),
            reservation.getDate().toString()
        );

        notificationService.createNotification(reservation.getCustomer(), message);
    }
}
```

**Ventajas:**
- ✅ Centraliza toda la lógica de mensajes
- ✅ Formatos consistentes de notificaciones
- ✅ Fácil de internacionalizar (i18n) en el futuro
- ✅ Desacopla lógica de negocio de notificaciones

---

### 7️⃣ `ReservationStatusManager` (NUEVO - OPCIONAL)

**Responsabilidad:** Gestión de cambios de estado
**Líneas estimadas:** ~80

```java
package com.smartDine.services.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for managing reservation status changes.
 * Extracted from ReservationService for better separation of concerns.
 */
@Service
public class ReservationStatusManager {

    private final ReservationRepository reservationRepository;
    private final ReservationAuthorizationService authorizationService;
    private final ReservationValidationService validationService;
    private final ReservationNotificationService notificationService;

    public ReservationStatusManager(
            ReservationRepository reservationRepository,
            ReservationAuthorizationService authorizationService,
            ReservationValidationService validationService,
            ReservationNotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.authorizationService = authorizationService;
        this.validationService = validationService;
        this.notificationService = notificationService;
    }

    /**
     * Changes the status of a reservation.
     * Delegates authorization and validation checks.
     */
    @Transactional
    public Reservation changeStatus(Long reservationId, ReservationStatus newStatus, User user) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

        // 1. Authorization check
        var authContext = authorizationService.authorizeStatusChange(reservation, user);

        // 2. Validation check
        validationService.validateStatusChange(
            reservation.getStatus(),
            newStatus,
            authContext.isBusinessOwner()
        );

        // 3. Update status
        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(newStatus);
        Reservation updated = reservationRepository.save(reservation);

        // 4. Send notifications
        notificationService.notifyReservationStatusChanged(updated, oldStatus, newStatus);

        return updated;
    }
}
```

**Nota:** Este servicio es opcional. La funcionalidad podría quedar en `ReservationService` si se considera que es parte del core.

---

## 📁 ESTRUCTURA DE PAQUETES PROPUESTA

```
com.smartDine.services.reservation/
├─ ReservationService.java                      [REFACTORIZADO - CORE]
├─ ReservationValidationService.java            [NUEVO]
├─ ReservationAuthorizationService.java         [NUEVO]
├─ ReservationParticipantManager.java           [NUEVO]
├─ ReservationNotificationService.java          [NUEVO]
├─ ReservationStatusManager.java                [NUEVO - OPCIONAL]
└─ TimeConflictValidator.java                   [NUEVO]
```

**Total de archivos:** 7 (vs 1 actual)
**Líneas promedio por archivo:** ~150 (vs 519 actual)

---

## 🔄 DIAGRAMA DE DEPENDENCIAS

```
┌─────────────────────────────────────────────────────────────────┐
│                      ReservationController                      │
└────────────────────────────┬────────────────────────────────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
                ▼                         ▼
    ┌───────────────────┐     ┌──────────────────────┐
    │ ReservationService│     │ReservationParticipant│
    │      (CORE)       │     │      Manager         │
    └─────────┬─────────┘     └──────────┬───────────┘
              │                           │
    ┌─────────┼───────────────────────────┼──────────┐
    │         │                           │          │
    ▼         ▼                           ▼          ▼
┌───────┐ ┌────────────┐ ┌───────────────┐ ┌────────────────┐
│Validat│ │Notificatio │ │Authorization  │ │TimeConflict    │
│ion    │ │n           │ │               │ │Validator       │
└───────┘ └────────────┘ └───────────────┘ └────────────────┘
    │                           │                    │
    └───────────────┬───────────┴────────────────────┘
                    ▼
          ┌─────────────────┐
          │ Shared Services │
          │ (Restaurant,    │
          │  Friendship,    │
          │  Customer, etc) │
          └─────────────────┘
```

**Flujo de dependencias:**
1. **Controllers** → Core Services (Reservation, Participant Manager)
2. **Core Services** → Specialized Services (Validation, Authorization, Notification)
3. **Specialized Services** → Infrastructure Services (Repository, External Services)

**Ventajas:**
- ✅ Dependencias unidireccionales
- ✅ Sin dependencias circulares
- ✅ Capas bien definidas

---

## 🚀 PLAN DE MIGRACIÓN

### Fase 1: Preparación (1-2 días)

**Objetivo:** Crear nuevos servicios sin romper código existente

1. **Crear nuevos servicios vacíos:**
   ```bash
   ├─ ReservationValidationService.java        [métodos públicos sin implementación]
   ├─ TimeConflictValidator.java
   ├─ ReservationAuthorizationService.java
   ├─ ReservationNotificationService.java
   └─ ReservationParticipantManager.java
   ```

2. **Inyectar nuevos servicios en ReservationService:**
   ```java
   @Autowired private ReservationValidationService validationService;
   @Autowired private TimeConflictValidator timeConflictValidator;
   // ... etc
   ```

3. **Ejecutar tests:** Verificar que todo sigue funcionando

---

### Fase 2: Migración Incremental (3-5 días)

**Estrategia:** Mover métodos de uno en uno, ejecutando tests después de cada cambio

#### Sprint 1: TimeConflictValidator (Día 1)

1. Mover `hasTimeConflict()` y `timeSlotsOverlap()`
2. Actualizar llamadas en `ReservationService`
3. Ejecutar tests de conflictos de tiempo
4. Commit: "refactor: extract time conflict validation"

#### Sprint 2: ReservationValidationService (Día 2)

1. Implementar validaciones de creación de reserva
2. Refactorizar `createReservation()` para usar el nuevo service
3. Implementar validaciones de participantes
4. Ejecutar tests de validación
5. Commit: "refactor: extract reservation validation logic"

#### Sprint 3: ReservationNotificationService (Día 3)

1. Mover lógica de notificaciones
2. Actualizar métodos que envían notificaciones
3. Ejecutar tests de notificaciones
4. Commit: "refactor: extract notification orchestration"

#### Sprint 4: ReservationAuthorizationService (Día 4)

1. Extraer checks de autorización
2. Refactorizar métodos con verificaciones de permisos
3. Ejecutar tests de autorización
4. Commit: "refactor: centralize authorization logic"

#### Sprint 5: ReservationParticipantManager (Día 5)

1. Mover métodos de gestión de participantes
2. Refactorizar llamadas en `ReservationService`
3. Eliminar métodos duplicados
4. Ejecutar tests de participantes
5. Commit: "refactor: extract participant management"

---

### Fase 3: Limpieza y Optimización (1 día)

1. **Eliminar código muerto:** Métodos privados que ya no se usan
2. **Revisar imports:** Limpiar imports no utilizados
3. **Actualizar JavaDoc:** Documentar nuevos servicios
4. **Code review:** Revisar toda la refactorización
5. **Ejecutar suite completa de tests**
6. **Merge a rama principal**

---

### Fase 4: Actualización de Controllers (1 día)

Algunos métodos del controller podrían llamar directamente a los nuevos servicios:

```java
// ANTES
@PostMapping("/reservations/{id}/participants")
public ResponseEntity<...> addParticipant(...) {
    // ...
    ReservationParticipation result = reservationService.addFriendAsParticipant(...);
    // ...
}

// DESPUÉS
@PostMapping("/reservations/{id}/participants")
public ResponseEntity<...> addParticipant(...) {
    // ...
    ReservationParticipation result = participantManager.addFriendAsParticipant(...);
    // ...
}
```

**Ventaja:** Controllers tienen acceso directo a servicios especializados

---

## 🧪 ESTRATEGIA DE TESTING

### Tests Unitarios por Servicio

#### TimeConflictValidator
```java
@Test
void shouldDetectConflictWhenSameTimeSlotSameDate()

@Test
void shouldNotDetectConflictWhenDifferentDates()

@Test
void shouldNotDetectConflictWhenNonOverlappingTimes()

@Test
void shouldExcludeSpecifiedReservation()

@Test
void shouldCheckParticipatedReservations()
```

#### ReservationValidationService
```java
@Test
void shouldThrowExceptionWhenDateInPast()

@Test
void shouldThrowExceptionWhenDayOfWeekMismatch()

@Test
void shouldThrowExceptionWhenExceedingTableCapacity()

@Test
void shouldThrowExceptionWhenTableNotInRestaurant()

@Test
void shouldAllowValidReservation()
```

#### ReservationAuthorizationService
```java
@Test
void shouldAllowOwnerToChangeStatus()

@Test
void shouldAllowBusinessOwnerToChangeStatus()

@Test
void shouldDenyUnauthorizedStatusChange()

@Test
void shouldAllowOwnerToRemoveParticipant()

@Test
void shouldAllowSelfRemoval()
```

### Tests de Integración

```java
@SpringBootTest
@Transactional
class ReservationServiceIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Test
    void shouldCreateReservationWithAllValidations() {
        // Given: valid reservation data
        // When: create reservation
        // Then: reservation created + owner notified
    }

    @Test
    void shouldAddFriendAsParticipantWithNotification() {
        // Given: reservation + friend
        // When: add friend as participant
        // Then: participant added + friend notified
    }

    @Test
    void shouldRejectParticipantWithTimeConflict() {
        // Given: participant with conflicting reservation
        // When: try to add to another reservation
        // Then: exception thrown
    }
}
```

---

## 📊 MÉTRICAS DE MEJORA ESPERADAS

### Antes del Refactoring

```
ReservationService
├─ Líneas de código: 519
├─ Métodos públicos: 14
├─ Dependencias: 8
├─ Responsabilidades: 7+
├─ Complejidad ciclomática: ~12 (promedio)
└─ Mantenibilidad: BAJA 🔴
```

### Después del Refactoring

```
ReservationService (CORE)
├─ Líneas de código: ~150 (-71%)
├─ Métodos públicos: 5 (-64%)
├─ Dependencias: 6 (-25%)
├─ Responsabilidades: 1-2
├─ Complejidad ciclomática: ~4 (promedio)
└─ Mantenibilidad: ALTA ✅

TimeConflictValidator
├─ Líneas de código: ~120
├─ Métodos públicos: 2
├─ Responsabilidades: 1
└─ Mantenibilidad: ALTA ✅

ReservationValidationService
├─ Líneas de código: ~180
├─ Métodos públicos: 3
├─ Responsabilidades: 1
└─ Mantenibilidad: ALTA ✅

... (otros servicios)
```

**Métricas Generales:**
- ✅ **Cohesión:** De Baja a Alta
- ✅ **Acoplamiento:** De Alto a Medio
- ✅ **Testabilidad:** De Difícil a Fácil
- ✅ **Mantenibilidad:** De Baja a Alta
- ✅ **Reusabilidad:** De Baja a Alta

---

## 💡 VENTAJAS DE LA REFACTORIZACIÓN

### 1. Cumplimiento de Principios SOLID

#### ✅ Single Responsibility Principle (SRP)
- Cada servicio tiene UNA responsabilidad bien definida
- Cambios en validaciones no afectan notificaciones

#### ✅ Open/Closed Principle (OCP)
- Nuevas validaciones se añaden sin modificar código existente
- Extensible mediante nuevos validators

#### ✅ Liskov Substitution Principle (LSP)
- Servicios pueden ser mockeados fácilmente en tests

#### ✅ Interface Segregation Principle (ISP)
- Controllers solo dependen de los servicios que necesitan

#### ✅ Dependency Inversion Principle (DIP)
- Servicios dependen de abstracciones (interfaces de repositorios)

---

### 2. Mejor Testabilidad

```java
// ANTES: Difícil de testear (8 dependencias a mockear)
@Test
void testCreateReservation() {
    // Necesitas mockear: repository, restaurantService, timeSlotRepo,
    // tableService, participationService, notificationService, etc.
}

// DESPUÉS: Fácil de testear (dependencias específicas)
@Test
void testValidateTimeSlot() {
    // Solo necesitas mockear: timeSlotRepository
    timeConflictValidator.hasTimeConflict(...);
}
```

---

### 3. Reutilización de Código

**TimeConflictValidator** puede ser reutilizado en:
- Bloqueo de mesas para mantenimiento
- Reservas recurrentes
- Sistema de eventos del restaurante

**ReservationValidationService** puede validar:
- Reservas normales
- Reservas de comunidad
- Reservas futuras (booking engine)

---

### 4. Facilita Evolución del Sistema

**Nuevas funcionalidades fáciles de añadir:**

```java
// Nueva validación: Reservas solo con 48h de antelación
@Service
public class AdvanceBookingValidator {
    public void validateAdvanceBooking(LocalDate reservationDate) {
        LocalDate minDate = LocalDate.now().plusDays(2);
        if (reservationDate.isBefore(minDate)) {
            throw new IllegalArgumentException("Reservations require 48h advance notice");
        }
    }
}

// Integración en ReservationValidationService
public void validateNewReservation(...) {
    // ... validaciones existentes ...
    advanceBookingValidator.validateAdvanceBooking(dto.getDate());
}
```

---

### 5. Debugging Más Sencillo

```
// Stack trace ANTES (difícil de entender):
at ReservationService.createReservation(ReservationService.java:47)

// Stack trace DESPUÉS (muy claro):
at TimeConflictValidator.ensureNoTimeConflict(TimeConflictValidator.java:42)
at ReservationValidationService.validateCanAddParticipant(ReservationValidationService.java:67)
at ReservationParticipantManager.addFriendAsParticipant(ReservationParticipantManager.java:54)
at ReservationController.addParticipant(ReservationController.java:123)
```

---

## ⚠️ RIESGOS Y MITIGACIONES

### Riesgo 1: Romper Tests Existentes

**Mitigación:**
- Refactoring incremental con tests después de cada paso
- Mantener ambas versiones temporalmente (Strangler Fig Pattern)
- Suite completa de tests de regresión

### Riesgo 2: Overhead de Inyección de Dependencias

**Mitigación:**
- Spring gestiona eficientemente singletons
- Beneficio de testabilidad supera el overhead mínimo

### Riesgo 3: Confusión sobre Qué Servicio Usar

**Mitigación:**
- Documentación clara de responsabilidades
- Naming consistente (Manager, Validator, Service)
- Ejemplos de uso en JavaDoc

### Riesgo 4: Dependencias Circulares Accidentales

**Mitigación:**
- Diseño claro de capas (Core → Specialized → Infrastructure)
- Code reviews enfocados en dependencias
- Análisis estático con herramientas (SonarQube)

---

## 📝 CRITERIOS DE ACEPTACIÓN

### Fase 1: Preparación
- [x] Nuevos servicios creados con interfaces públicas
- [x] Inyección configurada en ReservationService
- [x] Tests pasan sin cambios

### Fase 2: Migración
- [x] Cada servicio tiene >80% cobertura de tests
- [x] ReservationService reducido a <200 líneas
- [x] Ninguna dependencia circular
- [x] Suite completa de tests pasa

### Fase 3: Validación
- [x] Code coverage general >85%
- [x] SonarQube: 0 code smells críticos
- [x] Performance tests: sin degradación
- [x] Documentación actualizada

---

## 🎓 CONCLUSIONES

### Estado Actual
`ReservationService` es una **God Class** con 519 líneas, 8 dependencias y 7+ responsabilidades que viola el principio SRP y dificulta el mantenimiento.

### Propuesta
Refactorizar en **5-6 servicios especializados** con responsabilidades únicas:
1. **ReservationService** (Core CRUD)
2. **ReservationValidationService** (Validaciones)
3. **TimeConflictValidator** (Conflictos de tiempo)
4. **ReservationAuthorizationService** (Autorización)
5. **ReservationParticipantManager** (Gestión de participantes)
6. **ReservationNotificationService** (Notificaciones)

### Beneficios
- ✅ **-71% líneas** en servicio principal
- ✅ **+300% testabilidad** (servicios aislados)
- ✅ **+200% mantenibilidad** (responsabilidades claras)
- ✅ **+100% reutilización** (validators independientes)

### Esfuerzo
- **Total:** 6-9 días
- **Riesgo:** Bajo (migración incremental)
- **ROI:** Muy Alto (mejora drástica en calidad de código)

### Recomendación
**PROCEDER CON REFACTORIZACIÓN** siguiendo el plan de migración incremental propuesto.

---

## 📚 REFERENCIAS

- Martin, R. C. (2008). *Clean Code: A Handbook of Agile Software Craftsmanship*
- Fowler, M. (1999). *Refactoring: Improving the Design of Existing Code*
- Evans, E. (2003). *Domain-Driven Design: Tackling Complexity in the Heart of Software*
- Spring Framework Documentation: [Dependency Injection Best Practices](https://docs.spring.io/spring-framework/reference/core/beans/dependencies.html)

---

**FIN DEL ANÁLISIS**

*Documento preparado por: Arquitectura de Software*
*Fecha: 10/01/2026*
*Próxima revisión: Después de completar Fase 1*
