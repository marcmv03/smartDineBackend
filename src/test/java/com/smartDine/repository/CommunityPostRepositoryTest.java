package com.smartDine.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import com.smartDine.entity.Community;
import com.smartDine.entity.CommunityType;
import com.smartDine.entity.Customer;
import com.smartDine.entity.Member;
import com.smartDine.entity.MemberRole;
import com.smartDine.entity.community.CommunityPost;

@DataJpaTest
public class CommunityPostRepositoryTest {

    @Autowired
    private CommunityPostRepository communityPostRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    private Member member;
    private Community community;

    @BeforeEach
    void setUp() {
        Customer user = new Customer();
        user.setName("Tester");
        user.setEmail("tester@example.com");
        user.setPassword("password");
        user.setPhoneNumber(123456789L);
        userRepository.save(user);

        community = new Community();
        community.setName("Test Community");
        community.setDescription("A community for testing");
        community.setVisibility(true);
        community.setCommunityType(CommunityType.USER);
        communityRepository.save(community);

        member = new Member();
        member.setUser(user);
        member.setCommunity(community);
        member.setMemberRole(MemberRole.ADMIN);
        memberRepository.save(member);

        CommunityPost post1 = new CommunityPost();
        post1.setAuthor(member);
        post1.setCommunity(community);
        post1.setTitle("First title");
        post1.setDescription("A description about spring data");
        communityPostRepository.save(post1);

        CommunityPost post2 = new CommunityPost();
        post2.setAuthor(member);
        post2.setCommunity(community);
        post2.setTitle("Second");
        post2.setDescription("Other topic");
        communityPostRepository.save(post2);
    }

    @Test
    void searchByCommunityShouldFilterByText() {
        var results = communityPostRepository.searchByCommunity(community, "spring", PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("First title", results.getContent().get(0).getTitle());
    }

    @Test
    void findByAuthorReturnsAllPosts() {
        var results = communityPostRepository.findByAuthor(member, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
    }
}
