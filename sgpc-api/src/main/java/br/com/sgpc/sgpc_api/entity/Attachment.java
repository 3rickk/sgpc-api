package br.com.sgpc.sgpc_api.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa anexos de arquivos no sistema.
 * 
 * Esta entidade armazena metadados de arquivos anexados a outras entidades
 * do sistema (projetos, tarefas, etc.). Os arquivos físicos são armazenados
 * no sistema de arquivos, e esta entidade mantém as informações necessárias
 * para localizá-los e gerenciá-los.
 * 
 * Funcionalidades:
 * - Vinculação genérica a qualquer entidade via entityType/entityId
 * - Controle de versioning automático (createdAt/updatedAt)
 * - Rastreabilidade de quem fez o upload
 * - Metadados completos do arquivo (tamanho, tipo MIME, nomes)
 * 
 * Estrutura de armazenamento:
 * - filename: nome único gerado pelo sistema
 * - originalFilename: nome original do arquivo
 * - filePath: caminho relativo no sistema de arquivos
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    /**
     * Identificador único do anexo.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome único do arquivo no sistema de arquivos.
     * 
     * Gerado automaticamente pelo FileStorageService para evitar
     * conflitos e problemas com caracteres especiais.
     * Formato: timestamp_uuid.extensao
     */
    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    /**
     * Nome original do arquivo quando foi feito o upload.
     * 
     * Preserva o nome que o usuário deu ao arquivo,
     * usado para exibição e download.
     */
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    /**
     * Caminho relativo do arquivo no sistema de arquivos.
     * 
     * Formato: entityType/entityId/filename
     * Exemplo: "project/123/20240315143025_a1b2c3d4.pdf"
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * Tipo MIME do arquivo.
     * 
     * Exemplos: "application/pdf", "image/jpeg", "text/plain"
     * Usado para configurar headers HTTP corretos no download.
     */
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    /**
     * Tamanho do arquivo em bytes.
     * 
     * Usado para validações e informações ao usuário.
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * Tipo da entidade à qual o arquivo está anexado.
     * 
     * Exemplos: "Project", "Task", "MaterialRequest"
     * Permite vincular anexos a diferentes tipos de entidades.
     */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /**
     * ID da entidade à qual o arquivo está anexado.
     * 
     * Junto com entityType, forma a chave para localizar
     * todos os anexos de uma entidade específica.
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * Usuário que fez o upload do arquivo.
     * 
     * Relacionamento many-to-one para rastreabilidade
     * e controle de permissões.
     */
    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    /**
     * Data e hora de criação do registro.
     * 
     * Preenchida automaticamente pelo Hibernate.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Data e hora da última atualização.
     * 
     * Atualizada automaticamente pelo Hibernate em modificações.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Callback executado antes da persistência inicial.
     * 
     * Garante que as timestamps sejam definidas mesmo se
     * as anotações @CreationTimestamp/@UpdateTimestamp falharem.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Callback executado antes de cada atualização.
     * 
     * Atualiza o timestamp de modificação.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Construtor de conveniência para criação de anexos.
     * 
     * Usado pelo FileStorageService para criar novos anexos
     * com todos os dados necessários.
     * 
     * @param filename nome único do arquivo gerado pelo sistema
     * @param originalFilename nome original do arquivo
     * @param filePath caminho relativo no sistema de arquivos
     * @param contentType tipo MIME do arquivo
     * @param fileSize tamanho em bytes
     * @param entityType tipo da entidade vinculada
     * @param entityId ID da entidade vinculada
     * @param uploadedBy usuário que fez o upload
     */
    public Attachment(String filename, String originalFilename, String filePath, 
                     String contentType, Long fileSize, String entityType, 
                     Long entityId, User uploadedBy) {
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.filePath = filePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.entityType = entityType;
        this.entityId = entityId;
        this.uploadedBy = uploadedBy;
    }
} 