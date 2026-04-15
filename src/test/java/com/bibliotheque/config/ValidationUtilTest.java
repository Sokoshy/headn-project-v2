package com.bibliotheque.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationUtilTest {

    // --- isValidEmail ---

    @Test
    void isValidEmail_emailValide_retourneVrai() {
        assertThat(ValidationUtil.isValidEmail("alice@example.com")).isTrue();
    }

    @Test
    void isValidEmail_emailAvecSousDomaine_retourneVrai() {
        assertThat(ValidationUtil.isValidEmail("alice@mail.example.com")).isTrue();
    }

    @Test
    void isValidEmail_emailNull_retourneFaux() {
        assertThat(ValidationUtil.isValidEmail(null)).isFalse();
    }

    @Test
    void isValidEmail_emailVide_retourneFaux() {
        assertThat(ValidationUtil.isValidEmail("")).isFalse();
    }

    @Test
    void isValidEmail_emailSansArobase_retourneFaux() {
        assertThat(ValidationUtil.isValidEmail("aliceexample.com")).isFalse();
    }

    @Test
    void isValidEmail_emailSansDomaine_retourneFaux() {
        assertThat(ValidationUtil.isValidEmail("alice@")).isFalse();
    }

    // --- isValidNom ---

    @Test
    void isValidNom_nomValide_retourneVrai() {
        assertThat(ValidationUtil.isValidNom("Alice")).isTrue();
    }

    @Test
    void isValidNom_nomAvecAccents_retourneVrai() {
        assertThat(ValidationUtil.isValidNom("Élise")).isTrue();
    }

    @Test
    void isValidNom_nomAvecTiret_retourneVrai() {
        assertThat(ValidationUtil.isValidNom("Marie-Claire")).isTrue();
    }

    @Test
    void isValidNom_nomNull_retourneFaux() {
        assertThat(ValidationUtil.isValidNom(null)).isFalse();
    }

    @Test
    void isValidNom_nomVide_retourneFaux() {
        assertThat(ValidationUtil.isValidNom("")).isFalse();
    }

    @Test
    void isValidNom_nomTropCourt_retourneFaux() {
        assertThat(ValidationUtil.isValidNom("A")).isFalse();
    }

    // --- isValidTitre ---

    @Test
    void isValidTitre_titreValide_retourneVrai() {
        assertThat(ValidationUtil.isValidTitre("Dune")).isTrue();
    }

    @Test
    void isValidTitre_titreAvecAccents_retourneVrai() {
        assertThat(ValidationUtil.isValidTitre("L'Étranger")).isTrue();
    }

    @Test
    void isValidTitre_titreNull_retourneFaux() {
        assertThat(ValidationUtil.isValidTitre(null)).isFalse();
    }

    @Test
    void isValidTitre_titreVide_retourneFaux() {
        assertThat(ValidationUtil.isValidTitre("")).isFalse();
    }

    // --- isValidAuteur ---

    @Test
    void isValidAuteur_auteurValide_retourneVrai() {
        assertThat(ValidationUtil.isValidAuteur("Frank Herbert")).isTrue();
    }

    @Test
    void isValidAuteur_auteurNull_retourneFaux() {
        assertThat(ValidationUtil.isValidAuteur(null)).isFalse();
    }

    @Test
    void isValidAuteur_auteurVide_retourneFaux() {
        assertThat(ValidationUtil.isValidAuteur("")).isFalse();
    }

    // --- isValidId ---

    @Test
    void isValidId_idValide_retourneVrai() {
        assertThat(ValidationUtil.isValidId("1")).isTrue();
    }

    @Test
    void isValidId_idZero_retourneFaux() {
        assertThat(ValidationUtil.isValidId("0")).isFalse();
    }

    @Test
    void isValidId_idNegatif_retourneFaux() {
        assertThat(ValidationUtil.isValidId("-1")).isFalse();
    }

    @Test
    void isValidId_idNonNumerique_retourneFaux() {
        assertThat(ValidationUtil.isValidId("abc")).isFalse();
    }

    @Test
    void isValidId_idNull_retourneFaux() {
        assertThat(ValidationUtil.isValidId(null)).isFalse();
    }

    // --- sanitizeInput ---

    @Test
    void sanitizeInput_echappeLesCaracteresDangereux() {
        String resultat = ValidationUtil.sanitizeInput("<script>alert('xss')</script>");
        assertThat(resultat).doesNotContain("<script>");
        assertThat(resultat).contains("&lt;");
        assertThat(resultat).contains("&gt;");
    }

    @Test
    void sanitizeInput_null_retourneChaineVide() {
        assertThat(ValidationUtil.sanitizeInput(null)).isEmpty();
    }

    @Test
    void sanitizeInput_texteNormal_neModifiePas() {
        assertThat(ValidationUtil.sanitizeInput("Dune")).isEqualTo("Dune");
    }

    // --- limitLength ---

    @Test
    void limitLength_chaineTropLongue_estTronquee() {
        assertThat(ValidationUtil.limitLength("abcdefghijklmnopqrstuvwxyz", 10))
                .isEqualTo("abcdefghij");
    }

    @Test
    void limitLength_chaineCourte_nEstPasModifiee() {
        assertThat(ValidationUtil.limitLength("Dune", 10)).isEqualTo("Dune");
    }

    @Test
    void limitLength_null_retourneChaineVide() {
        assertThat(ValidationUtil.limitLength(null, 10)).isEmpty();
    }

    // --- isNotEmpty ---

    @Test
    void isNotEmpty_chaineNonVide_retourneVrai() {
        assertThat(ValidationUtil.isNotEmpty("Dune")).isTrue();
    }

    @Test
    void isNotEmpty_chaineVide_retourneFaux() {
        assertThat(ValidationUtil.isNotEmpty("")).isFalse();
    }

    @Test
    void isNotEmpty_chaineBlanche_retourneFaux() {
        assertThat(ValidationUtil.isNotEmpty("   ")).isFalse();
    }

    @Test
    void isNotEmpty_null_retourneFaux() {
        assertThat(ValidationUtil.isNotEmpty(null)).isFalse();
    }
}
