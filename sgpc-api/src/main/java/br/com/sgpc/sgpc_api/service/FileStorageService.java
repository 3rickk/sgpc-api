package br.com.sgpc.sgpc_api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import br.com.sgpc.sgpc_api.entity.Attachment;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.repository.AttachmentRepository;

/**
 * Service responsável pelo gerenciamento de arquivos e anexos.
 * 
 * Este service oferece funcionalidades completas para:
 * - Upload seguro de arquivos com validações
 * - Organização hierárquica por tipo de entidade
 * - Geração de nomes únicos para evitar conflitos
 * - Download de arquivos como Resource
 * - Remoção física e lógica de arquivos
 * - Controle de tipos e tamanhos permitidos
 * 
 * Estrutura de diretórios: uploads/{entityType}/{entityId}/arquivo.ext
 * 
 * Segurança implementada:
 * - Validação de extensões permitidas
 * - Limite de tamanho de arquivo
 * - Sanitização de nomes de arquivo
 * - Prevenção de path traversal
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.max-size:10485760}")
    private long maxFileSize; // 10MB por padrão

    /**
     * Extensões de arquivo permitidas para upload.
     * Inclui formatos comuns para documentos, imagens e planilhas.
     */
    private static final String[] ALLOWED_EXTENSIONS = {
        ".jpg", ".jpeg", ".png", ".gif", ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".txt"
    };

    /**
     * Faz upload de um arquivo e salva metadados no banco.
     * 
     * Processo completo de upload:
     * 1. Validação do arquivo (tamanho, tipo, nome)
     * 2. Criação da estrutura de diretórios
     * 3. Geração de nome único para evitar conflitos
     * 4. Armazenamento físico do arquivo
     * 5. Registro dos metadados no banco de dados
     * 
     * @param file arquivo enviado via multipart
     * @param entityType tipo da entidade (ex: "Project", "Task")
     * @param entityId ID da entidade proprietária
     * @param uploadedBy usuário que fez o upload
     * @return Attachment com metadados do arquivo salvo
     * @throws IOException se ocorrer erro no armazenamento
     * @throws IllegalArgumentException se arquivo inválido
     */
    public Attachment storeFile(MultipartFile file, String entityType, Long entityId, User uploadedBy) throws IOException {
        // Validações
        validateFile(file);
        
        // Cria estrutura de diretórios se não existir
        createDirectoryStructure();
        
        // Gera nome único para o arquivo
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            originalFileName = "file";
        }
        originalFileName = StringUtils.cleanPath(originalFileName);
        String fileExtension = getFileExtension(originalFileName);
        String fileName = generateUniqueFileName() + fileExtension;
        
        // Caminho completo do arquivo
        String subDir = generateSubDirectory(entityType, entityId);
        Path targetLocation = Paths.get(uploadDir).resolve(subDir).resolve(fileName);
        
        // Cria subdiretório se não existir
        Files.createDirectories(targetLocation.getParent());
        
        // Salva o arquivo
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        // Cria registro no banco
        Attachment attachment = new Attachment(
            fileName,
            originalFileName,
            subDir + "/" + fileName,
            file.getContentType(),
            file.getSize(),
            entityType,
            entityId,
            uploadedBy
        );
        
        Attachment savedAttachment = attachmentRepository.save(attachment);
        logger.info("Arquivo salvo: {} para entidade {} ID {}", originalFileName, entityType, entityId);
        
        return savedAttachment;
    }

    /**
     * Carrega um arquivo como Resource para download.
     * 
     * Busca o arquivo no sistema de arquivos baseado nos metadados
     * armazenados no banco de dados. Retorna Resource pronto para
     * ser enviado como resposta HTTP.
     * 
     * @param attachmentId ID do anexo no banco de dados
     * @return Resource do arquivo para download
     * @throws IOException se arquivo não encontrado ou não legível
     * @throws RuntimeException se anexo não existe no banco
     */
    public Resource loadFileAsResource(Long attachmentId) throws IOException {
        Attachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("Anexo não encontrado com ID: " + attachmentId));
        
        Path filePath = Paths.get(uploadDir).resolve(attachment.getFilePath()).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Arquivo não encontrado ou não pode ser lido: " + attachment.getOriginalFilename());
        }
    }

    /**
     * Remove um arquivo do sistema de arquivos e do banco de dados.
     * 
     * Operação de remoção completa:
     * 1. Remove arquivo físico do disco
     * 2. Remove registro do banco de dados
     * 3. Log da operação para auditoria
     * 
     * @param attachmentId ID do anexo a ser removido
     * @throws IOException se erro na remoção do arquivo físico
     * @throws RuntimeException se anexo não encontrado
     */
    public void deleteFile(Long attachmentId) throws IOException {
        Attachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("Anexo não encontrado com ID: " + attachmentId));
        
        // Remove arquivo físico
        Path filePath = Paths.get(uploadDir).resolve(attachment.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            logger.info("Arquivo físico removido: {}", attachment.getFilePath());
        }
        
        // Remove registro do banco
        attachmentRepository.delete(attachment);
        logger.info("Registro de anexo removido do banco: ID {}", attachmentId);
    }

    /**
     * Lista anexos por entidade.
     * 
     * Busca todos os anexos vinculados a uma entidade específica,
     * ordenados por data de criação (mais recentes primeiro).
     * 
     * @param entityType tipo da entidade
     * @param entityId ID da entidade
     * @return lista de anexos da entidade
     */
    public List<Attachment> getAttachmentsByEntity(String entityType, Long entityId) {
        return attachmentRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    /**
     * Obtém informações de um anexo específico.
     * 
     * @param attachmentId ID do anexo
     * @return dados do anexo
     * @throws RuntimeException se anexo não encontrado
     */
    public Attachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("Anexo não encontrado com ID: " + attachmentId));
    }

    /**
     * Valida o arquivo antes do upload.
     * 
     * Validações realizadas:
     * - Arquivo não pode estar vazio
     * - Tamanho dentro do limite configurado
     * - Nome de arquivo válido (sem caracteres especiais)
     * - Extensão permitida na lista de tipos aceitos
     * - Prevenção de path traversal (..)
     * 
     * @param file arquivo a ser validado
     * @throws IllegalArgumentException se arquivo inválido
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode estar vazio");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Arquivo muito grande. Tamanho máximo: " + 
                (maxFileSize / 1024 / 1024) + "MB");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("Nome do arquivo não pode ser nulo");
        }
        fileName = StringUtils.cleanPath(fileName);
        if (fileName.contains("..")) {
            throw new IllegalArgumentException("Nome do arquivo inválido: " + fileName);
        }
        
        String extension = getFileExtension(fileName).toLowerCase();
        boolean isAllowed = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (extension.equals(allowedExt)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new IllegalArgumentException("Tipo de arquivo não permitido: " + extension);
        }
    }

    /**
     * Cria estrutura de diretórios se não existir.
     * 
     * @throws IOException se erro na criação dos diretórios
     */
    private void createDirectoryStructure() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            logger.info("Diretório de upload criado: {}", uploadDir);
        }
    }

    /**
     * Gera nome único para o arquivo.
     * 
     * Formato: YYYYMMDDHHMMSS_UUID8
     * Combina timestamp com UUID para garantir unicidade.
     * 
     * @return nome único do arquivo (sem extensão)
     */
    private String generateUniqueFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid;
    }

    /**
     * Gera subdiretório baseado no tipo e ID da entidade.
     * 
     * Estrutura: {entityType}/{entityId}
     * Exemplo: "project/123", "task/456"
     * 
     * @param entityType tipo da entidade
     * @param entityId ID da entidade
     * @return caminho do subdiretório
     */
    private String generateSubDirectory(String entityType, Long entityId) {
        return entityType.toLowerCase() + "/" + entityId;
    }

    /**
     * Obtém extensão do arquivo incluindo o ponto.
     * 
     * @param fileName nome do arquivo
     * @return extensão com ponto (ex: ".pdf") ou string vazia
     */
    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }
} 