package org.kfokam48.stagemanagementbackend.controlleur;

import org.junit.jupiter.api.Test;
import org.kfokam48.stagemanagementbackend.config.JwtRequestFillter;
import org.kfokam48.stagemanagementbackend.config.JwtService;
import org.kfokam48.stagemanagementbackend.config.SecurityConfig;
import org.kfokam48.stagemanagementbackend.dto.etudiant.EtudiantResponseDTO;
import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.model.auth.CustomUserDetails;
import org.kfokam48.stagemanagementbackend.service.auth.CustomUserDetailsService;
import org.kfokam48.stagemanagementbackend.service.impl.EtudiantserviceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie les règles d'appartenance : un étudiant ne peut consulter que son propre profil.
 */
@WebMvcTest(EtudiantController.class)
@Import({SecurityConfig.class, JwtRequestFillter.class, org.kfokam48.stagemanagementbackend.config.AuthCookieService.class})
class EtudiantControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EtudiantserviceImpl etudiantService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private static CustomUserDetails principal(long id, Roles role) {
        return new CustomUserDetails(id, role, "user" + id + "@test.fr", "x",
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

    @Test
    void anonymousRequestGets401() throws Exception {
        mockMvc.perform(get("/api/etudiants/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void studentCanReadOwnProfile() throws Exception {
        when(etudiantService.getEtudiantById(anyLong())).thenReturn(new EtudiantResponseDTO());
        mockMvc.perform(get("/api/etudiants/1").with(user(principal(1, Roles.ETUDIANT))))
                .andExpect(status().isOk());
    }

    @Test
    void studentCannotReadAnotherStudentProfile() throws Exception {
        mockMvc.perform(get("/api/etudiants/2").with(user(principal(1, Roles.ETUDIANT))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadAnyProfile() throws Exception {
        when(etudiantService.getEtudiantById(anyLong())).thenReturn(new EtudiantResponseDTO());
        mockMvc.perform(get("/api/etudiants/2").with(user(principal(99, Roles.ADMIN))))
                .andExpect(status().isOk());
    }

    @Test
    void companyCannotListStudents() throws Exception {
        mockMvc.perform(get("/api/etudiants").with(user(principal(5, Roles.ENTREPRISE))))
                .andExpect(status().isForbidden());
    }
}
