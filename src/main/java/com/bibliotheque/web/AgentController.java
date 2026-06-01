package com.bibliotheque.web;

import com.bibliotheque.config.CurrentAgentProvider;
import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.model.Agent;
import com.bibliotheque.model.Role;
import com.bibliotheque.service.AgentForm;
import com.bibliotheque.service.AgentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
public class AgentController {

    private final AgentService agentService;
    private final CurrentAgentProvider currentAgentProvider;

    public AgentController(AgentService agentService, CurrentAgentProvider currentAgentProvider) {
        this.agentService = agentService;
        this.currentAgentProvider = currentAgentProvider;
    }

    @ModelAttribute("roles")
    public List<Role> roles() {
        return Arrays.asList(Role.values());
    }

    @GetMapping("/agents")
    public String liste(Model model) {
        model.addAttribute("agents", agentService.findAll());
        return "agents/liste";
    }

    @GetMapping("/agents/new")
    public String nouveau(Model model) {
        model.addAttribute("agent", new AgentForm("", "", "", "", Role.LIBRARIAN, null));
        model.addAttribute("editMode", false);
        return "agents/formulaire";
    }

    @GetMapping("/agents/{id}")
    public String voir(@PathVariable("id") Long id, Model model) {
        Agent agent = agentService.findById(id);
        model.addAttribute("agent", agent);
        return "agents/detail";
    }

    @GetMapping("/agents/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model) {
        Agent agent = agentService.findById(id);
        model.addAttribute("agentId", agent.getId());
        model.addAttribute("agent", new AgentForm(
                agent.getNom(),
                agent.getEmail(),
                null,
                null,
                agent.getRole(),
                agent.getTelephone()
        ));
        model.addAttribute("editMode", true);
        return "agents/formulaire";
    }

    @PostMapping("/agents")
    public String creer(@ModelAttribute("agent") AgentForm form,
                         RedirectAttributes redirectAttributes) {
        try {
            Agent agent = agentService.creer(form);
            redirectAttributes.addFlashAttribute("success",
                    "L'agent \"" + agent.getNom() + "\" a été créé avec succès.");
            return "redirect:/agents";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/agents/new";
        }
    }

    @PostMapping("/agents/{id}")
    public String modifier(@PathVariable("id") Long id,
                            @ModelAttribute("agent") AgentForm form,
                            RedirectAttributes redirectAttributes) {
        try {
            Agent agent = agentService.modifier(id, form);
            redirectAttributes.addFlashAttribute("success",
                    "L'agent \"" + agent.getNom() + "\" a été modifié avec succès.");
            return "redirect:/agents";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/agents/" + id + "/edit";
        }
    }

    @PostMapping("/agents/{id}/deactivate")
    public String desactiver(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Agent currentAgent = currentAgentProvider.getCurrentAgent();
            if (currentAgent.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error",
                        "Vous ne pouvez pas désactiver votre propre compte.");
                return "redirect:/agents";
            }
            Agent agent = agentService.findById(id);
            agentService.desactiver(id);
            redirectAttributes.addFlashAttribute("success",
                    "L'agent \"" + agent.getNom() + "\" a été désactivé.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/agents";
    }

    @PostMapping("/agents/{id}/reactivate")
    public String reactiver(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Agent agent = agentService.findById(id);
            agentService.reactiver(id);
            redirectAttributes.addFlashAttribute("success",
                    "L'agent \"" + agent.getNom() + "\" a été réactivé.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/agents";
    }
}