package com.smartDine.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.smartDine.dto.MemberDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Community;
import com.smartDine.entity.CommunityType;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Member;
import com.smartDine.entity.MemberRole;
import com.smartDine.exceptions.NoUserIsMemberException;
import com.smartDine.services.MemberService;

@ExtendWith(MockitoExtension.class)
public class MembersControllerIntegrationTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MembersController membersController;

    private Business owner;
    private Customer user;
    private Community community;
    private Member member;

    @BeforeEach
    void setUp() {
        owner = new Business("Owner", "owner@test.com", "password", 111111111L);
        owner.setId(1L);

        user = new Customer("User", "user@test.com", "password", 222222222L);
        user.setId(2L);

        community = new Community();
        community.setId(100L);
        community.setName("Test Community");
        community.setDescription("Test");
        community.setVisibility(true);
        community.setCommunityType(CommunityType.USER);

        member = new Member();
        member.setId(1L);
        member.setUser(user);
        member.setCommunity(community);
        member.setMemberRole(MemberRole.PARTICIPANT);
    }

    @Test
    @DisplayName("Should get member by ID successfully")
    void testGetMember() {
        when(memberService.getMemberById(1L)).thenReturn(member);

        ResponseEntity<MemberDTO> response = membersController.getMember(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(user.getId(), response.getBody().getUserId());
        assertEquals(community.getId(), response.getBody().getCommunityId());
        assertEquals("PARTICIPANT", response.getBody().getMemberRole());
        verify(memberService).getMemberById(1L);
    }

    @Test
    @DisplayName("Should return BAD_REQUEST when member not found")
    void testGetMemberNotFound() {
        when(memberService.getMemberById(99999L))
            .thenThrow(new IllegalArgumentException("Member not found with id: 99999"));

        try {
            membersController.getMember(99999L);
        } catch (IllegalArgumentException e) {
            assertEquals("Member not found with id: 99999", e.getMessage());
        }
        
        verify(memberService).getMemberById(99999L);
    }

    @Test
    @DisplayName("Should delete member when user is owner")
    void testDeleteMemberAsOwner() {
        doNothing().when(memberService).deleteMember(1L, owner);

        ResponseEntity<Void> response = membersController.deleteMember(1L, owner);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(memberService).deleteMember(1L, owner);
    }

    @Test
    @DisplayName("Should delete member when user deletes themselves")
    void testDeleteMemberSelf() {
        doNothing().when(memberService).deleteMember(1L, user);

        ResponseEntity<Void> response = membersController.deleteMember(1L, user);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(memberService).deleteMember(1L, user);
    }

    @Test
    @DisplayName("Should return UNAUTHORIZED when user is null")
    void testDeleteMemberUnauthorized() {
        ResponseEntity<Void> response = membersController.deleteMember(1L, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return CONFLICT when user lacks permission to delete")
    void testDeleteMemberNoPermission() {
        Customer anotherUser = new Customer("Another", "another@test.com", "password", 333333333L);
        anotherUser.setId(3L);

        doThrow(new NoUserIsMemberException("You do not have permission to delete this member"))
            .when(memberService).deleteMember(eq(1L), eq(anotherUser));

        try {
            membersController.deleteMember(1L, anotherUser);
        } catch (NoUserIsMemberException e) {
            assertEquals("You do not have permission to delete this member", e.getMessage());
        }
        
        verify(memberService).deleteMember(1L, anotherUser);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent member")
    void testDeleteMemberNotFound() {
        doThrow(new IllegalArgumentException("Member not found with id: 99999"))
            .when(memberService).deleteMember(eq(99999L), any());

        try {
            membersController.deleteMember(99999L, owner);
        } catch (IllegalArgumentException e) {
            assertEquals("Member not found with id: 99999", e.getMessage());
        }
        
        verify(memberService).deleteMember(99999L, owner);
    }
}
