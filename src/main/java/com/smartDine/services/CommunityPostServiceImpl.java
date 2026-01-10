package com.smartDine.services;

import java.util.List;
import java.util.Objects;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.dto.community.post.CreateCommunityPostRequestDTO;
import com.smartDine.dto.community.post.CreateOpenReservationPostDTO;
import com.smartDine.dto.community.post.UpdateCommunityPostRequestDTO;
import com.smartDine.entity.Community;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Member;
import com.smartDine.entity.MemberRole;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.Role;
import com.smartDine.entity.User;
import com.smartDine.entity.community.CommunityPost;
import com.smartDine.entity.community.OpenReservationPost;
import com.smartDine.entity.community.PostType;
import com.smartDine.exceptions.NoUserIsMemberException;
import com.smartDine.repository.CommunityMemberRepository;
import com.smartDine.repository.CommunityPostRepository;
import com.smartDine.repository.CommunityRepository;
import com.smartDine.repository.OpenReservationPostRepository;
import com.smartDine.repository.UserRepository;

@Service
public class CommunityPostServiceImpl implements CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final OpenReservationPostRepository openReservationPostRepository;
    private final ReservationService reservationService;
    private final NotificationService notificationService;

    public CommunityPostServiceImpl(CommunityPostRepository communityPostRepository,
            CommunityMemberRepository communityMemberRepository,
            CommunityRepository communityRepository,
            UserRepository userRepository,
            OpenReservationPostRepository openReservationPostRepository,
            ReservationService reservationService,
            NotificationService notificationService) {
        this.communityPostRepository = communityPostRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
        this.openReservationPostRepository = openReservationPostRepository;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public CommunityPost createPost(Long currentUserId, CreateCommunityPostRequestDTO requestDTO) {
        Community community = getCommunity(requestDTO.getCommunityId());
        User user = getUser(currentUserId);
        Member member = getMemberForCommunity(user, community);

        if (!isAdminOrOwner(member)) {
            throw new BadCredentialsException("Only administrators or owners can create posts in this community");
        }

        CommunityPost post = CreateCommunityPostRequestDTO.toEntity(requestDTO, community, member);
        return communityPostRepository.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityPost getPostById(Long postId, Long currentUserId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        validateReadAccess(post.getCommunity(), currentUserId);
        return post;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityPost> getPostsByMember(Long memberId, String search,
            Long currentUserId) {
        Member author = communityMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

        validateReadAccess(author.getCommunity(), currentUserId);

        if (search != null && !search.isBlank()) {
            return communityPostRepository.searchByAuthor(author, search);
        } else {
            return communityPostRepository.findByAuthor(author);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityPost> getPostsByCommunity(Long communityId, String search,
            Long currentUserId) {
        Community community = getCommunity(communityId);
        validateReadAccess(community, currentUserId);

        if (search != null && !search.isBlank()) {
            return communityPostRepository.searchByCommunity(community, search);
        } else {
            return communityPostRepository.findByCommunity(community);
        }
    }

    @Override
    @Transactional
    public CommunityPost updatePost(Long postId, Long currentUserId,
            UpdateCommunityPostRequestDTO requestDTO) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        Member actor = getMemberForCommunity(getUser(currentUserId), post.getCommunity());
        if (!isAdminOrOwner(actor) && !Objects.equals(actor.getUser().getId(), post.getAuthor().getUser().getId())) {
            throw new BadCredentialsException("Only post author, administrators or owners can update this post");
        }

        if (requestDTO.getTitle() != null) {
            post.setTitle(requestDTO.getTitle());
        }
        if (requestDTO.getDescription() != null) {
            post.setDescription(requestDTO.getDescription());
        }

        return communityPostRepository.save(post);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long currentUserId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        Member actor = getMemberForCommunity(getUser(currentUserId), post.getCommunity());
        if (!isAdminOrOwner(actor) && !Objects.equals(actor.getUser().getId(), post.getAuthor().getUser().getId())) {
            throw new BadCredentialsException("Only post author, administrators or owners can delete this post");
        }

        communityPostRepository.delete(post);
    }

    private void validateReadAccess(Community community, Long currentUserId) {
        if (community.isVisibility()) {
            return;
        }
        if (currentUserId == null) {
            throw new IllegalArgumentException("User must be authenticated to access this community");
        }
        getMemberForCommunity(getUser(currentUserId), community);
    }

    private Member getMemberForCommunity(User user, Community community) {
        return communityMemberRepository.findByUserAndCommunity(user, community)
                .orElseThrow(() -> new NoUserIsMemberException("User is not a member of this community"));
    }

    private Community getCommunity(Long communityId) {
        return communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found with id: " + communityId));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }

    private boolean isAdminOrOwner(Member member) {
        return member.getMemberRole() == MemberRole.ADMIN || member.getMemberRole() == MemberRole.OWNER;
    }

    @Override
    @Transactional
    public OpenReservationPost createOpenReservationPost(Long currentUserId, CreateOpenReservationPostDTO requestDTO) {
        Community community = getCommunity(requestDTO.getCommunityId());
        User user = getUser(currentUserId);
        Member member = getMemberForCommunity(user, community);

        // Validate user has OWNER or ADMIN role in community
        if (!isAdminOrOwner(member)) {
            throw new BadCredentialsException("Only administrators or owners can create open reservation posts");
        }

        // Validate user is a CUSTOMER (global role)
        if (user.getRole() != Role.ROLE_CUSTOMER) {
            throw new BadCredentialsException("Only customers can create open reservation posts");
        }

        Customer customer = (Customer) user;

        // Get and validate reservation
        Reservation reservation = reservationService.getReservationById(requestDTO.getReservationId());

        // Validate user is a participant in the reservation (creator)
        if (!reservationService.isParticipant(reservation, customer)) {
            throw new IllegalArgumentException("You must be a participant of the reservation to create an open reservation post");
        }

        // Validate maxParticipants doesn't exceed remaining table capacity
        // Available slots = table capacity - current total participants
        int currentTotal = reservationService.getTotalParticipantsCount(reservation);
        int availableSlots = reservation.getRestaurantTable().getCapacity() - currentTotal;
        
        if (requestDTO.getMaxParticipants() > availableSlots) {
            throw new IllegalArgumentException(
                "Max participants (" + requestDTO.getMaxParticipants() + 
                ") exceeds available table capacity (" + availableSlots + " slots remaining)"
            );
        }

        // Check if an open reservation post already exists for this reservation
        if (openReservationPostRepository.existsByReservation(reservation)) {
            throw new IllegalArgumentException("An open reservation post already exists for this reservation");
        }

        // Create and save the post
        OpenReservationPost post = CreateOpenReservationPostDTO.toEntity(requestDTO, community, member, reservation);
        return openReservationPostRepository.save(post);
    }

    @Override
    @Transactional
    public OpenReservationPost joinOpenReservationPost(Long postId, Long currentUserId) {
        // Get the post and validate it's an OpenReservationPost
        CommunityPost basePost = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        if (basePost.getType() != PostType.OPEN_RESERVATION || !(basePost instanceof OpenReservationPost)) {
            throw new IllegalArgumentException("Post is not an open reservation post");
        }

        OpenReservationPost post = (OpenReservationPost) basePost;
        
        User user = getUser(currentUserId);
        
        // Validate user is a member of the community
        getMemberForCommunity(user, post.getCommunity());

        // Validate user is a CUSTOMER
        if (user.getRole() != Role.ROLE_CUSTOMER) {
            throw new BadCredentialsException("Only customers can join open reservation posts");
        }

        Customer customer = (Customer) user;

        // Check if there are available slots in the post
        if (!post.hasAvailableSlots()) {
            throw new IllegalArgumentException("No available slots in this open reservation post");
        }

        // Delegate to ReservationService for all reservation validations
        // This will check: expiration, already participant, capacity, time conflicts
        reservationService.addParticipantToReservation(
            post.getReservation().getId(), 
            customer, 
            post.getMaxParticipants()
        );

        // Increment the post's participant count
        post.incrementParticipants();
        OpenReservationPost savedPost = openReservationPostRepository.save(post);
        
        // Notify the reservation creator that someone joined
        User reservationCreator = post.getReservation().getCustomer();
        String message = String.format("%s se ha unido a la reserva abierta %s de la comunidad %s",
                customer.getName(),
                post.getTitle(),
                post.getCommunity().getName());
        notificationService.createNotification(reservationCreator, message);
        
        return savedPost;
    }

    @Override
    @Transactional(readOnly = true)
    public OpenReservationPost getOpenReservationPostById(Long postId, Long currentUserId) {
        // Get the post
        CommunityPost basePost = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        // Validate it's an OpenReservationPost
        if (basePost.getType() != PostType.OPEN_RESERVATION || !(basePost instanceof OpenReservationPost)) {
            throw new IllegalArgumentException("Post is not an open reservation post");
        }

        OpenReservationPost post = (OpenReservationPost) basePost;

        // Validate read access to the community
        validateReadAccess(post.getCommunity(), currentUserId);

        return post;
    }
}
