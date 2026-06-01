package com.bibliotheque.web;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestPaths {

    private RequestPaths() {
        // utility class
    }

    /**
     * Returns the request path relative to the servlet context.
     * Strips the context path prefix; returns "/" for the root,
     * for a null request, or for a blank URI.
     */
    public static String from(HttpServletRequest request) {
        if (request == null) {
            return "/";
        }

        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (requestUri == null || requestUri.isBlank()) {
            return "/";
        }

        if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
            String path = requestUri.substring(contextPath.length());
            return path.isBlank() ? "/" : path;
        }

        return requestUri;
    }
}
