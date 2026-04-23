package com.offerdyne.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Simple demo-grade auth: clients pass X-Agent-Id header.
 * Real deployments would swap this for JWT / OAuth.
 */
@Component
public class AgentAuthFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Agent-Id";
    public static final String ATTR   = "agentId";

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String uri = req.getRequestURI();
        // Open endpoints (docs, health, H2 console)
        if (uri.startsWith("/h2-console") || uri.startsWith("/actuator") || uri.equals("/")) {
            chain.doFilter(req, res);
            return;
        }
        String agentId = req.getHeader(HEADER);
        if (agentId == null || agentId.isBlank()) {
            // Fallback to query param for easy demo in browser
            agentId = req.getParameter("agentId");
        }
        if (agentId != null && !agentId.isBlank()) {
            req.setAttribute(ATTR, Long.valueOf(agentId));
        }
        chain.doFilter(req, res);
    }
}
