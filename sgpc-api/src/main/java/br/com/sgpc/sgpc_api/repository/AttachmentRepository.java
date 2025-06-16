package br.com.sgpc.sgpc_api.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sgpc.sgpc_api.entity.Attachment;

/**
 * Repository para operações de acesso a dados da entidade Attachment.
 * 
 * Esta interface estende JpaRepository fornecendo operações CRUD básicas
 * e inclui queries personalizadas específicas para o gerenciamento de
 * anexos no sistema SGPC. Suporta anexos em múltiplas entidades.
 * 
 * Funcionalidades principais:
 * - Busca por entidade e ID (polimórfico)
 * - Filtros por usuário que fez upload
 * - Busca por tipo de arquivo
 * - Paginação para grandes volumes
 * - Contagem e cálculo de tamanho
 * - Busca por nome de arquivo
 * - Validação de duplicatas
 * 
 * Características especiais:
 * - Suporte polimórfico (entityType + entityId)
 * - Busca case-insensitive para nomes
 * - Ordenação por data de criação
 * - Cálculos de tamanho agregados
 * - Validação de caminhos de arquivo
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    /**
     * Busca anexos por tipo de entidade e ID específico.
     * 
     * Método principal para recuperar anexos de qualquer entidade
     * do sistema. Usado para exibir anexos em projetos, tarefas, etc.
     * 
     * @param entityType tipo da entidade (ex: "Project", "Task")
     * @param entityId ID específico da entidade
     * @return List<Attachment> anexos ordenados por data de criação decrescente
     */
    List<Attachment> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    /**
     * Busca anexos por usuário que fez upload.
     * 
     * Permite rastrear todos os arquivos enviados por um usuário
     * específico. Útil para análise de uso e controle de quota.
     * 
     * @param uploadedById ID do usuário que fez upload
     * @return List<Attachment> anexos do usuário ordenados por data decrescente
     */
    List<Attachment> findByUploadedByIdOrderByCreatedAtDesc(Long uploadedById);

    /**
     * Busca anexos por tipo de arquivo (prefixo do contentType).
     * 
     * Filtra anexos por categoria de arquivo usando o prefixo do
     * Content-Type. Ex: "image/" para imagens, "application/pdf" para PDFs.
     * 
     * @param contentTypePrefix prefixo do tipo de conteúdo
     * @return List<Attachment> anexos do tipo especificado
     */
    List<Attachment> findByContentTypeStartingWithOrderByCreatedAtDesc(String contentTypePrefix);

    /**
     * Busca anexos com paginação por entidade e ID.
     * 
     * Versão paginada da busca por entidade específica.
     * Essencial para entidades com muitos anexos.
     * 
     * @param entityType tipo da entidade
     * @param entityId ID da entidade
     * @param pageable configuração de paginação
     * @return Page<Attachment> página de anexos
     */
    Page<Attachment> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    /**
     * Conta o número de anexos por entidade.
     * 
     * Usado para exibir contadores de anexos em listagens
     * e validar limites de anexos por entidade.
     * 
     * @param entityType tipo da entidade
     * @param entityId ID da entidade
     * @return long número de anexos da entidade
     */
    long countByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Calcula o tamanho total de arquivos por entidade.
     * 
     * Soma o tamanho de todos os arquivos anexados a uma entidade.
     * Usado para controle de quota e análise de uso de espaço.
     * 
     * @param entityType tipo da entidade
     * @param entityId ID da entidade
     * @return Long tamanho total em bytes (null se não há anexos)
     */
    @Query("SELECT SUM(a.fileSize) FROM Attachment a WHERE a.entityType = :entityType AND a.entityId = :entityId")
    Long sumFileSizeByEntityTypeAndEntityId(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    /**
     * Busca anexos por nome de arquivo original (busca parcial).
     * 
     * Permite buscar arquivos pelo nome original, útil para
     * encontrar arquivos específicos entre muitos anexos.
     * 
     * @param filename parte do nome do arquivo original
     * @return List<Attachment> anexos com nomes correspondentes
     */
    List<Attachment> findByOriginalFilenameContainingIgnoreCaseOrderByCreatedAtDesc(String filename);

    /**
     * Verifica se existe arquivo com caminho específico.
     * 
     * Usado para validar se um arquivo físico já existe no sistema
     * antes de fazer upload, evitando duplicatas no filesystem.
     * 
     * @param filePath caminho completo do arquivo no sistema
     * @return boolean true se existe arquivo com esse caminho
     */
    boolean existsByFilePath(String filePath);
}