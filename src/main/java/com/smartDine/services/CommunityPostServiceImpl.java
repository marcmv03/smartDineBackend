package com.smartDine.services;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartDine.dto.community.post.CreateCommunityPostRequestDTO;
import com.smartDine.dto.community.post.UpdateCommunityPostRequestDTO;
import com.smartDine.entity.Community;
import com.smartDine.entity.Member;
import com.smartDine.entity.MemberRole;
import com.smartDine.entity.User;
import com.smartDine.entity.community.CommunityPost;
import com.smartDine.exceptions.NoUserIsMemberException;
import com.smartDine.repository.CommunityMemberRepository;
import com.smartDine.repository.CommunityPostRepository;
import com.smartDine.repository.CommunityRepository;
import com.smartDine.repository.UserRepository;

@Service
public class CommunityPostServiceImpl implements CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    public CommunityPostServiceImpl(CommunityPostRepository communityPostRepository,
            CommunityMemberRepository communityMemberRepository,
            CommunityRepository communityRepository,
            UserRepository userRepository) {
        this.communityPostRepository = communityPostRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CommunityPost createPost(Long currentUserId, CreateCommunityPostRequestDTO requestDTO) {
        Community community = getCommunity(requestDTO.getCommunityId());
        User user = getUser(currentUserId);
        Member member = getMemberForCommunity(user, community);

        if (!isAdminOrOwner(member)) {
            throw new BadCredentialsException("Only administrators or owners can create posts in this community");
        }

        CommunityPost post = CreateCommunityPostRequestDTO.toEntity(requestDTO, community, member);
        return communityPostRepository.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityPost getPostById(Long postId, Long currentUserId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        validateReadAccess(post.getCommunity(), currentUserId);
        return post;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommunityPost> getPostsByMember(Long memberId, String search, Pageable pageable,
            Long currentUserId) {
        Member author = communityMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

        validateReadAccess(author.getCommunity(), currentUserId);

        if (search != null && !search.isBlank()) {
            return communityPostRepository.searchByAuthor(author, search, pageable);
        } else {
            return communityPostRepository.findByAuthor(author, pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommunityPost> getPostsByCommunity(Long communityId, String search, Pageable pageable,
            Long currentUserId) {
        Community community = getCommunity(communityId);
        validateReadAccess(community, currentUserId);

        if (search != null && !search.isBlank()) {
            return communityPostRepository.searchByCommunity(community, search, pageable);
        } else {
            return communityPostRepository.findByCommunity(community, pageable);
        }
    }

    @Override
    @Transactional
    public CommunityPost updatePost(Long postId, Long currentUserId,
            UpdateCommunityPostRequestDTO requestDTO) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        Member actor = getMemberForCommunity(getUser(currentUserId), post.getCommunity());
        if (!isAdminOrOwner(actor) && !Objects.equals(actor.getUser().getId(), post.getAuthor().getUser().getId())) {
            throw new BadCredentialsException("Only post author, administrators or owners can update this post");
        }

        if (requestDTO.getTitle() != null) {
            post.setTitle(requestDTO.getTitle());
        }
        if (requestDTO.getDescription() != null) {
            post.setDescription(requestDTO.getDescription());
        }

        return communityPostRepository.save(post);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long currentUserId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        Member actor = getMemberForCommunity(getUser(currentUserId), post.getCommunity());
        if (!isAdminOrOwner(actor) && !Objects.equals(actor.getUser().getId(), post.getAuthor().getUser().getId())) {
            throw new BadCredentialsException("Only post author, administrators or owners can delete this post");
        }

        communityPostRepository.delete(post);
    }

    private void validateReadAccess(Community community, Long currentUserId) {
        if (community.isVisibility()) {
            return;
        }
        if (currentUserId == null) {
            throw new IllegalArgumentException("User must be authenticated to access this community");
        }
        getMemberForCommunity(getUser(currentUserId), community);
    }

    private Member getMemberForCommunity(User user, Community community) {
        return communityMemberRepository.findByUserAndCommunity(user, community)
                .orElseThrow(() -> new NoUserIsMemberException("User is not a member of this community"));
    }

    private Community getCommunity(Long communityId) {
        return communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Community not found with id: " + communityId));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }

    private boolean isAdminOrOwner(Member member) {
        return member.getMemberRole() == MemberRole.ADMIN || member.getMemberRole() == MemberRole.OWNER;
    }
}
