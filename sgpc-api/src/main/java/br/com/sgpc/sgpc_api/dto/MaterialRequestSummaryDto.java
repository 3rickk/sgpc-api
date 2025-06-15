package br.com.sgpc.sgpc_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.sgpc.sgpc_api.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequestSummaryDto {
    private Long id;
    private String projectName;
    private String requesterName;
    private LocalDate requestDate;
    private LocalDate neededDate;
    private RequestStatus status;
    private String statusDescription;
    private Integer itemCount;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
} 