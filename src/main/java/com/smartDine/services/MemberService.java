package com.smartDine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.entity.Community;
import com.smartDine.entity.Member;
import com.smartDine.entity.MemberRole;
import com.smartDine.entity.User;
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

    @Transactional
    public void deleteMember(Long memberId, User requestingUser) {
        // 1. Find the member to delete
        Member memberToDelete = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        // 3. OWNER can never be deleted
        if (memberToDelete.getMemberRole() == MemberRole.OWNER) {
            throw new IllegalArgumentException("Cannot delete the community owner");
        }

        // 4. Get the requesting user's membership
        Member requestingMember = memberRepository.findByUserAndCommunity(requestingUser, memberToDelete.getCommunity())
                .orElseThrow(() -> new IllegalArgumentException("You are not a member of this community"));

        // 5. Validate permissions based on role
        MemberRole requestingRole = requestingMember.getMemberRole();
        MemberRole targetRole = memberToDelete.getMemberRole();
        boolean isSelfDelete = requestingMember.getId().equals(memberId);

        // PARTICIPANT: can only delete themselves
        if (requestingRole == MemberRole.PARTICIPANT) {
            if (!isSelfDelete) {
                throw new IllegalArgumentException("Participants can only remove themselves");
            }
        }
        // ADMIN: can delete PARTICIPANTS or themselves
        else if (requestingRole == MemberRole.ADMIN) {
            if (!isSelfDelete && targetRole != MemberRole.PARTICIPANT) {
                throw new IllegalArgumentException("Admins can only remove participants");
            }
        }
        // OWNER: can delete PARTICIPANTS and ADMINS (but not themselves - already
        // protected above)
        else if (requestingRole == MemberRole.OWNER) {
            if (isSelfDelete) {
                throw new IllegalArgumentException("Owner cannot remove themselves");
            }
        }

        memberRepository.delete(memberToDelete);
    }
}
