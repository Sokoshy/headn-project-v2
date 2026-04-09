package com.bibliotheque.web;

import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @InitBinder("utilisateur")
    void initUtilisateurBinder(WebDataBinder binder) {
        binder.setAllowedFields("id", "nom", "email");
    }

    @GetMapping("/utilisateurs")
    public String liste(@ModelAttribute("recherche") String recherche, Model model) {
        model.addAttribute("utilisateurs", utilisateurService.findByRecherche(recherche));
        model.addAttribute("recherche", recherche);
        return "utilisateurs/liste";
    }

    @GetMapping("/utilisateurs/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "utilisateurs/formulaire";
    }

    @GetMapping("/utilisateurs/{id}")
    public String voir(@PathVariable Long id, Model model) {
        model.addAttribute("utilisateur", utilisateurService.findById(id));
        return "utilisateurs/detail";
    }

    @GetMapping("/utilisateurs/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("utilisateur", utilisateurService.findById(id));
        return "utilisateurs/formulaire";
    }

    @PostMapping("/utilisateurs")
    public String creer(@Valid @ModelAttribute("utilisateur") Utilisateur utilisateur,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return "utilisateurs/formulaire";
        }

        try {
            Utilisateur utilisateurCree = utilisateurService.creer(utilisateur);
            redirectAttributes.addFlashAttribute("success", "L'utilisateur \"" + utilisateurCree.getNom() + "\" a été créé avec succès.");
            return "redirect:/utilisateurs";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "utilisateurs/formulaire";
        }
    }

    @PostMapping("/utilisateurs/{id}")
    public String modifier(@PathVariable Long id,
                           @Valid @ModelAttribute("utilisateur") Utilisateur utilisateur,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        utilisateur.setId(id);
        if (bindingResult.hasErrors()) {
            return "utilisateurs/formulaire";
        }

        try {
            Utilisateur utilisateurModifie = utilisateurService.modifier(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "L'utilisateur \"" + utilisateurModifie.getNom() + "\" a été modifié avec succès.");
            return "redirect:/utilisateurs";
        } catch (BusinessException e) {
            utilisateur.setId(id);
            model.addAttribute("error", e.getMessage());
            return "utilisateurs/formulaire";
        }
    }
    @PostMapping("/utilisateurs/{id}/delete")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.findById(id);
            utilisateurService.supprimer(id);
            redirectAttributes.addFlashAttribute("success", "L'utilisateur \"" + utilisateur.getNom() + "\" a été supprimé avec succès.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/utilisateurs";
    }
}
