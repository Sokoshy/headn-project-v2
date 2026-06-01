package com.bibliotheque.web;

import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.model.Role;
import com.bibliotheque.service.AgentForm;
import com.bibliotheque.service.AgentService;
import com.bibliotheque.model.Agent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
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
class SetupControllerTest {

    @Mock
    private AgentService agentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SetupController controller = new SetupController(agentService);
        // No-op ViewResolver: MockMvc captures the view name without rendering/dispatching.
        // Standard idiom to avoid circular view path issues in standalone MockMvc tests
        // (e.g. controller returns view "setup" which would otherwise be re-dispatched
        // to /setup). Redirects still produce a real RedirectView so status 3xx and
        // Location header are honoured (required by redirectedUrl() matchers).
        ViewResolver noOpResolver = (viewName, locale) -> {
            if (viewName != null && viewName.startsWith(UrlBasedViewResolver.REDIRECT_URL_PREFIX)) {
                return new RedirectView(viewName.substring(UrlBasedViewResolver.REDIRECT_URL_PREFIX.length()));
            }
            return (model, request, response) -> { /* no-op: MockMvc tracks view name */ };
        };
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setViewResolvers(noOpResolver)
                .build();
    }

    @Test
    @DisplayName("GET /setup redirige vers /login si des agents existent déjà")
    void setupForm_agentsExistants_redirigeVersLogin() throws Exception {
        when(agentService.existsAny()).thenReturn(true);

        mockMvc.perform(get("/setup"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("GET /setup affiche le formulaire si aucun agent n'existe")
    void setupForm_aucunAgent_afficheLeFormulaire() throws Exception {
        when(agentService.existsAny()).thenReturn(false);

        mockMvc.perform(get("/setup"))
                .andExpect(status().isOk())
                .andExpect(view().name("setup"))
                .andExpect(model().attribute("role", Role.ADMIN));
    }

    @Test
    @DisplayName("POST /setup avec succès redirige vers /login et passe Role.ADMIN")
    void setupSubmit_succes_redirigeVersLogin() throws Exception {
        when(agentService.existsAny()).thenReturn(false);
        Agent admin = new Agent("Admin", "admin@bib.fr", "HASH", Role.ADMIN);
        admin.setId(1L);
        when(agentService.creer(any(AgentForm.class))).thenReturn(admin);

        mockMvc.perform(post("/setup")
                        .param("nom", "Admin")
                        .param("email", "admin@bib.fr")
                        .param("motDePasse", "secret123")
                        .param("motDePasseConfirmation", "secret123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("success"));

        ArgumentCaptor<AgentForm> captor = ArgumentCaptor.forClass(AgentForm.class);
        verify(agentService).creer(captor.capture());
        AgentForm form = captor.getValue();
        assertEquals(Role.ADMIN, form.role());
        assertNull(form.telephone());
    }

    @Test
    @DisplayName("POST /setup avec erreur métier redirige vers /setup avec les valeurs conservées")
    void setupSubmit_erreurMetier_redirigeVersSetup() throws Exception {
        when(agentService.existsAny()).thenReturn(false);
        when(agentService.creer(any(AgentForm.class)))
                .thenThrow(new BusinessException("MOT_DE_PASSE_INCOHERENT"));

        mockMvc.perform(post("/setup")
                        .param("nom", "Admin")
                        .param("email", "admin@bib.fr")
                        .param("motDePasse", "secret123")
                        .param("motDePasseConfirmation", "different")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/setup"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("nom", "Admin"))
                .andExpect(flash().attribute("email", "admin@bib.fr"));
    }
}
