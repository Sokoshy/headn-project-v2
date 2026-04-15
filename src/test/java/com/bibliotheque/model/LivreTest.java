package com.bibliotheque.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LivreTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void constructeurAvecParametres_creeUnLivreValide() {
        Livre livre = new Livre("Dune", "Frank Herbert");

        assertThat(livre.getTitre()).isEqualTo("Dune");
        assertThat(livre.getAuteur()).isEqualTo("Frank Herbert");
        assertThat(livre.isDisponible()).isTrue();
    }

    @Test
    void constructeurParDefaut_creeUnLivreDisponible() {
        Livre livre = new Livre();

        assertThat(livre.isDisponible()).isTrue();
    }

    @Test
    void validation_titreVide_estInvalide() {
        Livre livre = new Livre("", "Frank Herbert");

        Set<ConstraintViolation<Livre>> violations = validator.validate(livre);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("titre"));
    }

    @Test
    void validation_titreNull_estInvalide() {
        Livre livre = new Livre(null, "Frank Herbert");

        Set<ConstraintViolation<Livre>> violations = validator.validate(livre);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("titre"));
    }

    @Test
    void validation_titreBlanc_estInvalide() {
        Livre livre = new Livre("   ", "Frank Herbert");

        Set<ConstraintViolation<Livre>> violations = validator.validate(livre);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("titre"));
    }

    @Test
    void validation_auteurVide_estInvalide() {
        Livre livre = new Livre("Dune", "");

        Set<ConstraintViolation<Livre>> violations = validator.validate(livre);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("auteur"));
    }

    @Test
    void validation_auteurNull_estInvalide() {
        Livre livre = new Livre("Dune", null);

        Set<ConstraintViolation<Livre>> violations = validator.validate(livre);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("auteur"));
    }

    @Test
    void validation_titreTropLong_estInvalide() {
        Livre livre = new Livre("a".repeat(256), "Frank Herbert");

        Set<ConstraintViolation<Livre>> violations = validator.validate(livre);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("titre"));
    }

    @Test
    void validation_auteurTropLong_estInvalide() {
        Livre livre = new Livre("Dune", "a".repeat(256));

        Set<ConstraintViolation<Livre>> violations = validator.validate(livre);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("auteur"));
    }

    @Test
    void validation_livreValide_nAucuneViolation() {
        Livre livre = new Livre("Dune", "Frank Herbert");

        Set<ConstraintViolation<Livre>> violations = validator.validate(livre);
        assertThat(violations).isEmpty();
    }

    @Test
    void equals_deuxLivresAvecMemeId_sontEgaux() {
        Livre livre1 = new Livre("Dune", "Frank Herbert");
        livre1.setId(1L);
        Livre livre2 = new Livre("1984", "George Orwell");
        livre2.setId(1L);

        assertThat(livre1).isEqualTo(livre2);
    }

    @Test
    void equals_deuxLivresAvecIdDifferent_sontDifferents() {
        Livre livre1 = new Livre("Dune", "Frank Herbert");
        livre1.setId(1L);
        Livre livre2 = new Livre("Dune", "Frank Herbert");
        livre2.setId(2L);

        assertThat(livre1).isNotEqualTo(livre2);
    }

    @Test
    void onCreate_nePositionneDateCreationQueSiNull() {
        Livre livre = new Livre("Dune", "Frank Herbert");
        assertThat(livre.getDateCreation()).isNull();

        livre.onCreate();
        assertThat(livre.getDateCreation()).isNotNull();

        var dateExistante = livre.getDateCreation();
        livre.onCreate();
        assertThat(livre.getDateCreation()).isEqualTo(dateExistante);
    }
}
