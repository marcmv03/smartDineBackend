package com.smartDine.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.adapters.ImageAdapter;
import com.smartDine.dto.CreateCommunityDTO;
import com.smartDine.dto.UploadResponse;
import com.smartDine.entity.Business;
import com.smartDine.entity.Community;
import com.smartDine.entity.CommunityType;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Member;
import com.smartDine.entity.MemberRole;
import com.smartDine.repository.BusinessRepository;
import com.smartDine.repository.CommunityRepository;
import com.smartDine.repository.CustomerRepository;
import com.smartDine.repository.MemberRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CommunityServiceTest {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @MockBean
    private ImageAdapter imageAdapter;

    private Business testBusiness;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testBusiness = new Business("Test Business", "business@test.com", "password", 123456789L);
        testBusiness = businessRepository.save(testBusiness);

        testCustomer = new Customer("Test Customer", "customer@test.com", "password", 987654321L);
        testCustomer = customerRepository.save(testCustomer);
    }

    @Test
    @DisplayName("Should create a community for Business user")
    void testCreateCommunity_Business() {
        CreateCommunityDTO dto = new CreateCommunityDTO();
        dto.setName("Business Community");
        dto.setDescription("Description");
        dto.setVisibility(true);

        Community community = communityService.createCommunity(dto, testBusiness);

        assertNotNull(community.getId());
        assertEquals("Business Community", community.getName());
        assertEquals(CommunityType.RESTAURANT, community.getCommunityType());
        
        // Check owner member
        List<Member> members = memberRepository.findByCommunity(community);
        assertEquals(1, members.size());
        assertEquals(testBusiness.getId(), members.get(0).getUser().getId());
        assertEquals(MemberRole.OWNER, members.get(0).getMemberRole());
    }

    @Test
    @DisplayName("Should create a community for Customer user")
    void testCreateCommunity_Customer() {
        CreateCommunityDTO dto = new CreateCommunityDTO();
        dto.setName("Customer Community");
        dto.setDescription("Description");
        dto.setVisibility(true);

        Community community = communityService.createCommunity(dto, testCustomer);

        assertNotNull(community.getId());
        assertEquals("Customer Community", community.getName());
        assertEquals(CommunityType.USER, community.getCommunityType());
        
        // Check owner member
        List<Member> members = memberRepository.findByCommunity(community);
        assertEquals(1, members.size());
        assertEquals(testCustomer.getId(), members.get(0).getUser().getId());
        assertEquals(MemberRole.OWNER, members.get(0).getMemberRole());
    }

    @Test
    @DisplayName("Should throw exception for duplicate community name")
    void testCreateCommunity_DuplicateName() {
        CreateCommunityDTO dto1 = new CreateCommunityDTO();
        dto1.setName("Unique Name");
        dto1.setDescription("Description");
        communityService.createCommunity(dto1, testBusiness);

        CreateCommunityDTO dto2 = new CreateCommunityDTO();
        dto2.setName("Unique Name"); // Duplicate
        dto2.setDescription("Description 2");

        assertThrows(IllegalArgumentException.class, () -> communityService.createCommunity(dto2, testCustomer));
    }

    @Test
    @DisplayName("Should get communities with search")
    void testGetCommunities_Search() {
        CreateCommunityDTO dto1 = new CreateCommunityDTO();
        dto1.setName("Pizza Lovers");
        dto1.setDescription("Pizza");
        communityService.createCommunity(dto1, testBusiness);

        CreateCommunityDTO dto2 = new CreateCommunityDTO();
        dto2.setName("Burger Fans");
        dto2.setDescription("Burger");
        communityService.createCommunity(dto2, testBusiness);

        List<Community> results = communityService.getCommunities("Pizza");
        assertEquals(1, results.size());
        assertEquals("Pizza Lovers", results.get(0).getName());

        List<Community> all = communityService.getCommunities(null);
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("Should upload community image by OWNER")
    void testUploadCommunityImage_Owner() throws IOException {
        CreateCommunityDTO dto = new CreateCommunityDTO();
        dto.setName("Image Community");
        dto.setDescription("Description");
        Community community = communityService.createCommunity(dto, testBusiness);

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        UploadResponse mockResponse = new UploadResponse("key", "url", "image/jpeg", 100L);
        when(imageAdapter.uploadImage(any(), any())).thenReturn(mockResponse);

        UploadResponse response = communityService.uploadCommunityImage(community.getId(), file, testBusiness);
        
        assertNotNull(response);
        Community updated = communityService.getCommunityById(community.getId());
        assertNotNull(updated.getImageUrl());
    }

    @Test
    @DisplayName("Should fail upload community image by non-member")
    void testUploadCommunityImage_NonMember() {
        CreateCommunityDTO dto = new CreateCommunityDTO();
        dto.setName("Image Community 2");
        dto.setDescription("Description");
        Community community = communityService.createCommunity(dto, testBusiness);

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

        assertThrows(IllegalArgumentException.class, () -> 
            communityService.uploadCommunityImage(community.getId(), file, testCustomer));
    }

    @Test
    @DisplayName("Should get communities for user")
    void testGetCommunitiesForUser() {
        // Create multiple communities with different owners
        CreateCommunityDTO dto1 = new CreateCommunityDTO();
        dto1.setName("Business Community 1");
        dto1.setDescription("Description 1");
        dto1.setVisibility(true);
        Community community1 = communityService.createCommunity(dto1, testBusiness);

        CreateCommunityDTO dto2 = new CreateCommunityDTO();
        dto2.setName("Customer Community 1");
        dto2.setDescription("Description 2");
        dto2.setVisibility(true);
        Community community2 = communityService.createCommunity(dto2, testCustomer);

        CreateCommunityDTO dto3 = new CreateCommunityDTO();
        dto3.setName("Business Community 2");
        dto3.setDescription("Description 3");
        dto3.setVisibility(true);
        Community community3 = communityService.createCommunity(dto3, testBusiness);

        // Add testCustomer as member to community1
        Member member = new Member();
        member.setUser(testCustomer);
        member.setCommunity(community1);
        member.setMemberRole(MemberRole.PARTICIPANT);
        memberRepository.save(member);

        // Get communities for testBusiness (should be owner of 2 communities)
        List<Community> businessCommunities = communityService.getCommunitiesForUser(testBusiness);
        assertEquals(2, businessCommunities.size());
        assertTrue(businessCommunities.stream().anyMatch(c -> c.getName().equals("Business Community 1")));
        assertTrue(businessCommunities.stream().anyMatch(c -> c.getName().equals("Business Community 2")));

        // Get communities for testCustomer (should be owner of 1 and member of 1 = 2 total)
        List<Community> customerCommunities = communityService.getCommunitiesForUser(testCustomer);
        assertEquals(2, customerCommunities.size());
        assertTrue(customerCommunities.stream().anyMatch(c -> c.getName().equals("Customer Community 1")));
        assertTrue(customerCommunities.stream().anyMatch(c -> c.getName().equals("Business Community 1")));
    }
}
