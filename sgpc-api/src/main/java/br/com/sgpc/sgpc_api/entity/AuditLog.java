package br.com.sgpc.sgpc_api.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "operation", nullable = false, length = 20)
    private String operation;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "user_ip", length = 45)
    private String userIp;

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // Constructor para facilitar a criação de logs
    public AuditLog(String entityType, Long entityId, String operation, User user, String userIp, 
                   String oldValues, String newValues) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.operation = operation;
        this.user = user;
        this.userEmail = user != null ? user.getEmail() : null;
        this.userIp = userIp;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.timestamp = LocalDateTime.now();
    }
} 