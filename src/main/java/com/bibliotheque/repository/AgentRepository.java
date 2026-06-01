package com.bibliotheque.repository;

import com.bibliotheque.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    Optional<Agent> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Agent> findByActifTrue();

    long countByActifTrue();
}