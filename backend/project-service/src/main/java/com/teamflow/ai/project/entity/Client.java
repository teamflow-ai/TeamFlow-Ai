package com.teamflow.ai.project.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** An external client a project is delivered for. */
@Entity
@Table(name = "clients",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_clients_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_clients_deleted", columnList = "deleted")
        })
@Getter
@Setter
@NoArgsConstructor
public class Client extends AuditableEntity {

    @NotBlank(message = "Client name is required")
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Client code is required")
    @Size(max = 30)
    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Size(max = 100)
    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Email(message = "Email must be a valid email address")
    @Size(max = 150)
    @Column(name = "email", length = 150)
    private String email;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 100)
    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
