package hello.springblog.controller;

import hello.springblog.dto.CreateAccessTokenRequest;
import hello.springblog.dto.CreateAccessTokenResponse;
import hello.springblog.service.RefreshTokenService;
import hello.springblog.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import hello.springblog.util.CookieUtil;
import java.security.Principal;

@RequiredArgsConstructor
@RestController
public class TokenApiController {
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/api/token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(@RequestBody CreateAccessTokenRequest request) {
        String newAccessToken = tokenService.createNewAccessToken(request.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateAccessTokenResponse(newAccessToken));
    }

    @DeleteMapping("/api/token")
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                       HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.ok().build();
        }

        for (Cookie cookie : cookies) {
            if ("refresh_token".equals(cookie.getName())) {
                String refreshToken = cookie.getValue();
                System.out.println("cookie refreshToken = " + refreshToken);

                // 1. DB에서 refresh token 삭제
                refreshTokenService.deleteByRefreshToken(refreshToken);

                // 2. 쿠키 삭제
                CookieUtil.deleteCookie(request, response, "refresh_token");
                break; // refresh_token은 하나면 충분
            }
        }

        return ResponseEntity.ok().build();
    }
}
