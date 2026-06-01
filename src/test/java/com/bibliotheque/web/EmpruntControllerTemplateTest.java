package com.bibliotheque.web;

import com.bibliotheque.model.activite.LoanActivity;
import com.bibliotheque.model.activite.LoanHistory;
import com.bibliotheque.model.activite.LoanStatus;
import com.bibliotheque.model.preparation.LoanPreparation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EmpruntControllerTemplateTest {

    private final SpringTemplateEngine templateEngine = createTemplateEngine();

    @Test
    @DisplayName("returned loan filters render history rows without embedded error page")
    void liste_withReturnedFilter_rendersHistoryWithoutEmbeddedErrorPage() {
        LoanHistory history = new LoanHistory(
                20L,
                2L,
                "1984",
                3L,
                "Bob Martin",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 15),
                LoanStatus.RETURNED
        );
        LoanActivity activity = new LoanActivity(List.of(), 0L, List.of(history), 0, 1, 1L, 1L, false);
        LoanPreparation preparation = new LoanPreparation(List.of(), List.of(), LocalDate.of(2026, 2, 1));

        WebContext context = webContext(Map.of(
                "loanActivity", activity,
                "loanPreparation", preparation,
                "statutActif", "tous",
                "statutHistorique", "termines",
                "currentPath", "/emprunts",
                "_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token")
        ));

        String html = templateEngine.process("emprunts/liste", context);

        assertThat(html).contains("1984", "Bob Martin", "Terminé");
        assertThat(html).doesNotContain("Error 500");
    }

    private static WebContext webContext(Map<String, Object> variables) {
        MockServletContext servletContext = new MockServletContext();
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext, "GET", "/emprunts");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
        Map<String, Object> allVariables = new HashMap<>(variables);
        allVariables.put("searchUser", null);
        allVariables.put("searchBook", null);
        return new WebContext(application.buildExchange(request, response), Locale.FRANCE, allVariables);
    }

    private static SpringTemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
