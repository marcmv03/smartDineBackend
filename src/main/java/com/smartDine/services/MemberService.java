package com.smartDine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.entity.Community;
import com.smartDine.entity.Member;
import com.smartDine.entity.MemberRole;
import com.smartDine.entity.User;
import com.smartDine.exceptions.NoUserIsMemberException;
import com.smartDine.repository.CommunityRepository;
import com.smartDine.repository.MemberRepository;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Transactional
    public Member joinCommunity(Long communityId, User user) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found with ID: " + communityId));

        // Check visibility
        if (!community.isVisibility()) {
            throw new IllegalArgumentException("Cannot join a private community directly");
        }

        // Check if already a member
        if (memberRepository.existsByUserAndCommunity(user, community)) {
            throw new IllegalArgumentException("User is already a member of this community");
        }

        Member member = new Member();
        member.setUser(user);
        member.setCommunity(community);
        member.setMemberRole(MemberRole.PARTICIPANT);

        return memberRepository.save(member);
    }

    /**
     * Get a member by ID
     * @param id Member ID
     * @return Member entity
     * @throws IllegalArgumentException if member not found
     */
    @Transactional(readOnly = true)
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + id));
    }

    /**
     * Delete a member by ID
     * Only allowed if:
     * - The requesting user is the owner of the community (OWNER role), OR
     * - The member being deleted is the requesting user themselves
     * 
     * @param memberId ID of the member to delete
     * @param requestingUser User making the delete request
     * @throws IllegalArgumentException if member not found
     * @throws NoUserIsMemberException if user lacks permission to delete
     */
    @Transactional
    public void deleteMember(Long memberId, User requestingUser) {
        // Find the member to delete
        Member memberToDelete = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

        // Check if requesting user is the owner of the community
        Member requestingMember = memberRepository.findByUserAndCommunity(
            requestingUser, memberToDelete.getCommunity()
        ).orElse(null);

        boolean isOwner = requestingMember != null && requestingMember.getMemberRole() == MemberRole.OWNER;
        boolean isDeletingSelf = memberToDelete.getUser().getId().equals(requestingUser.getId());

        // Allow deletion if user is owner OR deleting themselves
        if (!isOwner && !isDeletingSelf) {
            throw new NoUserIsMemberException(
                "You do not have permission to delete this member. Only community owners or the member themselves can perform this action."
            );
        }

        memberRepository.delete(memberToDelete);
    }
}
