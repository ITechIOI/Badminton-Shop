package com.example.gateway.Filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class GatewayFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        // Nếu request không có "X-Forwarded-For", có nghĩa là truy cập trực tiếp -> Chặn
        if (forwardedFor == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Must go through API Gateway");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
