package com.bibliotheque.web;

import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.model.Emprunt;
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

    public EmpruntController(EmpruntService empruntService,
                              LoanActivityService loanActivityService,
                              LoanPreparationService loanPreparationService) {
        this.empruntService = empruntService;
        this.loanActivityService = loanActivityService;
        this.loanPreparationService = loanPreparationService;
    }

    @GetMapping("/emprunts")
    public String liste(@RequestParam(value = "searchUser", required = false) String searchUser,
                        @RequestParam(value = "searchBook", required = false) String searchBook,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "statut", defaultValue = "tous") String statut,
                        Model model) {
        model.addAttribute("loanActivity",
                loanActivityService.getLoanActivity(searchUser, searchBook, page, statut));
        model.addAttribute("loanPreparation", loanPreparationService.getPreparation());
        model.addAttribute("searchUser", searchUser);
        model.addAttribute("searchBook", searchBook);
        model.addAttribute("statut", statut);
        return "emprunts/liste";
    }

    @GetMapping("/emprunts/{id}")
    public String voir(@PathVariable("id") Long id, Model model) {
        model.addAttribute("emprunt", empruntService.findDetailById(id));
        return "emprunts/detail";
    }

    @PostMapping("/emprunts")
    public String creer(@ModelAttribute("utilisateurId") Long utilisateurId,
                        @ModelAttribute("livreId") Long livreId,
                        @RequestParam(value = "dateRetourPrevue", required = false) LocalDate dateRetourPrevue,
                        RedirectAttributes redirectAttributes) {
        try {
            Emprunt emprunt = empruntService.creer(utilisateurId, livreId, dateRetourPrevue);
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
            Emprunt emprunt = empruntService.corrigerDateRetourPrevue(id, dateRetourPrevue);
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
            Emprunt emprunt = empruntService.effectuerRetour(id);
            redirectAttributes.addFlashAttribute("success",
                    "Retour enregistré pour \"" + emprunt.getLivre().getTitre() + "\"");
            return "redirect:/emprunts";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/emprunts";
        }
    }
}
