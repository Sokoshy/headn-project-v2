package com.bibliotheque.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
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
