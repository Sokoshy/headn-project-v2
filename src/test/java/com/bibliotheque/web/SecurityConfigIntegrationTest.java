package com.bibliotheque.web;

import com.bibliotheque.BibliothequeApplication;
import com.bibliotheque.model.Agent;
import com.bibliotheque.model.Role;
import com.bibliotheque.repository.AgentRepository;
import com.bibliotheque.repository.AuditLoanRepository;
import com.bibliotheque.service.AgentForm;
import com.bibliotheque.service.AgentService;
import com.bibliotheque.support.PostgresIntegrationTestBase;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration test for the SecurityFilterChain defined in
 * {@link com.bibliotheque.config.SecurityConfig}. Boots the full Spring context
 * (controllers, services, repositories, security filters) against a real
 * PostgreSQL container to verify the security model behaves as designed.
 *
 * <p>Covers: unauthenticated access (HTML vs JSON entry points), role-based
 * authorization (LIBRARIAN vs ADMIN), login/logout flows with the real
 * AuthenticationManager, CSRF protection, and the first-boot /setup guard.
 *
 * <p>Note: Spring Boot 4.0.4 has dropped the {@code @AutoConfigureMockMvc} auto-configuration
 * annotation, so we manually wire MockMvc from the {@link WebApplicationContext} with
 * {@link SecurityMockMvcConfigurers#springSecurity()} applied.
 */
@SpringBootTest(classes = BibliothequeApplication.class)
class SecurityConfigIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AuditLoanRepository auditLoanRepository;

    private MockMvc mockMvc;

    private static final String LIBRARIAN_EMAIL = "alice@bib.fr";
    private static final String ADMIN_EMAIL = "admin@bib.fr";
    private static final String PASSWORD = "secret123";

    @BeforeEach
    void setUp() {
        // audit_loans.agent_id has FK ON DELETE RESTRICT, so we must wipe
        // audit_loans before deleting agents. Earlier tests may have left rows
        // behind in the shared Testcontainers instance.
        auditLoanRepository.deleteAllInBatch();
        agentRepository.deleteAllInBatch();
        // Build MockMvc with the full web application context (including the real
        // SecurityFilterChain defined in SecurityConfig) so the filter chain is
        // exercised end-to-end.
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private void createLibrarian() {
        agentService.creer(new AgentForm("Alice", LIBRARIAN_EMAIL, PASSWORD, PASSWORD, Role.LIBRARIAN, null));
    }

    private void createAdmin() {
        agentService.creer(new AgentForm("Admin", ADMIN_EMAIL, PASSWORD, PASSWORD, Role.ADMIN, null));
    }

    /**
     * Performs a GET /login and returns the XSRF-TOKEN cookie value set by
     * CookieCsrfTokenRepository. Throws if the cookie is absent.
     */
    private String fetchCsrfTokenFromLoginGet() throws Exception {
        MvcResult result = mockMvc.perform(get("/login")).andReturn();
        return Arrays.stream(result.getResponse().getCookies())
                .filter(c -> "XSRF-TOKEN".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AssertionError("XSRF-TOKEN cookie not set on GET /login"));
    }

    // ================================================================
    // Unauthenticated access
    // ================================================================

    @Test
    @DisplayName("GET /livres non authentifié (Accept HTML) redirige vers /login")
    void getLivres_unauthentifie_redirigeVersLogin() throws Exception {
        MvcResult result = mockMvc.perform(get("/livres").header("Accept", "text/html"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertThat(result.getResponse().getHeader("Location"))
                .as("Unauthenticated HTML request should redirect to /login")
                .contains("/login");
    }

    @Test
    @DisplayName("GET /livres non authentifié (Accept JSON) retourne 401 (entry point API)")
    void getLivres_unauthentifieApiHeader_retourne401() throws Exception {
        mockMvc.perform(get("/livres").header("Accept", "application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /agents non authentifié (Accept HTML) redirige vers /login")
    void getAgents_unauthentifie_redirigeVersLogin() throws Exception {
        MvcResult result = mockMvc.perform(get("/agents").header("Accept", "text/html"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertThat(result.getResponse().getHeader("Location"))
                .as("Unauthenticated HTML request to admin-only URL should redirect to /login")
                .contains("/login");
    }

    // ================================================================
    // Role-based authorization
    // ================================================================

    @Test
    @DisplayName("GET /agents en LIBRARIAN retourne 403 (accès refusé)")
    void getAgents_librarian_retourne403() throws Exception {
        createLibrarian();

        mockMvc.perform(get("/agents").with(user(LIBRARIAN_EMAIL).roles("LIBRARIAN")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /agents en ADMIN retourne 200")
    void getAgents_admin_retourne200() throws Exception {
        createAdmin();

        mockMvc.perform(get("/agents").with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /audit en LIBRARIAN retourne 200 (audit ouvert à tout authentifié)")
    void getAudit_librarian_retourne200() throws Exception {
        createLibrarian();

        mockMvc.perform(get("/audit").with(user(LIBRARIAN_EMAIL).roles("LIBRARIAN")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /audit en ADMIN retourne 200")
    void getAudit_admin_retourne200() throws Exception {
        createAdmin();

        mockMvc.perform(get("/audit").with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk());
    }

    // ================================================================
    // Login flow
    // ================================================================

    @Test
    @DisplayName("POST /login avec identifiants valides + CSRF redirige vers / (succès)")
    void postLogin_credentialsValides_redirigeVersAccueil() throws Exception {
        createLibrarian();

        MvcResult result = mockMvc.perform(post("/login")
                        .param("username", LIBRARIAN_EMAIL)
                        .param("password", PASSWORD)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String location = result.getResponse().getRedirectedUrl();
        assertThat(location)
                .as("Successful login should redirect to defaultSuccessUrl('/')")
                .isNotNull();
        assertThat(location).endsWith("/");
    }

    @Test
    @DisplayName("POST /login avec mot de passe invalide + CSRF redirige vers /login?error")
    void postLogin_credentialsInvalides_redirigeVersLoginAvecError() throws Exception {
        createLibrarian();

        MvcResult result = mockMvc.perform(post("/login")
                        .param("username", LIBRARIAN_EMAIL)
                        .param("password", "wrong-password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertThat(result.getResponse().getRedirectedUrl())
                .as("Failed login should redirect to failureUrl('/login?error')")
                .contains("?error");
    }

    @Test
    @DisplayName("POST /login avec agent désactivé redirige vers /login?error (AgentDetailsService refuse)")
    void postLogin_agentInactif_redirigeVersLoginAvecError() throws Exception {
        createLibrarian();
        Agent librarian = agentRepository.findByEmail(LIBRARIAN_EMAIL).orElseThrow();
        agentService.desactiver(librarian.getId());

        MvcResult result = mockMvc.perform(post("/login")
                        .param("username", LIBRARIAN_EMAIL)
                        .param("password", PASSWORD)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertThat(result.getResponse().getRedirectedUrl())
                .as("Login with inactive agent should be rejected like any bad credential")
                .contains("?error");
    }

    // ================================================================
    // Logout flow
    // ================================================================

    @Test
    @DisplayName("POST /logout authentifié redirige vers /login?logout")
    void postLogout_authentifie_redirigeVersLoginAvecLogout() throws Exception {
        createLibrarian();

        // 1. Login — establish an authenticated session
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .param("username", LIBRARIAN_EMAIL)
                        .param("password", PASSWORD)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        // 2. Logout with CSRF and the established session
        MvcResult logoutResult = mockMvc.perform(post("/logout")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertThat(logoutResult.getResponse().getRedirectedUrl())
                .as("Logout should redirect to logoutSuccessUrl('/login?logout')")
                .contains("?logout");
    }

    // ================================================================
    // CSRF
    // ================================================================

    @Test
    @DisplayName("POST /login avec cookie CSRF mais sans paramètre _csrf retourne 403")
    void postLogin_sansCsrfToken_retourne403() throws Exception {
        createLibrarian();

        // 1. GET /login to obtain a valid XSRF-TOKEN cookie
        String token = fetchCsrfTokenFromLoginGet();
        Cookie xsrfCookie = new Cookie("XSRF-TOKEN", token);

        // 2. POST /login WITH the cookie but WITHOUT the _csrf form parameter
        mockMvc.perform(post("/login")
                        .param("username", LIBRARIAN_EMAIL)
                        .param("password", PASSWORD)
                        .cookie(xsrfCookie))
                .andExpect(status().isForbidden());
    }

    // ================================================================
    // First-boot / setup flow
    // ================================================================

    @Test
    @DisplayName("GET /setup sans agents est accessible (permitAll + premier démarrage)")
    void getSetup_aucunAgent_permetAcces() throws Exception {
        // @BeforeEach already cleaned the agents table
        mockMvc.perform(get("/setup"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /login sans agents redirige vers /setup (LoginController first-boot guard)")
    void getLogin_aucunAgent_redirigeVersSetup() throws Exception {
        // @BeforeEach already cleaned the agents table
        MvcResult result = mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertThat(result.getResponse().getRedirectedUrl())
                .as("LoginController should redirect to /setup when no agent exists")
                .endsWith("/setup");
    }
}
