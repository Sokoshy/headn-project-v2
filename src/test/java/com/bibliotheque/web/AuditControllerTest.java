package com.bibliotheque.web;

import com.bibliotheque.model.AuditAction;
import com.bibliotheque.model.audit.AuditEntry;
import com.bibliotheque.service.AgentService;
import com.bibliotheque.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    @Mock
    private AuditService auditService;

    @Mock
    private AgentService agentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuditController controller = new AuditController(auditService, agentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /audit sans filtres appelle le service avec des valeurs nulles")
    void journal_sansFiltres_appelleServiceAvecNulls() throws Exception {
        Page<AuditEntry> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(auditService.lister(isNull(), isNull(), isNull(), isNull(), eq(0))).thenReturn(emptyPage);

        mockMvc.perform(get("/audit"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("entries"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 0))
                .andExpect(model().attribute("totalItems", 0L));
    }

    @Test
    @DisplayName("GET /audit avec tous les filtres les passe au service")
    void journal_avecTousLesFiltres_lesPasseAuService() throws Exception {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 6, 1);
        Page<AuditEntry> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(auditService.lister(eq(1L), eq(AuditAction.CREATION), eq(from), eq(to), eq(0)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/audit")
                        .param("agentId", "1")
                        .param("action", "CREATION")
                        .param("from", "2026-01-01")
                        .param("to", "2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedAgentId", 1L))
                .andExpect(model().attribute("selectedAction", "CREATION"))
                .andExpect(model().attribute("selectedFrom", from))
                .andExpect(model().attribute("selectedTo", to));

        verify(auditService).lister(eq(1L), eq(AuditAction.CREATION), eq(from), eq(to), eq(0));
    }

    @Test
    @DisplayName("GET /audit avec action inconnue passe null au service")
    void journal_actionInconnue_passeNullAuService() throws Exception {
        Page<AuditEntry> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(auditService.lister(isNull(), isNull(), isNull(), isNull(), eq(0))).thenReturn(emptyPage);

        mockMvc.perform(get("/audit")
                        .param("action", "BOGUS"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedAction", "BOGUS"));

        verify(auditService).lister(isNull(), isNull(), isNull(), isNull(), eq(0));
    }

    @Test
    @DisplayName("GET /audit avec action en minuscules est normalisée en CREATION")
    void journal_actionMinuscule_estNormalisee() throws Exception {
        Page<AuditEntry> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(auditService.lister(isNull(), eq(AuditAction.CREATION), isNull(), isNull(), eq(0)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/audit")
                        .param("action", "creation"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedAction", "creation"));

        verify(auditService).lister(isNull(), eq(AuditAction.CREATION), isNull(), isNull(), eq(0));
    }

    @Test
    @DisplayName("GET /audit avec page=2 est passé au service")
    void journal_pageDeux_estPasseeAuService() throws Exception {
        Page<AuditEntry> emptyPage = new PageImpl<>(List.of(), PageRequest.of(2, 20), 0);
        when(auditService.lister(isNull(), isNull(), isNull(), isNull(), eq(2))).thenReturn(emptyPage);

        mockMvc.perform(get("/audit")
                        .param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentPage", 2));

        verify(auditService).lister(isNull(), isNull(), isNull(), isNull(), eq(2));
    }

    @Test
    @DisplayName("GET /audit contient les agents et actions dans le modèle")
    void journal_modeleContientAgentsEtActions() throws Exception {
        Page<AuditEntry> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(auditService.lister(isNull(), isNull(), isNull(), isNull(), eq(0))).thenReturn(emptyPage);
        when(agentService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/audit"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("agents"))
                .andExpect(model().attributeExists("actions"));
    }
}
