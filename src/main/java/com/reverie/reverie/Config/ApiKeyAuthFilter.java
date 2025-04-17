package com.reverie.reverie.Config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiKeyAuthFilter implements Filter {

    @Value("${api.key}")
    private String validApiKey;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        // Always allow OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        String apiKey = httpRequest.getHeader("X-API-Key");
        System.out.println(apiKey);

        if (apiKey == null || !apiKey.equals(validApiKey)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}