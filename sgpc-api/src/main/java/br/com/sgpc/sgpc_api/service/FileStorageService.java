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

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.max-size:10485760}")
    private long maxFileSize; // 10MB por padrão

    private static final String[] ALLOWED_EXTENSIONS = {
        ".jpg", ".jpeg", ".png", ".gif", ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".txt"
    };

    /**
     * Faz upload de um arquivo e salva metadados no banco
     */
    public Attachment storeFile(MultipartFile file, String entityType, Long entityId, User uploadedBy) throws IOException {
        // Validações
        validateFile(file);
        
        // Cria estrutura de diretórios se não existir
        createDirectoryStructure();
        
        // Gera nome único para o arquivo
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
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
     * Carrega um arquivo como Resource
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
     * Remove um arquivo do sistema e do banco
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
     * Lista anexos por entidade
     */
    public List<Attachment> getAttachmentsByEntity(String entityType, Long entityId) {
        return attachmentRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    /**
     * Obtém informações de um anexo específico
     */
    public Attachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("Anexo não encontrado com ID: " + attachmentId));
    }

    /**
     * Valida o arquivo antes do upload
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode estar vazio");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Arquivo muito grande. Tamanho máximo: " + 
                (maxFileSize / 1024 / 1024) + "MB");
        }
        
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
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
     * Cria estrutura de diretórios
     */
    private void createDirectoryStructure() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            logger.info("Diretório de upload criado: {}", uploadDir);
        }
    }

    /**
     * Gera nome único para o arquivo
     */
    private String generateUniqueFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid;
    }

    /**
     * Gera subdiretório baseado no tipo e ID da entidade
     */
    private String generateSubDirectory(String entityType, Long entityId) {
        return entityType.toLowerCase() + "/" + entityId;
    }

    /**
     * Obtém extensão do arquivo
     */
    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }
} 