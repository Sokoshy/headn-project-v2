package com.bibliotheque.web;

import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.exception.DuplicateResourceException;
import com.bibliotheque.model.Livre;
import com.bibliotheque.service.LivreService;
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
class LivreControllerTest {

    @Mock
    private LivreService livreService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LivreController controller = new LivreController(livreService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void liste_afficheTousLesLivres() throws Exception {
        when(livreService.findByRecherche("")).thenReturn(List.of(
                new Livre("Dune", "Frank Herbert"),
                new Livre("1984", "George Orwell")
        ));

        mockMvc.perform(get("/livres"))
                .andExpect(status().isOk())
                .andExpect(view().name("livres/liste"))
                .andExpect(model().attributeExists("livres"));
    }

    @Test
    void liste_avecRechercheFiltreLesLivres() throws Exception {
        when(livreService.findByRecherche("Dune")).thenReturn(List.of(
                new Livre("Dune", "Frank Herbert")
        ));

        mockMvc.perform(get("/livres").param("recherche", "Dune"))
                .andExpect(status().isOk())
                .andExpect(view().name("livres/liste"))
                .andExpect(model().attribute("recherche", "Dune"));
    }

    @Test
    void nouveau_afficheLeFormulaire() throws Exception {
        mockMvc.perform(get("/livres/nouveau"))
                .andExpect(status().isOk())
                .andExpect(view().name("livres/formulaire"))
                .andExpect(model().attributeExists("livre"));
    }

    @Test
    void voir_afficheLeDetail() throws Exception {
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(1L);
        when(livreService.findById(1L)).thenReturn(livre);

        mockMvc.perform(get("/livres/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("livres/detail"))
                .andExpect(model().attribute("livre", livre));
    }

    @Test
    void edit_afficheLeFormulairePreRempli() throws Exception {
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(1L);
        when(livreService.findById(1L)).thenReturn(livre);

        mockMvc.perform(get("/livres/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("livres/formulaire"))
                .andExpect(model().attribute("livre", livre));
    }

    @Test
    void creer_succesRedirigeVersListe() throws Exception {
        Livre livreCree = new Livre("Dune", "Frank Herbert");
        livreCree.setId(1L);
        when(livreService.creer(any(Livre.class))).thenReturn(livreCree);

        mockMvc.perform(post("/livres")
                        .param("titre", "Dune")
                        .param("auteur", "Frank Herbert")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livres"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void creer_erreurValidationResteSurFormulaire() throws Exception {
        mockMvc.perform(post("/livres")
                        .param("titre", "")
                        .param("auteur", "Frank Herbert")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("livres/formulaire"));
    }

    @Test
    void creer_erreurMetierResteSurFormulaire() throws Exception {
        when(livreService.creer(any(Livre.class)))
                .thenThrow(new DuplicateResourceException("Un livre avec le titre 'Dune' de l'auteur 'Frank Herbert' existe déjà"));

        mockMvc.perform(post("/livres")
                        .param("titre", "Dune")
                        .param("auteur", "Frank Herbert")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("livres/formulaire"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void modifier_succesRedirigeVersListe() throws Exception {
        Livre livreModifie = new Livre("Dune Messiah", "Frank Herbert");
        livreModifie.setId(1L);
        when(livreService.modifier(anyLong(), any(Livre.class))).thenReturn(livreModifie);

        mockMvc.perform(post("/livres/1")
                        .param("titre", "Dune Messiah")
                        .param("auteur", "Frank Herbert")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livres"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void modifier_erreurValidationResteSurFormulaire() throws Exception {
        mockMvc.perform(post("/livres/1")
                        .param("titre", "")
                        .param("auteur", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("livres/formulaire"));
    }

    @Test
    void supprimer_succesRedirigeVersListe() throws Exception {
        mockMvc.perform(post("/livres/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livres"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void supprimer_erreurMetierRedirigeAvecErreur() throws Exception {
        doThrow(new BusinessException("Impossible de supprimer le livre : un historique d'emprunts lui est associé"))
                .when(livreService).supprimer(1L);

        mockMvc.perform(post("/livres/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livres"))
                .andExpect(flash().attributeExists("error"));
    }
}
