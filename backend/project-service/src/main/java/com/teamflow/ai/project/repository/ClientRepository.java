package com.teamflow.ai.project.repository;

import com.teamflow.ai.project.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByIdAndDeletedFalse(UUID id);

    Page<Client> findAllByDeletedFalse(Pageable pageable);

    boolean existsByCodeIgnoreCaseAndDeletedFalse(String code);

    List<Client> findTop5ByNameContainingIgnoreCaseAndDeletedFalse(String keyword);
}
