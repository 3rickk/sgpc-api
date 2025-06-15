package br.com.sgpc.sgpc_api.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.sgpc.sgpc_api.dto.ProjectCreateDto;
import br.com.sgpc.sgpc_api.dto.ProjectDetailsDto;
import br.com.sgpc.sgpc_api.dto.ProjectSummaryDto;
import br.com.sgpc.sgpc_api.dto.ProjectUpdateDto;
import br.com.sgpc.sgpc_api.dto.UserDto;
import br.com.sgpc.sgpc_api.entity.Attachment;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.enums.ProjectStatus;
import br.com.sgpc.sgpc_api.repository.UserRepository;
import br.com.sgpc.sgpc_api.service.FileStorageService;
import br.com.sgpc.sgpc_api.service.ProjectService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ProjectDetailsDto> createProject(@Valid @RequestBody ProjectCreateDto projectCreateDto) {
        try {
            ProjectDetailsDto createdProject = projectService.createProject(projectCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar projeto: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ProjectSummaryDto>> getAllProjects() {
        List<ProjectSummaryDto> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailsDto> getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id)
                .map(project -> ResponseEntity.ok(project))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDetailsDto> updateProject(@PathVariable Long id, 
                                                          @Valid @RequestBody ProjectUpdateDto projectUpdateDto) {
        try {
            ProjectDetailsDto updatedProject = projectService.updateProject(id, projectUpdateDto);
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar projeto: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar projeto: " + e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProjectSummaryDto>> getProjectsByStatus(@PathVariable String status) {
        try {
            ProjectStatus projectStatus = ProjectStatus.fromString(status);
            List<ProjectSummaryDto> projects = projectService.getProjectsByStatus(projectStatus);
            return ResponseEntity.ok(projects);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/client/{client}")
    public ResponseEntity<List<ProjectSummaryDto>> getProjectsByClient(@PathVariable String client) {
        List<ProjectSummaryDto> projects = projectService.getProjectsByClient(client);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProjectSummaryDto>> searchProjects(@RequestParam String name) {
        List<ProjectSummaryDto> projects = projectService.searchProjectsByName(name);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProjectSummaryDto>> getProjectsByUser(@PathVariable Long userId) {
        List<ProjectSummaryDto> projects = projectService.getProjectsByUserId(userId);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/delayed")
    public ResponseEntity<List<ProjectSummaryDto>> getDelayedProjects() {
        List<ProjectSummaryDto> projects = projectService.getDelayedProjects();
        return ResponseEntity.ok(projects);
    }

    // Endpoints para gerenciamento de equipe
    @PostMapping("/{projectId}/team/{userId}")
    public ResponseEntity<ProjectDetailsDto> addTeamMember(@PathVariable Long projectId, 
                                                          @PathVariable Long userId) {
        try {
            ProjectDetailsDto project = projectService.addTeamMember(projectId, userId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao adicionar membro à equipe: " + e.getMessage());
        }
    }

    @DeleteMapping("/{projectId}/team/{userId}")
    public ResponseEntity<ProjectDetailsDto> removeTeamMember(@PathVariable Long projectId, 
                                                             @PathVariable Long userId) {
        try {
            ProjectDetailsDto project = projectService.removeTeamMember(projectId, userId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover membro da equipe: " + e.getMessage());
        }
    }

    @GetMapping("/{projectId}/team")
    public ResponseEntity<List<UserDto>> getProjectTeamMembers(@PathVariable Long projectId) {
        List<UserDto> teamMembers = projectService.getProjectTeamMembers(projectId);
        return ResponseEntity.ok(teamMembers);
    }

    // Endpoints para anexos
    @PostMapping("/{projectId}/attachments")
    public ResponseEntity<Attachment> uploadAttachment(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            // Verificar se o projeto existe
            projectService.getProjectById(projectId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

            // Obter usuário autenticado
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Fazer upload do arquivo
            Attachment attachment = fileStorageService.storeFile(file, "Project", projectId, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(attachment);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload do arquivo: " + e.getMessage());
        }
    }

    @GetMapping("/{projectId}/attachments")
    public ResponseEntity<List<Attachment>> getProjectAttachments(@PathVariable Long projectId) {
        // Verificar se o projeto existe
        projectService.getProjectById(projectId)
            .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        List<Attachment> attachments = fileStorageService.getAttachmentsByEntity("Project", projectId);
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        try {
            Attachment attachment = fileStorageService.getAttachment(attachmentId);
            Resource resource = fileStorageService.loadFileAsResource(attachmentId);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + attachment.getOriginalFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao baixar arquivo: " + e.getMessage());
        }
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId) {
        try {
            fileStorageService.deleteFile(attachmentId);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao deletar arquivo: " + e.getMessage());
        }
    }
} 