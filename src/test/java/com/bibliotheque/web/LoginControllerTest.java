package com.bibliotheque.web;

import com.bibliotheque.service.AgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private AgentService agentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LoginController controller = new LoginController(agentService);
        // No-op ViewResolver: MockMvc captures the view name without rendering/dispatching.
        // Redirects still produce a real RedirectView so status 3xx and Location header
        // are honoured (required by redirectedUrl() matchers).
        ViewResolver noOpResolver = (viewName, locale) -> {
            if (viewName != null && viewName.startsWith(UrlBasedViewResolver.REDIRECT_URL_PREFIX)) {
                return new RedirectView(viewName.substring(UrlBasedViewResolver.REDIRECT_URL_PREFIX.length()));
            }
            return (model, request, response) -> { /* no-op: MockMvc tracks view name */ };
        };
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(noOpResolver)
                .build();
    }

    @Test
    @DisplayName("GET /login affiche le formulaire quand des agents existent")
    void login_agentsExistants_afficheFormulaire() throws Exception {
        when(agentService.existsAny()).thenReturn(true);

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("GET /login redirige vers /setup si aucun agent n'existe")
    void login_aucunAgent_redirigeVersSetup() throws Exception {
        when(agentService.existsAny()).thenReturn(false);

        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/setup"));
    }

    @Test
    @DisplayName("GET /login?error affiche le formulaire (message géré par le template)")
    void login_parametreError_afficheFormulaire() throws Exception {
        when(agentService.existsAny()).thenReturn(true);

        mockMvc.perform(get("/login").param("error", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("GET /login?logout affiche le formulaire (message géré par le template)")
    void login_parametreLogout_afficheFormulaire() throws Exception {
        when(agentService.existsAny()).thenReturn(true);

        mockMvc.perform(get("/login").param("logout", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("GET /login?error redirige vers /setup si aucun agent n'existe")
    void login_avecErrorEtSansAgent_redirigeVersSetup() throws Exception {
        when(agentService.existsAny()).thenReturn(false);

        mockMvc.perform(get("/login").param("error", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/setup"));
    }

    @Test
    @DisplayName("GET /login?logout redirige vers /setup si aucun agent n'existe")
    void login_avecLogoutEtSansAgent_redirigeVersSetup() throws Exception {
        when(agentService.existsAny()).thenReturn(false);

        mockMvc.perform(get("/login").param("logout", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/setup"));
    }
}
