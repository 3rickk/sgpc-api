package br.com.sgpc.sgpc_api.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.Attachment;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    // Buscar por tipo de entidade e ID
    List<Attachment> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    // Buscar por usuário que fez upload
    List<Attachment> findByUploadedByIdOrderByCreatedAtDesc(Long uploadedById);

    // Buscar por tipo de arquivo
    List<Attachment> findByContentTypeStartingWithOrderByCreatedAtDesc(String contentTypePrefix);

    // Buscar com paginação
    Page<Attachment> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    // Contar anexos por entidade
    long countByEntityTypeAndEntityId(String entityType, Long entityId);

    // Calcular tamanho total dos arquivos por entidade
    @Query("SELECT SUM(a.fileSize) FROM Attachment a WHERE a.entityType = :entityType AND a.entityId = :entityId")
    Long sumFileSizeByEntityTypeAndEntityId(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    // Buscar por nome de arquivo original
    List<Attachment> findByOriginalFilenameContainingIgnoreCaseOrderByCreatedAtDesc(String filename);

    // Verificar se existe arquivo com caminho específico
    boolean existsByFilePath(String filePath);
} 