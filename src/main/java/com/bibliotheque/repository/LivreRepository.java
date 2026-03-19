package com.bibliotheque.repository;

import com.bibliotheque.model.Livre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivreRepository extends JpaRepository<Livre, Long> {

    List<Livre> findByTitreContainingIgnoreCase(String titre);

    List<Livre> findByAuteurContainingIgnoreCase(String auteur);

    List<Livre> findByDisponibleTrue();

    List<Livre> findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCase(String titre, String auteur);

    boolean existsByTitreAndAuteur(String titre, String auteur);
}
