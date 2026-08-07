package com.teamflow.ai.identity.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A named bundle of {@link Permission}s assigned to users.
 *
 * <p>Permissions are fetched eagerly: the set is small, bounded, and required on
 * every login to build the token's permission claim, so lazy loading would only add
 * a second query behind an open-session workaround.
 */
@Entity
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_roles_name", columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
public class Role extends AuditableEntity {

    @NotBlank(message = "Role name is required")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{2,49}$",
            message = "Role name must be uppercase letters, digits and underscores")
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Column(name = "description", length = 255)
    private String description;

    /**
     * System roles are seeded by migration and may not be deleted or renamed,
     * because {@code @PreAuthorize} expressions reference them by name.
     */
    @Column(name = "system_role", nullable = false)
    private boolean systemRole = false;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_role_permissions", columnNames = {"role_id", "permission_id"}))
    private Set<Permission> permissions = new LinkedHashSet<>();

    public Role(String name, String description, boolean systemRole) {
        this.name = name;
        this.description = description;
        this.systemRole = systemRole;
    }

    public void addPermission(Permission permission) {
        permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }
}
