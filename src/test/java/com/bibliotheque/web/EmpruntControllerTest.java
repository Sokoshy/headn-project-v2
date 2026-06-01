package com.bibliotheque.web;

import com.bibliotheque.config.CurrentAgentProvider;
import com.bibliotheque.exception.EmpruntDejaRetourneException;
import com.bibliotheque.exception.LivreNonDisponibleException;
import com.bibliotheque.model.Agent;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Role;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.activite.ActiveLoan;
import com.bibliotheque.model.activite.LoanActivity;
import com.bibliotheque.model.activite.LoanHistory;
import com.bibliotheque.model.activite.LoanStatus;
import com.bibliotheque.model.audit.AuditEntry;
import com.bibliotheque.model.preparation.LoanPreparation;
import com.bibliotheque.model.preparation.SelectableBook;
import com.bibliotheque.model.preparation.SelectableUser;
import com.bibliotheque.service.AuditService;
import com.bibliotheque.service.EmpruntService;
import com.bibliotheque.service.LoanActivityService;
import com.bibliotheque.service.LoanPreparationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
class EmpruntControllerTest {

    @Mock
    private EmpruntService empruntService;

    @Mock
    private LoanActivityService loanActivityService;

    @Mock
    private LoanPreparationService loanPreparationService;

    @Mock
    private AuditService auditService;

    @Mock
    private CurrentAgentProvider currentAgentProvider;

    private MockMvc mockMvc;

    private Agent agent;

    @BeforeEach
    void setUp() {
        agent = new Agent("Alice", "alice@bib.fr", "HASH", Role.LIBRARIAN);
        agent.setId(1L);

        EmpruntController controller = new EmpruntController(empruntService, loanActivityService,
                loanPreparationService, auditService, currentAgentProvider);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Authenticated context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "alice@bib.fr", "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_LIBRARIAN"))));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Liste : reçoit LoanActivity et LoanPreparation")
    void liste_recoitLoanActivityEtLoanPreparation() throws Exception {
        ActiveLoan actif = new ActiveLoan(10L, 1L, "Dune", 1L, "Alice",
                LocalDate.now().minusDays(5), null, LoanStatus.ACTIVE);
        LoanHistory hist = new LoanHistory(20L, 2L, "1984", 2L, "Bob",
                LocalDate.now().minusDays(60), LocalDate.now().minusDays(40),
                LocalDate.now().minusDays(30), LoanStatus.RETURNED);
        LoanActivity activity = new LoanActivity(List.of(actif), 1L, List.of(hist), 0, 1, 1L, 1L, false);

        SelectableBook book = new SelectableBook(1L, "Fondation", "Isaac Asimov");
        SelectableUser user = new SelectableUser(1L, "Alice");
        LoanPreparation preparation = new LoanPreparation(List.of(book), List.of(user), LocalDate.now().plusDays(30));

        when(loanActivityService.getLoanActivity(isNull(), isNull(), eq(0), eq("tous"), eq("tous"))).thenReturn(activity);
        when(loanPreparationService.getPreparation()).thenReturn(preparation);

        mockMvc.perform(get("/emprunts"))
                .andExpect(status().isOk())
                .andExpect(view().name("emprunts/liste"))
                .andExpect(model().attributeExists("loanActivity"))
                .andExpect(model().attributeExists("loanPreparation"));
    }

    @Test
    @DisplayName("Liste : passe les paramètres de recherche au service")
    void liste_passesSearchParamsToService() throws Exception {
        LoanActivity activity = new LoanActivity(List.of(), 0L, List.of(), 0, 1, 0L, 0L, true);

        when(loanActivityService.getLoanActivity(eq("Alice"), eq("Dune"), eq(0), eq("tous"), eq("tous"))).thenReturn(activity);
        when(loanPreparationService.getPreparation()).thenReturn(emptyPreparation());

        mockMvc.perform(get("/emprunts")
                        .param("searchUser", "Alice")
                        .param("searchBook", "Dune"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("searchUser", "Alice"))
                .andExpect(model().attribute("searchBook", "Dune"));
    }

    @Test
    @DisplayName("Liste : passe le paramètre de page au service")
    void liste_passesPageParamToService() throws Exception {
        LoanActivity activity = new LoanActivity(List.of(), 0L, List.of(), 2, 3, 25L, 25L, false);

        when(loanActivityService.getLoanActivity(isNull(), isNull(), eq(2), eq("tous"), eq("tous"))).thenReturn(activity);
        when(loanPreparationService.getPreparation()).thenReturn(emptyPreparation());

        mockMvc.perform(get("/emprunts").param("page", "2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Détail : affiche le détail avec l'audit")
    void voir_afficheLeDetailAvecAudit() throws Exception {
        Livre livre = new Livre("Dune", "Frank Herbert");
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Emprunt emprunt = new Emprunt(utilisateur, livre);
        emprunt.setId(1L);
        when(empruntService.findDetailById(1L)).thenReturn(emprunt);
        when(auditService.historiquePourEmprunt(1L)).thenReturn(List.of(
                new AuditEntry(1L, 1L, "Dune", "Alice", 1L, "Alice", "alice@bib.fr",
                        com.bibliotheque.model.AuditAction.CREATION, LocalDateTime.now())
        ));

        mockMvc.perform(get("/emprunts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("emprunts/detail"))
                .andExpect(model().attribute("emprunt", emprunt))
                .andExpect(model().attributeExists("auditEntries"));
    }

    @Test
    @DisplayName("Création : succès redirige vers la liste")
    void creer_succesRedirigeVersListe() throws Exception {
        Livre livre = new Livre("Dune", "Frank Herbert");
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Emprunt emprunt = new Emprunt(utilisateur, livre);
        emprunt.setId(1L);
        when(currentAgentProvider.getCurrentAgent()).thenReturn(agent);
        when(empruntService.creer(eq(1L), eq(2L), any(LocalDate.class), eq(agent))).thenReturn(emprunt);

        mockMvc.perform(post("/emprunts")
                        .param("utilisateurId", "1")
                        .param("livreId", "2")
                        .param("dateRetourPrevue", LocalDate.now().plusDays(30).toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprunts"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @DisplayName("Création : livre non disponible redirige avec erreur")
    void creer_livreNonDisponibleRedirigeAvecErreur() throws Exception {
        when(currentAgentProvider.getCurrentAgent()).thenReturn(agent);
        when(empruntService.creer(eq(1L), eq(2L), any(LocalDate.class), eq(agent)))
                .thenThrow(new LivreNonDisponibleException("Dune"));

        mockMvc.perform(post("/emprunts")
                        .param("utilisateurId", "1")
                        .param("livreId", "2")
                        .param("dateRetourPrevue", LocalDate.now().plusDays(30).toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprunts"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("Retour : succès redirige vers la liste")
    void effectuerRetour_succesRedirigeVersListe() throws Exception {
        Livre livre = new Livre("Dune", "Frank Herbert");
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Emprunt emprunt = new Emprunt(utilisateur, livre);
        emprunt.setId(1L);
        when(currentAgentProvider.getCurrentAgent()).thenReturn(agent);
        when(empruntService.effectuerRetour(1L, agent)).thenReturn(emprunt);

        mockMvc.perform(post("/emprunts/1/retour")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprunts"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @DisplayName("Retour : emprunt déjà retourné redirige avec erreur")
    void effectuerRetour_empruntDejaRetourneRedirigeAvecErreur() throws Exception {
        when(currentAgentProvider.getCurrentAgent()).thenReturn(agent);
        when(empruntService.effectuerRetour(1L, agent))
                .thenThrow(new EmpruntDejaRetourneException(1L));

        mockMvc.perform(post("/emprunts/1/retour")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprunts"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("Correction date : succès redirige vers le détail")
    void corrigerDateRetourPrevue_succesRedirigeVersDetail() throws Exception {
        Livre livre = new Livre("Dune", "Frank Herbert");
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Emprunt emprunt = new Emprunt(utilisateur, livre);
        emprunt.setId(1L);
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(30));
        when(currentAgentProvider.getCurrentAgent()).thenReturn(agent);
        when(empruntService.corrigerDateRetourPrevue(eq(1L), any(LocalDate.class))).thenReturn(emprunt);

        mockMvc.perform(post("/emprunts/1/date-retour-prevue")
                        .param("dateRetourPrevue", LocalDate.now().plusDays(45).toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprunts/1"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @DisplayName("Correction date : erreur redirige vers le détail")
    void corrigerDateRetourPrevue_erreurRedirigeVersDetail() throws Exception {
        when(empruntService.corrigerDateRetourPrevue(eq(1L), any(LocalDate.class)))
                .thenThrow(new com.bibliotheque.exception.DateRetourPrevueDansLePasseException());

        mockMvc.perform(post("/emprunts/1/date-retour-prevue")
                        .param("dateRetourPrevue", LocalDate.now().minusDays(1).toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprunts/1"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("Liste : passe le paramètre statutActif au service")
    void liste_passesStatutActifParamToService() throws Exception {
        LoanActivity activity = new LoanActivity(List.of(), 0L, List.of(), 0, 1, 0L, 0L, false);

        when(loanActivityService.getLoanActivity(isNull(), isNull(), eq(0), eq("en_retard"), eq("tous"))).thenReturn(activity);
        when(loanPreparationService.getPreparation()).thenReturn(emptyPreparation());

        mockMvc.perform(get("/emprunts").param("statutActif", "en_retard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("statutActif", "en_retard"))
                .andExpect(model().attribute("statutHistorique", "tous"));
    }

    @Test
    @DisplayName("Liste : statutActif= en_retard est transmis au service avec statutHistorique par défaut")
    void liste_statutActifEnRetard_forwardsOnlyActiveFilter() throws Exception {
        LoanActivity activity = new LoanActivity(List.of(), 0L, List.of(), 0, 1, 0L, 0L, false);

        when(loanActivityService.getLoanActivity(isNull(), isNull(), eq(0), eq("en_retard"), eq("tous")))
                .thenReturn(activity);
        when(loanPreparationService.getPreparation()).thenReturn(emptyPreparation());

        mockMvc.perform(get("/emprunts").param("statutActif", "en_retard"))
                .andExpect(status().isOk());

        verify(loanActivityService).getLoanActivity(isNull(), isNull(), eq(0), eq("en_retard"), eq("tous"));
    }

    @Test
    @DisplayName("Liste : statutHistorique=termines est transmis au service avec statutActif par défaut")
    void liste_statutHistoriqueTermines_forwardsOnlyHistoryFilter() throws Exception {
        LoanActivity activity = new LoanActivity(List.of(), 0L, List.of(), 0, 1, 0L, 0L, false);

        when(loanActivityService.getLoanActivity(isNull(), isNull(), eq(0), eq("tous"), eq("termines")))
                .thenReturn(activity);
        when(loanPreparationService.getPreparation()).thenReturn(emptyPreparation());

        mockMvc.perform(get("/emprunts").param("statutHistorique", "termines"))
                .andExpect(status().isOk());

        verify(loanActivityService).getLoanActivity(isNull(), isNull(), eq(0), eq("tous"), eq("termines"));
    }

    @Test
    @DisplayName("Liste : statutActif et statutHistorique sont transmis simultanément au service")
    void liste_bothFilters_areForwardedSimultaneously() throws Exception {
        LoanActivity activity = new LoanActivity(List.of(), 0L, List.of(), 0, 1, 0L, 0L, false);

        when(loanActivityService.getLoanActivity(isNull(), isNull(), eq(0), eq("en_retard"), eq("termines")))
                .thenReturn(activity);
        when(loanPreparationService.getPreparation()).thenReturn(emptyPreparation());

        mockMvc.perform(get("/emprunts")
                        .param("statutActif", "en_retard")
                        .param("statutHistorique", "termines"))
                .andExpect(status().isOk());

        verify(loanActivityService).getLoanActivity(isNull(), isNull(), eq(0), eq("en_retard"), eq("termines"));
    }

    @Test
    @DisplayName("Liste : statutActif et statutHistorique sont exposés au modèle")
    void liste_bothStatuses_areExposedInModel() throws Exception {
        LoanActivity activity = new LoanActivity(List.of(), 0L, List.of(), 0, 1, 0L, 0L, false);

        when(loanActivityService.getLoanActivity(isNull(), isNull(), eq(0), eq("en_retard"), eq("termines")))
                .thenReturn(activity);
        when(loanPreparationService.getPreparation()).thenReturn(emptyPreparation());

        mockMvc.perform(get("/emprunts")
                        .param("statutActif", "en_retard")
                        .param("statutHistorique", "termines"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("statutActif", "en_retard"))
                .andExpect(model().attribute("statutHistorique", "termines"));
    }

    @Test
    @DisplayName("Liste : le statutActif par défaut est 'tous'")
    void liste_passesStatutActifDefaultTous() throws Exception {
        LoanActivity activity = new LoanActivity(List.of(), 0L, List.of(), 0, 1, 0L, 0L, false);

        when(loanActivityService.getLoanActivity(isNull(), isNull(), eq(0), eq("tous"), eq("tous"))).thenReturn(activity);
        when(loanPreparationService.getPreparation()).thenReturn(emptyPreparation());

        mockMvc.perform(get("/emprunts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("statutActif", "tous"))
                .andExpect(model().attribute("statutHistorique", "tous"));
    }

    private LoanPreparation emptyPreparation() {
        return new LoanPreparation(List.of(), List.of(), LocalDate.now().plusDays(30));
    }
}