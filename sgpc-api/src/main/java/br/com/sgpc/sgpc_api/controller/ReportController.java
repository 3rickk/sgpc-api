package br.com.sgpc.sgpc_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sgpc.sgpc_api.dto.CostReportDto;
import br.com.sgpc.sgpc_api.dto.ProjectReportDto;
import br.com.sgpc.sgpc_api.dto.StockReportDto;
import br.com.sgpc.sgpc_api.service.CsvExportService;
import br.com.sgpc.sgpc_api.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller responsável pela geração de relatórios do sistema.
 * 
 * Este controller fornece endpoints para geração de relatórios
 * de projetos, custos e estoque, com opção de exportação em
 * formato JSON ou CSV.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Relatórios", description = "Endpoints para geração de relatórios gerenciais")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private CsvExportService csvExportService;

    /**
     * Gera relatório de projetos.
     * 
     * @param format formato de saída (opcional): "csv" para CSV, caso contrário JSON
     * @return List<ProjectReportDto> ou arquivo CSV com dados dos projetos
     */
    @Operation(
        summary = "Relatório de projetos",
        description = "Gera relatório completo dos projetos incluindo progresso, custos e cronograma"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectReportDto.class)),
                    @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))
                }),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/projects")
    public ResponseEntity<?> getProjectReport(
            @RequestParam(value = "format", required = false) 
            @Parameter(description = "Formato de saída: 'csv' para CSV, omitir para JSON", 
                      example = "csv") String format) {
        try {
            List<ProjectReportDto> projectReport = reportService.getProjectReport();
            
            if ("csv".equalsIgnoreCase(format)) {
                byte[] csvData = csvExportService.exportToCsv(projectReport, ProjectReportDto.class);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(csvExportService.getContentType()));
                headers.setContentDispositionFormData("attachment", "relatorio_projetos.csv");
                headers.setContentLength(csvData.length);
                
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
            }
            
            return ResponseEntity.ok(projectReport);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório de projetos: " + e.getMessage());
        }
    }

    /**
     * Gera relatório de custos.
     * 
     * @param format formato de saída (opcional): "csv" para CSV, caso contrário JSON
     * @return List<CostReportDto> ou arquivo CSV com dados de custos
     */
    @Operation(
        summary = "Relatório de custos",
        description = "Gera relatório detalhado dos custos por projeto e tarefa"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CostReportDto.class)),
                    @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))
                }),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/costs")
    public ResponseEntity<?> getCostReport(
            @RequestParam(value = "format", required = false) 
            @Parameter(description = "Formato de saída: 'csv' para CSV, omitir para JSON", 
                      example = "csv") String format) {
        try {
            List<CostReportDto> costReport = reportService.getCostReport();
            
            if ("csv".equalsIgnoreCase(format)) {
                byte[] csvData = csvExportService.exportToCsv(costReport, CostReportDto.class);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(csvExportService.getContentType()));
                headers.setContentDispositionFormData("attachment", "relatorio_custos.csv");
                headers.setContentLength(csvData.length);
                
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
            }
            
            return ResponseEntity.ok(costReport);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório de custos: " + e.getMessage());
        }
    }

    /**
     * Gera relatório de estoque.
     * 
     * @param format formato de saída (opcional): "csv" para CSV, caso contrário JSON
     * @return List<StockReportDto> ou arquivo CSV com dados de estoque
     */
    @Operation(
        summary = "Relatório de estoque",
        description = "Gera relatório do estoque de materiais incluindo quantidades e alertas de baixo estoque"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso",
                content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StockReportDto.class)),
                    @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))
                }),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/stock")
    public ResponseEntity<?> getStockReport(
            @RequestParam(value = "format", required = false) 
            @Parameter(description = "Formato de saída: 'csv' para CSV, omitir para JSON", 
                      example = "csv") String format) {
        try {
            List<StockReportDto> stockReport = reportService.getStockReport();
            
            if ("csv".equalsIgnoreCase(format)) {
                byte[] csvData = csvExportService.exportToCsv(stockReport, StockReportDto.class);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(csvExportService.getContentType()));
                headers.setContentDispositionFormData("attachment", "relatorio_estoque.csv");
                headers.setContentLength(csvData.length);
                
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
            }
            
            return ResponseEntity.ok(stockReport);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório de estoque: " + e.getMessage());
        }
    }

    /**
     * Gera resumo consolidado de todos os relatórios.
     * 
     * @return ReportSummaryDto resumo com todos os relatórios
     */
    @Operation(
        summary = "Resumo de relatórios",
        description = "Gera um resumo consolidado contendo dados de projetos, custos e estoque"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resumo gerado com sucesso",
                content = @Content(schema = @Schema(implementation = ReportSummaryDto.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryDto> getReportSummary() {
        try {
            List<ProjectReportDto> projectReport = reportService.getProjectReport();
            List<CostReportDto> costReport = reportService.getCostReport();
            List<StockReportDto> stockReport = reportService.getStockReport();
            
            ReportSummaryDto summary = new ReportSummaryDto(projectReport, costReport, stockReport);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar resumo de relatórios: " + e.getMessage());
        }
    }
    
    /**
     * DTO para resumo consolidado de relatórios.
     * 
     * Classe interna que encapsula todos os tipos de relatórios
     * disponíveis no sistema para facilitar a visualização
     * consolidada dos dados.
     */
    @Schema(description = "Resumo consolidado de todos os relatórios")
    public static class ReportSummaryDto {
        
        @Schema(description = "Relatório de projetos")
        private final List<ProjectReportDto> projects;
        
        @Schema(description = "Relatório de custos")
        private final List<CostReportDto> costs;
        
        @Schema(description = "Relatório de estoque")
        private final List<StockReportDto> stock;
        
        /**
         * Construtor do resumo de relatórios.
         * 
         * @param projects lista de relatórios de projetos
         * @param costs lista de relatórios de custos
         * @param stock lista de relatórios de estoque
         */
        public ReportSummaryDto(List<ProjectReportDto> projects, List<CostReportDto> costs, List<StockReportDto> stock) {
            this.projects = projects;
            this.costs = costs;
            this.stock = stock;
        }
        
        /**
         * Obtém relatórios de projetos.
         * 
         * @return List<ProjectReportDto> relatórios de projetos
         */
        public List<ProjectReportDto> getProjects() { return projects; }
        
        /**
         * Obtém relatórios de custos.
         * 
         * @return List<CostReportDto> relatórios de custos
         */
        public List<CostReportDto> getCosts() { return costs; }
        
        /**
         * Obtém relatórios de estoque.
         * 
         * @return List<StockReportDto> relatórios de estoque
         */
        public List<StockReportDto> getStock() { return stock; }
    }
} 