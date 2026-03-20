package com.bibliotheque.repository;

import com.bibliotheque.model.Livre;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LivreRepository extends JpaRepository<Livre, Long> {

    List<Livre> findByTitreContainingIgnoreCase(String titre);

    List<Livre> findByAuteurContainingIgnoreCase(String auteur);

    @Query("""
            SELECT l FROM Livre l
            WHERE NOT EXISTS (
                SELECT 1 FROM Emprunt e
                WHERE e.livre = l AND e.dateRetour IS NULL
            )
            """)
    List<Livre> findDisponibles();

    List<Livre> findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCase(String titre, String auteur);

    boolean existsByTitreAndAuteur(String titre, String auteur);

    @Query("""
            SELECT COUNT(l) FROM Livre l
            WHERE NOT EXISTS (
                SELECT 1 FROM Emprunt e
                WHERE e.livre = l AND e.dateRetour IS NULL
            )
            """)
    long countDisponibles();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Livre l WHERE l.id = :id")
    Optional<Livre> findByIdForUpdate(@Param("id") Long id);
}
