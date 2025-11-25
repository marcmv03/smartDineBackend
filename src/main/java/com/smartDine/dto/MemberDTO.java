package com.smartDine.dto;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.smartDine.entity.Member;
import com.smartDine.entity.MemberRole;

public class MemberDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long communityId;
    private String memberRole;
    private String joinedAt;

    public MemberDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Long getCommunityId() { return communityId; }
    public void setCommunityId(Long communityId) { this.communityId = communityId; }

    public String getMemberRole() { return memberRole; }
    public void setMemberRole(String memberRole) { this.memberRole = memberRole; }

    public String getJoinedAt() { return joinedAt; }
    public void setJoinedAt(String joinedAt) { this.joinedAt = joinedAt; }

    // Conversion methods
    public static Member toEntity(MemberDTO dto) {
        Member member = new Member();
        if (dto.getId() != null) {
            member.setId(dto.getId());
        }
        if (dto.getMemberRole() != null) {
            member.setMemberRole(MemberRole.valueOf(dto.getMemberRole()));
        }
        // Note: User and Community references are usually set by the service, not directly from DTO ID in toEntity
        // unless we fetch them. For simplicity in DTO->Entity, we often just set scalar fields or rely on service.
        return member;
    }

    public static MemberDTO fromEntity(Member member) {
        MemberDTO dto = new MemberDTO();
        dto.setId(member.getId());
        if (member.getUser() != null) {
            dto.setUserId(member.getUser().getId());
            dto.setUserName(member.getUser().getName());
        }
        if (member.getCommunity() != null) {
            dto.setCommunityId(member.getCommunity().getId());
        }
        if (member.getMemberRole() != null) {
            dto.setMemberRole(member.getMemberRole().name());
        }
        if (member.getJoinedAt() != null) {
            dto.setJoinedAt(member.getJoinedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        return dto;
    }

    public static List<MemberDTO> fromEntity(List<Member> members) {
        return members.stream()
            .map(MemberDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
