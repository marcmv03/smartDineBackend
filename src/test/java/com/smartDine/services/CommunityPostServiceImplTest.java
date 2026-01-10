package com.smartDine.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import com.smartDine.dto.community.post.CreateCommunityPostRequestDTO;
import com.smartDine.dto.community.post.CreateOpenReservationPostDTO;
import com.smartDine.dto.community.post.UpdateCommunityPostRequestDTO;
import com.smartDine.entity.Community;
import com.smartDine.entity.CommunityType;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Member;
import com.smartDine.entity.MemberRole;
import com.smartDine.entity.Notification;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.ReservationStatus;
import com.smartDine.entity.Restaurant;
import com.smartDine.entity.RestaurantTable;
import com.smartDine.entity.TimeSlot;
import com.smartDine.entity.community.CommunityPost;
import com.smartDine.entity.community.OpenReservationPost;
import com.smartDine.exceptions.ExpiredOpenReservationException;
import com.smartDine.exceptions.NoUserIsMemberException;
import com.smartDine.repository.CommunityMemberRepository;
import com.smartDine.repository.CommunityPostRepository;
import com.smartDine.repository.CommunityRepository;
import com.smartDine.repository.OpenReservationPostRepository;
import com.smartDine.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CommunityPostServiceImplTest {

    @Mock
    private CommunityPostRepository communityPostRepository;
    @Mock
    private CommunityMemberRepository communityMemberRepository;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OpenReservationPostRepository openReservationPostRepository;
    @Mock
    private ReservationService reservationService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommunityPostServiceImpl communityPostService;

    private Community community;
    private Member adminMember;
    private Member participantMember;
    private Customer user;
    private Customer otherUser;

    @BeforeEach
    void init() {
        user = new Customer();
        user.setId(1L);
        user.setName("Admin");
        user.setEmail("admin@example.com");
        user.setPassword("password");
        user.setPhoneNumber(123L);

        otherUser = new Customer();
        otherUser.setId(2L);
        otherUser.setName("Other");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password");
        otherUser.setPhoneNumber(456L);

        community = new Community();
        community.setId(10L);
        community.setName("Community");
        community.setDescription("desc");
        community.setVisibility(false);
        community.setCommunityType(CommunityType.USER);

        adminMember = new Member();
        adminMember.setId(20L);
        adminMember.setUser(user);
        adminMember.setCommunity(community);
        adminMember.setMemberRole(MemberRole.ADMIN);

        participantMember = new Member();
        participantMember.setId(21L);
        participantMember.setUser(otherUser);
        participantMember.setCommunity(community);
        participantMember.setMemberRole(MemberRole.PARTICIPANT);
    }

    @Test
    void createPostShouldPersistWhenUserIsAdmin() {
        CreateCommunityPostRequestDTO requestDTO = new CreateCommunityPostRequestDTO();
        requestDTO.setCommunityId(community.getId());
        requestDTO.setTitle("Hello");
        requestDTO.setDescription("World");

        when(communityRepository.findById(anyLong())).thenReturn(Optional.of(community));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(communityMemberRepository.findByUserAndCommunity(user, community)).thenReturn(Optional.of(adminMember));
        when(communityPostRepository.save(any(CommunityPost.class))).thenAnswer(inv -> {
            CommunityPost post = inv.getArgument(0);
            post.setId(100L);
            return post;
        });

        var response = communityPostService.createPost(user.getId(), requestDTO);
        assertEquals(100L, response.getId());
        assertEquals("Hello", response.getTitle());
    }

    @Test
    void createPostShouldThrowNoUserIsMemberExceptionWhenUserNotMember() {
        CreateCommunityPostRequestDTO requestDTO = new CreateCommunityPostRequestDTO();
        requestDTO.setCommunityId(community.getId());
        requestDTO.setTitle("Test");
        requestDTO.setDescription("Desc");

        when(communityRepository.findById(anyLong())).thenReturn(Optional.of(community));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(otherUser));
        when(communityMemberRepository.findByUserAndCommunity(otherUser, community)).thenReturn(Optional.empty());

        assertThrows(NoUserIsMemberException.class,
                () -> communityPostService.createPost(otherUser.getId(), requestDTO));
    }

    @Test
    void createPostShouldThrowBadCredentialsWhenUserIsNotAdminOrOwner() {
        CreateCommunityPostRequestDTO requestDTO = new CreateCommunityPostRequestDTO();
        requestDTO.setCommunityId(community.getId());
        requestDTO.setTitle("Test");
        requestDTO.setDescription("Desc");

        when(communityRepository.findById(anyLong())).thenReturn(Optional.of(community));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(otherUser));
        when(communityMemberRepository.findByUserAndCommunity(otherUser, community)).thenReturn(Optional.of(participantMember));

        assertThrows(BadCredentialsException.class,
                () -> communityPostService.createPost(otherUser.getId(), requestDTO));
    }

    @Test
    void getPostsByCommunityShouldFailForPrivateCommunityIfNotMember() {
        when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
        // Removed unnecessary stubs - userId is null so these mocks are never used

        assertThrows(IllegalArgumentException.class, () -> communityPostService
                .getPostsByCommunity(community.getId(), null, null));
    }

    @Test
    void updatePostShouldThrowBadCredentialsWhenNotAuthorNorAdmin() {
        CommunityPost post = new CommunityPost();
        post.setId(300L);
        post.setCommunity(community);
        post.setAuthor(adminMember);
        post.setTitle("Title");
        post.setDescription("Desc");

        when(communityPostRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));
        when(communityMemberRepository.findByUserAndCommunity(otherUser, community)).thenReturn(Optional.of(participantMember));

        UpdateCommunityPostRequestDTO updateDTO = new UpdateCommunityPostRequestDTO();
        updateDTO.setTitle("Updated");

        assertThrows(BadCredentialsException.class,
                () -> communityPostService.updatePost(post.getId(), otherUser.getId(), updateDTO));
    }

    @Test
    void deletePostShouldThrowBadCredentialsWhenNotAuthorNorAdmin() {
        CommunityPost post = new CommunityPost();
        post.setId(400L);
        post.setCommunity(community);
        post.setAuthor(adminMember);
        post.setTitle("Title");
        post.setDescription("Desc");

        when(communityPostRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));
        when(communityMemberRepository.findByUserAndCommunity(otherUser, community)).thenReturn(Optional.of(participantMember));

        assertThrows(BadCredentialsException.class,
                () -> communityPostService.deletePost(post.getId(), otherUser.getId()));
    }

    // ==================== OpenReservationPost Tests ====================

    @Test
    void createOpenReservationPostShouldSucceedWhenUserIsAdminAndCustomer() {
        // Setup customer user (must be a Customer for this to work)
        Customer customerUser = new Customer();
        customerUser.setId(1L);
        customerUser.setName("Admin Customer");
        customerUser.setEmail("admincustomer@example.com");
        customerUser.setPassword("password");
        customerUser.setPhoneNumber(123L);

        Member adminCustomerMember = new Member();
        adminCustomerMember.setId(20L);
        adminCustomerMember.setUser(customerUser);
        adminCustomerMember.setCommunity(community);
        adminCustomerMember.setMemberRole(MemberRole.ADMIN);

        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");

        RestaurantTable table = new RestaurantTable();
        table.setId(1L);
        table.setCapacity(6);
        table.setRestaurant(restaurant);

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId(1L);
        timeSlot.setStartTime(12.0);
        timeSlot.setEndTime(14.0);
        timeSlot.setDayOfWeek(DayOfWeek.MONDAY);
        timeSlot.setRestaurant(restaurant);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setCustomer(customerUser);
        reservation.setRestaurant(restaurant);
        reservation.setRestaurantTable(table);
        reservation.setTimeSlot(timeSlot);
        reservation.setDate(LocalDate.now().plusDays(1));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setParticipants(new HashSet<>());

        CreateOpenReservationPostDTO requestDTO = new CreateOpenReservationPostDTO();
        requestDTO.setCommunityId(community.getId());
        requestDTO.setTitle("Join my dinner!");
        requestDTO.setDescription("Looking for friends to join");
        requestDTO.setReservationId(1L);
        requestDTO.setMaxParticipants(3);

        when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
        when(userRepository.findById(customerUser.getId())).thenReturn(Optional.of(customerUser));
        when(communityMemberRepository.findByUserAndCommunity(customerUser, community)).thenReturn(Optional.of(adminCustomerMember));
        when(reservationService.getReservationById(1L)).thenReturn(reservation);
        when(reservationService.isParticipant(reservation, customerUser)).thenReturn(true);
        when(reservationService.getTotalParticipantsCount(reservation)).thenReturn(1);
        when(openReservationPostRepository.existsByReservation(reservation)).thenReturn(false);
        when(openReservationPostRepository.save(any(OpenReservationPost.class))).thenAnswer(inv -> {
            OpenReservationPost post = inv.getArgument(0);
            post.setId(500L);
            return post;
        });

        OpenReservationPost result = communityPostService.createOpenReservationPost(customerUser.getId(), requestDTO);

        assertNotNull(result);
        assertEquals(500L, result.getId());
        assertEquals("Join my dinner!", result.getTitle());
        assertEquals(3, result.getMaxParticipants());
    }

    @Test
    void createOpenReservationPostShouldFailWhenUserNotAdminOrOwner() {
        CreateOpenReservationPostDTO requestDTO = new CreateOpenReservationPostDTO();
        requestDTO.setCommunityId(community.getId());
        requestDTO.setTitle("Test");
        requestDTO.setDescription("Desc");
        requestDTO.setReservationId(1L);
        requestDTO.setMaxParticipants(3);

        when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));
        when(communityMemberRepository.findByUserAndCommunity(otherUser, community)).thenReturn(Optional.of(participantMember));

        assertThrows(BadCredentialsException.class,
                () -> communityPostService.createOpenReservationPost(otherUser.getId(), requestDTO));
    }

    @Test
    void joinOpenReservationPostShouldSucceedWhenValid() {
        Customer joiner = new Customer();
        joiner.setId(3L);
        joiner.setName("Joiner");
        joiner.setEmail("joiner@example.com");
        joiner.setPassword("password");
        joiner.setPhoneNumber(789L);

        Member joinerMember = new Member();
        joinerMember.setId(30L);
        joinerMember.setUser(joiner);
        joinerMember.setCommunity(community);
        joinerMember.setMemberRole(MemberRole.PARTICIPANT);

        // Reservation owner (the one who receives the notification)
        Customer reservationOwner = new Customer();
        reservationOwner.setId(10L);
        reservationOwner.setName("Reservation Owner");
        reservationOwner.setEmail("owner@example.com");
        reservationOwner.setPassword("password");
        reservationOwner.setPhoneNumber(999L);

        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setRestaurant(restaurant);
        reservation.setCustomer(reservationOwner);

        OpenReservationPost post = new OpenReservationPost();
        post.setId(500L);
        post.setCommunity(community);
        post.setAuthor(adminMember);
        post.setTitle("Join dinner");
        post.setDescription("Desc");
        post.setReservation(reservation);
        post.setMaxParticipants(3);
        post.setCurrentParticipants(0);

        when(communityPostRepository.findById(500L)).thenReturn(Optional.of(post));
        when(userRepository.findById(joiner.getId())).thenReturn(Optional.of(joiner));
        when(communityMemberRepository.findByUserAndCommunity(joiner, community)).thenReturn(Optional.of(joinerMember));
        doNothing().when(reservationService).addParticipantToReservation(reservation.getId(), joiner, 3);
        when(openReservationPostRepository.save(any(OpenReservationPost.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notificationService.createNotification(any(), any())).thenAnswer(inv -> {
            Notification n = new Notification(inv.getArgument(0), inv.getArgument(1));
            n.setId(100L);
            return n;
        });

        OpenReservationPost result = communityPostService.joinOpenReservationPost(500L, joiner.getId());

        assertNotNull(result);
        assertEquals(1, result.getCurrentParticipants());
        
        // Verify notification was sent to reservation owner
        org.mockito.Mockito.verify(notificationService).createNotification(
            org.mockito.ArgumentMatchers.eq(reservationOwner),
            org.mockito.ArgumentMatchers.contains("Joiner se ha unido a la reserva abierta")
        );
    }

    @Test
    void joinOpenReservationPostShouldFailWhenNoSlotsAvailable() {
        Customer joiner = new Customer();
        joiner.setId(3L);
        joiner.setName("Joiner");
        joiner.setEmail("joiner@example.com");
        joiner.setPassword("password");
        joiner.setPhoneNumber(789L);

        Member joinerMember = new Member();
        joinerMember.setId(30L);
        joinerMember.setUser(joiner);
        joinerMember.setCommunity(community);
        joinerMember.setMemberRole(MemberRole.PARTICIPANT);

        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setRestaurant(restaurant);

        OpenReservationPost post = new OpenReservationPost();
        post.setId(500L);
        post.setCommunity(community);
        post.setAuthor(adminMember);
        post.setReservation(reservation);
        post.setMaxParticipants(2);
        post.setCurrentParticipants(2); // Full

        when(communityPostRepository.findById(500L)).thenReturn(Optional.of(post));
        when(userRepository.findById(joiner.getId())).thenReturn(Optional.of(joiner));
        when(communityMemberRepository.findByUserAndCommunity(joiner, community)).thenReturn(Optional.of(joinerMember));

        assertThrows(IllegalArgumentException.class,
                () -> communityPostService.joinOpenReservationPost(500L, joiner.getId()));
    }

    @Test
    void joinOpenReservationPostShouldFailWhenReservationExpired() {
        Customer joiner = new Customer();
        joiner.setId(3L);
        joiner.setName("Joiner");
        joiner.setEmail("joiner@example.com");
        joiner.setPassword("password");
        joiner.setPhoneNumber(789L);

        Member joinerMember = new Member();
        joinerMember.setId(30L);
        joinerMember.setUser(joiner);
        joinerMember.setCommunity(community);
        joinerMember.setMemberRole(MemberRole.PARTICIPANT);

        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setRestaurant(restaurant);

        OpenReservationPost post = new OpenReservationPost();
        post.setId(500L);
        post.setCommunity(community);
        post.setAuthor(adminMember);
        post.setReservation(reservation);
        post.setMaxParticipants(3);
        post.setCurrentParticipants(0);

        when(communityPostRepository.findById(500L)).thenReturn(Optional.of(post));
        when(userRepository.findById(joiner.getId())).thenReturn(Optional.of(joiner));
        when(communityMemberRepository.findByUserAndCommunity(joiner, community)).thenReturn(Optional.of(joinerMember));
        doThrow(new ExpiredOpenReservationException("Reservation expired"))
            .when(reservationService).addParticipantToReservation(anyLong(), any(Customer.class), anyInt());

        assertThrows(ExpiredOpenReservationException.class,
                () -> communityPostService.joinOpenReservationPost(500L, joiner.getId()));
    }
}
