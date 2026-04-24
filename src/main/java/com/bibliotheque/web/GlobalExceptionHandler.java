package com.bibliotheque.web;

import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        logger.warn("Ressource non trouvée: {}", ex.getMessage());
        return errorView(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        logger.warn("Route introuvable: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return errorView(HttpStatus.NOT_FOUND, "La page demandée est introuvable.", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        logger.warn("Ressource statique introuvable: {}", ex.getResourcePath());
        return errorView(HttpStatus.NOT_FOUND, "La page demandée est introuvable.", request);
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex, RedirectAttributes redirectAttributes) {
        logger.warn("Erreur métier: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(Exception ex, HttpServletRequest request) {
        logger.error("Erreur inattendue", ex);
        return errorView(HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur inattendue s'est produite. Veuillez réessayer.", request);
    }

    private ModelAndView errorView(HttpStatus status, String message, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("error/" + status.value());
        modelAndView.setStatus(status);
        modelAndView.addObject("status", status.value());
        modelAndView.addObject("error", message);
        modelAndView.addObject("currentPath", currentPath(request));
        return modelAndView;
    }

    private String currentPath(HttpServletRequest request) {
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
