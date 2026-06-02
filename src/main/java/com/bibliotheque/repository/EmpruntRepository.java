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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface EmpruntRepository extends JpaRepository<Emprunt, Long> {

    List<Emprunt> findByUtilisateurOrderByDateEmpruntDesc(Utilisateur utilisateur);

    boolean existsByLivreAndDateRetourIsNull(Livre livre);

    boolean existsByLivreIdAndDateRetourIsNull(Long livreId);

    boolean existsByLivre(Livre livre);

    boolean existsByUtilisateur(Utilisateur utilisateur);

    long countByDateRetourIsNull();

    long countByDateRetourIsNullAndDateRetourPrevueBefore(LocalDate date);

    @Query("SELECT e FROM Emprunt e JOIN FETCH e.utilisateur JOIN FETCH e.livre WHERE e.dateRetour IS NULL ORDER BY e.dateEmprunt ASC")
    List<Emprunt> findActiveLoans();

    @Query("SELECT e FROM Emprunt e JOIN FETCH e.utilisateur JOIN FETCH e.livre " +
           "WHERE e.dateRetour IS NULL " +
           "AND (COALESCE(:searchUser, '') = '' OR LOWER(e.utilisateur.nom) LIKE :searchUser) " +
           "AND (COALESCE(:searchBook, '') = '' OR LOWER(e.livre.titre) LIKE :searchBook) " +
           "ORDER BY e.dateEmprunt ASC")
    List<Emprunt> findActiveLoansFiltered(@Param("searchUser") String searchUser,
                                          @Param("searchBook") String searchBook);

    @Query("SELECT e FROM Emprunt e JOIN FETCH e.utilisateur JOIN FETCH e.livre WHERE e.id = :id")
    java.util.Optional<Emprunt> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT e FROM Emprunt e JOIN FETCH e.utilisateur JOIN FETCH e.livre " +
           "WHERE e.dateRetour IS NOT NULL " +
           "AND (COALESCE(:searchUser, '') = '' OR LOWER(e.utilisateur.nom) LIKE :searchUser) " +
           "AND (COALESCE(:searchBook, '') = '' OR LOWER(e.livre.titre) LIKE :searchBook) " +
           "AND (COALESCE(:statut, '') IN ('', 'tous') OR " +
           "     (:statut = 'termines' AND e.dateRetour <= e.dateRetourPrevue) OR " +
           "     (:statut = 'rendus_en_retard' AND e.dateRetour > e.dateRetourPrevue)) " +
           "ORDER BY e.dateRetour DESC")
    Page<Emprunt> findHistoryPaged(@Param("searchUser") String searchUser,
                                   @Param("searchBook") String searchBook,
                                   @Param("statut") String statut,
                                   Pageable pageable);

    @Query("SELECT COUNT(e) FROM Emprunt e " +
           "WHERE e.dateRetour IS NOT NULL " +
           "AND (COALESCE(:searchUser, '') = '' OR LOWER(e.utilisateur.nom) LIKE :searchUser) " +
           "AND (COALESCE(:searchBook, '') = '' OR LOWER(e.livre.titre) LIKE :searchBook) " +
           "AND (COALESCE(:statut, '') IN ('', 'tous') OR " +
           "     (:statut = 'termines' AND e.dateRetour <= e.dateRetourPrevue) OR " +
           "     (:statut = 'rendus_en_retard' AND e.dateRetour > e.dateRetourPrevue))")
    long countHistoryFiltered(@Param("searchUser") String searchUser,
                              @Param("searchBook") String searchBook,
                              @Param("statut") String statut);
}
