package com.smartDine.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.entity.Customer;
import com.smartDine.entity.Friendship;
import com.smartDine.exceptions.FriendshipAlreadyExistsException;
import com.smartDine.exceptions.FriendshipNotFoundException;
import com.smartDine.repository.CustomerRepository;
import com.smartDine.repository.FriendshipRepository;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final CustomerRepository customerRepository;

    public FriendshipService(FriendshipRepository friendshipRepository, CustomerRepository customerRepository) {
        this.friendshipRepository = friendshipRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Get all friends for a customer.
     * 
     * @param customer The customer
     * @return List of friendships
     */
    @Transactional(readOnly = true)
    public List<Friendship> getFriends(Customer customer) {
        return friendshipRepository.findAllByUser(customer);
    }

    /**
     * Check if two customers are friends.
     * 
     * @param userA First customer
     * @param userB Second customer
     * @return true if they are friends
     */
    @Transactional(readOnly = true)
    public boolean areFriends(Customer userA, Customer userB) {
        return friendshipRepository.existsBetween(userA, userB);
    }

    /**
     * Create a friendship between two customers.
     * Called internally when a friend request is accepted.
     * 
     * @param userA First customer
     * @param userB Second customer
     * @return The created friendship
     * @throws FriendshipAlreadyExistsException if they are already friends
     */
    @Transactional
    public Friendship createFriendship(Customer userA, Customer userB) {
        if (areFriends(userA, userB)) {
            throw new FriendshipAlreadyExistsException();
        }

        Friendship friendship = new Friendship(userA, userB);
        return friendshipRepository.save(friendship);
    }

    /**
     * Remove a friendship.
     * 
     * @param customer The customer removing the friend
     * @param friendId The ID of the friend to remove
     * @throws FriendshipNotFoundException if the friendship doesn't exist
     * @throws IllegalArgumentException if the friend is not found
     */
    @Transactional
    public void removeFriend(Customer customer, Long friendId) {
        Customer friend = customerRepository.findById(friendId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + friendId));

        Friendship friendship = friendshipRepository.findByUsers(customer, friend)
                .orElseThrow(() -> new FriendshipNotFoundException("You are not friends with this user"));

        friendshipRepository.delete(friendship);
    }
}
