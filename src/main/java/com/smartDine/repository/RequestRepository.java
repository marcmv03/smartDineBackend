package com.smartDine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Request;
import com.smartDine.entity.RequestStatus;
import com.smartDine.entity.RequestType;
import com.smartDine.entity.User;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    /**
     * Find all requests for a receiver with a specific status, ordered by date descending.
     */
    List<Request> findByReceiverAndStatusOrderByDateDesc(User receiver, RequestStatus status);

    /**
     * Find all requests for a receiver with a specific type and status.
     */
    List<Request> findByReceiverAndRequestTypeAndStatusOrderByDateDesc(
            User receiver, RequestType requestType, RequestStatus status);

    /**
     * Check if a pending request of a specific type already exists between two users.
     */
    boolean existsBySenderAndReceiverAndRequestTypeAndStatus(
            User sender, User receiver, RequestType requestType, RequestStatus status);

    /**
     * Check if any pending request exists between two users (in either direction).
     */
    boolean existsBySenderAndReceiverAndStatusOrReceiverAndSenderAndStatus(
            User sender1, User receiver1, RequestStatus status1,
            User sender2, User receiver2, RequestStatus status2);
}
