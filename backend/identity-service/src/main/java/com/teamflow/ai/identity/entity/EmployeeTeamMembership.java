package com.teamflow.ai.identity.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employee_teams",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_employee_team", columnNames = {"employee_id", "team_id"})
        })
@Getter
@Setter
@NoArgsConstructor
public class EmployeeTeamMembership extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, 
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_employee_teams_employee"))
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_employee_teams_team"))
    private Team team;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;
    
    public EmployeeTeamMembership(Employee employee, Team team, boolean primary) {
        this.employee = employee;
        this.team = team;
        this.primary = primary;
    }
}
