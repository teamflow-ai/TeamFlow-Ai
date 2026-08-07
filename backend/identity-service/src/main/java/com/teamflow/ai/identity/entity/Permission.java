package com.teamflow.ai.identity.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single grantable capability, e.g. {@code CREATE_PROJECT}.
 *
 * <p>Permissions are rows rather than enum constants so an administrator can
 * re-map what a role may do without a redeploy, which is what the platform's
 * "permissions must be configurable" requirement demands.
 */
@Entity
@Table(name = "permissions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_permissions_name", columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
public class Permission extends AuditableEntity {

    @NotBlank(message = "Permission name is required")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{2,49}$",
            message = "Permission name must be uppercase letters, digits and underscores")
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Column(name = "description", length = 255)
    private String description;

    /** Grouping label used by the admin UI, e.g. {@code PROJECT} or {@code HR}. */
    @Size(max = 50)
    @Column(name = "category", length = 50)
    private String category;

    public Permission(String name, String description, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }
}
