package com.teamflow.ai.identity.mapper;

import com.teamflow.ai.identity.dto.response.UserResponse;
import com.teamflow.ai.identity.entity.Employee;
import com.teamflow.ai.identity.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Builds the public user projection.
 *
 * <p>Hand-written rather than MapStruct-generated: the mapping flattens three
 * associations and must tolerate a null employee (platform admins with no HR
 * record), which is clearer as explicit code than as a stack of {@code @Mapping}
 * expressions.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        Employee employee = user.getEmployee();

        List<String> permissions = user.getRole().getPermissions().stream()
                .map(permission -> permission.getName())
                .sorted()
                .toList();

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(employee != null ? employee.getFirstName() : null)
                .lastName(employee != null ? employee.getLastName() : null)
                .fullName(employee != null ? employee.getFullName() : user.getEmail())
                .role(user.getRole().getName())
                .permissions(permissions)
                .employeeId(employee != null ? employee.getId() : null)
                .designation(employee != null ? employee.getDesignation() : null)
                .profileImageUrl(employee != null ? employee.getProfileImageUrl() : null)
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
