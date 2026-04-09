package com.bibliotheque.web;

import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.model.Livre;
import com.bibliotheque.service.LivreService;
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
public class LivreController {

    private final LivreService livreService;

    public LivreController(LivreService livreService) {
        this.livreService = livreService;
    }

    @InitBinder("livre")
    void initLivreBinder(WebDataBinder binder) {
        binder.setAllowedFields("id", "titre", "auteur");
    }

    @GetMapping("/livres")
    public String liste(@ModelAttribute("recherche") String recherche, Model model) {
        model.addAttribute("livres", livreService.findByRecherche(recherche));
        model.addAttribute("recherche", recherche);
        return "livres/liste";
    }

    @GetMapping("/livres/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("livre", new Livre());
        return "livres/formulaire";
    }

    @GetMapping("/livres/{id}")
    public String voir(@PathVariable Long id, Model model) {
        model.addAttribute("livre", livreService.findById(id));
        return "livres/detail";
    }

    @GetMapping("/livres/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("livre", livreService.findById(id));
        return "livres/formulaire";
    }

    @PostMapping("/livres")
    public String creer(@Valid @ModelAttribute("livre") Livre livre,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return "livres/formulaire";
        }

        try {
            Livre livreCree = livreService.creer(livre);
            redirectAttributes.addFlashAttribute("success", "Le livre \"" + livreCree.getTitre() + "\" a été créé avec succès.");
            return "redirect:/livres";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "livres/formulaire";
        }
    }

    @PostMapping("/livres/{id}")
    public String modifier(@PathVariable Long id,
                           @Valid @ModelAttribute("livre") Livre livre,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        livre.setId(id);
        if (bindingResult.hasErrors()) {
            return "livres/formulaire";
        }

        try {
            Livre livreModifie = livreService.modifier(id, livre);
            redirectAttributes.addFlashAttribute("success", "Le livre \"" + livreModifie.getTitre() + "\" a été modifié avec succès.");
            return "redirect:/livres";
        } catch (BusinessException e) {
            livre.setId(id);
            model.addAttribute("error", e.getMessage());
            return "livres/formulaire";
        }
    }

    @PostMapping("/livres/{id}/delete")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            livreService.supprimer(id);
            redirectAttributes.addFlashAttribute("success", "Le livre a été supprimé avec succès.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/livres";
    }
}
