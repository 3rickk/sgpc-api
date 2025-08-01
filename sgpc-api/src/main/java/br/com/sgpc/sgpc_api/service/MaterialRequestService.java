package br.com.sgpc.sgpc_api.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.dto.MaterialRequestApprovalDto;
import br.com.sgpc.sgpc_api.dto.MaterialRequestCreateDto;
import br.com.sgpc.sgpc_api.dto.MaterialRequestCreateItemDto;
import br.com.sgpc.sgpc_api.dto.MaterialRequestDetailsDto;
import br.com.sgpc.sgpc_api.dto.MaterialRequestItemDto;
import br.com.sgpc.sgpc_api.dto.MaterialRequestSummaryDto;
import br.com.sgpc.sgpc_api.entity.Material;
import br.com.sgpc.sgpc_api.entity.MaterialRequest;
import br.com.sgpc.sgpc_api.entity.MaterialRequestItem;
import br.com.sgpc.sgpc_api.entity.Project;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.enums.RequestStatus;
import br.com.sgpc.sgpc_api.repository.MaterialRepository;
import br.com.sgpc.sgpc_api.repository.MaterialRequestItemRepository;
import br.com.sgpc.sgpc_api.repository.MaterialRequestRepository;
import br.com.sgpc.sgpc_api.repository.ProjectRepository;
import br.com.sgpc.sgpc_api.repository.UserRepository;

/**
 * Serviço responsável pelo gerenciamento de solicitações de materiais.
 * 
 * Esta classe implementa o workflow completo de solicitações de materiais,
 * incluindo criação, aprovação, rejeição e controle de estoque automático.
 * Integra-se com o sistema de materiais para garantir disponibilidade.
 * 
 * Principais funcionalidades:
 * - Criação de solicitações com múltiplos itens
 * - Workflow de aprovação com validações
 * - Baixa automática de estoque na aprovação
 * - Controle de status e rastreabilidade
 * - Filtros por projeto e status
 * - Histórico completo de solicitações
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Service
@Transactional
public class MaterialRequestService {

    @Autowired
    private MaterialRequestRepository materialRequestRepository;

    @Autowired
    private MaterialRequestItemRepository materialRequestItemRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MaterialService materialService;

    /**
     * Cria uma nova solicitação de materiais.
     * 
     * Valida o projeto, usuário solicitante e materiais, criando uma
     * requisição com múltiplos itens no status PENDENTE para aprovação.
     * 
     * @param requestDto dados da solicitação com lista de materiais
     * @param requesterId ID do usuário que está fazendo a solicitação
     * @return MaterialRequestDetailsDto solicitação criada com detalhes completos
     * @throws RuntimeException se usuário ou material não for encontrado
     * @throws ProjectNotFoundException se projeto não for encontrado
     */
    public MaterialRequestDetailsDto createMaterialRequest(MaterialRequestCreateDto requestDto, Long requesterId) {
        // Obter usuário solicitante
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Validar projeto
        Project project = projectRepository.findById(requestDto.getProjectId())
                .orElseThrow(() -> new br.com.sgpc.sgpc_api.exception.ProjectNotFoundException("Projeto com ID " + requestDto.getProjectId() + " não foi encontrado"));

        // Criar requisição
        MaterialRequest materialRequest = new MaterialRequest();
        materialRequest.setProject(project);
        materialRequest.setRequester(requester);
        materialRequest.setNeededDate(requestDto.getNeededDate());
        materialRequest.setObservations(requestDto.getObservations());
        materialRequest.setStatus(RequestStatus.PENDENTE);

        // Salvar requisição primeiro
        materialRequest = materialRequestRepository.save(materialRequest);

        // Criar itens da requisição
        for (MaterialRequestCreateItemDto itemDto : requestDto.getItems()) {
            Material material = materialRepository.findByIdAndIsActiveTrue(itemDto.getMaterialId())
                    .orElseThrow(() -> new RuntimeException("Material não encontrado: " + itemDto.getMaterialId()));

            MaterialRequestItem item = new MaterialRequestItem();
            item.setMaterialRequest(materialRequest);
            item.setMaterial(material);
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(material.getUnitPrice());
            item.setObservations(itemDto.getObservations());

            materialRequest.addItem(item);
        }

        // Salvar novamente com os itens
        materialRequest = materialRequestRepository.save(materialRequest);

        return convertToDetailsDto(materialRequest);
    }

    /**
     * Lista todas as solicitações de materiais do sistema.
     * 
     * Retorna lista completa ordenada por data de criação,
     * incluindo informações básicas para visualização geral.
     * 
     * @return List<MaterialRequestSummaryDto> lista de todas as solicitações
     */
    @Transactional(readOnly = true)
    public List<MaterialRequestSummaryDto> getAllMaterialRequests() {
        return materialRequestRepository.findAllWithDetails().stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma solicitação de materiais pelo ID.
     * 
     * Retorna detalhes completos incluindo todos os itens
     * solicitados e informações de aprovação/rejeição.
     * 
     * @param id ID da solicitação
     * @return Optional<MaterialRequestDetailsDto> solicitação encontrada ou empty
     */
    @Transactional(readOnly = true)
    public Optional<MaterialRequestDetailsDto> getMaterialRequestById(Long id) {
        MaterialRequest materialRequest = materialRequestRepository.findByIdWithDetails(id);
        if (materialRequest == null) {
            return Optional.empty();
        }
        
        // Carregar itens
        List<MaterialRequestItem> items = materialRequestItemRepository
                .findByMaterialRequestIdWithMaterial(id);
        materialRequest.setItems(items);
        
        return Optional.of(convertToDetailsDto(materialRequest));
    }

    /**
     * Lista solicitações por status.
     * 
     * Filtra solicitações por status específico (PENDENTE, APROVADA, REJEITADA),
     * útil para workflows de aprovação e controle.
     * 
     * @param status status das solicitações a buscar
     * @return List<MaterialRequestSummaryDto> solicitações do status especificado
     */
    @Transactional(readOnly = true)
    public List<MaterialRequestSummaryDto> getMaterialRequestsByStatus(RequestStatus status) {
        return materialRequestRepository.findByStatusWithDetails(status).stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Lista solicitações de um projeto específico.
     * 
     * Retorna todas as solicitações relacionadas a um projeto,
     * ordenadas por data de criação mais recente.
     * 
     * @param projectId ID do projeto
     * @return List<MaterialRequestSummaryDto> solicitações do projeto
     */
    @Transactional(readOnly = true)
    public List<MaterialRequestSummaryDto> getMaterialRequestsByProject(Long projectId) {
        return materialRequestRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Aprova uma solicitação de materiais.
     * 
     * Valida disponibilidade de estoque para todos os itens antes
     * de aprovar. Na aprovação, dá baixa automática no estoque
     * de todos os materiais solicitados.
     * 
     * @param id ID da solicitação a ser aprovada
     * @param approverId ID do usuário que está aprovando
     * @return MaterialRequestDetailsDto solicitação aprovada
     * @throws RuntimeException se solicitação não estiver pendente, usuário não encontrado ou estoque insuficiente
     */
    public MaterialRequestDetailsDto approveMaterialRequest(Long id, Long approverId) {
        MaterialRequest materialRequest = materialRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisição não encontrada"));

        if (materialRequest.getStatus() != RequestStatus.PENDENTE) {
            throw new RuntimeException("Requisição não está pendente");
        }

        // Obter usuário que está aprovando
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Verificar se há estoque suficiente para todos os itens
        List<MaterialRequestItem> items = materialRequestItemRepository
                .findByMaterialRequestIdWithMaterial(id);
        
        for (MaterialRequestItem item : items) {
            Material material = item.getMaterial();
            if (material.getCurrentStock().compareTo(item.getQuantity()) < 0) {
                throw new RuntimeException("Estoque insuficiente para o material: " + material.getName() + 
                                         ". Estoque atual: " + material.getCurrentStock() + 
                                         ", Quantidade solicitada: " + item.getQuantity());
            }
        }

        // Dar baixa no estoque
        for (MaterialRequestItem item : items) {
            materialService.removeStock(item.getMaterial().getId(), item.getQuantity());
        }

        // Aprovar a requisição
        materialRequest.approve(approver);
        materialRequest = materialRequestRepository.save(materialRequest);

        return convertToDetailsDto(materialRequest);
    }

    /**
     * Rejeita uma solicitação de materiais.
     * 
     * Marca a solicitação como rejeitada com motivo da rejeição,
     * sem afetar o estoque de materiais.
     * 
     * @param id ID da solicitação a ser rejeitada
     * @param approverId ID do usuário que está rejeitando
     * @param approvalDto dados da rejeição incluindo motivo
     * @return MaterialRequestDetailsDto solicitação rejeitada
     * @throws RuntimeException se solicitação não estiver pendente ou usuário não encontrado
     */
    public MaterialRequestDetailsDto rejectMaterialRequest(Long id, Long approverId, MaterialRequestApprovalDto approvalDto) {
        MaterialRequest materialRequest = materialRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisição não encontrada"));

        if (materialRequest.getStatus() != RequestStatus.PENDENTE) {
            throw new RuntimeException("Requisição não está pendente");
        }

        // Obter usuário que está rejeitando
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Rejeitar a requisição
        materialRequest.reject(approver, approvalDto.getRejectionReason());
        materialRequest = materialRequestRepository.save(materialRequest);

        return convertToDetailsDto(materialRequest);
    }

    /**
     * Converte entidade MaterialRequest para MaterialRequestDetailsDto.
     * 
     * Inclui todas as informações da solicitação, dados do solicitante,
     * projeto, status e itens solicitados com detalhes completos.
     * 
     * @param materialRequest entidade da solicitação
     * @return MaterialRequestDetailsDto dados detalhados para API
     */
    private MaterialRequestDetailsDto convertToDetailsDto(MaterialRequest materialRequest) {
        MaterialRequestDetailsDto dto = new MaterialRequestDetailsDto();
        dto.setId(materialRequest.getId());
        dto.setProjectId(materialRequest.getProject().getId());
        dto.setProjectName(materialRequest.getProject().getName());
        dto.setRequesterId(materialRequest.getRequester().getId());
        dto.setRequesterName(materialRequest.getRequester().getFullName());
        dto.setRequestDate(materialRequest.getRequestDate());
        dto.setNeededDate(materialRequest.getNeededDate());
        dto.setStatus(materialRequest.getStatus());
        dto.setStatusDescription(materialRequest.getStatus().getDescription());
        dto.setRejectionReason(materialRequest.getRejectionReason());
        
        if (materialRequest.getApprovedBy() != null) {
            dto.setApprovedById(materialRequest.getApprovedBy().getId());
            dto.setApprovedByName(materialRequest.getApprovedBy().getFullName());
        }
        
        dto.setApprovedAt(materialRequest.getApprovedAt());
        dto.setObservations(materialRequest.getObservations());
        dto.setCreatedAt(materialRequest.getCreatedAt());
        dto.setUpdatedAt(materialRequest.getUpdatedAt());

        // Converter itens
        List<MaterialRequestItemDto> itemDtos = materialRequest.getItems().stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList());
        dto.setItems(itemDtos);

        // Calcular valor total
        BigDecimal totalAmount = itemDtos.stream()
                .map(MaterialRequestItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalAmount(totalAmount);

        return dto;
    }

    private MaterialRequestSummaryDto convertToSummaryDto(MaterialRequest materialRequest) {
        MaterialRequestSummaryDto dto = new MaterialRequestSummaryDto();
        dto.setId(materialRequest.getId());
        dto.setProjectName(materialRequest.getProject().getName());
        dto.setRequesterName(materialRequest.getRequester().getFullName());
        dto.setRequestDate(materialRequest.getRequestDate());
        dto.setNeededDate(materialRequest.getNeededDate());
        dto.setStatus(materialRequest.getStatus());
        dto.setStatusDescription(materialRequest.getStatus().getDescription());
        dto.setItemCount(materialRequest.getItems().size());
        dto.setCreatedAt(materialRequest.getCreatedAt());

        // Calcular valor total se há itens
        if (!materialRequest.getItems().isEmpty()) {
            BigDecimal totalAmount = materialRequest.getItems().stream()
                    .map(item -> item.getUnitPrice().multiply(item.getQuantity()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setTotalAmount(totalAmount);
        } else {
            dto.setTotalAmount(BigDecimal.ZERO);
        }

        return dto;
    }

    private MaterialRequestItemDto convertToItemDto(MaterialRequestItem item) {
        MaterialRequestItemDto dto = new MaterialRequestItemDto();
        dto.setId(item.getId());
        dto.setMaterialId(item.getMaterial().getId());
        dto.setMaterialName(item.getMaterial().getName());
        dto.setMaterialUnitOfMeasure(item.getMaterial().getUnitOfMeasure());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getTotalPrice());
        dto.setObservations(item.getObservations());
        return dto;
    }
} 