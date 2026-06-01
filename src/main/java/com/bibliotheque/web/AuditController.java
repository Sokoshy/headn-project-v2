package com.bibliotheque.web;

import com.bibliotheque.model.AuditAction;
import com.bibliotheque.model.audit.AuditEntry;
import com.bibliotheque.service.AgentService;
import com.bibliotheque.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Controller
public class AuditController {

    private final AuditService auditService;
    private final AgentService agentService;

    public AuditController(AuditService auditService, AgentService agentService) {
        this.auditService = auditService;
        this.agentService = agentService;
    }

    @GetMapping("/audit")
    public String journal(@RequestParam(value = "agentId", required = false) Long agentId,
                           @RequestParam(value = "action", required = false) String action,
                           @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                           @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           Model model) {
        AuditAction actionEnum = parseAction(action);

        Page<AuditEntry> resultPage = auditService.lister(agentId, actionEnum, from, to, page);

        model.addAttribute("entries", resultPage.getContent());
        model.addAttribute("currentPage", resultPage.getNumber());
        model.addAttribute("totalPages", resultPage.getTotalPages());
        model.addAttribute("totalItems", resultPage.getTotalElements());
        model.addAttribute("agents", agentService.findAll());
        model.addAttribute("actions", Arrays.asList(AuditAction.values()));
        model.addAttribute("selectedAgentId", agentId);
        model.addAttribute("selectedAction", action);
        model.addAttribute("selectedFrom", from);
        model.addAttribute("selectedTo", to);
        return "audit/liste";
    }

    private AuditAction parseAction(String action) {
        if (action == null || action.isBlank()) {
            return null;
        }
        try {
            return AuditAction.valueOf(action.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}