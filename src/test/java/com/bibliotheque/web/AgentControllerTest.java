package com.bibliotheque.web;

import com.bibliotheque.config.CurrentAgentProvider;
import com.bibliotheque.exception.AgentNotFoundException;
import com.bibliotheque.exception.EmailAgentDejaUtiliseException;
import com.bibliotheque.model.Agent;
import com.bibliotheque.model.Role;
import com.bibliotheque.service.AgentForm;
import com.bibliotheque.service.AgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
class AgentControllerTest {

    @Mock
    private AgentService agentService;

    @Mock
    private CurrentAgentProvider currentAgentProvider;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AgentController controller = new AgentController(agentService, currentAgentProvider);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("La page liste affiche tous les agents")
    void liste_afficheLaListeDesAgents() throws Exception {
        Agent alice = nouvelAgent();
        Agent bob = new Agent("Bob", "bob@bib.fr", "HASH", Role.ADMIN);
        bob.setId(2L);
        when(agentService.findAll()).thenReturn(List.of(alice, bob));

        mockMvc.perform(get("/agents"))
                .andExpect(status().isOk())
                .andExpect(view().name("agents/liste"))
                .andExpect(model().attributeExists("agents"));
    }

    @Test
    @DisplayName("La page nouveau affiche un formulaire vide")
    void nouveau_afficheLeFormulaire() throws Exception {
        mockMvc.perform(get("/agents/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("agents/formulaire"))
                .andExpect(model().attributeExists("agent"))
                .andExpect(model().attribute("editMode", false));
    }

    @Test
    @DisplayName("La page détail affiche l'agent par ID")
    void voir_afficheLeDetail() throws Exception {
        Agent agent = nouvelAgent();
        when(agentService.findById(1L)).thenReturn(agent);

        mockMvc.perform(get("/agents/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("agents/detail"))
                .andExpect(model().attribute("agent", agent));
    }

    @Test
    @DisplayName("La page edit affiche le formulaire pré-rempli")
    void edit_afficheLeFormulairePreRempli() throws Exception {
        Agent agent = nouvelAgent();
        agent.setTelephone("0612345678");
        when(agentService.findById(1L)).thenReturn(agent);

        mockMvc.perform(get("/agents/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("agents/formulaire"))
                .andExpect(model().attribute("agentId", 1L))
                .andExpect(model().attribute("editMode", true));
    }

    @Test
    @DisplayName("Créer un agent avec succès redirige vers la liste avec un message contenant le nom")
    void creer_succesRedirigeVersListe() throws Exception {
        Agent agentCree = nouvelAgent();
        when(agentService.creer(any(AgentForm.class))).thenReturn(agentCree);

        mockMvc.perform(post("/agents")
                        .param("nom", "Alice")
                        .param("email", "alice@bib.fr")
                        .param("motDePasse", "secret123")
                        .param("motDePasseConfirmation", "secret123")
                        .param("role", "LIBRARIAN")
                        .param("telephone", "0612345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agents"))
                .andExpect(flash().attribute("success", containsString("Alice")));
    }

    @Test
    @DisplayName("Créer un agent avec email déjà utilisé redirige vers le formulaire avec erreur")
    void creer_emailDejaUtilise_redirigeVersFormulaire() throws Exception {
        when(agentService.creer(any(AgentForm.class)))
                .thenThrow(new EmailAgentDejaUtiliseException("alice@bib.fr"));

        mockMvc.perform(post("/agents")
                        .param("nom", "Alice")
                        .param("email", "alice@bib.fr")
                        .param("motDePasse", "secret123")
                        .param("motDePasseConfirmation", "secret123")
                        .param("role", "LIBRARIAN")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agents/new"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("Modifier un agent avec succès redirige vers la liste")
    void modifier_succesRedirigeVersListe() throws Exception {
        Agent agentModifie = new Agent("Alice Updated", "alice@bib.fr", "HASH", Role.ADMIN);
        agentModifie.setId(1L);
        when(agentService.modifier(eq(1L), any(AgentForm.class))).thenReturn(agentModifie);

        mockMvc.perform(post("/agents/1")
                        .param("nom", "Alice Updated")
                        .param("email", "alice@bib.fr")
                        .param("role", "ADMIN")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agents"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @DisplayName("Modifier un agent avec email déjà utilisé redirige vers le formulaire avec erreur")
    void modifier_emailDejaUtilise_redirigeVersFormulaire() throws Exception {
        when(agentService.modifier(eq(1L), any(AgentForm.class)))
                .thenThrow(new EmailAgentDejaUtiliseException("duplicate@bib.fr"));

        mockMvc.perform(post("/agents/1")
                        .param("nom", "Alice")
                        .param("email", "duplicate@bib.fr")
                        .param("role", "LIBRARIAN")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agents/1/edit"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("Désactiver un agent existant redirige vers la liste et appelle le service")
    void desactiver_existant_redirigeVersListe() throws Exception {
        Agent currentAgent = new Agent("Admin", "admin@bib.fr", "HASH", Role.ADMIN);
        currentAgent.setId(99L);
        when(currentAgentProvider.getCurrentAgent()).thenReturn(currentAgent);
        Agent agent = nouvelAgent();
        when(agentService.findById(1L)).thenReturn(agent);

        mockMvc.perform(post("/agents/1/deactivate")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agents"))
                .andExpect(flash().attributeExists("success"));

        verify(agentService).desactiver(1L);
    }

    @Test
    @DisplayName("Désactiver un agent inexistant redirige vers la liste avec un message d'erreur")
    void desactiver_agentInexistant_redirigeAvecErreur() throws Exception {
        Agent currentAgent = new Agent("Admin", "admin@bib.fr", "HASH", Role.ADMIN);
        currentAgent.setId(99L);
        when(currentAgentProvider.getCurrentAgent()).thenReturn(currentAgent);
        when(agentService.findById(1L)).thenThrow(new AgentNotFoundException(1L));

        mockMvc.perform(post("/agents/1/deactivate")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agents"))
                .andExpect(flash().attributeExists("error"));

        verify(agentService, never()).desactiver(anyLong());
    }

    @Test
    @DisplayName("Réactiver un agent existant redirige vers la liste et appelle le service")
    void reactiver_existant_redirigeVersListe() throws Exception {
        Agent agent = nouvelAgent();
        when(agentService.findById(1L)).thenReturn(agent);

        mockMvc.perform(post("/agents/1/reactivate")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agents"))
                .andExpect(flash().attributeExists("success"));

        verify(agentService).reactiver(1L);
    }

    @Test
    @DisplayName("Réactiver un agent inexistant redirige vers la liste avec un message d'erreur")
    void reactiver_agentInexistant_redirigeAvecErreur() throws Exception {
        when(agentService.findById(1L)).thenThrow(new AgentNotFoundException(1L));

        mockMvc.perform(post("/agents/1/reactivate")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agents"))
                .andExpect(flash().attributeExists("error"));

        verify(agentService, never()).reactiver(anyLong());
    }

    private static Agent nouvelAgent() {
        Agent alice = new Agent("Alice", "alice@bib.fr", "HASH", Role.LIBRARIAN);
        alice.setId(1L);
        return alice;
    }
}
