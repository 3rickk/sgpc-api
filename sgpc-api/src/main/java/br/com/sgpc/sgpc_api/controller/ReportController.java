package br.com.sgpc.sgpc_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sgpc.sgpc_api.dto.CostReportDto;
import br.com.sgpc.sgpc_api.dto.ErrorResponseDto;
import br.com.sgpc.sgpc_api.dto.ProjectReportDto;
import br.com.sgpc.sgpc_api.dto.StockReportDto;
import br.com.sgpc.sgpc_api.service.CsvExportService;
import br.com.sgpc.sgpc_api.service.PdfReportService;
import br.com.sgpc.sgpc_api.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
 * Permissões:
 * - ADMIN: Acesso completo a todos os relatórios
 * - MANAGER: Acesso completo a todos os relatórios
 * - USER: Acesso limitado a relatórios dos projetos que participa
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Relatórios", description = "Endpoints para geração de relatórios gerenciais")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private CsvExportService csvExportService;

    @Autowired
    private PdfReportService pdfReportService;

    /**
     * Gera relatório de projetos.
     * ADMIN e MANAGER podem gerar relatórios de todos os projetos.
     * USER pode gerar relatórios apenas dos projetos que participa.
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
                    @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = ProjectReportDto.class),
                            examples = @ExampleObject(
                                name = "Resposta JSON",
                                summary = "Lista de projetos em JSON",
                                value = "[{\"projectId\":1,\"projectName\":\"Construção Alpha\",\"status\":\"EM_ANDAMENTO\",\"progress\":65.5,\"totalCost\":150000.00,\"estimatedCost\":200000.00}]"
                            )),
                    @Content(mediaType = "text/csv", 
                            schema = @Schema(type = "string", format = "binary"))
                }),
        @ApiResponse(responseCode = "400", description = "Formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Formato inválido\",\"mensagem\":\"Formato 'xml' não é suportado. Use 'csv' ou omita para JSON\",\"path\":\"/api/reports/projects\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/reports/projects\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para gerar relatórios\",\"path\":\"/api/reports/projects\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/reports/projects\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @GetMapping("/projects")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<?> getProjectReport(
            @RequestParam(value = "format", required = false) 
            @Parameter(description = "Formato de saída: 'csv' para CSV, 'pdf' para PDF, omitir para JSON", 
                      example = "pdf") String format) {
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
        
        if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdfData = pdfReportService.generateProjectReportPdf(projectReport, false);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "relatorio_projetos.pdf");
            headers.setContentLength(pdfData.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
        }
        
        return ResponseEntity.ok(projectReport);
    }

    /**
     * Gera relatório de um projeto específico.
     * Usuários podem gerar relatórios apenas dos projetos que participam.
     * 
     * @param projectId ID do projeto
     * @param format formato de saída (opcional): "csv" para CSV, "pdf" para PDF, caso contrário JSON
     * @return ProjectReportDto ou arquivo no formato especificado
     */
    @Operation(
        summary = "Relatório de projeto específico",
        description = "Gera relatório detalhado de um projeto específico incluindo progresso, custos e cronograma"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/projects/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<?> getProjectReportById(
            @PathVariable Long projectId,
            @RequestParam(value = "format", required = false) 
            @Parameter(description = "Formato de saída: 'csv' para CSV, 'pdf' para PDF, omitir para JSON", 
                      example = "pdf") String format) {
        List<ProjectReportDto> projectReport = reportService.getProjectReportById(projectId);
        
        if (projectReport.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        if ("csv".equalsIgnoreCase(format)) {
            byte[] csvData = csvExportService.exportToCsv(projectReport, ProjectReportDto.class);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(csvExportService.getContentType()));
            headers.setContentDispositionFormData("attachment", "relatorio_projeto_" + projectId + ".csv");
            headers.setContentLength(csvData.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
        }
        
        if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdfData = pdfReportService.generateProjectReportPdf(projectReport, true);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "relatorio_projeto_" + projectId + ".pdf");
            headers.setContentLength(pdfData.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
        }
        
        return ResponseEntity.ok(projectReport.get(0));
    }

    /**
     * Gera relatório de custos.
     * ADMIN e MANAGER podem gerar relatórios de custos de todos os projetos.
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
                    @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = CostReportDto.class),
                            examples = @ExampleObject(
                                name = "Resposta JSON",
                                summary = "Lista de custos em JSON",
                                value = "[{\"projectId\":1,\"projectName\":\"Construção Alpha\",\"totalMaterialCost\":50000.00,\"totalLaborCost\":80000.00,\"totalEquipmentCost\":20000.00}]"
                            )),
                    @Content(mediaType = "text/csv", 
                            schema = @Schema(type = "string", format = "binary"))
                }),
        @ApiResponse(responseCode = "400", description = "Formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Formato inválido\",\"mensagem\":\"Formato 'xml' não é suportado. Use 'csv' ou omita para JSON\",\"path\":\"/api/reports/costs\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/reports/costs\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para gerar relatórios de custos\",\"path\":\"/api/reports/costs\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/reports/costs\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @GetMapping("/costs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getCostReport(
            @RequestParam(value = "format", required = false) 
            @Parameter(description = "Formato de saída: 'csv' para CSV, 'pdf' para PDF, omitir para JSON", 
                      example = "pdf") String format) {
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
        
        if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdfData = pdfReportService.generateCostReportPdf(costReport, false);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "relatorio_custos.pdf");
            headers.setContentLength(pdfData.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
        }
        
        return ResponseEntity.ok(costReport);
    }

    /**
     * Gera relatório de custos de um projeto específico.
     * ADMIN e MANAGER podem gerar relatórios de custos de projetos específicos.
     * 
     * @param projectId ID do projeto
     * @param format formato de saída (opcional): "csv" para CSV, "pdf" para PDF, caso contrário JSON
     * @return CostReportDto ou arquivo no formato especificado
     */
    @Operation(
        summary = "Relatório de custos de projeto específico",
        description = "Gera relatório detalhado dos custos de um projeto específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/costs/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getCostReportById(
            @PathVariable Long projectId,
            @RequestParam(value = "format", required = false) 
            @Parameter(description = "Formato de saída: 'csv' para CSV, 'pdf' para PDF, omitir para JSON", 
                      example = "pdf") String format) {
        List<CostReportDto> costReport = reportService.getCostReportById(projectId);
        
        if (costReport.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        if ("csv".equalsIgnoreCase(format)) {
            byte[] csvData = csvExportService.exportToCsv(costReport, CostReportDto.class);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(csvExportService.getContentType()));
            headers.setContentDispositionFormData("attachment", "relatorio_custos_projeto_" + projectId + ".csv");
            headers.setContentLength(csvData.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
        }
        
        if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdfData = pdfReportService.generateCostReportPdf(costReport, true);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "relatorio_custos_projeto_" + projectId + ".pdf");
            headers.setContentLength(pdfData.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
        }
        
        return ResponseEntity.ok(costReport.get(0));
    }

    /**
     * Gera relatório de estoque.
     * ADMIN e MANAGER podem gerar relatórios de estoque completos.
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
                    @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = StockReportDto.class),
                            examples = @ExampleObject(
                                name = "Resposta JSON",
                                summary = "Lista de estoque em JSON",
                                value = "[{\"materialId\":1,\"materialName\":\"Cimento\",\"currentQuantity\":100.0,\"minimumQuantity\":50.0,\"unit\":\"kg\",\"isLowStock\":false}]"
                            )),
                    @Content(mediaType = "text/csv", 
                            schema = @Schema(type = "string", format = "binary"))
                }),
        @ApiResponse(responseCode = "400", description = "Formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":400,\"erro\":\"Formato inválido\",\"mensagem\":\"Formato 'xml' não é suportado. Use 'csv' ou omita para JSON\",\"path\":\"/api/reports/stock\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/reports/stock\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para gerar relatórios de estoque\",\"path\":\"/api/reports/stock\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/reports/stock\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @GetMapping("/stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getStockReport(
            @RequestParam(value = "format", required = false) 
            @Parameter(description = "Formato de saída: 'csv' para CSV, 'pdf' para PDF, omitir para JSON", 
                      example = "pdf") String format) {
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
        
        if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdfData = pdfReportService.generateStockReportPdf(stockReport, false);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "relatorio_estoque.pdf");
            headers.setContentLength(pdfData.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
        }
        
        return ResponseEntity.ok(stockReport);
    }

    /**
     * Gera relatório de um material específico.
     * ADMIN e MANAGER podem gerar relatórios de materiais específicos.
     * 
     * @param materialId ID do material
     * @param format formato de saída (opcional): "csv" para CSV, "pdf" para PDF, caso contrário JSON
     * @return StockReportDto ou arquivo no formato especificado
     */
    @Operation(
        summary = "Relatório de material específico",
        description = "Gera relatório detalhado de um material específico incluindo estoque e movimentações"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Material não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/stock/{materialId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getStockReportById(
            @PathVariable Long materialId,
            @RequestParam(value = "format", required = false) 
            @Parameter(description = "Formato de saída: 'csv' para CSV, 'pdf' para PDF, omitir para JSON", 
                      example = "pdf") String format) {
        List<StockReportDto> stockReport = reportService.getStockReportById(materialId);
        
        if (stockReport.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        if ("csv".equalsIgnoreCase(format)) {
            byte[] csvData = csvExportService.exportToCsv(stockReport, StockReportDto.class);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(csvExportService.getContentType()));
            headers.setContentDispositionFormData("attachment", "relatorio_material_" + materialId + ".csv");
            headers.setContentLength(csvData.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
        }
        
        if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdfData = pdfReportService.generateStockReportPdf(stockReport, true);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "relatorio_material_" + materialId + ".pdf");
            headers.setContentLength(pdfData.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
        }
        
        return ResponseEntity.ok(stockReport.get(0));
    }

    /**
     * Gera resumo consolidado de todos os relatórios.
     * 
     * @return ReportSummaryDto resumo com todos os relatórios
     */
    @Operation(
        summary = "Resumo de relatórios",
        description = "Gera um resumo consolidado contendo todos os tipos de relatórios em um único endpoint"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resumo gerado com sucesso",
                    content = @Content(schema = @Schema(implementation = ReportSummaryDto.class),
                    examples = @ExampleObject(
                        name = "Resumo completo",
                        summary = "Todos os relatórios consolidados",
                        value = "{\"projects\":[{\"projectId\":1,\"projectName\":\"Construção Alpha\"}],\"costs\":[{\"projectId\":1,\"totalCost\":150000.00}],\"stock\":[{\"materialId\":1,\"materialName\":\"Cimento\"}]}"
                    ))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":401,\"erro\":\"Não autorizado\",\"mensagem\":\"Token JWT inválido ou expirado\",\"path\":\"/api/reports/summary\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":403,\"erro\":\"Acesso negado\",\"mensagem\":\"Usuário não possui permissão para gerar resumo de relatórios\",\"path\":\"/api/reports/summary\",\"timestamp\":\"2024-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                    examples = @ExampleObject(value = "{\"status\":500,\"erro\":\"Erro interno\",\"mensagem\":\"Erro interno do servidor\",\"path\":\"/api/reports/summary\",\"timestamp\":\"2024-01-15T10:30:00\"}")))
    })
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ReportSummaryDto> getReportSummary() {
        List<ProjectReportDto> projects = reportService.getProjectReport();
        List<CostReportDto> costs = reportService.getCostReport();
        List<StockReportDto> stock = reportService.getStockReport();
        
        ReportSummaryDto summary = new ReportSummaryDto(projects, costs, stock);
        return ResponseEntity.ok(summary);
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