package org.kfokam48.stagemanagementbackend.config;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kfokam48.stagemanagementbackend.controlleur.EtudiantController;
import org.kfokam48.stagemanagementbackend.dto.etudiant.EtudiantResponseDTO;
import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.model.auth.CustomUserDetails;
import org.kfokam48.stagemanagementbackend.service.auth.CustomUserDetailsService;
import org.kfokam48.stagemanagementbackend.service.impl.EtudiantserviceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Authentification par cookie HttpOnly et protection CSRF (en-tête X-Requested-With),
 * avec de vrais jetons signés par JwtService.
 */
@WebMvcTest(EtudiantController.class)
@Import({SecurityConfig.class, JwtRequestFillter.class, AuthCookieService.class, CookieAuthenticationTest.JwtTestConfig.class})
class CookieAuthenticationTest {

    static class JwtTestConfig {
        @org.springframework.context.annotation.Bean
        JwtService jwtService() {
            return new JwtService(Base64.getEncoder().encodeToString(new byte[64]), 60_000, 120_000, 30_000);
        }
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private EtudiantserviceImpl etudiantService;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private static final String EMAIL = "etudiant1@test.fr";

    @BeforeEach
    void setUp() {
        when(customUserDetailsService.loadUserByUsername(EMAIL)).thenReturn(new CustomUserDetails(
                1L, Roles.ETUDIANT, EMAIL, "x", List.of(new SimpleGrantedAuthority("ROLE_ETUDIANT"))));
        when(etudiantService.getEtudiantById(anyLong())).thenReturn(new EtudiantResponseDTO());
        when(etudiantService.updateEtudiant(anyLong(), any())).thenReturn(new EtudiantResponseDTO());
    }

    private Cookie accessCookie() {
        return new Cookie(AuthCookieService.ACCESS_COOKIE, jwtService.generateAccessToken(EMAIL));
    }

    @Test
    void readWithAccessCookieIsAuthenticated() throws Exception {
        mockMvc.perform(get("/api/etudiants/1").cookie(accessCookie()))
                .andExpect(status().isOk());
    }

    @Test
    void refreshTokenInAccessCookieIsRejected() throws Exception {
        Cookie refreshAsAccess = new Cookie(AuthCookieService.ACCESS_COOKIE, jwtService.generateRefreshToken(EMAIL));
        mockMvc.perform(get("/api/etudiants/1").cookie(refreshAsAccess))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cookieWriteWithoutCsrfHeaderIsRejected() throws Exception {
        mockMvc.perform(put("/api/etudiants/1").cookie(accessCookie())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("CSRF_HEADER_MISSING"));
    }

    @Test
    void cookieWriteWithCsrfHeaderIsAccepted() throws Exception {
        mockMvc.perform(put("/api/etudiants/1").cookie(accessCookie())
                        .header(JwtRequestFillter.CSRF_HEADER, "XMLHttpRequest")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void bearerHeaderDoesNotNeedCsrfHeader() throws Exception {
        mockMvc.perform(put("/api/etudiants/1")
                        .header("Authorization", "Bearer " + jwtService.generateAccessToken(EMAIL))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }
}
