package com.smartDine.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Community;
import com.smartDine.entity.Member;
import com.smartDine.entity.community.CommunityPost;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    Page<CommunityPost> findByAuthor(Member author, Pageable pageable);

    @Query("SELECT p FROM CommunityPost p WHERE p.author = :author AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<CommunityPost> searchByAuthor(Member author, String query, Pageable pageable);

    Page<CommunityPost> findByCommunity(Community community, Pageable pageable);

    @Query("SELECT p FROM CommunityPost p WHERE p.community = :community AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<CommunityPost> searchByCommunity(Community community, String query, Pageable pageable);
}
