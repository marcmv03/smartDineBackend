package com.smartDine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.entity.User;
import com.smartDine.services.MemberService;

@RestController
@RequestMapping("/smartdine/api/members")
public class CommunityMemberController {

    private final MemberService memberService;

    public CommunityMemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(

            @PathVariable Long memberId,
            @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        memberService.deleteMember(memberId, user);
        return ResponseEntity.noContent().build();
    }
}
