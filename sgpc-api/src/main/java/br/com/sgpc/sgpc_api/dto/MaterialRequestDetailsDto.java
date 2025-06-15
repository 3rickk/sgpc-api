package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.com.sgpc.sgpc_api.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequestDetailsDto {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long requesterId;
    private String requesterName;
    private LocalDate requestDate;
    private LocalDate neededDate;
    private RequestStatus status;
    private String statusDescription;
    private String rejectionReason;
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String observations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MaterialRequestItemDto> items;
    private BigDecimal totalAmount;
} 