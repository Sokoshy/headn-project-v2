package com.bibliotheque.web;

import com.bibliotheque.service.EmpruntService;
import com.bibliotheque.service.LivreService;
import com.bibliotheque.service.UtilisateurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final LivreService livreService;
    private final UtilisateurService utilisateurService;
    private final EmpruntService empruntService;

    public HomeController(LivreService livreService,
                          UtilisateurService utilisateurService,
                          EmpruntService empruntService) {
        this.livreService = livreService;
        this.utilisateurService = utilisateurService;
        this.empruntService = empruntService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("totalLivres", livreService.countTotal());
        model.addAttribute("livresDisponibles", livreService.countDisponibles());
        model.addAttribute("totalUtilisateurs", utilisateurService.countTotal());
        model.addAttribute("empruntsActifs", empruntService.countActifs());
        model.addAttribute("empruntsEnRetard", empruntService.countEnRetard());
        return "index";
    }
}
