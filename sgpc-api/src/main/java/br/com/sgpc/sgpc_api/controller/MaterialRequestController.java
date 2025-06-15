package br.com.sgpc.sgpc_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sgpc.sgpc_api.dto.MaterialRequestApprovalDto;
import br.com.sgpc.sgpc_api.dto.MaterialRequestCreateDto;
import br.com.sgpc.sgpc_api.dto.MaterialRequestDetailsDto;
import br.com.sgpc.sgpc_api.dto.MaterialRequestSummaryDto;
import br.com.sgpc.sgpc_api.enums.RequestStatus;
import br.com.sgpc.sgpc_api.service.MaterialRequestService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/material-requests")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MaterialRequestController {

    @Autowired
    private MaterialRequestService materialRequestService;

    @PostMapping
    public ResponseEntity<MaterialRequestDetailsDto> createMaterialRequest(
            @Valid @RequestBody MaterialRequestCreateDto requestDto,
            @RequestParam Long requesterId) {
        try {
            MaterialRequestDetailsDto createdRequest = materialRequestService.createMaterialRequest(requestDto, requesterId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<MaterialRequestSummaryDto>> getAllMaterialRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long projectId) {
        try {
            List<MaterialRequestSummaryDto> requests;
            
            if (status != null) {
                RequestStatus requestStatus = RequestStatus.fromString(status);
                requests = materialRequestService.getMaterialRequestsByStatus(requestStatus);
            } else if (projectId != null) {
                requests = materialRequestService.getMaterialRequestsByProject(projectId);
            } else {
                requests = materialRequestService.getAllMaterialRequests();
            }
            
            return ResponseEntity.ok(requests);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialRequestDetailsDto> getMaterialRequestById(@PathVariable Long id) {
        return materialRequestService.getMaterialRequestById(id)
                .map(request -> ResponseEntity.ok(request))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<MaterialRequestDetailsDto> approveMaterialRequest(
            @PathVariable Long id,
            @RequestParam Long approverId) {
        try {
            MaterialRequestDetailsDto approvedRequest = materialRequestService.approveMaterialRequest(id, approverId);
            return ResponseEntity.ok(approvedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<MaterialRequestDetailsDto> rejectMaterialRequest(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @Valid @RequestBody MaterialRequestApprovalDto approvalDto) {
        try {
            MaterialRequestDetailsDto rejectedRequest = materialRequestService.rejectMaterialRequest(id, approverId, approvalDto);
            return ResponseEntity.ok(rejectedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<MaterialRequestSummaryDto>> getPendingMaterialRequests() {
        List<MaterialRequestSummaryDto> pendingRequests = 
                materialRequestService.getMaterialRequestsByStatus(RequestStatus.PENDENTE);
        return ResponseEntity.ok(pendingRequests);
    }

    @GetMapping("/approved")
    public ResponseEntity<List<MaterialRequestSummaryDto>> getApprovedMaterialRequests() {
        List<MaterialRequestSummaryDto> approvedRequests = 
                materialRequestService.getMaterialRequestsByStatus(RequestStatus.APROVADA);
        return ResponseEntity.ok(approvedRequests);
    }

    @GetMapping("/rejected")
    public ResponseEntity<List<MaterialRequestSummaryDto>> getRejectedMaterialRequests() {
        List<MaterialRequestSummaryDto> rejectedRequests = 
                materialRequestService.getMaterialRequestsByStatus(RequestStatus.REJEITADA);
        return ResponseEntity.ok(rejectedRequests);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<MaterialRequestSummaryDto>> getMaterialRequestsByProject(@PathVariable Long projectId) {
        List<MaterialRequestSummaryDto> projectRequests = 
                materialRequestService.getMaterialRequestsByProject(projectId);
        return ResponseEntity.ok(projectRequests);
    }
} 