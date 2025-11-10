package com.sampoom.backend.common.jwt;

import com.sampoom.backend.common.entity.Role;
import com.sampoom.backend.common.exception.CustomAuthenticationException;
import com.sampoom.backend.common.response.ErrorStatus;
import com.sampoom.backend.common.security.CustomAuthEntryPoint;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomAuthEntryPoint customAuthEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.equals("/swagger-ui.html")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String accessToken = jwtProvider.resolveAccessToken(request);
            if (accessToken == null) {
                filterChain.doFilter(request, response);
                return;
            }
            if (accessToken.isBlank()) {
                throw new CustomAuthenticationException(ErrorStatus.NULL_BLANK_TOKEN);
            }
            Claims claims = jwtProvider.parse(accessToken);

            // 토큰 타입 검증
            String type = claims.get("type", String.class);

            // service 토큰 검증
            if ("service".equals(type)) {
                String role = claims.get("role", String.class);
                String subject = claims.getSubject(); // 토큰 발급자 정보 (auth-service)
                if (role == null) {
                    throw new CustomAuthenticationException(ErrorStatus.NULL_TOKEN_ROLE);
                }
                if (role.isBlank()) {
                    throw new CustomAuthenticationException(ErrorStatus.BLANK_TOKEN_ROLE);
                }
                if (!role.startsWith("SVC_")) {
                    throw new CustomAuthenticationException(ErrorStatus.NOT_SERVICE_TOKEN);
                }

                // Feign 내부 호출용 권한 통과
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                subject,
                                null,
                                List.of(new SimpleGrantedAuthority(role))
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);
                // service 토큰은 더 이상 검증할 필요 없음
                filterChain.doFilter(request, response);
                return;
            }

            // 그 외 토큰 (refresh) 예외 처리
            if ("refresh".equals(type)) {
                SecurityContextHolder.clearContext(); // 인증 정보 제거
                throw new CustomAuthenticationException(ErrorStatus.NOT_ACCESS_TOKEN);
            }

            // 토큰에서 userId, role 가져오기
            String userId = claims.getSubject();
            String roleClaim = claims.get("role", String.class);
            if (userId == null || userId.isBlank() || roleClaim == null || roleClaim.isBlank()) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
            // 권한 파싱 (문자열 -> Enum)
            Role role;
            try {
                role = Role.valueOf(roleClaim);
            } catch (IllegalArgumentException ex) {
                throw new CustomAuthenticationException(ErrorStatus.INVALID_TOKEN_ROLE);
            }

            // 권한 매핑 (Enum Role → Security 권한명)
            String authority;
            switch (role) {
                case USER -> authority = "ROLE_USER";
                case ADMIN -> authority = "ROLE_ADMIN";
                default -> authority = "ROLE_" + role.name();
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(new SimpleGrantedAuthority(authority))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (CustomAuthenticationException ex) {
            SecurityContextHolder.clearContext();
            customAuthEntryPoint.commence(request, response, ex);
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            customAuthEntryPoint.commence(request, response,
                    new CustomAuthenticationException(ErrorStatus.INVALID_TOKEN));
        }
    }
}
