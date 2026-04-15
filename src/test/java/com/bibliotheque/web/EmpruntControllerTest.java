package com.bibliotheque.web;

import com.bibliotheque.exception.EmpruntDejaRetourneException;
import com.bibliotheque.exception.LivreNonDisponibleException;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.service.EmpruntService;
import com.bibliotheque.service.LivreService;
import com.bibliotheque.service.UtilisateurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

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
class EmpruntControllerTest {

    @Mock
    private EmpruntService empruntService;

    @Mock
    private LivreService livreService;

    @Mock
    private UtilisateurService utilisateurService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        EmpruntController controller = new EmpruntController(empruntService, livreService, utilisateurService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void liste_afficheEmpruntsEtDonneesAssociees() throws Exception {
        when(empruntService.findActifs()).thenReturn(List.of());
        when(empruntService.findHistorique()).thenReturn(List.of());
        when(empruntService.findEnRetard()).thenReturn(List.of());
        when(livreService.findDisponibles()).thenReturn(List.of());
        when(utilisateurService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/emprunts"))
                .andExpect(status().isOk())
                .andExpect(view().name("emprunts/liste"))
                .andExpect(model().attributeExists("empruntsActifs"))
                .andExpect(model().attributeExists("historique"))
                .andExpect(model().attributeExists("empruntsEnRetard"))
                .andExpect(model().attributeExists("livresDisponibles"))
                .andExpect(model().attributeExists("utilisateurs"));
    }

    @Test
    void voir_afficheLeDetail() throws Exception {
        Livre livre = new Livre("Dune", "Frank Herbert");
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Emprunt emprunt = new Emprunt(utilisateur, livre);
        emprunt.setId(1L);
        when(empruntService.findDetailById(1L)).thenReturn(emprunt);

        mockMvc.perform(get("/emprunts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("emprunts/detail"))
                .andExpect(model().attribute("emprunt", emprunt));
    }

    @Test
    void creer_succesRedirigeVersListe() throws Exception {
        Livre livre = new Livre("Dune", "Frank Herbert");
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Emprunt emprunt = new Emprunt(utilisateur, livre);
        emprunt.setId(1L);
        when(empruntService.creer(1L, 2L)).thenReturn(emprunt);

        mockMvc.perform(post("/emprunts")
                        .param("utilisateurId", "1")
                        .param("livreId", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprunts"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void creer_livreNonDisponibleRedirigeAvecErreur() throws Exception {
        when(empruntService.creer(1L, 2L))
                .thenThrow(new LivreNonDisponibleException("Dune"));

        mockMvc.perform(post("/emprunts")
                        .param("utilisateurId", "1")
                        .param("livreId", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprunts"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void effectuerRetour_succesRedirigeVersListe() throws Exception {
        Livre livre = new Livre("Dune", "Frank Herbert");
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Emprunt emprunt = new Emprunt(utilisateur, livre);
        emprunt.setId(1L);
        when(empruntService.effectuerRetour(1L)).thenReturn(emprunt);

        mockMvc.perform(post("/emprunts/1/retour")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprunts"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void effectuerRetour_empruntDejaRetourneRedirigeAvecErreur() throws Exception {
        when(empruntService.effectuerRetour(1L))
                .thenThrow(new EmpruntDejaRetourneException(1L));

        mockMvc.perform(post("/emprunts/1/retour")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprunts"))
                .andExpect(flash().attributeExists("error"));
    }
}
