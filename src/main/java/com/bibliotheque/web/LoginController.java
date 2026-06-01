package com.bibliotheque.web;

import com.bibliotheque.service.AgentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the login form. Spring Security's formLogin() is wired with loginPage("/login"),
 * failureUrl("/login?error") and logoutSuccessUrl("/login?logout"), so this controller
 * must answer GET /login (Spring's defaults render the form for the underlying
 * /login POST and redirect on success without a controller).
 *
 * <p>First-boot guard: if no agents exist in the database, the login form is unreachable
 * anyway (no one can log in), so we redirect to /setup where the first admin is created.</p>
 */
@Controller
public class LoginController {

    private final AgentService agentService;

    public LoginController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/login")
    public String login() {
        if (!agentService.existsAny()) {
            return "redirect:/setup";
        }
        return "login";
    }
}
