package com.teamflow.ai.auth.dto.response;

import com.teamflow.ai.auth.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response payload representing a user's public-facing profile information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String employeeId;

    private String designation;

    private String profileImage;

    private Boolean isActive;

    private Boolean isEmailVerified;

    private LocalDateTime lastLogin;

    private Role role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
