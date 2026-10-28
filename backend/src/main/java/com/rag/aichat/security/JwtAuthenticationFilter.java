package com.rag.aichat.security;

import com.rag.aichat.common.BusinessException;
import com.rag.aichat.common.ApiResponse;
import com.rag.aichat.entity.User;
import com.rag.aichat.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final AntPathRequestMatcher ADMIN_PATH = new AntPathRequestMatcher("/api/admin/**");
    private static final OrRequestMatcher PROTECTED_PATH = new OrRequestMatcher(
            new AntPathRequestMatcher("/api/auth/me"),
            new AntPathRequestMatcher("/api/chat/**")
    );

    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserMapper userMapper, ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            if (PROTECTED_PATH.matches(request)) {
                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, 401, "unauthenticated");
                return;
            }
            if (ADMIN_PATH.matches(request)) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, 403, "access denied");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtTokenProvider.parseToken(authorization.substring(7));
            User user = userMapper.findById(claims.getSubject());
            if (user == null) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), 401, "user not found");
            }
            LoginUser loginUser = new LoginUser(user);
            if (ADMIN_PATH.matches(request) && !isAdmin(loginUser)) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, 403, "access denied");
                return;
            }
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | BusinessException exception) {
            log.debug("Invalid jwt token", exception);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAdmin(LoginUser loginUser) {
        Collection<? extends GrantedAuthority> authorities = loginUser.getAuthorities();
        return authorities != null && authorities.stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private void writeJson(HttpServletResponse response, int status, int code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new ApiResponse<Void>(code, message, null));
    }
}
