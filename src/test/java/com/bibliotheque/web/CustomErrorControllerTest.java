package com.bibliotheque.web;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;

class CustomErrorControllerTest {

    private final CustomErrorController controller = new CustomErrorController();

    @Test
    void error_status404_retourneVue404() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 404);

        ModelAndView mav = controller.error(request);

        assertThat(mav.getViewName()).isEqualTo("error/404");
        assertThat(mav.getModel().get("status")).isEqualTo(404);
    }

    @Test
    void error_status403_retourneVue403() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 403);

        ModelAndView mav = controller.error(request);

        assertThat(mav.getViewName()).isEqualTo("error/403");
        assertThat(mav.getModel().get("status")).isEqualTo(403);
    }

    @Test
    void error_status401_retourneVue401() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 401);

        ModelAndView mav = controller.error(request);

        assertThat(mav.getViewName()).isEqualTo("error/401");
        assertThat(mav.getModel().get("status")).isEqualTo(401);
    }

    @Test
    void error_status500_retourneVue500() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 500);

        ModelAndView mav = controller.error(request);

        assertThat(mav.getViewName()).isEqualTo("error/500");
        assertThat(mav.getModel().get("status")).isEqualTo(500);
    }

    @Test
    void error_statusInvalide_retourneVue500() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 999);

        ModelAndView mav = controller.error(request);

        assertThat(mav.getViewName()).isEqualTo("error/500");
        assertThat(mav.getModel().get("status")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void error_avecMessage_ajouteErreurAuModel() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 500);
        request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "Boom");

        ModelAndView mav = controller.error(request);

        assertThat(mav.getModel().get("error")).isEqualTo("Boom");
    }

    @Test
    void errorByStatus_retourneLaVueCorrespondante() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        ModelAndView mav = controller.errorByStatus(404, request);

        assertThat(mav.getViewName()).isEqualTo("error/404");
        assertThat(mav.getModel().get("status")).isEqualTo(404);
    }
}
