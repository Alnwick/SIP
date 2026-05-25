package com.upiicsa.ApiSIP.Security.Filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Repository.UserRepository;
import com.upiicsa.ApiSIP.Utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.upiicsa.ApiSIP.Security.Config.SecurityConfig.PUBLIC_PATHS;

public class JwtTokenValidator extends OncePerRequestFilter {

    private UserRepository userRepository;
    private JwtUtils jwtUtils;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtTokenValidator(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String jwtToken = null;

        if(request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if("jwtToken".equals(cookie.getName())) {
                    jwtToken = cookie.getValue();
                    break;
                }
            }
        }

        if (jwtToken != null) {
            try {
                DecodedJWT decodedJWT = jwtUtils.validateToken(jwtToken);
                String email = jwtUtils.extractUsername(decodedJWT);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Cargar el usuario de la base de datos
                    UserSIP user = userRepository.findByEmail(email).orElse(null);

                    if (user != null) {
                        //Crear autenticacion para el usuario
                        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null,
                                user.getAuthorities());
                        //Colocar la autenticacion en el contexto de seguridad
                        SecurityContext context = SecurityContextHolder.getContext();
                        context.setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error to validate token" + e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        for (String publicPath : PUBLIC_PATHS) {
            if (pathMatcher.match(publicPath, path)) {
                return true;
            }
        }
        return false;
    }
}
