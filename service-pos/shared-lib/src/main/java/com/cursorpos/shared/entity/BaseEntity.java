package com.cursorpos.shared.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Base entity class for all database entities.
 * Provides common fields for auditing, tenant isolation, and soft delete.
 * 
 * <p>All entities should extend this class to ensure consistent
 * audit trails and tenant data isolation.</p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key using UUID for global uniqueness across distributed systems.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Tenant identifier for multi-tenant data isolation.
     * All queries must filter by this column.
     */
    @Column(name = "tenant_id", nullable = false, updatable = false, length = 36)
    private String tenantId;

    /**
     * Timestamp when the entity was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * User who created the entity (user ID).
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 36)
    private String createdBy;

    /**
     * Timestamp when the entity was last updated.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * User who last updated the entity (user ID).
     */
    @LastModifiedBy
    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    /**
     * Soft delete timestamp. If set, the entity is considered deleted.
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * Version number for optimistic locking.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Checks if the entity is soft-deleted.
     * 
     * @return true if deleted_at is set, false otherwise
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Soft deletes the entity by setting deleted_at to current timestamp.
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    /**
     * Restores a soft-deleted entity.
     */
    public void restore() {
        this.deletedAt = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
