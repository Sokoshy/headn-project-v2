package com.bibliotheque.web;

import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.exception.EmailDejaUtiliseException;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.service.UtilisateurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class UtilisateurControllerTest {

    @Mock
    private UtilisateurService utilisateurService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UtilisateurController controller = new UtilisateurController(utilisateurService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void liste_afficheTousLesUtilisateurs() throws Exception {
        when(utilisateurService.findByRecherche("")).thenReturn(List.of(
                new Utilisateur("Alice", "alice@example.com"),
                new Utilisateur("Bob", "bob@example.com")
        ));

        mockMvc.perform(get("/utilisateurs"))
                .andExpect(status().isOk())
                .andExpect(view().name("utilisateurs/liste"))
                .andExpect(model().attributeExists("utilisateurs"));
    }

    @Test
    void liste_avecRechercheFiltreLesUtilisateurs() throws Exception {
        when(utilisateurService.findByRecherche("Alice")).thenReturn(List.of(
                new Utilisateur("Alice", "alice@example.com")
        ));

        mockMvc.perform(get("/utilisateurs").param("recherche", "Alice"))
                .andExpect(status().isOk())
                .andExpect(view().name("utilisateurs/liste"))
                .andExpect(model().attribute("recherche", "Alice"));
    }

    @Test
    void nouveau_afficheLeFormulaire() throws Exception {
        mockMvc.perform(get("/utilisateurs/nouveau"))
                .andExpect(status().isOk())
                .andExpect(view().name("utilisateurs/formulaire"))
                .andExpect(model().attributeExists("utilisateur"));
    }

    @Test
    void voir_afficheLeDetail() throws Exception {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        utilisateur.setId(1L);
        when(utilisateurService.findById(1L)).thenReturn(utilisateur);

        mockMvc.perform(get("/utilisateurs/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("utilisateurs/detail"))
                .andExpect(model().attribute("utilisateur", utilisateur));
    }

    @Test
    void edit_afficheLeFormulairePreRempli() throws Exception {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        utilisateur.setId(1L);
        when(utilisateurService.findById(1L)).thenReturn(utilisateur);

        mockMvc.perform(get("/utilisateurs/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("utilisateurs/formulaire"))
                .andExpect(model().attribute("utilisateur", utilisateur));
    }

    @Test
    void creer_succesRedirigeVersListe() throws Exception {
        Utilisateur utilisateurCree = new Utilisateur("Alice", "alice@example.com");
        utilisateurCree.setId(1L);
        when(utilisateurService.creer(any(Utilisateur.class))).thenReturn(utilisateurCree);

        mockMvc.perform(post("/utilisateurs")
                        .param("nom", "Alice")
                        .param("email", "alice@example.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/utilisateurs"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void creer_erreurValidationResteSurFormulaire() throws Exception {
        mockMvc.perform(post("/utilisateurs")
                        .param("nom", "")
                        .param("email", "invalide")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("utilisateurs/formulaire"));
    }

    @Test
    void creer_erreurEmailDupliqueResteSurFormulaire() throws Exception {
        when(utilisateurService.creer(any(Utilisateur.class)))
                .thenThrow(new EmailDejaUtiliseException("alice@example.com"));

        mockMvc.perform(post("/utilisateurs")
                        .param("nom", "Alice")
                        .param("email", "alice@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("utilisateurs/formulaire"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void modifier_succesRedirigeVersListe() throws Exception {
        Utilisateur utilisateurModifie = new Utilisateur("Alice Dupont", "alice.dupont@example.com");
        utilisateurModifie.setId(1L);
        when(utilisateurService.modifier(anyLong(), any(Utilisateur.class))).thenReturn(utilisateurModifie);

        mockMvc.perform(post("/utilisateurs/1")
                        .param("nom", "Alice Dupont")
                        .param("email", "alice.dupont@example.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/utilisateurs"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void modifier_erreurValidationResteSurFormulaire() throws Exception {
        mockMvc.perform(post("/utilisateurs/1")
                        .param("nom", "")
                        .param("email", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("utilisateurs/formulaire"));
    }

    @Test
    void supprimer_succesRedirigeVersListe() throws Exception {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        utilisateur.setId(1L);
        when(utilisateurService.findById(1L)).thenReturn(utilisateur);

        mockMvc.perform(post("/utilisateurs/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/utilisateurs"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void supprimer_erreurMetierRedirigeAvecErreur() throws Exception {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        utilisateur.setId(1L);
        when(utilisateurService.findById(1L)).thenReturn(utilisateur);
        doThrow(new BusinessException("Impossible de supprimer l'utilisateur : un historique d'emprunts lui est associé"))
                .when(utilisateurService).supprimer(1L);

        mockMvc.perform(post("/utilisateurs/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/utilisateurs"))
                .andExpect(flash().attributeExists("error"));
    }
}
