package com.smartDine.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Community;
import com.smartDine.entity.Member;
import com.smartDine.entity.User;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserAndCommunity(User user, Community community);
    boolean existsByUserAndCommunity(User user, Community community);
    List<Member> findByCommunity(Community community);
    List<Member> findByUser(User user);
}
