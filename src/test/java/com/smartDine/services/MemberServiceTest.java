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
import com.smartDine.repository.MemberRepository;

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

    @Autowired
    private MemberRepository memberRepository;

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
        assertThrows(IllegalArgumentException.class, () -> memberService.joinCommunity(publicCommunity.getId(), owner));
    }

    // ==================== DELETE MEMBER TESTS ====================

    @Test
    @DisplayName("Should allow participant to delete themselves")
    void deleteMember_ParticipantDeletesSelf_Success() {
        // User joins and becomes PARTICIPANT
        Member member = memberService.joinCommunity(publicCommunity.getId(), user);

        // Self-delete
        memberService.deleteMember(member.getId(), user);

        // Verify deleted - should throw when joining again (would fail if still member)
        Member newMember = memberService.joinCommunity(publicCommunity.getId(), user);
        assertNotNull(newMember);
    }

    @Test
    @DisplayName("Should fail when participant tries to delete another member")
    void deleteMember_ParticipantDeletesOther_Fails() {
        // Two users join
        Member member1 = memberService.joinCommunity(publicCommunity.getId(), user);

        Customer user2 = new Customer("User2", "user2@test.com", "password", 333333333L);
        user2 = customerRepository.save(user2);
        Member member2 = memberService.joinCommunity(publicCommunity.getId(), user2);

        // User1 tries to delete user2
        assertThrows(IllegalArgumentException.class,
                () -> memberService.deleteMember(member2.getId(), user));
    }

    @Test
    @DisplayName("Should allow owner to delete participant")
    void deleteMember_OwnerDeletesParticipant_Success() {
        // User joins
        Member member = memberService.joinCommunity(publicCommunity.getId(), user);

        // Owner deletes participant
        memberService.deleteMember(member.getId(), owner);

        // Verify deleted - user can join again
        Member newMember = memberService.joinCommunity(publicCommunity.getId(), user);
        assertNotNull(newMember);
    }

    @Test
    @DisplayName("Should fail when trying to delete the owner")
    void deleteMember_DeleteOwner_Fails() {
        // Find the owner member using repository
        Member ownerMember = memberRepository.findByUserAndCommunity(owner, publicCommunity)
                .orElseThrow();

        // Try to delete owner - should fail
        assertThrows(IllegalArgumentException.class,
                () -> memberService.deleteMember(ownerMember.getId(), owner));
    }

    @Test
    @DisplayName("Should fail when owner tries to delete themselves")
    void deleteMember_OwnerDeletesSelf_Fails() {
        // Find owner's member record using repository
        Member ownerMember = memberRepository.findByUserAndCommunity(owner, publicCommunity)
                .orElseThrow();

        assertThrows(IllegalArgumentException.class,
                () -> memberService.deleteMember(ownerMember.getId(), owner));
    }

    // Try to delete using wrong community ID (test logic changed: now checking if
    // member exists/belongs is handled inside, but since we don't pass communityId,
    // this test case might be redundant or needs different logic.
    // Actually, the original test was "MemberNotInCommunity". Since we don't pass
    // communityId anymore, we can't check if member belongs to *passed*
    // communityId.
    // We can only check if member exists. If member exists, we find its community.
    // The original test `deleteMember(privateCommunity.getId(), member.getId(),
    // owner)` was trying to delete a member of publicCommunity using
    // privateCommunity ID.
    // With the new signature `deleteMember(memberId, user)`, we can't simulate
    // "wrong community ID" error because the method looks up the community FROM the
    // member.
    // So this test case is effectively obsolete or should test something else (like
    // user not being in the SAME community as the member).
    // Let's remove this test case or adapt it. The user said "MemberServiceTest ya
    // lo he actualizado yo" but clearly didn't.
    // I will remove this test case as it's no longer applicable in its current form
    // (mismatching community ID is impossible by design now).
    // Wait, I should probably replace it with a test that verifies the requester is
    // in the SAME community.
    // The next test `deleteMember_RequesterNotMember_Fails` covers "Requester not
    // member".
    // So I will just DELETE this test case.

    @Test
    @DisplayName("Should fail when requesting user is not a member")
    void deleteMember_RequesterNotMember_Fails() {
        // User joins
        Member member = memberService.joinCommunity(publicCommunity.getId(), user);

        // Create non-member user
        Customer nonMember = new Customer("NonMember", "nonmember@test.com", "password", 444444444L);
        nonMember = customerRepository.save(nonMember);

        final Customer finalNonMember = nonMember;
        assertThrows(IllegalArgumentException.class,
                () -> memberService.deleteMember(member.getId(), finalNonMember));
    }
}
