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

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private CsvExportService csvExportService;

    @GetMapping("/projects")
    public ResponseEntity<?> getProjectReport(@RequestParam(value = "format", required = false) String format) {
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

    @GetMapping("/costs")
    public ResponseEntity<?> getCostReport(@RequestParam(value = "format", required = false) String format) {
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

    @GetMapping("/stock")
    public ResponseEntity<?> getStockReport(@RequestParam(value = "format", required = false) String format) {
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
    
    public static class ReportSummaryDto {
        private final List<ProjectReportDto> projects;
        private final List<CostReportDto> costs;
        private final List<StockReportDto> stock;
        
        public ReportSummaryDto(List<ProjectReportDto> projects, List<CostReportDto> costs, List<StockReportDto> stock) {
            this.projects = projects;
            this.costs = costs;
            this.stock = stock;
        }
        
        public List<ProjectReportDto> getProjects() { return projects; }
        public List<CostReportDto> getCosts() { return costs; }
        public List<StockReportDto> getStock() { return stock; }
    }
} 