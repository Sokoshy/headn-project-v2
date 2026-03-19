package com.bibliotheque.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class FlashMessageUtil {
    public static final String MESSAGE_ATTR = "message";
    public static final String ERROR_ATTR = "error";

    private FlashMessageUtil() {
    }

    public static void consume(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        move(session, request, MESSAGE_ATTR);
        move(session, request, ERROR_ATTR);
    }

    public static void success(HttpServletRequest request, String message) {
        request.getSession().setAttribute(MESSAGE_ATTR, message);
    }

    public static void error(HttpServletRequest request, String message) {
        request.getSession().setAttribute(ERROR_ATTR, message);
    }

    private static void move(HttpSession session, HttpServletRequest request, String key) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(key, value);
            session.removeAttribute(key);
        }
    }
}
