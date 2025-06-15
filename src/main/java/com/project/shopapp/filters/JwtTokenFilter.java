package com.project.shopapp.filters;

import com.project.shopapp.components.JwtTonkenUtil;
import com.project.shopapp.models.User;
import com.project.shopapp.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    @Value("${api.prefix}")
    private String apiPrefix;
    private final UserDetailsServiceImpl userDetailsService;
    private  final JwtTonkenUtil jwtTonkenUtil;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        try{
            if (isBypassToken(request)){
                filterChain.doFilter(request,response);
                return;
            }

            final String authHeader = request.getHeader("Authorization");

            if (authHeader ==  null || !authHeader.startsWith("Bearer")){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
            final String token = authHeader.substring(7);
            //final String phoneNumber = jwtTonkenUtil.extractPhoneNumber(token);
            final Long userId = jwtTonkenUtil.extractUserId(token);


            if (userId == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token: missing userId");
                return;
            }

            if (userId != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null){
                //User userDetails = (User) userDetailsService.loadUserByUsername(userId.toString());
                User userDetails = (User) userDetailsService.loadUserById(userId);
                if (jwtTonkenUtil.validateToken(token,userDetails)){

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
            filterChain.doFilter(request,response);

        }catch (Exception e){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }

    }

    private boolean isBypassToken(@NotNull HttpServletRequest request){
        final List<Pair<String,String>> bypassTokens = Arrays.asList(
                //nhung request khong yeu cau token
                Pair.of(String.format("%s/roles", apiPrefix), "GET"),
                Pair.of(String.format("%s/products", apiPrefix), "GET"),
                Pair.of(String.format("%s/categories", apiPrefix), "GET"),
                Pair.of(String.format("%s/brands", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/register", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/login", apiPrefix), "POST"),
                Pair.of(String.format("%s/auth/google", apiPrefix), "POST")

        );

        //String requestPath = request.getServletPath();
        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();

        // ✅ BỎ QUA token cho ảnh sản phẩm
        if (requestMethod.equals("GET") && requestPath.startsWith("/api/v1/products/images/")) {
            System.out.println("✅ BYPASS TOKEN: ảnh sản phẩm");
            return true;
        }


        if (requestPath.equals(String.format("%s/orders",apiPrefix))
                && requestMethod.equals("GET")){
            return true;
        }

        for(Pair<String,String> bypassToken: bypassTokens){
            if (requestPath.contains(bypassToken.getFirst()) &&
                    requestMethod.equals(bypassToken.getSecond())){
                return true;
            }
        }
        return false;
    }

    // ✅ BỎ QUA FILTER cho WebSocket
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/ws") || path.startsWith("/api/v1/products/images/")
                || path.startsWith("/api/admin/rename-files");
    }
}
