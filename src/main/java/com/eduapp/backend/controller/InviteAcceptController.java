package com.eduapp.backend.controller;

import com.eduapp.backend.dto.AcceptInviteRequest;
import com.eduapp.backend.dto.UserResponse;
import com.eduapp.backend.model.User;
import com.eduapp.backend.service.SchoolInviteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public invite acceptance. Unauthenticated by design — the single-use token is the
 * authorization. This is the only path that creates a TEACHER (public registration
 * always yields STUDENT). Must be permitted in SecurityConfig.
 */
@RestController
@RequestMapping("/api/invites")
public class InviteAcceptController {

    private final SchoolInviteService inviteService;

    public InviteAcceptController(SchoolInviteService inviteService) {
        this.inviteService = inviteService;
    }

    @PostMapping("/accept")
    public ResponseEntity<UserResponse> accept(@RequestBody AcceptInviteRequest req) {
        User user = inviteService.acceptInvite(req.getToken(), req.getName(), req.getPassword());
        return ResponseEntity.ok(
                new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole()));
    }
}
