package br.com.sgpc.sgpc_api.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import br.com.sgpc.sgpc_api.dto.ErrorResponseDto;

/**
 * Tratador global de exceções para a API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                "Usuário não encontrado",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentialsException(
            InvalidCredentialsException ex, WebRequest request) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Credenciais inválidas");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<ErrorResponseDto> handleUserInactiveException(
            UserInactiveException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.UNAUTHORIZED.value(),
                "Conta inativa",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                "Email já cadastrado",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidTokenException(
            InvalidTokenException ex, WebRequest request) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Token de redefinição inválido ou expirado.");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<Map<String, String>> handleExpiredTokenException(
            ExpiredTokenException ex, WebRequest request) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Token de redefinição inválido ou expirado.");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Exceções para gerenciamento de custos e recursos

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleTaskNotFoundException(
            TaskNotFoundException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                "Tarefa não encontrada",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleProjectNotFoundException(
            ProjectNotFoundException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                "Projeto não encontrado",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleServiceNotFoundException(
            ServiceNotFoundException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                "Serviço não encontrado",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ServiceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleServiceAlreadyExistsException(
            ServiceAlreadyExistsException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                "Serviço já existe",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ServiceAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponseDto> handleServiceAlreadyAssignedException(
            ServiceAlreadyAssignedException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                "Serviço já atribuído",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ServiceNotAssignedToTaskException.class)
    public ResponseEntity<ErrorResponseDto> handleServiceNotAssignedToTaskException(
            ServiceNotAssignedToTaskException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                "Serviço não atribuído à tarefa",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DashboardDataException.class)
    public ResponseEntity<ErrorResponseDto> handleDashboardDataException(
            DashboardDataException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro no carregamento do dashboard",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Exceções para gerenciamento de materiais

    @ExceptionHandler(MaterialNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleMaterialNotFoundException(
            MaterialNotFoundException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                "Material não encontrado",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MaterialAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleMaterialAlreadyExistsException(
            MaterialAlreadyExistsException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                "Material já existe",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponseDto> handleInsufficientStockException(
            InsufficientStockException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Estoque insuficiente",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidMovementTypeException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidMovementTypeException(
            InvalidMovementTypeException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Tipo de movimentação inválido",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Exceções para gerenciamento de projetos

    @ExceptionHandler(ProjectAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleProjectAlreadyExistsException(
            ProjectAlreadyExistsException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                "Projeto já existe",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserAlreadyInTeamException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyInTeamException(
            UserAlreadyInTeamException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                "Usuário já está na equipe",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotInTeamException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotInTeamException(
            UserNotInTeamException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                "Usuário não está na equipe",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidProjectStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidProjectStatusException(
            InvalidProjectStatusException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Status de projeto inválido",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidDateException(
            InvalidDateException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Data inválida",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTaskStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidTaskStatusException(
            InvalidTaskStatusException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Status de tarefa inválido",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AttachmentNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleAttachmentNotFoundException(
            AttachmentNotFoundException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                "Anexo não encontrado",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Dados inválidos");
        
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Dados inválidos",
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Dados inválidos",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        String message = ex.getMessage();
        HttpStatus status;
        String error;
        
        if (message != null) {
            if (message.contains("não encontrada") || message.contains("não encontrado")) {
                status = HttpStatus.NOT_FOUND;
                error = "Recurso não encontrado";
            } else if (message.contains("não pode ser aprovada") || message.contains("não está pendente")) {
                status = HttpStatus.BAD_REQUEST;
                error = "Operação inválida";
            } else if (message.contains("Estoque insuficiente")) {
                status = HttpStatus.BAD_REQUEST;
                error = "Estoque insuficiente";
            } else {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                error = "Erro interno do servidor";
                message = "Ocorreu um erro inesperado. Tente novamente mais tarde";
            }
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            error = "Erro interno do servidor";
            message = "Ocorreu um erro inesperado. Tente novamente mais tarde";
        }
        
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                status.value(),
                error,
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(
            Exception ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor",
                "Ocorreu um erro inesperado. Tente novamente mais tarde",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 