package com.teamflow.ai.identity.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * The HR record for a person. Distinct from {@link User}, which is their login.
 *
 * <p>Separating the two matters: a contractor may have an employee record with no
 * login, and the AI service reasons about employees (skills, capacity, leave)
 * without caring about credentials.
 *
 * <p>{@code skills} and {@code weeklyCapacityHours} exist here specifically to feed
 * the smart task-assignment scorer.
 */
@Entity
@Table(name = "employees",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_employees_code", columnNames = "employee_code"),
                @UniqueConstraint(name = "uk_employees_work_email", columnNames = "work_email")
        },
        indexes = {
                @Index(name = "idx_employees_department", columnList = "department_id"),
                @Index(name = "idx_employees_manager", columnList = "manager_id"),
                @Index(name = "idx_employees_deleted", columnList = "deleted")
        })
@Getter
@Setter
@NoArgsConstructor
public class Employee extends AuditableEntity {

    @NotBlank(message = "Employee code is required")
    @Size(max = 30)
    @Column(name = "employee_code", nullable = false, length = 30)
    private String employeeCode;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Work email is required")
    @Email(message = "Work email must be a valid email address")
    @Size(max = 150)
    @Column(name = "work_email", nullable = false, unique = true, length = 150)
    private String workEmail;

    @jakarta.validation.constraints.Pattern(regexp = "^$|^[+]?[0-9 ()-]{7,20}$",
            message = "Phone must be a valid phone number")
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 100)
    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "date_of_joining")
    private LocalDate dateOfJoining;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_employees_department"))
    private Department department;

    @jakarta.persistence.OneToMany(mappedBy = "employee", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeTeamMembership> teams = new java.util.LinkedHashSet<>();

    /** Reporting manager; self-reference kept as a plain id to avoid recursive fetch joins. */
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "manager_id", length = 36)
    private UUID managerId;

    /**
     * Technology skills, stored in a side table.
     *
     * <p>An {@code @ElementCollection} rather than a comma-joined string, because the
     * assignment scorer must intersect skill sets in SQL, which a delimited column
     * cannot do without a full scan.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "employee_skills",
            joinColumns = @JoinColumn(name = "employee_id"),
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_employee_skills_employee"))
    @Column(name = "skill", length = 60, nullable = false)
    private Set<String> skills = new LinkedHashSet<>();

    /** Contracted hours per week; the denominator in every utilisation calculation. */
    @Min(value = 1, message = "Weekly capacity must be at least 1 hour")
    @Max(value = 80, message = "Weekly capacity must not exceed 80 hours")
    @Column(name = "weekly_capacity_hours", nullable = false)
    private Integer weeklyCapacityHours = 40;

    /** Years of professional experience, used to weight complex-task suitability. */
    @Min(0)
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "annual_leave_balance", nullable = false)
    private Integer annualLeaveBalance = 24;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
