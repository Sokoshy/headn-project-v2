package com.bibliotheque.repository;

import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmpruntRepository extends JpaRepository<Emprunt, Long> {

    List<Emprunt> findByUtilisateur(Utilisateur utilisateur);

    List<Emprunt> findByLivre(Livre livre);

    List<Emprunt> findByDateRetourIsNull();

    List<Emprunt> findByDateRetourIsNotNull();

    List<Emprunt> findByUtilisateurOrderByDateEmpruntDesc(Utilisateur utilisateur);

    List<Emprunt> findByUtilisateurAndDateRetourIsNull(Utilisateur utilisateur);

    List<Emprunt> findByLivreAndDateRetourIsNull(Livre livre);

    boolean existsByLivreAndDateRetourIsNull(Livre livre);

    boolean existsByLivreIdAndDateRetourIsNull(Long livreId);

    boolean existsByLivre(Livre livre);

    boolean existsByUtilisateur(Utilisateur utilisateur);

    @Query("SELECT e FROM Emprunt e WHERE e.dateRetour IS NULL AND e.dateEmprunt < :date")
    List<Emprunt> findEmpruntsEnRetard(@Param("date") LocalDate date);

    @Query("SELECT e FROM Emprunt e JOIN FETCH e.utilisateur JOIN FETCH e.livre ORDER BY e.dateEmprunt DESC")
    List<Emprunt> findAllWithDetails();

    @Query("SELECT e FROM Emprunt e JOIN FETCH e.utilisateur JOIN FETCH e.livre WHERE e.dateRetour IS NULL ORDER BY e.dateEmprunt ASC")
    List<Emprunt> findEmpruntsActifs();

    @Query("SELECT e FROM Emprunt e JOIN FETCH e.utilisateur JOIN FETCH e.livre WHERE e.dateRetour IS NOT NULL ORDER BY e.dateRetour DESC")
    List<Emprunt> findHistorique();

    long countByDateRetourIsNull();

    @Query("SELECT COUNT(e) FROM Emprunt e WHERE e.dateRetour IS NULL AND e.dateEmprunt < :date")
    long countEmpruntsEnRetard(@Param("date") LocalDate date);
}
