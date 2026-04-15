package com.bibliotheque.web;

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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private LivreService livreService;

    @Mock
    private UtilisateurService utilisateurService;

    @Mock
    private EmpruntService empruntService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        HomeController controller = new HomeController(livreService, utilisateurService, empruntService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void index_afficheLaPageDAccueilAvecLesStatistiques() throws Exception {
        when(livreService.countTotal()).thenReturn(42L);
        when(livreService.countDisponibles()).thenReturn(30L);
        when(utilisateurService.countTotal()).thenReturn(15L);
        when(empruntService.countActifs()).thenReturn(12L);
        when(empruntService.countEnRetard()).thenReturn(3L);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("totalLivres", 42L))
                .andExpect(model().attribute("livresDisponibles", 30L))
                .andExpect(model().attribute("totalUtilisateurs", 15L))
                .andExpect(model().attribute("empruntsActifs", 12L))
                .andExpect(model().attribute("empruntsEnRetard", 3L));
    }
}
