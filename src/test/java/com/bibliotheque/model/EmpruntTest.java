package com.bibliotheque.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class EmpruntTest {

    @Test
    void constructeurAvecParametres_creeUnEmpruntEnCours() {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Livre livre = new Livre("Dune", "Frank Herbert");

        Emprunt emprunt = new Emprunt(utilisateur, livre);

        assertThat(emprunt.getUtilisateur()).isSameAs(utilisateur);
        assertThat(emprunt.getLivre()).isSameAs(livre);
        assertThat(emprunt.getDateEmprunt()).isEqualTo(LocalDate.now());
        assertThat(emprunt.getDateRetour()).isNull();
    }

    @Test
    void estEnCours_sansDateRetour_retourneVrai() {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateEmprunt(LocalDate.now());

        assertThat(emprunt.estEnCours()).isTrue();
    }

    @Test
    void estEnCours_avecDateRetour_retourneFaux() {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateEmprunt(LocalDate.now().minusDays(10));
        emprunt.setDateRetour(LocalDate.now());

        assertThat(emprunt.estEnCours()).isFalse();
    }

    @Test
    void estEnRetard_empruntDePlusDe30Jours_retourneVrai() {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateEmprunt(LocalDate.now().minusDays(31));

        assertThat(emprunt.estEnRetard()).isTrue();
    }

    @Test
    void estEnRetard_empruntDeMoinsDe30Jours_retourneFaux() {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateEmprunt(LocalDate.now().minusDays(29));

        assertThat(emprunt.estEnRetard()).isFalse();
    }

    @Test
    void estEnRetard_empruntDeExactement30Jours_retourneFaux() {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateEmprunt(LocalDate.now().minusDays(30));

        assertThat(emprunt.estEnRetard()).isFalse();
    }

    @Test
    void estEnRetard_empruntRetourne_retourneFaux() {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateEmprunt(LocalDate.now().minusDays(60));
        emprunt.setDateRetour(LocalDate.now().minusDays(20));

        assertThat(emprunt.estEnRetard()).isFalse();
    }

    @Test
    void getNombreJoursEmprunt_empruntEnCours_calculeDepuisDateEmprunt() {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateEmprunt(LocalDate.now().minusDays(5));

        assertThat(emprunt.getNombreJoursEmprunt()).isEqualTo(5);
    }

    @Test
    void getNombreJoursEmprunt_empruntRetourne_calculeDepuisDateRetour() {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateEmprunt(LocalDate.now().minusDays(10));
        emprunt.setDateRetour(LocalDate.now().minusDays(3));

        assertThat(emprunt.getNombreJoursEmprunt()).isEqualTo(7);
    }

    @Test
    void equals_deuxEmpruntsAvecMemeId_sontEgaux() {
        Emprunt e1 = new Emprunt();
        e1.setId(1L);
        Emprunt e2 = new Emprunt();
        e2.setId(1L);

        assertThat(e1).isEqualTo(e2);
    }

    @Test
    void equals_deuxEmpruntsAvecIdDifferent_sontDifferents() {
        Emprunt e1 = new Emprunt();
        e1.setId(1L);
        Emprunt e2 = new Emprunt();
        e2.setId(2L);

        assertThat(e1).isNotEqualTo(e2);
    }
}
