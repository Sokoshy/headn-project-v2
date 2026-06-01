package com.bibliotheque.web;

import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.model.Role;
import com.bibliotheque.service.AgentForm;
import com.bibliotheque.service.AgentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * First-admin setup. The /setup page is reachable only while the agents table is empty.
 * After the first agent is created, the setup flow is closed and /setup redirects to /login.
 */
@Controller
public class SetupController {

    private final AgentService agentService;

    public SetupController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/setup")
    public String setupForm(Model model, RedirectAttributes redirectAttributes) {
        if (agentService.existsAny()) {
            redirectAttributes.addFlashAttribute("error",
                    "Le setup est déjà terminé. Veuillez vous connecter.");
            return "redirect:/login";
        }
        model.addAttribute("role", Role.ADMIN);
        return "setup";
    }

    @PostMapping("/setup")
    public String setupSubmit(@RequestParam("nom") String nom,
                               @RequestParam("email") String email,
                               @RequestParam("motDePasse") String motDePasse,
                               @RequestParam("motDePasseConfirmation") String motDePasseConfirmation,
                               RedirectAttributes redirectAttributes) {
        if (agentService.existsAny()) {
            return "redirect:/login";
        }
        try {
            agentService.creer(new AgentForm(
                    nom,
                    email,
                    motDePasse,
                    motDePasseConfirmation,
                    Role.ADMIN,
                    null
            ));
            redirectAttributes.addFlashAttribute("success",
                    "Compte administrateur créé. Vous pouvez maintenant vous connecter.");
            return "redirect:/login";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("nom", nom);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/setup";
        }
    }
}