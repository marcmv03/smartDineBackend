package com.smartDine.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    private Community publicCommunity;
    private Community privateCommunity;

    @BeforeEach
    void setUp() {
        owner = new Business("Owner", "owner@test.com", "password", 111111111L);
        owner = businessRepository.save(owner);

        user = new Customer("User", "user@test.com", "password", 222222222L);
        user = customerRepository.save(user);

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
        assertThrows(IllegalArgumentException.class, () -> 
            memberService.joinCommunity(privateCommunity.getId(), user));
    }

    @Test
    @DisplayName("Should fail to join if already a member")
    void testJoinDuplicate() {
        memberService.joinCommunity(publicCommunity.getId(), user);

        assertThrows(IllegalArgumentException.class, () -> 
            memberService.joinCommunity(publicCommunity.getId(), user));
    }

    @Test
    @DisplayName("Should fail to join if owner tries to join again")
    void testOwnerJoinAgain() {
        assertThrows(IllegalArgumentException.class, () -> 
            memberService.joinCommunity(publicCommunity.getId(), owner));
    }
}
