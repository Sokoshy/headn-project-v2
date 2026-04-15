package com.bibliotheque.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UtilisateurTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void constructeurAvecParametres_creeUnUtilisateurValide() {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");

        assertThat(utilisateur.getNom()).isEqualTo("Alice");
        assertThat(utilisateur.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void validation_nomVide_estInvalide() {
        Utilisateur utilisateur = new Utilisateur("", "alice@example.com");

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("nom"));
    }

    @Test
    void validation_nomNull_estInvalide() {
        Utilisateur utilisateur = new Utilisateur(null, "alice@example.com");

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("nom"));
    }

    @Test
    void validation_nomTropLong_estInvalide() {
        Utilisateur utilisateur = new Utilisateur("a".repeat(256), "alice@example.com");

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("nom"));
    }

    @Test
    void validation_emailVide_estInvalide() {
        Utilisateur utilisateur = new Utilisateur("Alice", "");

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void validation_emailNull_estInvalide() {
        Utilisateur utilisateur = new Utilisateur("Alice", null);

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void validation_emailInvalide_estInvalide() {
        Utilisateur utilisateur = new Utilisateur("Alice", "pas-un-email");

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void validation_emailTropLong_estInvalide() {
        Utilisateur utilisateur = new Utilisateur("Alice", "a".repeat(256) + "@example.com");

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void validation_utilisateurValide_nAucuneViolation() {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");

        Set<ConstraintViolation<Utilisateur>> violations = validator.validate(utilisateur);
        assertThat(violations).isEmpty();
    }

    @Test
    void equals_deuxUtilisateursAvecMemeId_sontEgaux() {
        Utilisateur u1 = new Utilisateur("Alice", "alice@example.com");
        u1.setId(1L);
        Utilisateur u2 = new Utilisateur("Bob", "bob@example.com");
        u2.setId(1L);

        assertThat(u1).isEqualTo(u2);
    }

    @Test
    void equals_deuxUtilisateursAvecIdDifferent_sontDifferents() {
        Utilisateur u1 = new Utilisateur("Alice", "alice@example.com");
        u1.setId(1L);
        Utilisateur u2 = new Utilisateur("Alice", "alice@example.com");
        u2.setId(2L);

        assertThat(u1).isNotEqualTo(u2);
    }

    @Test
    void onCreate_nePositionneDateInscriptionQueSiNull() {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        assertThat(utilisateur.getDateInscription()).isNull();

        utilisateur.onCreate();
        assertThat(utilisateur.getDateInscription()).isNotNull();

        var dateExistante = utilisateur.getDateInscription();
        utilisateur.onCreate();
        assertThat(utilisateur.getDateInscription()).isEqualTo(dateExistante);
    }
}
