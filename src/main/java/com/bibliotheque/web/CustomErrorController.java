package com.bibliotheque.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/error")
public class CustomErrorController {

    @GetMapping
    public ModelAndView error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        
        String viewName = determineViewName(status);
        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject("status", status.value());
        
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (errorMessage != null && !errorMessage.isEmpty()) {
            mav.addObject("error", errorMessage);
        }
        
        return mav;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView errorHtml(HttpServletRequest request) {
        return error(request);
    }

    private String determineViewName(HttpStatus status) {
        return switch (status.value()) {
            case 401 -> "error/401";
            case 404 -> "error/404";
            case 403 -> "error/403";
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
}
