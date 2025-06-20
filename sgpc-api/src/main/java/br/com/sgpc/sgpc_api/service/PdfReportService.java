package br.com.sgpc.sgpc_api.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import br.com.sgpc.sgpc_api.dto.CostReportDto;
import br.com.sgpc.sgpc_api.dto.ProjectReportDto;
import br.com.sgpc.sgpc_api.dto.StockReportDto;

/**
 * Serviço responsável pela geração de relatórios em PDF com gráficos e visualizações.
 * 
 * Este serviço oferece funcionalidades para:
 * - Geração de PDFs profissionais com logotipo e formatação
 * - Criação de gráficos usando JFreeChart
 * - Relatórios detalhados com tabelas e análises
 * - Suporte a relatórios gerais e específicos
 * - Formatação automática de valores monetários e datas
 * 
 * Características dos PDFs gerados:
 * - Cabeçalho com logotipo e informações da empresa
 * - Gráficos interativos (barras, pizza, linhas)
 * - Tabelas formatadas com dados detalhados
 * - Análises e insights automáticos
 * - Rodapé com data de geração e paginação
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Service
public class PdfReportService {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Gera relatório de projetos em PDF com gráficos e análises.
     * 
     * O relatório inclui:
     * - Resumo executivo com estatísticas gerais
     * - Gráfico de status dos projetos
     * - Gráfico de progresso por projeto
     * - Tabela detalhada com todos os dados
     * - Análise de projetos em atraso
     * 
     * @param projects lista de projetos para o relatório
     * @param specificProject se true, relatório específico; se false, relatório geral
     * @return array de bytes do PDF gerado
     */
    public byte[] generateProjectReportPdf(List<ProjectReportDto> projects, boolean specificProject) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Cabeçalho
            addHeader(document, specificProject ? "Relatório Detalhado de Projeto" : "Relatório Geral de Projetos");

            if (!projects.isEmpty()) {
                // Resumo executivo (apenas para relatório geral)
                if (!specificProject) {
                    addProjectSummary(document, projects);
                }

                if (specificProject && projects.size() == 1) {
                    // Para projeto específico: gráficos de tarefas e desempenho da equipe
                    ProjectReportDto project = projects.get(0);
                    addTaskStatusChart(document, project);
                    addTeamPerformanceChart(document, project);
                } else {
                    // Para relatório geral: gráficos de distribuição e progresso
                    addProjectStatusChart(document, projects);
                    addProjectProgressChart(document, projects);
                }

                // Tabela detalhada
                addProjectTable(document, projects);

                // Análise de projetos em atraso (apenas para relatório geral)
                if (!specificProject) {
                    addDelayedProjectsAnalysis(document, projects);
                }
            } else {
                document.add(new Paragraph("Nenhum projeto encontrado para gerar o relatório.")
                    .setTextAlignment(TextAlignment.CENTER));
            }

            // Rodapé
            addFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório PDF de projetos: " + e.getMessage(), e);
        }
    }

    /**
     * Gera relatório de custos em PDF com análises financeiras e gráficos.
     * 
     * O relatório inclui:
     * - Resumo financeiro consolidado
     * - Gráfico de distribuição de custos por categoria
     * - Gráfico de utilização orçamentária
     * - Tabela detalhada com análise por projeto
     * - Alertas de projetos com estouro orçamentário
     * 
     * @param costs lista de custos para o relatório
     * @param specificProject se true, relatório específico; se false, relatório geral
     * @return array de bytes do PDF gerado
     */
    public byte[] generateCostReportPdf(List<CostReportDto> costs, boolean specificProject) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Cabeçalho
            addHeader(document, specificProject ? "Relatório Detalhado de Custos" : "Relatório Geral de Custos");

            if (!costs.isEmpty()) {
                // Resumo financeiro
                addCostSummary(document, costs);

                // Gráfico de distribuição de custos
                addCostDistributionChart(document, costs);

                // Gráfico de utilização orçamentária
                addBudgetUtilizationChart(document, costs);

                // Tabela detalhada
                addCostTable(document, costs);

                // Análise de projetos com estouro
                addOverBudgetAnalysis(document, costs);
            } else {
                document.add(new Paragraph("Nenhum dado de custo encontrado para gerar o relatório.")
                    .setTextAlignment(TextAlignment.CENTER));
            }

            // Rodapé
            addFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório PDF de custos: " + e.getMessage(), e);
        }
    }

    /**
     * Gera relatório de estoque em PDF com análises de inventário e gráficos.
     * 
     * O relatório inclui:
     * - Resumo do inventário total
     * - Gráfico de materiais com baixo estoque
     * - Gráfico de valor imobilizado por categoria
     * - Tabela detalhada com todos os materiais
     * - Alertas de reposição necessária
     * 
     * @param stock lista de materiais para o relatório
     * @param specificMaterial se true, relatório específico; se false, relatório geral
     * @return array de bytes do PDF gerado
     */
    public byte[] generateStockReportPdf(List<StockReportDto> stock, boolean specificMaterial) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Cabeçalho
            addHeader(document, specificMaterial ? "Relatório Detalhado de Material" : "Relatório Geral de Estoque");

            if (!stock.isEmpty()) {
                // Resumo do estoque
                addStockSummary(document, stock);

                // Gráfico de baixo estoque
                addLowStockChart(document, stock);

                // Gráfico de valor por categoria
                addStockValueChart(document, stock);

                // Tabela detalhada
                addStockTable(document, stock);

                // Análise de reposição
                addReplenishmentAnalysis(document, stock);
            } else {
                document.add(new Paragraph("Nenhum material encontrado para gerar o relatório.")
                    .setTextAlignment(TextAlignment.CENTER));
            }

            // Rodapé
            addFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório PDF de estoque: " + e.getMessage(), e);
        }
    }

    private void addHeader(Document document, String title) {
        // Cabeçalho com fundo azul
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1}));
        headerTable.setWidth(UnitValue.createPercentValue(100));
        
        Cell headerCell = new Cell()
            .add(new Paragraph("SGPC - Sistema de Gerenciamento de Projetos de Construção")
                .setFontSize(18)
                .setBold()
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5))
            .add(new Paragraph(title)
                .setFontSize(14)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER))
            .setBackgroundColor(new DeviceRgb(63, 81, 181))
            .setPadding(20)
            .setBorder(Border.NO_BORDER);
        
        headerTable.addCell(headerCell);
        document.add(headerTable);
        
        // Data de geração
        document.add(new Paragraph("Data de Geração: " + LocalDate.now().format(DATE_FORMAT))
            .setFontSize(10)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginTop(10)
            .setMarginBottom(20));
    }

    private void addFooter(Document document) {
        document.add(new Paragraph("\n\nRelatório gerado automaticamente pelo Sistema SGPC")
            .setFontSize(8)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.GRAY));
    }

    private void addProjectSummary(Document document, List<ProjectReportDto> projects) {
        long totalProjects = projects.size();
        long completedProjects = projects.stream().filter(p -> "CONCLUIDO".equals(p.getStatus())).count();
        long delayedProjects = projects.stream().filter(p -> Boolean.TRUE.equals(p.getDelayed())).count();
        
        BigDecimal totalBudget = projects.stream()
            .map(ProjectReportDto::getTotalBudget)
            .filter(budget -> budget != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalUsed = projects.stream()
            .map(ProjectReportDto::getUsedBudget)
            .filter(used -> used != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Título da seção com estilo
        document.add(new Paragraph("Resumo Executivo")
            .setFontSize(16)
            .setBold()
            .setFontColor(new DeviceRgb(63, 81, 181))
            .setMarginBottom(15)
            .setMarginTop(10));

        // Cards de resumo em layout horizontal
        Table cardsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}));
        cardsTable.setWidth(UnitValue.createPercentValue(100));

        // Card 1 - Total de Projetos
        Cell card1 = createSummaryCard("Total de Projetos", String.valueOf(totalProjects), new DeviceRgb(33, 150, 243));
        cardsTable.addCell(card1);

        // Card 2 - Projetos Concluídos
        Cell card2 = createSummaryCard("Concluídos", String.valueOf(completedProjects), new DeviceRgb(76, 175, 80));
        cardsTable.addCell(card2);

        // Card 3 - Projetos em Atraso
        Cell card3 = createSummaryCard("Em Atraso", String.valueOf(delayedProjects), new DeviceRgb(244, 67, 54));
        cardsTable.addCell(card3);

        // Card 4 - Taxa de Conclusão
        double conclusionRate = totalProjects > 0 ? (double) completedProjects / totalProjects * 100 : 0;
        Cell card4 = createSummaryCard("Taxa Conclusão", String.format("%.1f%%", conclusionRate), new DeviceRgb(156, 39, 176));
        cardsTable.addCell(card4);

        document.add(cardsTable);
        document.add(new Paragraph("\n"));

        // Informações orçamentárias com design melhorado
        document.add(new Paragraph("Informações Orçamentárias")
            .setFontSize(14)
            .setBold()
            .setFontColor(new DeviceRgb(63, 81, 181))
            .setMarginBottom(10));

        Table budgetTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}));
        budgetTable.setWidth(UnitValue.createPercentValue(100));

        budgetTable.addHeaderCell(createStyledHeaderCell("Orçamento Total"));
        budgetTable.addHeaderCell(createStyledHeaderCell("Custo Total"));
        budgetTable.addHeaderCell(createStyledHeaderCell("% Utilização"));
        budgetTable.addHeaderCell(createStyledHeaderCell("Saldo"));

        budgetTable.addCell(createStyledDataCell(CURRENCY_FORMAT.format(totalBudget)));
        budgetTable.addCell(createStyledDataCell(CURRENCY_FORMAT.format(totalUsed)));
        
        double utilizationPercentage = totalBudget.compareTo(BigDecimal.ZERO) > 0 ? 
            totalUsed.divide(totalBudget, 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100 : 0;
        budgetTable.addCell(createStyledDataCell(String.format("%.1f%%", utilizationPercentage)));
        
        BigDecimal saldo = totalBudget.subtract(totalUsed);
        String saldoText = CURRENCY_FORMAT.format(saldo);
        budgetTable.addCell(createStyledDataCell(saldo.compareTo(BigDecimal.ZERO) >= 0 ? saldoText : saldoText + " (DÉFICIT)"));

        document.add(budgetTable);
        document.add(new Paragraph("\n"));
    }

    private Cell createSummaryCard(String title, String value, DeviceRgb color) {
        Cell card = new Cell()
            .add(new Paragraph(title)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5))
            .add(new Paragraph(value)
                .setFontSize(20)
                .setBold()
                .setFontColor(color)
                .setTextAlignment(TextAlignment.CENTER))
            .setPadding(15)
            .setBorder(new com.itextpdf.layout.borders.SolidBorder(color, 2))
            .setBackgroundColor(new DeviceRgb(248, 249, 250));
        
        return card;
    }

    private Cell createStyledHeaderCell(String content) {
        return new Cell()
            .add(new Paragraph(content)
                .setFontSize(11)
                .setBold()
                .setFontColor(ColorConstants.WHITE))
            .setBackgroundColor(new DeviceRgb(63, 81, 181))
            .setPadding(10)
            .setTextAlignment(TextAlignment.CENTER)
            .setBorder(Border.NO_BORDER);
    }

    private Cell createStyledDataCell(String content) {
        return new Cell()
            .add(new Paragraph(content)
                .setFontSize(10))
            .setPadding(10)
            .setTextAlignment(TextAlignment.CENTER)
            .setBorder(new com.itextpdf.layout.borders.SolidBorder(new DeviceRgb(224, 224, 224), 1));
    }

    private void addProjectStatusChart(Document document, List<ProjectReportDto> projects) {
        try {
            DefaultPieDataset dataset = new DefaultPieDataset();
            
            // Contar projetos por status
            projects.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    ProjectReportDto::getStatus, 
                    java.util.stream.Collectors.counting()))
                .forEach((status, count) -> dataset.setValue(status, count));

            JFreeChart chart = ChartFactory.createPieChart(
                "Distribuição de Projetos por Status",
                dataset,
                true,
                true,
                false
            );

            // Design moderno e profissional
            PiePlot plot = (PiePlot) chart.getPlot();
            
            // Cores modernas e vibrantes
            plot.setSectionPaint("CONCLUIDO", new Color(76, 175, 80));      // Verde Material
            plot.setSectionPaint("EM_ANDAMENTO", new Color(33, 150, 243));  // Azul Material
            plot.setSectionPaint("PAUSADO", new Color(255, 152, 0));        // Laranja Material
            plot.setSectionPaint("CANCELADO", new Color(244, 67, 54));      // Vermelho Material
            plot.setSectionPaint("PLANEJAMENTO", new Color(156, 39, 176));  // Roxo Material
            
            // Configurações visuais modernas
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            plot.setShadowPaint(null);
            plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
            plot.setLabelOutlinePaint(null);
            plot.setLabelShadowPaint(null);
            plot.setLabelFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
            plot.setStartAngle(90);
            // plot.setDirection removido - funcionalidade deprecated
            
            // Mostrar percentuais nas fatias
            plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})", new java.text.DecimalFormat("0"), new java.text.DecimalFormat("0.0%")));
            
            // Estilo do título mais moderno
            chart.getTitle().setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
            chart.getTitle().setPaint(new Color(63, 81, 181));
            
            // Estilo da legenda
            chart.getLegend().setItemFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
            // chart.getLegend().setFrame removido - funcionalidade deprecated
            chart.getLegend().setBackgroundPaint(Color.WHITE);
            
            addChartToDocument(document, chart);

        } catch (Exception e) {
            document.add(new Paragraph("Erro ao gerar gráfico de status dos projetos."));
        }
    }

    private void addProjectProgressChart(Document document, List<ProjectReportDto> projects) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            // Incluir TODOS os projetos
            projects.forEach(project -> {
                String projectName = project.getName() != null && project.getName().length() > 20 ? 
                    project.getName().substring(0, 20) + "..." : 
                    (project.getName() != null ? project.getName() : "Projeto");
                Integer progress = project.getProgressPercentage() != null ? project.getProgressPercentage() : 0;
                dataset.addValue(progress, "Progresso (%)", projectName);
            });

            JFreeChart chart = ChartFactory.createBarChart(
                "Progresso dos Projetos (%)",
                "",
                "Progresso (%)",
                dataset
            );

            CategoryPlot plot = chart.getCategoryPlot();
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            
            // Gradiente de cores baseado no progresso - mais moderno
            renderer.setBarPainter(new org.jfree.chart.renderer.category.GradientBarPainter(0.1, 0.2, 0.3));
            
            // Cores dinâmicas baseadas no progresso de cada projeto
            for (int i = 0; i < projects.size(); i++) {
                Integer progress = projects.get(i).getProgressPercentage() != null ? 
                    projects.get(i).getProgressPercentage() : 0;
                
                Color barColor;
                if (progress >= 90) {
                    barColor = new Color(76, 175, 80);     // Verde escuro - excelente
                } else if (progress >= 75) {
                    barColor = new Color(139, 195, 74);    // Verde claro - bom
                } else if (progress >= 50) {
                    barColor = new Color(33, 150, 243);    // Azul - médio
                } else if (progress >= 25) {
                    barColor = new Color(255, 152, 0);     // Laranja - baixo
                } else {
                    barColor = new Color(244, 67, 54);     // Vermelho - crítico
                }
                renderer.setSeriesPaint(0, barColor);
            }
            
            // Design moderno e limpo
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinesVisible(false);
            plot.setRangeGridlinesVisible(true);
            plot.setRangeGridlinePaint(new Color(230, 230, 230));
            plot.setOutlineVisible(false);
            
            // Estilo dos eixos
            plot.getDomainAxis().setTickLabelFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 9));
            plot.getRangeAxis().setTickLabelFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
            plot.getDomainAxis().setLabelFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
            plot.getRangeAxis().setLabelFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
            
            // Rotacionar labels dos projetos para melhor legibilidade
            plot.getDomainAxis().setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.UP_45);
            
            // Título moderno
            chart.getTitle().setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
            chart.getTitle().setPaint(new Color(63, 81, 181));
            
            // Remover legenda (desnecessária para gráfico de barras simples)
            chart.removeLegend();
            
            addChartToDocument(document, chart);

        } catch (Exception e) {
            e.printStackTrace();
            document.add(new Paragraph("Erro ao gerar gráfico de progresso dos projetos: " + e.getMessage()));
        }
    }

    private void addProjectTable(Document document, List<ProjectReportDto> projects) {
        document.add(new Paragraph("Detalhamento dos Projetos")
            .setFontSize(16)
            .setBold()
            .setFontColor(new DeviceRgb(63, 81, 181))
            .setMarginTop(20)
            .setMarginBottom(15));

        for (ProjectReportDto project : projects) {
            // Seção para cada projeto com design melhorado
            document.add(new Paragraph(project.getName() != null ? project.getName() : "Projeto Sem Nome")
                .setFontSize(14)
                .setBold()
                .setFontColor(new DeviceRgb(33, 150, 243))
                .setMarginBottom(10));

            // Informações básicas do projeto
            Table projectInfoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1, 1}));
            projectInfoTable.setWidth(UnitValue.createPercentValue(100));

            projectInfoTable.addHeaderCell(createStyledHeaderCell("Cliente"));
            projectInfoTable.addHeaderCell(createStyledHeaderCell("Status"));
            projectInfoTable.addHeaderCell(createStyledHeaderCell("Progresso"));
            projectInfoTable.addHeaderCell(createStyledHeaderCell("Orçamento"));
            projectInfoTable.addHeaderCell(createStyledHeaderCell("Custo Total"));

            projectInfoTable.addCell(createStyledDataCell(project.getClient() != null ? project.getClient() : "N/A"));
            projectInfoTable.addCell(createStyledDataCell(project.getStatus() != null ? project.getStatus() : "N/A"));
            projectInfoTable.addCell(createStyledDataCell((project.getProgressPercentage() != null ? project.getProgressPercentage() : 0) + "%"));
            projectInfoTable.addCell(createStyledDataCell(CURRENCY_FORMAT.format(project.getTotalBudget() != null ? project.getTotalBudget() : BigDecimal.ZERO)));
            projectInfoTable.addCell(createStyledDataCell(CURRENCY_FORMAT.format(project.getUsedBudget() != null ? project.getUsedBudget() : BigDecimal.ZERO)));

            document.add(projectInfoTable);

            // Seção de Tarefas
            if (project.getTasks() != null && !project.getTasks().isEmpty()) {
                document.add(new Paragraph("Tarefas")
                    .setFontSize(12)
                    .setBold()
                    .setFontColor(new DeviceRgb(76, 175, 80))
                    .setMarginTop(10)
                    .setMarginBottom(5));

                Table tasksTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 1, 2, 2}));
                tasksTable.setWidth(UnitValue.createPercentValue(100));

                tasksTable.addHeaderCell(createStyledHeaderCell("Descrição"));
                tasksTable.addHeaderCell(createStyledHeaderCell("Status"));
                tasksTable.addHeaderCell(createStyledHeaderCell("Progresso"));
                tasksTable.addHeaderCell(createStyledHeaderCell("Responsável"));
                tasksTable.addHeaderCell(createStyledHeaderCell("Custo"));

                project.getTasks().forEach(task -> {
                    tasksTable.addCell(createStyledDataCell(task.getTitle() != null ? task.getTitle() : "N/A"));
                    tasksTable.addCell(createStyledDataCell(task.getStatus() != null ? task.getStatus() : "N/A"));
                    tasksTable.addCell(createStyledDataCell((task.getProgressPercentage() != null ? task.getProgressPercentage() : 0) + "%"));
                    tasksTable.addCell(createStyledDataCell(task.getAssignedUserName() != null ? task.getAssignedUserName() : "N/A"));
                    tasksTable.addCell(createStyledDataCell(CURRENCY_FORMAT.format(task.getTotalCost() != null ? task.getTotalCost() : BigDecimal.ZERO)));
                });

                document.add(tasksTable);
            } else {
                document.add(new Paragraph("Nenhuma tarefa encontrada para este projeto.")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(5));
            }

            // Seção de Equipe
            if (project.getTeamMembers() != null && !project.getTeamMembers().isEmpty()) {
                document.add(new Paragraph("Equipe")
                    .setFontSize(12)
                    .setBold()
                    .setFontColor(new DeviceRgb(156, 39, 176))
                    .setMarginTop(10)
                    .setMarginBottom(5));

                Table teamTable = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1, 1}));
                teamTable.setWidth(UnitValue.createPercentValue(100));

                teamTable.addHeaderCell(createStyledHeaderCell("Nome"));
                teamTable.addHeaderCell(createStyledHeaderCell("Função"));
                teamTable.addHeaderCell(createStyledHeaderCell("Tarefas"));
                teamTable.addHeaderCell(createStyledHeaderCell("Concluídas"));
                teamTable.addHeaderCell(createStyledHeaderCell("Performance"));

                project.getTeamMembers().forEach(member -> {
                    teamTable.addCell(createStyledDataCell(member.getFullName() != null ? member.getFullName() : "N/A"));
                    teamTable.addCell(createStyledDataCell(member.getRole() != null ? member.getRole() : "N/A"));
                    teamTable.addCell(createStyledDataCell(String.valueOf(member.getAssignedTasksCount() != null ? member.getAssignedTasksCount() : 0)));
                    teamTable.addCell(createStyledDataCell(String.valueOf(member.getCompletedTasksCount() != null ? member.getCompletedTasksCount() : 0)));
                    
                    String performance = "N/A";
                    if (member.getTaskCompletionRate() != null) {
                        performance = String.format("%.1f%%", member.getTaskCompletionRate());
                    }
                    teamTable.addCell(createStyledDataCell(performance));
                });

                document.add(teamTable);
            } else {
                document.add(new Paragraph("Nenhum membro de equipe encontrado para este projeto.")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(5));
            }

            // Separador entre projetos
            document.add(new Paragraph("\n"));
        }
    }

    private void addDelayedProjectsAnalysis(Document document, List<ProjectReportDto> projects) {
        List<ProjectReportDto> delayedProjects = projects.stream()
            .filter(p -> Boolean.TRUE.equals(p.getDelayed()))
            .toList();

        if (!delayedProjects.isEmpty()) {
            document.add(new Paragraph("Análise de Projetos em Atraso")
                .setFontSize(12)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10));

            document.add(new Paragraph(String.format(
                "Foram identificados %d projeto(s) em atraso, representando %.1f%% do total.",
                delayedProjects.size(),
                (double) delayedProjects.size() / projects.size() * 100))
                .setMarginBottom(10));

            Table delayedTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 1}));
            delayedTable.setWidth(UnitValue.createPercentValue(100));

            delayedTable.addHeaderCell(createCell("Projeto", true));
            delayedTable.addHeaderCell(createCell("Cliente", true));
            delayedTable.addHeaderCell(createCell("Dias de Atraso", true));

            delayedProjects.forEach(project -> {
                delayedTable.addCell(createCell(project.getName(), false));
                delayedTable.addCell(createCell(project.getClient() != null ? project.getClient() : "N/A", false));
                delayedTable.addCell(createCell(
                    project.getDaysRemaining() != null && project.getDaysRemaining() < 0 ? 
                        String.valueOf(Math.abs(project.getDaysRemaining())) : "N/A", 
                    false));
            });

            document.add(delayedTable);
        }
    }

    private void addCostSummary(Document document, List<CostReportDto> costs) {
        BigDecimal totalBudget = costs.stream()
            .map(CostReportDto::getTotalBudget)
            .filter(budget -> budget != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalMaterialCosts = costs.stream()
            .map(CostReportDto::getMaterialCosts)
            .filter(cost -> cost != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalServiceCosts = costs.stream()
            .map(CostReportDto::getServiceCosts)
            .filter(cost -> cost != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalCosts = costs.stream()
            .map(CostReportDto::getTotalCosts)
            .filter(cost -> cost != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long overBudgetProjects = costs.stream()
            .filter(c -> Boolean.TRUE.equals(c.getOverBudget()))
            .count();

        document.add(new Paragraph("Resumo Financeiro")
            .setFontSize(12)
            .setBold()
            .setMarginBottom(10));

        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}));
        summaryTable.setWidth(UnitValue.createPercentValue(100));

        summaryTable.addHeaderCell(createCell("Orçamento Total", true));
        summaryTable.addHeaderCell(createCell("Custos Materiais", true));
        summaryTable.addHeaderCell(createCell("Custos Serviços", true));
        summaryTable.addHeaderCell(createCell("Total Gasto", true));

        summaryTable.addCell(createCell(CURRENCY_FORMAT.format(totalBudget), false));
        summaryTable.addCell(createCell(CURRENCY_FORMAT.format(totalMaterialCosts), false));
        summaryTable.addCell(createCell(CURRENCY_FORMAT.format(totalServiceCosts), false));
        summaryTable.addCell(createCell(CURRENCY_FORMAT.format(totalCosts), false));

        document.add(summaryTable);
        document.add(new Paragraph("\n"));

        if (overBudgetProjects > 0) {
            document.add(new Paragraph(String.format(
                "ATENÇÃO: %d projeto(s) estão com estouro orçamentário!", overBudgetProjects))
                .setFontColor(new DeviceRgb(244, 67, 54))
                .setBold()
                .setMarginBottom(10));
        }
    }

    private void addCostDistributionChart(Document document, List<CostReportDto> costs) {
        try {
            BigDecimal totalMaterial = costs.stream()
                .map(CostReportDto::getMaterialCosts)
                .filter(cost -> cost != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            BigDecimal totalService = costs.stream()
                .map(CostReportDto::getServiceCosts)
                .filter(cost -> cost != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            DefaultPieDataset dataset = new DefaultPieDataset();
            if (totalMaterial.compareTo(BigDecimal.ZERO) > 0) {
                dataset.setValue("Materiais", totalMaterial);
            }
            if (totalService.compareTo(BigDecimal.ZERO) > 0) {
                dataset.setValue("Serviços", totalService);
            }

            JFreeChart chart = ChartFactory.createPieChart(
                "Distribuição de Custos por Categoria",
                dataset,
                true,
                true,
                false
            );

            // Design moderno para gráfico de pizza
            PiePlot plot = (PiePlot) chart.getPlot();
            
            // Cores profissionais e modernas
            plot.setSectionPaint("Materiais", new Color(67, 160, 71));      // Verde Material 600
            plot.setSectionPaint("Serviços", new Color(30, 136, 229));      // Azul Material 600
            plot.setSectionPaint("Mão de Obra", new Color(251, 140, 0));    // Laranja Material 600
            plot.setSectionPaint("Equipamentos", new Color(142, 36, 170));  // Roxo Material 600
            
            // Configurações visuais modernas
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            plot.setShadowPaint(null);
            plot.setLabelBackgroundPaint(new Color(255, 255, 255, 180));
            plot.setLabelOutlinePaint(new Color(200, 200, 200));
            plot.setLabelShadowPaint(null);
            plot.setLabelFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
            plot.setStartAngle(45);
            // plot.setDirection removido - funcionalidade deprecated
            
            // Mostrar valores e percentuais
            plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
                "{0}\n{1}\n({2})", 
                CURRENCY_FORMAT, 
                new java.text.DecimalFormat("0.0%")));
            
            // Estilo do título
            chart.getTitle().setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
            chart.getTitle().setPaint(new Color(63, 81, 181));
            
            // Estilo da legenda
            chart.getLegend().setItemFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 11));
            // chart.getLegend().setFrame removido - funcionalidade deprecated
            chart.getLegend().setBackgroundPaint(Color.WHITE);
            
            addChartToDocument(document, chart);

        } catch (Exception e) {
            document.add(new Paragraph("Erro ao gerar gráfico de distribuição de custos."));
        }
    }

    private void addBudgetUtilizationChart(Document document, List<CostReportDto> costs) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            costs.stream()
                .limit(10)
                .forEach(cost -> {
                    String projectName = cost.getProjectName().length() > 15 ? 
                        cost.getProjectName().substring(0, 15) + "..." : cost.getProjectName();
                    dataset.addValue(cost.getBudgetUtilizationPercent(), "Utilização (%)", projectName);
                });

            JFreeChart chart = ChartFactory.createBarChart(
                "Utilização Orçamentária por Projeto (%)",
                "Projetos",
                "Utilização (%)",
                dataset
            );

            CategoryPlot plot = chart.getCategoryPlot();
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, new Color(255, 152, 0));

            addChartToDocument(document, chart);

        } catch (Exception e) {
            document.add(new Paragraph("Erro ao gerar gráfico de utilização orçamentária."));
        }
    }

    private void addCostTable(Document document, List<CostReportDto> costs) {
        document.add(new Paragraph("Detalhamento de Custos por Projeto")
            .setFontSize(12)
            .setBold()
            .setMarginTop(20)
            .setMarginBottom(10));

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1.5f, 1.5f, 1.5f, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(createCell("Projeto", true));
        table.addHeaderCell(createCell("Orçamento", true));
        table.addHeaderCell(createCell("Materiais", true));
        table.addHeaderCell(createCell("Serviços", true));
        table.addHeaderCell(createCell("Utilização", true));
        table.addHeaderCell(createCell("Estouro", true));

        costs.forEach(cost -> {
            table.addCell(createCell(cost.getProjectName(), false));
            table.addCell(createCell(CURRENCY_FORMAT.format(cost.getTotalBudget()), false));
            table.addCell(createCell(CURRENCY_FORMAT.format(cost.getMaterialCosts()), false));
            table.addCell(createCell(CURRENCY_FORMAT.format(cost.getServiceCosts()), false));
            table.addCell(createCell(String.format("%.1f%%", cost.getBudgetUtilizationPercent()), false));
            table.addCell(createCell(Boolean.TRUE.equals(cost.getOverBudget()) ? "SIM" : "NÃO", false));
        });

        document.add(table);
    }

    private void addOverBudgetAnalysis(Document document, List<CostReportDto> costs) {
        List<CostReportDto> overBudgetProjects = costs.stream()
            .filter(c -> Boolean.TRUE.equals(c.getOverBudget()))
            .toList();

        if (!overBudgetProjects.isEmpty()) {
            document.add(new Paragraph("Análise de Projetos com Estouro Orçamentário")
                .setFontSize(12)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10));

            BigDecimal totalOverage = overBudgetProjects.stream()
                .map(p -> p.getTotalCosts().subtract(p.getTotalBudget()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            document.add(new Paragraph(String.format(
                "Total de estouro: %s em %d projeto(s).",
                CURRENCY_FORMAT.format(totalOverage),
                overBudgetProjects.size()))
                .setMarginBottom(10));

            Table overBudgetTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 1}));
            overBudgetTable.setWidth(UnitValue.createPercentValue(100));

            overBudgetTable.addHeaderCell(createCell("Projeto", true));
            overBudgetTable.addHeaderCell(createCell("Orçamento", true));
            overBudgetTable.addHeaderCell(createCell("Gasto", true));
            overBudgetTable.addHeaderCell(createCell("Estouro", true));

            overBudgetProjects.forEach(project -> {
                BigDecimal overage = project.getTotalCosts().subtract(project.getTotalBudget());
                overBudgetTable.addCell(createCell(project.getProjectName(), false));
                overBudgetTable.addCell(createCell(CURRENCY_FORMAT.format(project.getTotalBudget()), false));
                overBudgetTable.addCell(createCell(CURRENCY_FORMAT.format(project.getTotalCosts()), false));
                overBudgetTable.addCell(createCell(CURRENCY_FORMAT.format(overage), false));
            });

            document.add(overBudgetTable);
        }
    }

    private void addStockSummary(Document document, List<StockReportDto> stock) {
        long totalMaterials = stock.size();
        long lowStockMaterials = stock.stream().filter(s -> Boolean.TRUE.equals(s.getLowStock())).count();
        
        BigDecimal totalValue = stock.stream()
            .map(StockReportDto::getTotalValue)
            .filter(value -> value != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalQuantity = stock.stream()
            .mapToInt(StockReportDto::getCurrentQuantity)
            .sum();

        document.add(new Paragraph("Resumo do Inventário")
            .setFontSize(12)
            .setBold()
            .setMarginBottom(10));

        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}));
        summaryTable.setWidth(UnitValue.createPercentValue(100));

        summaryTable.addHeaderCell(createCell("Total de Materiais", true));
        summaryTable.addHeaderCell(createCell("Baixo Estoque", true));
        summaryTable.addHeaderCell(createCell("Valor Total", true));
        summaryTable.addHeaderCell(createCell("Quantidade Total", true));

        summaryTable.addCell(createCell(String.valueOf(totalMaterials), false));
        summaryTable.addCell(createCell(String.valueOf(lowStockMaterials), false));
        summaryTable.addCell(createCell(CURRENCY_FORMAT.format(totalValue), false));
        summaryTable.addCell(createCell(String.valueOf(totalQuantity), false));

        document.add(summaryTable);
        document.add(new Paragraph("\n"));

        if (lowStockMaterials > 0) {
            document.add(new Paragraph(String.format(
                "ATENÇÃO: %d material(is) com estoque baixo!", lowStockMaterials))
                .setFontColor(new DeviceRgb(244, 67, 54))
                .setBold()
                .setMarginBottom(10));
        }
    }

    private void addLowStockChart(Document document, List<StockReportDto> stock) {
        try {
            long lowStock = stock.stream().filter(s -> Boolean.TRUE.equals(s.getLowStock())).count();
            long normalStock = stock.size() - lowStock;

            DefaultPieDataset dataset = new DefaultPieDataset();
            if (normalStock > 0) {
                dataset.setValue("Estoque Normal", normalStock);
            }
            if (lowStock > 0) {
                dataset.setValue("Estoque Baixo", lowStock);
            }

            JFreeChart chart = ChartFactory.createPieChart(
                "Situação do Estoque",
                dataset,
                true,
                true,
                false
            );

            // Design moderno para situação do estoque
            PiePlot plot = (PiePlot) chart.getPlot();
            
            // Cores intuitivas para status do estoque
            plot.setSectionPaint("Estoque Normal", new Color(67, 160, 71));    // Verde Material 600
            plot.setSectionPaint("Estoque Baixo", new Color(229, 57, 53));     // Vermelho Material 600
            plot.setSectionPaint("Estoque Alto", new Color(30, 136, 229));     // Azul Material 600
            
            // Configurações visuais modernas
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            plot.setShadowPaint(null);
            plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
            plot.setLabelOutlinePaint(new Color(180, 180, 180));
            plot.setLabelShadowPaint(null);
            plot.setLabelFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
            plot.setStartAngle(0);
            // plot.setDirection removido - funcionalidade deprecated
            
            // Labels informativos com quantidade
            plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
                "{0}\n{1} itens\n({2})", 
                new java.text.DecimalFormat("0"), 
                new java.text.DecimalFormat("0.0%")));
            
            // Título estilizado
            chart.getTitle().setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
            chart.getTitle().setPaint(new Color(63, 81, 181));
            
            // Legenda moderna
            chart.getLegend().setItemFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 11));
            // chart.getLegend().setFrame removido - funcionalidade deprecated
            chart.getLegend().setBackgroundPaint(Color.WHITE);

            addChartToDocument(document, chart);

        } catch (Exception e) {
            document.add(new Paragraph("Erro ao gerar gráfico de situação do estoque."));
        }
    }

    private void addStockValueChart(Document document, List<StockReportDto> stock) {
        try {
            java.util.Map<String, BigDecimal> valueByCategory = stock.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    StockReportDto::getCategory,
                    java.util.stream.Collectors.reducing(BigDecimal.ZERO, StockReportDto::getTotalValue, BigDecimal::add)
                ));

            DefaultPieDataset dataset = new DefaultPieDataset();
            valueByCategory.forEach((category, value) -> {
                if (value.compareTo(BigDecimal.ZERO) > 0) {
                    dataset.setValue(category, value);
                }
            });

            JFreeChart chart = ChartFactory.createPieChart(
                "Valor do Estoque por Categoria",
                dataset,
                true,
                true,
                false
            );

            addChartToDocument(document, chart);

        } catch (Exception e) {
            document.add(new Paragraph("Erro ao gerar gráfico de valor por categoria."));
        }
    }

    private void addStockTable(Document document, List<StockReportDto> stock) {
        document.add(new Paragraph("Detalhamento do Estoque")
            .setFontSize(12)
            .setBold()
            .setMarginTop(20)
            .setMarginBottom(10));

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1, 1.5f, 1}));
        table.setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(createCell("Material", true));
        table.addHeaderCell(createCell("Categoria", true));
        table.addHeaderCell(createCell("Qtd Atual", true));
        table.addHeaderCell(createCell("Qtd Mínima", true));
        table.addHeaderCell(createCell("Valor Total", true));
        table.addHeaderCell(createCell("Status", true));

        stock.forEach(item -> {
            table.addCell(createCell(item.getMaterialName(), false));
            table.addCell(createCell(item.getCategory(), false));
            table.addCell(createCell(item.getCurrentQuantity() + " " + item.getUnit(), false));
            table.addCell(createCell(item.getMinimumQuantity() + " " + item.getUnit(), false));
            table.addCell(createCell(CURRENCY_FORMAT.format(item.getTotalValue()), false));
            table.addCell(createCell(Boolean.TRUE.equals(item.getLowStock()) ? "BAIXO" : "OK", false));
        });

        document.add(table);
    }

    private void addReplenishmentAnalysis(Document document, List<StockReportDto> stock) {
        List<StockReportDto> lowStockItems = stock.stream()
            .filter(s -> Boolean.TRUE.equals(s.getLowStock()))
            .toList();

        if (!lowStockItems.isEmpty()) {
            document.add(new Paragraph("Análise de Reposição Necessária")
                .setFontSize(12)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10));

            document.add(new Paragraph(String.format(
                "Materiais que necessitam reposição urgente: %d",
                lowStockItems.size()))
                .setMarginBottom(10));

            Table replenishmentTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 2}));
            replenishmentTable.setWidth(UnitValue.createPercentValue(100));

            replenishmentTable.addHeaderCell(createCell("Material", true));
            replenishmentTable.addHeaderCell(createCell("Atual", true));
            replenishmentTable.addHeaderCell(createCell("Mínimo", true));
            replenishmentTable.addHeaderCell(createCell("Fornecedor", true));

            lowStockItems.forEach(item -> {
                replenishmentTable.addCell(createCell(item.getMaterialName(), false));
                replenishmentTable.addCell(createCell(item.getCurrentQuantity() + " " + item.getUnit(), false));
                replenishmentTable.addCell(createCell(item.getMinimumQuantity() + " " + item.getUnit(), false));
                replenishmentTable.addCell(createCell(item.getSupplier() != null ? item.getSupplier() : "N/A", false));
            });

            document.add(replenishmentTable);
        }
    }

    /**
     * Adiciona gráfico de status das tarefas para projeto específico
     */
    private void addTaskStatusChart(Document document, ProjectReportDto project) {
        try {
            DefaultPieDataset dataset = new DefaultPieDataset();
            
            if (project.getTaskStatusSummary() != null) {
                var summary = project.getTaskStatusSummary();
                if (summary.getTodoTasks() != null && summary.getTodoTasks() > 0) {
                    dataset.setValue("A Fazer", summary.getTodoTasks());
                }
                if (summary.getInProgressTasks() != null && summary.getInProgressTasks() > 0) {
                    dataset.setValue("Em Andamento", summary.getInProgressTasks());
                }
                if (summary.getCompletedTasks() != null && summary.getCompletedTasks() > 0) {
                    dataset.setValue("Concluídas", summary.getCompletedTasks());
                }
                if (summary.getOverdueTasks() != null && summary.getOverdueTasks() > 0) {
                    dataset.setValue("Em Atraso", summary.getOverdueTasks());
                }
            }

            JFreeChart chart = ChartFactory.createPieChart(
                "Status das Tarefas do Projeto",
                dataset,
                true,
                true,
                false
            );

            // Design moderno para status das tarefas
            PiePlot plot = (PiePlot) chart.getPlot();
            
            // Cores específicas para status de tarefas
            plot.setSectionPaint("A Fazer", new Color(158, 158, 158));        // Cinza
            plot.setSectionPaint("Em Andamento", new Color(33, 150, 243));    // Azul
            plot.setSectionPaint("Concluídas", new Color(76, 175, 80));       // Verde
            plot.setSectionPaint("Em Atraso", new Color(244, 67, 54));        // Vermelho
            
            // Configurações visuais
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            plot.setShadowPaint(null);
            plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
            plot.setLabelOutlinePaint(new Color(180, 180, 180));
            plot.setLabelShadowPaint(null);
            plot.setLabelFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
            plot.setStartAngle(45);
            
            // Labels informativos
            plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
                "{0}\n{1} tarefas\n({2})", 
                new java.text.DecimalFormat("0"), 
                new java.text.DecimalFormat("0.0%")));
            
            // Título estilizado
            chart.getTitle().setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
            chart.getTitle().setPaint(new Color(63, 81, 181));
            
            // Legenda
            chart.getLegend().setItemFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 11));
            chart.getLegend().setBackgroundPaint(Color.WHITE);
            
            addChartToDocument(document, chart);

        } catch (Exception e) {
            document.add(new Paragraph("Erro ao gerar gráfico de status das tarefas."));
        }
    }

    /**
     * Adiciona gráfico de desempenho da equipe para projeto específico
     */
    private void addTeamPerformanceChart(Document document, ProjectReportDto project) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            if (project.getTeamMembers() != null && !project.getTeamMembers().isEmpty()) {
                project.getTeamMembers().forEach(member -> {
                    String memberName = member.getFullName() != null && member.getFullName().length() > 15 ? 
                        member.getFullName().substring(0, 15) + "..." : 
                        (member.getFullName() != null ? member.getFullName() : "Membro");
                    
                    Double performance = member.getTaskCompletionRate() != null ? 
                        member.getTaskCompletionRate() : 0.0;
                    
                    dataset.addValue(performance, "Performance (%)", memberName);
                });
            } else {
                // Se não há membros, adicionar um valor padrão para evitar gráfico vazio
                dataset.addValue(0, "Performance (%)", "Sem membros");
            }

            JFreeChart chart = ChartFactory.createBarChart(
                "Desempenho da Equipe (%)",
                "",
                "Taxa de Conclusão (%)",
                dataset
            );

            CategoryPlot plot = chart.getCategoryPlot();
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            
            // Gradiente moderno para as barras
            renderer.setBarPainter(new org.jfree.chart.renderer.category.GradientBarPainter(0.1, 0.2, 0.3));
            
            // CORREÇÃO: Fixar escala do eixo Y entre 0 e 100%
            org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
            rangeAxis.setRange(0.0, 100.0);
            rangeAxis.setStandardTickUnits(org.jfree.chart.axis.NumberAxis.createIntegerTickUnits());
            
            // Cores baseadas na performance
            if (project.getTeamMembers() != null) {
                for (int i = 0; i < project.getTeamMembers().size(); i++) {
                    Double performance = project.getTeamMembers().get(i).getTaskCompletionRate();
                    if (performance != null) {
                        Color barColor;
                        if (performance >= 90) {
                            barColor = new Color(76, 175, 80);     // Verde - excelente
                        } else if (performance >= 75) {
                            barColor = new Color(139, 195, 74);    // Verde claro - bom
                        } else if (performance >= 50) {
                            barColor = new Color(33, 150, 243);    // Azul - médio
                        } else if (performance >= 25) {
                            barColor = new Color(255, 152, 0);     // Laranja - baixo
                        } else {
                            barColor = new Color(244, 67, 54);     // Vermelho - crítico
                        }
                        renderer.setSeriesPaint(0, barColor);
                    }
                }
            }
            
            // Design limpo
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinesVisible(false);
            plot.setRangeGridlinesVisible(true);
            plot.setRangeGridlinePaint(new Color(230, 230, 230));
            plot.setOutlineVisible(false);
            
            // Estilo dos eixos
            plot.getDomainAxis().setTickLabelFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 9));
            plot.getRangeAxis().setTickLabelFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
            plot.getDomainAxis().setLabelFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
            plot.getRangeAxis().setLabelFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
            
            // Rotacionar labels para melhor legibilidade
            plot.getDomainAxis().setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.UP_45);
            
            // Título moderno
            chart.getTitle().setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
            chart.getTitle().setPaint(new Color(63, 81, 181));
            
            // Remover legenda
            chart.removeLegend();
            
            addChartToDocument(document, chart);

        } catch (Exception e) {
            document.add(new Paragraph("Erro ao gerar gráfico de desempenho da equipe."));
        }
    }

    private void addChartToDocument(Document document, JFreeChart chart) throws IOException {
        // Aumentar resolução e qualidade do gráfico
        BufferedImage bufferedImage = chart.createBufferedImage(800, 500);
        
        ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", chartStream);
        
        Image chartImage = new Image(ImageDataFactory.create(chartStream.toByteArray()));
        chartImage.setWidth(UnitValue.createPercentValue(85));
        chartImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
        chartImage.setMarginTop(10);
        chartImage.setMarginBottom(15);
        
        // Adicionar uma sombra sutil ao gráfico
        chartImage.setBorder(new com.itextpdf.layout.borders.SolidBorder(new DeviceRgb(230, 230, 230), 1));
        
        document.add(chartImage);
        document.add(new Paragraph("\n"));
    }

    private Cell createCell(String content, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(content));
        cell.setBorder(Border.NO_BORDER);
        cell.setTextAlignment(TextAlignment.CENTER);
        
        if (isHeader) {
            cell.setBackgroundColor(new DeviceRgb(63, 81, 181));
            cell.setFontColor(ColorConstants.WHITE);
            cell.setBold();
        }
        
        return cell;
    }
}