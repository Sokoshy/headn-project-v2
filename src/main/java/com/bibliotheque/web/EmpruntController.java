package com.bibliotheque.web;

import com.bibliotheque.config.CurrentAgentProvider;
import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.model.Agent;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.service.AuditService;
import com.bibliotheque.service.EmpruntService;
import com.bibliotheque.service.LoanActivityService;
import com.bibliotheque.service.LoanPreparationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class EmpruntController {

    private final EmpruntService empruntService;
    private final LoanActivityService loanActivityService;
    private final LoanPreparationService loanPreparationService;
    private final AuditService auditService;
    private final CurrentAgentProvider currentAgentProvider;

    public EmpruntController(EmpruntService empruntService,
                              LoanActivityService loanActivityService,
                              LoanPreparationService loanPreparationService,
                              AuditService auditService,
                              CurrentAgentProvider currentAgentProvider) {
        this.empruntService = empruntService;
        this.loanActivityService = loanActivityService;
        this.loanPreparationService = loanPreparationService;
        this.auditService = auditService;
        this.currentAgentProvider = currentAgentProvider;
    }

    @GetMapping("/emprunts")
    public String liste(@RequestParam(value = "searchUser", required = false) String searchUser,
                        @RequestParam(value = "searchBook", required = false) String searchBook,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "statutActif", defaultValue = "tous") String statutActif,
                        @RequestParam(value = "statutHistorique", defaultValue = "tous") String statutHistorique,
                        Model model) {
        model.addAttribute("loanActivity",
                loanActivityService.getLoanActivity(searchUser, searchBook, page, statutActif, statutHistorique));
        model.addAttribute("loanPreparation", loanPreparationService.getPreparation());
        model.addAttribute("searchUser", searchUser);
        model.addAttribute("searchBook", searchBook);
        model.addAttribute("statutActif", statutActif);
        model.addAttribute("statutHistorique", statutHistorique);
        return "emprunts/liste";
    }

    @GetMapping("/emprunts/{id}")
    public String voir(@PathVariable("id") Long id, Model model) {
        model.addAttribute("emprunt", empruntService.findDetailById(id));
        model.addAttribute("auditEntries", auditService.historiquePourEmprunt(id));
        return "emprunts/detail";
    }

    @PostMapping("/emprunts")
    public String creer(@ModelAttribute("utilisateurId") Long utilisateurId,
                        @ModelAttribute("livreId") Long livreId,
                        @RequestParam(value = "dateRetourPrevue", required = false) LocalDate dateRetourPrevue,
                        RedirectAttributes redirectAttributes) {
        try {
            Agent agent = currentAgentProvider.getCurrentAgent();
            Emprunt emprunt = empruntService.creer(utilisateurId, livreId, dateRetourPrevue, agent);
            redirectAttributes.addFlashAttribute("success",
                    "Emprunt enregistré : \"" + emprunt.getLivre().getTitre() +
                    "\" par " + emprunt.getUtilisateur().getNom());
            return "redirect:/emprunts";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/emprunts";
        }
    }

    @PostMapping("/emprunts/{id}/date-retour-prevue")
    public String corrigerDateRetourPrevue(@PathVariable("id") Long id,
                                            @RequestParam(value = "dateRetourPrevue", required = false) LocalDate dateRetourPrevue,
                                            RedirectAttributes redirectAttributes) {
        try {
            Agent agent = currentAgentProvider.getCurrentAgent();
            Emprunt emprunt = empruntService.corrigerDateRetourPrevue(id, dateRetourPrevue);
            auditService.enregistrer(emprunt, agent, com.bibliotheque.model.AuditAction.DATE_CORRECTION);
            redirectAttributes.addFlashAttribute("success",
                    "Date de retour prévue mise à jour pour \"" + emprunt.getLivre().getTitre() + "\"");
            return "redirect:/emprunts/" + id;
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/emprunts/" + id;
        }
    }

    @PostMapping("/emprunts/{id}/retour")
    public String effectuerRetour(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Agent agent = currentAgentProvider.getCurrentAgent();
            Emprunt emprunt = empruntService.effectuerRetour(id, agent);
            redirectAttributes.addFlashAttribute("success",
                    "Retour enregistré pour \"" + emprunt.getLivre().getTitre() + "\"");
            return "redirect:/emprunts";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/emprunts";
        }
    }
}