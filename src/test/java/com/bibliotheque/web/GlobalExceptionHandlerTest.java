package com.bibliotheque.web;

import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.exception.LivreNotFoundException;
import com.bibliotheque.service.LivreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private LivreService livreService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LivreController controller = new LivreController(livreService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleResourceNotFound_redirigeVersAccueil() throws Exception {
        doThrow(new LivreNotFoundException(999L)).when(livreService).findById(999L);

        mockMvc.perform(get("/livres/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void handleBusinessException_redirigeVersAccueil() throws Exception {
        doThrow(new BusinessException("Erreur métier")).when(livreService).findById(1L);

        mockMvc.perform(get("/livres/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("error"));
    }
}
