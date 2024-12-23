package com.project.collaboration.event.controller;

import com.project.collaboration.event.dto.CollaborationDto;
import com.project.collaboration.event.dto.CollaborationRequestDto;
import com.project.collaboration.event.service.CollaborationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/collaboration")
@RequiredArgsConstructor
public class CollaborationController {

    private final CollaborationService collaborationService;

    @PostMapping
    public ResponseEntity<?> registerCollaborationProduct(@RequestBody CollaborationRequestDto requestDto) {
        CollaborationDto collaborationDto = collaborationService.saveCollaborationProduct(requestDto);
        return ResponseEntity.ok(collaborationDto);
    }
}
