package com.bibliotheque.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class RequestPathsTest {

    @Test
    @DisplayName("Avec contextPath /bibliotheque et URI /bibliotheque/livres retourne /livres")
    void from_avecContextPathBibliotheque_retournePathRelatif() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/bibliotheque");
        request.setRequestURI("/bibliotheque/livres");

        assertThat(RequestPaths.from(request)).isEqualTo("/livres");
    }

    @Test
    @DisplayName("Sans contextPath (vide) et URI /livres retourne /livres")
    void from_sansContextPath_retourneUriComplete() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("");
        request.setRequestURI("/livres");

        assertThat(RequestPaths.from(request)).isEqualTo("/livres");
    }

    @Test
    @DisplayName("URI égale au contextPath retourne /")
    void from_uriEstContextPath_retourneSlash() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/bibliotheque");
        request.setRequestURI("/bibliotheque");

        assertThat(RequestPaths.from(request)).isEqualTo("/");
    }

    @Test
    @DisplayName("contextPath null retourne l'URI telle quelle")
    void from_contextPathNull_retourneUri() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath(null);
        request.setRequestURI("/livres");

        assertThat(RequestPaths.from(request)).isEqualTo("/livres");
    }

    @Test
    @DisplayName("URI = / avec contextPath /bibliotheque retourne /")
    void from_uriEstSlash_retourneSlash() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/bibliotheque");
        request.setRequestURI("/");

        assertThat(RequestPaths.from(request)).isEqualTo("/");
    }
}
