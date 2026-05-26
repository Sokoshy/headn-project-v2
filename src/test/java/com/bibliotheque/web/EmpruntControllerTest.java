package com.bibliotheque.web;

import com.bibliotheque.exception.EmpruntDejaRetourneException;
import com.bibliotheque.exception.LivreNonDisponibleException;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.activite.ActiveLoan;
import com.bibliotheque.model.activite.LoanActivity;
import com.bibliotheque.model.activite.LoanHistory;
import com.bibliotheque.model.activite.LoanStatus;
import com.bibliotheque.model.preparation.LoanPreparation;
import com.bibliotheque.model.preparation.SelectableBook;
import com.bibliotheque.model.preparation.SelectableUser;
import com.bibliotheque.service.EmpruntService;
import com.bibliotheque.service.LoanActivityService;
import com.bibliotheque.service.LoanPreparationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
class EmpruntControllerTest {

    @Mock
    private EmpruntService empruntService;

    @Mock
    private LoanActivityService loanActivityService;

    @Mock
    private LoanPreparationService loanPreparationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        EmpruntController controller = new EmpruntController(empruntService, loanActivityService,
                loanPreparationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void liste_recoitLoanActivityEtLoanPreparation() throws Exception {
        ActiveLoan actif = new ActiveLoan(10L, 1L, "Dune", 1L, "Alice",
                LocalDate.now().minusDays(5), null, LoanStatus.ACTIVE);
        LoanHistory hist = new LoanHistory(20L, 2L, "1984", 2L, "Bob",
                LocalDate.now().minusDays(60), LocalDate.now().minusDays(40),
                LocalDate.now().minusDays(30), LoanStatus.RETURNED);
        LoanActivity activity = new LoanActivity(List.of(actif), List.of(hist), 0, 1, 1L, false);

        SelectableBook book = new SelectableBook(1L, "Fondation", "Isaac Asimov");
        SelectableUser user = new SelectableUser(1L, "Alice");
        LoanPreparation preparation = new LoanPreparation(List.of(book), List.of(user), LocalDate.now().plusDays(30));

        when(loanActivityService.getLoanActivity(null, null, 0, "tous")).thenReturn(activity);
        when(loanPreparationService.getPreparation()).thenReturn(preparation);

        mockMvc.perform(get("/emprunts"))
                .andExpect(status().isOk())
                .andExpect(view().name("emprunts/liste"))
                .andExpect(model().attributeExists("loanActivity"))
                .andExpect(model().attributeExists("loanPreparation"));
    }

    @Test
    void liste_passesSearchParamsToService() throws Exception {
        LoanActivity activity = new LoanActivity(List.of(), List.of(), 0, 1, 0L, true);

        when(loanActivityService.getLoanActivity("Alice", "Dune", 0, "tous")).thenReturn(activity);
        when(loanPreparationService.getPreparation()).thenReturn(emptyPreparation());

        mockMvc.perform(get("/emprunts")
                        .param("searchUser", "Alice")
                        .param("searchBook", "Dune"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("searchUser", "Alice"))
                .andExpect(model().attribute("searchBook", "Dune"));
    }

    @Test
    void liste_passesPageParamToService() throws Exception {
        LoanActivity activity = new LoanActivity(List.of(), List.of(), 2, 3, 25L, false);

        when(loanActivityService.getLoanActivity(null, null, 2, "tous")).thenReturn(activity);
        when(loanPreparationService.getPreparation()).thenReturn(emptyPreparation());

        mockMvc.perform(get("/emprunts").param("page", "2"))
                .andExpect(status().isOk());
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
        when(empruntService.creer(eq(1L), eq(2L), any(LocalDate.class))).thenReturn(emprunt);

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
    void creer_livreNonDisponibleRedirigeAvecErreur() throws Exception {
        when(empruntService.creer(eq(1L), eq(2L), any(LocalDate.class)))
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

    @Test
    void corrigerDateRetourPrevue_succesRedirigeVersDetail() throws Exception {
        Livre livre = new Livre("Dune", "Frank Herbert");
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Emprunt emprunt = new Emprunt(utilisateur, livre);
        emprunt.setId(1L);
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(30));
        when(empruntService.corrigerDateRetourPrevue(eq(1L), any(LocalDate.class))).thenReturn(emprunt);

        mockMvc.perform(post("/emprunts/1/date-retour-prevue")
                        .param("dateRetourPrevue", LocalDate.now().plusDays(45).toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprunts/1"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
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
    void liste_passesStatutParamToService() throws Exception {
        LoanActivity activity = new LoanActivity(List.of(), List.of(), 0, 1, 0L, false);

        when(loanActivityService.getLoanActivity(null, null, 0, "en_retard")).thenReturn(activity);
        when(loanPreparationService.getPreparation()).thenReturn(emptyPreparation());

        mockMvc.perform(get("/emprunts").param("statut", "en_retard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("statut", "en_retard"));
    }

    @Test
    void liste_passesStatutDefaultTous() throws Exception {
        LoanActivity activity = new LoanActivity(List.of(), List.of(), 0, 1, 0L, false);

        when(loanActivityService.getLoanActivity(null, null, 0, "tous")).thenReturn(activity);
        when(loanPreparationService.getPreparation()).thenReturn(emptyPreparation());

        mockMvc.perform(get("/emprunts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("statut", "tous"));
    }

    private LoanPreparation emptyPreparation() {
        return new LoanPreparation(List.of(), List.of(), LocalDate.now().plusDays(30));
    }
}