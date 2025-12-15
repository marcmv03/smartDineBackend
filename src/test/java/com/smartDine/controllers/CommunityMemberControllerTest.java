package com.smartDine.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.dto.CreateCommunityDTO;
import com.smartDine.entity.Business;
import com.smartDine.entity.Community;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Member;
import com.smartDine.repository.BusinessRepository;
import com.smartDine.repository.CommunityRepository;
import com.smartDine.repository.CustomerRepository;
import com.smartDine.repository.MemberRepository;
import com.smartDine.services.CommunityService;
import com.smartDine.services.JwtService;
import com.smartDine.services.MemberService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CommunityMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberService memberService;

    @Autowired
    private CommunityService communityService;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtService jwtService;

    private Business owner;
    private Customer user;
    private Community community;
    private Member member;
    private String userToken;
    private String ownerToken;

    @BeforeEach
    void setUp() {
        // Create owner
        owner = new Business("Owner", "owner@test.com", "password", 111111111L);
        owner = businessRepository.save(owner);
        ownerToken = "Bearer " + jwtService.generateToken(owner);

        // Create user
        user = new Customer("User", "user@test.com", "password", 222222222L);
        user = customerRepository.save(user);
        userToken = "Bearer " + jwtService.generateToken(user);

        // Create community
        CreateCommunityDTO dto = new CreateCommunityDTO();
        dto.setName("Test Community");
        dto.setDescription("Description");
        dto.setVisibility(true);
        community = communityService.createCommunity(dto, owner);

        // Join user to community
        member = memberService.joinCommunity(community.getId(), user);
    }

    @Test
    @DisplayName("Should delete member successfully when authorized")
    void deleteMember_Success() throws Exception {
        mockMvc.perform(delete("/smartdine/api/members/" + member.getId())
                .header("Authorization", userToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 400 when member not found")
    void deleteMember_NotFound() throws Exception {
        mockMvc.perform(delete("/smartdine/api/members/999999")
                .header("Authorization", userToken))
                .andExpect(status().isBadRequest());
    }
}
