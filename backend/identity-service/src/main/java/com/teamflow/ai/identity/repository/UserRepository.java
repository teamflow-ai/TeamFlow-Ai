package com.teamflow.ai.identity.repository;

import com.teamflow.ai.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Loads a user with role and permissions in a single statement.
     *
     * <p>The join fetch is deliberate: login needs the permission list to build the
     * token, and lazily walking {@code user -> role -> permissions} would issue
     * three queries per authentication.
     */
    @Query("""
            select u from User u
            join fetch u.role r
            left join fetch r.permissions
            where lower(u.email) = lower(:email) and u.deleted = false
            """)
    Optional<User> findActiveByEmailWithRole(@Param("email") String email);

    @Query("""
            select u from User u
            join fetch u.role r
            left join fetch r.permissions
            where u.id = :id and u.deleted = false
            """)
    Optional<User> findActiveByIdWithRole(@Param("id") UUID id);

    boolean existsByEmailIgnoreCaseAndDeletedFalse(String email);

    Optional<User> findByIdAndDeletedFalse(UUID id);
}
