package com.smartDine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.MemberDTO;
import com.smartDine.entity.Member;
import com.smartDine.entity.User;
import com.smartDine.services.MemberService;


/**
 *
 * @author Marc Martinez
 */
@RestController 
@RequestMapping("/smartdine/api/members")
public class MembersController {
    
    private final MemberService memberService;

    public MembersController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * Get a member by ID
     * @param id Member ID
     * @return MemberDTO with member information
     */
    @GetMapping("/{id}")
    public ResponseEntity<MemberDTO> getMember(@PathVariable Long id) {
        Member member = memberService.getMemberById(id);
        return ResponseEntity.ok(MemberDTO.fromEntity(member));
    }

    /**
     * Delete a member by ID
     * Only allowed if user is the community owner or deleting themselves
     * @param id Member ID to delete
     * @param user Authenticated user making the request
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        memberService.deleteMember(id, user);
        return ResponseEntity.noContent().build();
    }
}
