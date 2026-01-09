package com.smartDine.services;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartDine.entity.Customer;
import com.smartDine.entity.Friendship;
import com.smartDine.exceptions.FriendshipAlreadyExistsException;
import com.smartDine.exceptions.FriendshipNotFoundException;
import com.smartDine.repository.CustomerRepository;
import com.smartDine.repository.FriendshipRepository;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private FriendshipService friendshipService;

    private Customer userA;
    private Customer userB;
    private Customer userC;
    private Friendship friendship;

    @BeforeEach
    void setUp() {
        userA = new Customer();
        userA.setId(1L);
        userA.setName("User A");
        userA.setEmail("userA@test.com");

        userB = new Customer();
        userB.setId(2L);
        userB.setName("User B");
        userB.setEmail("userB@test.com");

        userC = new Customer();
        userC.setId(3L);
        userC.setName("User C");
        userC.setEmail("userC@test.com");

        friendship = new Friendship(userA, userB);
        friendship.setId(10L);
    }

    // ========== Get Friends Tests ==========
    @Nested
    @DisplayName("getFriends")
    class GetFriendsTests {

        @Test
        @DisplayName("Should return empty list when no friends")
        void getFriendsEmpty() {
            when(friendshipRepository.findAllByUser(userA)).thenReturn(List.of());

            List<Friendship> result = friendshipService.getFriends(userA);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return list of friendships")
        void getFriendsSuccess() {
            Friendship friendship2 = new Friendship(userA, userC);
            friendship2.setId(11L);

            when(friendshipRepository.findAllByUser(userA)).thenReturn(List.of(friendship, friendship2));

            List<Friendship> result = friendshipService.getFriends(userA);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should return friendships where user is userB")
        void getFriendsWhenUserIsB() {
            when(friendshipRepository.findAllByUser(userB)).thenReturn(List.of(friendship));

            List<Friendship> result = friendshipService.getFriends(userB);

            assertEquals(1, result.size());
            assertTrue(result.get(0).involves(userB));
        }
    }

    // ========== Are Friends Tests ==========
    @Nested
    @DisplayName("areFriends")
    class AreFriendsTests {

        @Test
        @DisplayName("Should return true when users are friends")
        void areFriendsTrue() {
            when(friendshipRepository.existsBetween(userA, userB)).thenReturn(true);

            boolean result = friendshipService.areFriends(userA, userB);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when users are not friends")
        void areFriendsFalse() {
            when(friendshipRepository.existsBetween(userA, userC)).thenReturn(false);

            boolean result = friendshipService.areFriends(userA, userC);

            assertFalse(result);
        }

        @Test
        @DisplayName("Should be symmetric - areFriends(A, B) same as areFriends(B, A)")
        void areFriendsSymmetric() {
            when(friendshipRepository.existsBetween(userA, userB)).thenReturn(true);
            when(friendshipRepository.existsBetween(userB, userA)).thenReturn(true);

            boolean resultAB = friendshipService.areFriends(userA, userB);
            boolean resultBA = friendshipService.areFriends(userB, userA);

            assertEquals(resultAB, resultBA);
        }
    }

    // ========== Create Friendship Tests ==========
    @Nested
    @DisplayName("createFriendship")
    class CreateFriendshipTests {

        @Test
        @DisplayName("Should throw FriendshipAlreadyExistsException when already friends")
        void createFriendshipAlreadyExists() {
            when(friendshipRepository.existsBetween(userA, userB)).thenReturn(true);

            assertThrows(FriendshipAlreadyExistsException.class, () -> {
                friendshipService.createFriendship(userA, userB);
            });

            verify(friendshipRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create and save friendship successfully")
        void createFriendshipSuccess() {
            when(friendshipRepository.existsBetween(userA, userB)).thenReturn(false);
            when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> {
                Friendship f = invocation.getArgument(0);
                f.setId(20L);
                return f;
            });

            Friendship result = friendshipService.createFriendship(userA, userB);

            assertNotNull(result);
            assertEquals(20L, result.getId());
            assertTrue(result.involves(userA));
            assertTrue(result.involves(userB));
        }
    }

    // ========== Remove Friend Tests ==========
    @Nested
    @DisplayName("removeFriend")
    class RemoveFriendTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when friend not found")
        void removeFriendUserNotFound() {
            when(customerRepository.findById(999L)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                friendshipService.removeFriend(userA, 999L);
            });

            assertTrue(exception.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("Should throw FriendshipNotFoundException when friendship does not exist")
        void removeFriendNotFriends() {
            when(customerRepository.findById(3L)).thenReturn(Optional.of(userC));
            when(friendshipRepository.findByUsers(userA, userC)).thenReturn(Optional.empty());

            assertThrows(FriendshipNotFoundException.class, () -> {
                friendshipService.removeFriend(userA, 3L);
            });
        }

        @Test
        @DisplayName("Should delete friendship successfully")
        void removeFriendSuccess() {
            when(customerRepository.findById(2L)).thenReturn(Optional.of(userB));
            when(friendshipRepository.findByUsers(userA, userB)).thenReturn(Optional.of(friendship));

            friendshipService.removeFriend(userA, 2L);

            verify(friendshipRepository).delete(friendship);
        }

        @Test
        @DisplayName("Should work when caller is userB in friendship")
        void removeFriendAsUserB() {
            when(customerRepository.findById(1L)).thenReturn(Optional.of(userA));
            when(friendshipRepository.findByUsers(userB, userA)).thenReturn(Optional.of(friendship));

            friendshipService.removeFriend(userB, 1L);

            verify(friendshipRepository).delete(friendship);
        }
    }
}
