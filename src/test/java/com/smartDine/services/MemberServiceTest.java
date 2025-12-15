package com.smartDine.services;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.dto.CreateCommunityDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Community;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Member;
import com.smartDine.entity.MemberRole;
import com.smartDine.exceptions.NoUserIsMemberException;
import com.smartDine.repository.BusinessRepository;
import com.smartDine.repository.CustomerRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private CommunityService communityService;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private CustomerRepository customerRepository;


    private Business owner;
    private Customer user;
    private Customer anotherUser;
    private Community publicCommunity;
    private Community privateCommunity;

    @BeforeEach
    void setUp() {
        owner = new Business("Owner", "owner@test.com", "password", 111111111L);
        owner = businessRepository.save(owner);

        user = new Customer("User", "user@test.com", "password", 222222222L);
        user = customerRepository.save(user);

        anotherUser = new Customer("Another User", "another@test.com", "password", 333333333L);
        anotherUser = customerRepository.save(anotherUser);

        CreateCommunityDTO publicDto = new CreateCommunityDTO();
        publicDto.setName("Public Community");
        publicDto.setDescription("Public");
        publicDto.setVisibility(true);
        publicCommunity = communityService.createCommunity(publicDto, owner);

        CreateCommunityDTO privateDto = new CreateCommunityDTO();
        privateDto.setName("Private Community");
        privateDto.setDescription("Private");
        privateDto.setVisibility(false);
        privateCommunity = communityService.createCommunity(privateDto, owner);
    }

    @Test
    @DisplayName("Should join public community successfully")
    void testJoinPublicCommunity() {
        Member member = memberService.joinCommunity(publicCommunity.getId(), user);

        assertNotNull(member.getId());
        assertEquals(user.getId(), member.getUser().getId());
        assertEquals(publicCommunity.getId(), member.getCommunity().getId());
        assertEquals(MemberRole.PARTICIPANT, member.getMemberRole());
    }

    @Test
    @DisplayName("Should fail to join private community")
    void testJoinPrivateCommunity() {
        assertThrows(IllegalArgumentException.class, () -> memberService.joinCommunity(privateCommunity.getId(), user));
    }

    @Test
    @DisplayName("Should fail to join if already a member")
    void testJoinDuplicate() {
        memberService.joinCommunity(publicCommunity.getId(), user);

        assertThrows(IllegalArgumentException.class, () -> memberService.joinCommunity(publicCommunity.getId(), user));
    }

    @Test
    @DisplayName("Should fail to join if owner tries to join again")
    void testOwnerJoinAgain() {
        assertThrows(IllegalArgumentException.class, () -> 
            memberService.joinCommunity(publicCommunity.getId(), owner));
    }

    // Tests for getMemberById
    @Test
    @DisplayName("Should get member by ID successfully")
    void testGetMemberById() {
        Member member = memberService.joinCommunity(publicCommunity.getId(), user);
        
        Member retrievedMember = memberService.getMemberById(member.getId());
        
        assertNotNull(retrievedMember);
        assertEquals(member.getId(), retrievedMember.getId());
        assertEquals(user.getId(), retrievedMember.getUser().getId());
        assertEquals(publicCommunity.getId(), retrievedMember.getCommunity().getId());
    }

    @Test
    @DisplayName("Should fail to get member with non-existent ID")
    void testGetMemberByIdNotFound() {
        assertThrows(IllegalArgumentException.class, () -> 
            memberService.getMemberById(99999L));
    }

    // Tests for deleteMember
    @Test
    @DisplayName("Should delete member when user is community owner")
    void testDeleteMemberAsOwner() {
        // User joins the community
        Member member = memberService.joinCommunity(publicCommunity.getId(), user);
        Long memberId = member.getId();
        
        // Owner deletes the member
        memberService.deleteMember(memberId, owner);
        
        // Verify member is deleted
        assertThrows(IllegalArgumentException.class, () -> 
            memberService.getMemberById(memberId));
    }

    @Test
    @DisplayName("Should delete member when user deletes themselves")
    void testDeleteMemberSelf() {
        // User joins the community
        Member member = memberService.joinCommunity(publicCommunity.getId(), user);
        Long memberId = member.getId();
        
        // User deletes themselves
        memberService.deleteMember(memberId, user);
        
        // Verify member is deleted
        assertThrows(IllegalArgumentException.class, () -> 
            memberService.getMemberById(memberId));
    }

    @Test
    @DisplayName("Should fail to delete member when user is not owner and not self")
    void testDeleteMemberUnauthorized() {
        // User joins the community
        Member member = memberService.joinCommunity(publicCommunity.getId(), user);
        Long memberId = member.getId();
        
        // Another user (not owner, not self) tries to delete
        assertThrows(NoUserIsMemberException.class, () -> 
            memberService.deleteMember(memberId, anotherUser));
    }

    @Test
    @DisplayName("Should fail to delete non-existent member")
    void testDeleteMemberNotFound() {
        assertThrows(IllegalArgumentException.class, () -> 
            memberService.deleteMember(99999L, owner));
    }

    @Test
    @DisplayName("Should allow participant to delete themselves")
    void testDeleteSelfAsParticipant() {
        // User joins as participant
        Member member = memberService.joinCommunity(publicCommunity.getId(), user);
        Long memberId = member.getId();
        
        assertEquals(MemberRole.PARTICIPANT, member.getMemberRole());
        
        // Participant deletes themselves
        memberService.deleteMember(memberId, user);
        
        // Verify deleted
        assertThrows(IllegalArgumentException.class, () -> 
            memberService.getMemberById(memberId));
    }

    // Tests for getMembersByCommunityId
    @Test
    @DisplayName("Should get all members by community ID")
    void testGetMembersByCommunityId() {
        // Owner is already a member, join another user
        Member member = memberService.joinCommunity(publicCommunity.getId(), user);
        
        List<Member> members = memberService.getMembersByCommunityId(publicCommunity.getId());
        
        // Should have 2 members: owner + user
        assertEquals(2, members.size());
        assertTrue(members.stream().anyMatch(m -> m.getUser().getId().equals(owner.getId())));
        assertTrue(members.stream().anyMatch(m -> m.getUser().getId().equals(user.getId())));
    }

    @Test
    @DisplayName("Should fail to get members of non-existent community")
    void testGetMembersByCommunityIdNotFound() {
        assertThrows(IllegalArgumentException.class, () -> 
            memberService.getMembersByCommunityId(99999L));
    }
}
