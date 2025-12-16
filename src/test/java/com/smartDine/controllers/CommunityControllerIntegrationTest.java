package com.smartDine.controllers;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import com.smartDine.dto.CommunityDTO;
import com.smartDine.dto.CreateCommunityDTO;
import com.smartDine.dto.MemberDTO;
import com.smartDine.dto.UploadResponse;
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

    @Test
    public void uploadCommunityImage_shouldReturnUploadResponse() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes());

        UploadResponse mockResponse = new UploadResponse(
                "communities/100/images/test-uuid.jpg",
                "https://smartdine-s3-bucket.s3.amazonaws.com/communities/100/images/test-uuid.jpg",
                "image/jpeg",
                file.getSize());

        when(communityService.uploadCommunityImage(eq(100L), any(), eq(sampleOwner))).thenReturn(mockResponse);

        ResponseEntity<UploadResponse> resp = communityController.uploadCommunityImage(100L, file, sampleOwner);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getKey());
        assertNotNull(resp.getBody().getUrl());
        assertNotNull(resp.getBody().getContentType());
        assertNotNull(resp.getBody().getSize());
        assertEquals("image/jpeg", resp.getBody().getContentType());
        assertEquals(file.getSize(), resp.getBody().getSize());
    }

    @Test
    public void getCommunityMembers_shouldReturnMembersList() {
        Member member1 = new Member();
        member1.setId(1L);
        member1.setUser(sampleOwner);
        member1.setCommunity(sampleCommunity);
        member1.setMemberRole(MemberRole.OWNER);

        Member member2 = new Member();
        member2.setId(2L);
        member2.setUser(sampleUser);
        member2.setCommunity(sampleCommunity);
        member2.setMemberRole(MemberRole.PARTICIPANT);

        when(communityService.getCommunityMembers(100L)).thenReturn(List.of(member1, member2));

        ResponseEntity<List<MemberDTO>> resp = communityController.getCommunityMembers(100L, null);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(2, resp.getBody().size());
        assertEquals("OWNER", resp.getBody().get(0).getMemberRole());
        assertEquals("PARTICIPANT", resp.getBody().get(1).getMemberRole());
    }

    @Test
    public void getCommunityMembers_shouldThrowExceptionWhenNotFound() {
        when(communityService.getCommunityMembers(9999L))
                .thenThrow(new IllegalArgumentException("Community not found"));

        assertThrows(IllegalArgumentException.class, () -> {
            communityController.getCommunityMembers(9999L, null);
        });
    }
}

