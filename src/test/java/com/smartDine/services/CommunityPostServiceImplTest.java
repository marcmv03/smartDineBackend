package com.smartDine.services;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;

import com.smartDine.dto.community.post.CreateCommunityPostRequestDTO;
import com.smartDine.dto.community.post.UpdateCommunityPostRequestDTO;
import com.smartDine.entity.Community;
import com.smartDine.entity.CommunityType;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Member;
import com.smartDine.entity.MemberRole;
import com.smartDine.entity.community.CommunityPost;
import com.smartDine.exceptions.NoUserIsMemberException;
import com.smartDine.repository.CommunityMemberRepository;
import com.smartDine.repository.CommunityPostRepository;
import com.smartDine.repository.CommunityRepository;
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
                .getPostsByCommunity(community.getId(), null, Pageable.unpaged(), null));
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
}
