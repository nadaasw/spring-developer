package hello.springblog.config.oauth;


import hello.springblog.config.jwt.TokenProvider;
import hello.springblog.domain.RefreshToken;
import hello.springblog.domain.User;
import hello.springblog.repository.RefreshTokenRepository;
import hello.springblog.repository.UserRepository;
import hello.springblog.service.UserService;
import hello.springblog.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class FormLoginSuccessHanlder extends SimpleUrlAuthenticationSuccessHandler {
    private final TokenProvider tokenProvider;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail((String)userDetails.getUsername()); // email로 User 엔티티 조회

        String refreshToken = tokenProvider.generateToken(user, Duration.ofDays(14));
        String accessToken  = tokenProvider.generateToken(user, Duration.ofDays(1));

        refreshTokenRepository.save(
                new RefreshToken(user.getId(), refreshToken)
        );

        CookieUtil.addCookie(response, "refresh_token", refreshToken, 14 * 24 * 60 * 60);

        getRedirectStrategy().sendRedirect(
                request,
                response,
                "/articles?token=" + accessToken
        );
    }
}
