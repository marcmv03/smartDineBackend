package com.smartDine.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.smartDine.dto.CommunityDTO;
import com.smartDine.dto.CreateCommunityDTO;
import com.smartDine.dto.MemberDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Community;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Member;
import com.smartDine.entity.MemberRole;
import com.smartDine.services.CommunityService;
import com.smartDine.services.MemberService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class CommunityControllerIntegrationTest {

    @Mock
    private CommunityService communityService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private CommunityController communityController;

    private Community sampleCommunity;
    private Business sampleOwner;
    private Customer sampleUser;

    @BeforeEach
    void setUp() {
        sampleOwner = new Business("Owner", "owner@test.com", "password", 111111111L);
        sampleOwner.setId(1L);

        sampleUser = new Customer("User", "user@test.com", "password", 222222222L);
        sampleUser.setId(2L);

        sampleCommunity = new Community();
        sampleCommunity.setId(100L);
        sampleCommunity.setName("Test Community");
        sampleCommunity.setDescription("Description");
        sampleCommunity.setVisibility(true);
    }

    @Test
    public void getCommunities_shouldReturnList() {
        when(communityService.getCommunities(null)).thenReturn(List.of(sampleCommunity));

        ResponseEntity<List<CommunityDTO>> resp = communityController.getCommunities(null);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(1, resp.getBody().size());
        assertEquals("Test Community", resp.getBody().get(0).getName());
    }

    @Test
    public void getCommunityById_shouldReturnCommunity() {
        when(communityService.getCommunityById(100L)).thenReturn(sampleCommunity);

        ResponseEntity<CommunityDTO> resp = communityController.getCommunityById(100L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(100L, resp.getBody().getId());
    }

    @Test
    public void getCommunityById_shouldThrowExceptionWhenNotFound() {
        when(communityService.getCommunityById(9999L))
            .thenThrow(new IllegalArgumentException("Community not found with ID: 9999"));

        assertThrows(IllegalArgumentException.class, () -> {
            communityController.getCommunityById(9999L);
        });
    }

    @Test
    public void createCommunity_shouldReturnCreated() {
        CreateCommunityDTO dto = new CreateCommunityDTO();
        dto.setName("New Community");
        dto.setDescription("Desc");
        dto.setVisibility(true);

        Community created = new Community();
        created.setId(101L);
        created.setName("New Community");
        created.setDescription("Desc");

        when(communityService.createCommunity(any(CreateCommunityDTO.class), eq(sampleOwner))).thenReturn(created);

        ResponseEntity<CommunityDTO> resp = communityController.createCommunity(dto, sampleOwner);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(101L, resp.getBody().getId());
        assertEquals("New Community", resp.getBody().getName());
    }

    @Test
    public void joinCommunity_shouldReturnMember() {
        Member member = new Member();
        member.setId(500L);
        member.setUser(sampleUser);
        member.setCommunity(sampleCommunity);
        member.setMemberRole(MemberRole.PARTICIPANT);

        when(memberService.joinCommunity(100L, sampleUser)).thenReturn(member);

        ResponseEntity<MemberDTO> resp = communityController.joinCommunity(100L, sampleUser);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(500L, resp.getBody().getId());
        assertEquals(MemberRole.PARTICIPANT.name(), resp.getBody().getMemberRole());
    }
}

