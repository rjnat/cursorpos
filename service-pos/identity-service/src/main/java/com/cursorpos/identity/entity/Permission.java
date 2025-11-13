package com.cursorpos.identity.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Permission entity for fine-grained access control.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Entity
@Table(name = "permissions", indexes = {
        @Index(name = "idx_permissions_tenant_code", columnList = "tenant_id,code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission extends BaseEntity {

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "resource", nullable = false, length = 50)
    private String resource;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;
}
