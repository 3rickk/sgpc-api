package br.com.sgpc.sgpc_api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.dto.CostReportDto;
import br.com.sgpc.sgpc_api.dto.ProjectReportDto;
import br.com.sgpc.sgpc_api.dto.StockReportDto;
import br.com.sgpc.sgpc_api.entity.Material;
import br.com.sgpc.sgpc_api.entity.Project;
import br.com.sgpc.sgpc_api.entity.Task;
import br.com.sgpc.sgpc_api.enums.TaskStatus;
import br.com.sgpc.sgpc_api.repository.MaterialRepository;
import br.com.sgpc.sgpc_api.repository.ProjectRepository;
import br.com.sgpc.sgpc_api.repository.TaskRepository;

/**
 * Service responsável pela geração de relatórios do sistema.
 * 
 * Este service oferece diferentes tipos de relatórios:
 * - Relatório de Projetos: progresso, datas, orçamento
 * - Relatório de Custos: análise financeira por projeto
 * - Relatório de Estoque: situação atual dos materiais
 * 
 * Todos os cálculos incluem métricas como:
 * - Percentuais de progresso e utilização
 * - Indicadores de atraso e baixo estoque
 * - Comparações entre planejado vs realizado
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MaterialRepository materialRepository;

    /**
     * Gera relatório consolidado de todos os projetos.
     * 
     * O relatório inclui para cada projeto:
     * - Informações básicas (nome, cliente, datas)
     * - Status e progresso calculado
     * - Contagem de tarefas (total vs concluídas)
     * - Indicadores de atraso
     * - Dias restantes para conclusão
     * - Dados orçamentários
     * 
     * @return Lista de ProjectReportDto com dados consolidados
     */
    public List<ProjectReportDto> getProjectReport() {
        List<Project> projects = projectRepository.findAll();
        
        return projects.stream().map(project -> {
            List<Task> projectTasks = taskRepository.findByProjectId(project.getId());
            Integer totalTasks = projectTasks.size();
            Integer completedTasks = (int) projectTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.CONCLUIDA)
                .count();
            
            Integer progressPercentage = calculateProgressPercentage(project, totalTasks, completedTasks);
            Boolean delayed = isProjectDelayed(project);
            Long daysRemaining = calculateDaysRemaining(project);
            
            return new ProjectReportDto(
                project.getId(),
                project.getName(),
                project.getClient(),
                project.getStartDatePlanned(),
                project.getEndDatePlanned(),
                project.getStartDateActual(),
                project.getEndDateActual(),
                project.getStatus().getDescription(),
                progressPercentage,
                project.getTotalBudget(),
                project.getRealizedCost(),
                totalTasks,
                completedTasks,
                delayed,
                daysRemaining
            );
        }).collect(Collectors.toList());
    }

    /**
     * Gera relatório de custos por projeto.
     * 
     * Análise financeira incluindo:
     * - Orçamento total vs gastos realizados
     * - Separação por categoria (material/serviço aproximada)
     * - Percentual de utilização do orçamento
     * - Valor restante disponível
     * - Indicador de estouro orçamentário
     * 
     * NOTA: A separação material/serviço usa aproximação percentual
     * até implementação de categorização detalhada de custos.
     * 
     * @return Lista de CostReportDto com análise financeira
     */
    public List<CostReportDto> getCostReport() {
        List<Project> projects = projectRepository.findAll();
        
        return projects.stream().map(project -> {
            BigDecimal totalBudget = project.getTotalBudget() != null ? project.getTotalBudget() : BigDecimal.ZERO;
            BigDecimal totalCosts = project.getRealizedCost() != null ? project.getRealizedCost() : BigDecimal.ZERO;
            
            // Separar custos por categoria (aproximação)
            BigDecimal materialCosts = totalCosts.multiply(BigDecimal.valueOf(0.6)); // 60% material
            BigDecimal serviceCosts = totalCosts.multiply(BigDecimal.valueOf(0.4));   // 40% serviço
            
            BigDecimal remainingBudget = totalBudget.subtract(totalCosts);
            Double budgetUtilizationPercent = totalBudget.compareTo(BigDecimal.ZERO) > 0 ? 
                totalCosts.divide(totalBudget, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0.0;
            Boolean overBudget = totalCosts.compareTo(totalBudget) > 0;
            
            return new CostReportDto(
                project.getId(),
                project.getName(),
                project.getClient(),
                totalBudget,
                materialCosts,
                serviceCosts,
                totalCosts,
                remainingBudget,
                budgetUtilizationPercent,
                overBudget,
                LocalDate.now()
            );
        }).collect(Collectors.toList());
    }

    /**
     * Gera relatório de situação do estoque.
     * 
     * Para cada material, o relatório mostra:
     * - Quantidade atual vs mínima
     * - Indicador de baixo estoque
     * - Valor total do material em estoque
     * - Informações do fornecedor
     * - Data de referência do relatório
     * 
     * Essencial para:
     * - Controle de reposição de materiais
     * - Análise de valor imobilizado
     * - Planejamento de compras
     * 
     * @return Lista de StockReportDto com situação atual do estoque
     */
    public List<StockReportDto> getStockReport() {
        List<Material> materials = materialRepository.findAll();
        
        return materials.stream().map(material -> {
            BigDecimal currentStock = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;
            BigDecimal minimumStock = material.getMinimumStock() != null ? material.getMinimumStock() : BigDecimal.ZERO;
            Boolean lowStock = currentStock.compareTo(minimumStock) <= 0;
            BigDecimal totalValue = currentStock.multiply(material.getUnitPrice());
            
            return new StockReportDto(
                material.getId(),
                material.getName(),
                "Geral", // Categoria não definida na entidade
                material.getUnitOfMeasure(),
                currentStock.intValue(),
                minimumStock.intValue(),
                lowStock,
                material.getUnitPrice(),
                totalValue,
                LocalDate.now(),
                material.getSupplier()
            );
        }).collect(Collectors.toList());
    }

    /**
     * Calcula o percentual de progresso de um projeto.
     * 
     * Lógica de cálculo:
     * 1. Se há tarefas: percentual baseado em tarefas concluídas
     * 2. Se não há tarefas: usa progresso manual do projeto
     * 3. Padrão: 0% se não há dados
     * 
     * @param project projeto para calcular progresso
     * @param totalTasks número total de tarefas
     * @param completedTasks número de tarefas concluídas
     * @return percentual de progresso (0-100)
     */
    private Integer calculateProgressPercentage(Project project, Integer totalTasks, Integer completedTasks) {
        if (totalTasks == 0) {
            BigDecimal projectProgress = project.getProgressPercentage();
            return projectProgress != null ? projectProgress.intValue() : 0;
        }
        return (completedTasks * 100) / totalTasks;
    }

    /**
     * Verifica se um projeto está atrasado.
     * 
     * Critérios para atraso:
     * - Data planejada de conclusão definida
     * - Data atual após a data planejada
     * - Projeto ainda não concluído (sem data real de conclusão)
     * 
     * @param project projeto a verificar
     * @return true se projeto está atrasado, false caso contrário
     */
    private Boolean isProjectDelayed(Project project) {
        if (project.getEndDatePlanned() == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return today.isAfter(project.getEndDatePlanned()) && 
               project.getEndDateActual() == null;
    }

    /**
     * Calcula dias restantes para conclusão planejada.
     * 
     * Regras de cálculo:
     * - Se projeto já concluído: 0 dias
     * - Se sem data planejada: 0 dias
     * - Diferença entre hoje e data planejada (pode ser negativo se atrasado)
     * 
     * @param project projeto para calcular dias restantes
     * @return número de dias até conclusão planejada
     */
    private Long calculateDaysRemaining(Project project) {
        if (project.getEndDatePlanned() == null || project.getEndDateActual() != null) {
            return 0L;
        }
        LocalDate today = LocalDate.now();
        return ChronoUnit.DAYS.between(today, project.getEndDatePlanned());
    }
} 