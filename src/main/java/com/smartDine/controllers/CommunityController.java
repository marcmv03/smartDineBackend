package com.smartDine.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smartDine.dto.CommunityDTO;
import com.smartDine.dto.CreateCommunityDTO;
import com.smartDine.dto.MemberDTO;
import com.smartDine.dto.UploadResponse;
import com.smartDine.entity.Community;
import com.smartDine.entity.Member;
import com.smartDine.entity.User;
import com.smartDine.services.CommunityService;
import com.smartDine.services.MemberService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/smartdine/api/communities")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private MemberService memberService;

    @GetMapping
    public ResponseEntity<List<CommunityDTO>> getCommunities(@RequestParam(required = false) String search) {
        List<Community> communities = communityService.getCommunities(search);
        return ResponseEntity.ok(CommunityDTO.fromEntity(communities));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityDTO> getCommunityById(@PathVariable Long id) {
        Community community = communityService.getCommunityById(id);
        return ResponseEntity.ok(CommunityDTO.fromEntity(community));
    }

    @PostMapping
    public ResponseEntity<CommunityDTO> createCommunity(
            @Valid @RequestBody CreateCommunityDTO createDTO,
            @AuthenticationPrincipal User user) {
        Community community = communityService.createCommunity(createDTO, user);
        return ResponseEntity.ok(CommunityDTO.fromEntity(community));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<MemberDTO> joinCommunity(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        Member member = memberService.joinCommunity(id, user);
        return ResponseEntity.ok(MemberDTO.fromEntity(member));
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<UploadResponse> uploadCommunityImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) throws IOException {
        UploadResponse response = communityService.uploadCommunityImage(id, file, user);
        return ResponseEntity.ok(response);
    }
}
