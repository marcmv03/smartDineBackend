package com.smartDine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Community;
import com.smartDine.entity.Member;
import com.smartDine.entity.community.CommunityPost;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    List<CommunityPost> findByAuthor(Member author);

    @Query("SELECT p FROM CommunityPost p WHERE p.author = :author AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<CommunityPost> searchByAuthor(Member author, String query);

    List<CommunityPost> findByCommunity(Community community);

    @Query("SELECT p FROM CommunityPost p WHERE p.community = :community AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<CommunityPost> searchByCommunity(Community community, String query);
}
