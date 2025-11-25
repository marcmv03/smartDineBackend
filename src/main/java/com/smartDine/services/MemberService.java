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
}
