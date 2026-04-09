package com.bibliotheque.web;

import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.service.EmpruntService;
import com.bibliotheque.service.LivreService;
import com.bibliotheque.service.UtilisateurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EmpruntController {

    private final EmpruntService empruntService;
    private final LivreService livreService;
    private final UtilisateurService utilisateurService;

    public EmpruntController(EmpruntService empruntService,
                              LivreService livreService,
                              UtilisateurService utilisateurService) {
        this.empruntService = empruntService;
        this.livreService = livreService;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/emprunts")
    public String liste(Model model) {
        model.addAttribute("empruntsActifs", empruntService.findActifs());
        model.addAttribute("historique", empruntService.findHistorique());
        model.addAttribute("empruntsEnRetard", empruntService.findEnRetard());
        model.addAttribute("livresDisponibles", livreService.findDisponibles());
        model.addAttribute("utilisateurs", utilisateurService.findAll());
        return "emprunts/liste";
    }

    @GetMapping("/emprunts/{id}")
    public String voir(@PathVariable Long id, Model model) {
        model.addAttribute("emprunt", empruntService.findDetailById(id));
        return "emprunts/detail";
    }

    @PostMapping("/emprunts")
    public String creer(@ModelAttribute("utilisateurId") Long utilisateurId,
                        @ModelAttribute("livreId") Long livreId,
                        RedirectAttributes redirectAttributes) {
        try {
            Emprunt emprunt = empruntService.creer(utilisateurId, livreId);
            redirectAttributes.addFlashAttribute("success",
                    "Emprunt enregistré : \"" + emprunt.getLivre().getTitre() +
                    "\" par " + emprunt.getUtilisateur().getNom());
            return "redirect:/emprunts";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/emprunts";
        }
    }

    @PostMapping("/emprunts/{id}/retour")
    public String effectuerRetour(@PathVariable Long id, RedirectAttributes redirectAttributes) {
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
