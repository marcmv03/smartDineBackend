package com.smartDine.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.smartDine.adapters.ImageAdapter;
import com.smartDine.dto.CreateCommunityDTO;
import com.smartDine.dto.UploadResponse;
import com.smartDine.entity.Business;
import com.smartDine.entity.Community;
import com.smartDine.entity.CommunityType;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Member;
import com.smartDine.entity.MemberRole;
import com.smartDine.entity.User;
import com.smartDine.repository.CommunityRepository;
import com.smartDine.repository.MemberRepository;

@Service
public class CommunityService {

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ImageAdapter imageAdapter;

    @Transactional(readOnly = true)
    public List<Community> getCommunities(String search) {
        if (search != null && !search.trim().isEmpty()) {
            return communityRepository.findByNameContainingIgnoreCase(search.trim());
        }
        return communityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Community getCommunityById(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Community not found with ID: " + id));
    }

    @Transactional
    public Community createCommunity(CreateCommunityDTO createDTO, User user) {
        // Check if name exists
        if (communityRepository.existsByNameIgnoreCase(createDTO.getName())) {
            throw new IllegalArgumentException("A community with this name already exists");
        }

        Community community = new Community();
        community.setName(createDTO.getName());
        community.setDescription(createDTO.getDescription());
        community.setVisibility(createDTO.isVisibility());
        
        // Determine community type
        if (user instanceof Business) {
            community.setCommunityType(CommunityType.RESTAURANT);
        } else if (user instanceof Customer) {
            community.setCommunityType(CommunityType.USER);
        } else {
            // Default or handle other roles
            community.setCommunityType(CommunityType.USER);
        }

        Community savedCommunity = communityRepository.save(community);

        // Add creator as OWNER
        Member member = new Member();
        member.setUser(user);
        member.setCommunity(savedCommunity);
        member.setMemberRole(MemberRole.OWNER);
        memberRepository.save(member);
        
        // Add member to the list so it's available in the returned object
        if (savedCommunity.getMembers() == null) {
            savedCommunity.setMembers(new java.util.ArrayList<>());
        }
        savedCommunity.getMembers().add(member);
        
        return savedCommunity;
    }

    @Transactional
    public UploadResponse uploadCommunityImage(Long communityId, MultipartFile file, User user) throws IOException {
        Community community = getCommunityById(communityId);
        
        // Check if user is OWNER or ADMIN
        Member member = memberRepository.findByUserAndCommunity(user, community)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this community"));
        
        if (member.getMemberRole() != MemberRole.OWNER && member.getMemberRole() != MemberRole.ADMIN) {
            throw new IllegalArgumentException("Only OWNER or ADMIN can upload community image");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String ext = Optional.ofNullable(file.getOriginalFilename())
                .filter(n -> n.contains("."))
                .map(n -> n.substring(n.lastIndexOf('.') + 1))
                .orElse("jpg");

        String keyName = "communities/%d/images/%s.%s"
                .formatted(communityId, UUID.randomUUID(), ext);

        UploadResponse response = imageAdapter.uploadImage(file, keyName);

        community.setImageUrl(keyName);
        communityRepository.save(community);

        return response;
    }
    @Transactional(readOnly = true)
    public List<Community> getCommunitiesForUser(User user) {
        List<Member> memberships = memberRepository.findByUser(user);
        return memberships.stream()
                .map(Member::getCommunity)
                .toList();
    }
}
