package org.kfokam48.stagemanagementbackend.controlleur.auth;

import org.junit.jupiter.api.Test;
import org.kfokam48.stagemanagementbackend.config.AuthCookieService;
import org.kfokam48.stagemanagementbackend.config.JwtRequestFillter;
import org.kfokam48.stagemanagementbackend.config.JwtService;
import org.kfokam48.stagemanagementbackend.config.SecurityConfig;
import org.kfokam48.stagemanagementbackend.service.auth.AuthService;
import org.kfokam48.stagemanagementbackend.service.auth.CurrentUserService;
import org.kfokam48.stagemanagementbackend.service.auth.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Connexion / déconnexion : les jetons sont posés en cookies HttpOnly et absents du corps JSON.
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtRequestFillter.class, AuthCookieService.class})
// Valeur de production : les jetons ne doivent pas apparaître dans le corps JSON
@org.springframework.test.context.TestPropertySource(properties = "app.auth.tokens-in-body=false")
class AuthControllerCookieTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private CurrentUserService currentUserService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loginSetsHttpOnlyCookiesAndHidesTokens() throws Exception {
        when(authService.login(any())).thenReturn(new AuthService.LoginResult(
                Map.of("id", 1, "role", "etudiant"), "ACCESS", "REFRESH"));
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900_000L);
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604_800_000L);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.fr\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.id").value(1))
                .andExpect(jsonPath("$.data.token").doesNotExist())
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andExpect(header().stringValues("Set-Cookie",
                        org.hamcrest.Matchers.hasItems(
                                allOf(containsString("access_token=ACCESS"), containsString("HttpOnly"),
                                        containsString("Path=/api"), containsString("SameSite=Lax")),
                                allOf(containsString("refresh_token=REFRESH"), containsString("HttpOnly"),
                                        containsString("Path=/api/auth")))));
    }

    @Test
    void logoutClearsCookies() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().stringValues("Set-Cookie",
                        org.hamcrest.Matchers.hasItems(
                                allOf(containsString("access_token="), containsString("Max-Age=0")),
                                allOf(containsString("refresh_token="), containsString("Max-Age=0")))));
    }

    @Test
    void refreshWithoutCookieReturns401() throws Exception {
        when(authService.refreshAccessToken(any())).thenThrow(
                new org.kfokam48.stagemanagementbackend.exception.InvalidTokenException("manquant"));
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
