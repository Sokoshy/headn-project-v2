package com.bibliotheque.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/error")
public class CustomErrorController {

    @GetMapping
    public ModelAndView error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        return buildErrorView(status, errorMessage, request);
    }

    @GetMapping("/{statusCode}")
    public ModelAndView errorByStatus(@PathVariable int statusCode, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return buildErrorView(status, null, request);
    }

    private ModelAndView buildErrorView(HttpStatus status, String errorMessage, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView(determineViewName(status));
        mav.setStatus(status);
        mav.addObject("status", status.value());
        mav.addObject("currentPath", currentPath(request));

        if (errorMessage != null && !errorMessage.isEmpty()) {
            mav.addObject("error", errorMessage);
        }

        return mav;
    }

    private String determineViewName(HttpStatus status) {
        return switch (status.value()) {
            case 401 -> "error/401";
            case 403 -> "error/403";
            case 404 -> "error/404";
            case 500 -> "error/500";
            default -> "error/500";
        };
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            return HttpStatus.valueOf(statusCode);
        } catch (Exception e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
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
