
package com.enoca.flowpilot.controller;

import com.enoca.flowpilot.security.SecurityConfig;
import com.enoca.flowpilot.service.RequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RequestController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class RequestControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RequestService requestService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    void tokenOlmadanIstek401Donmeli() throws Exception {

        mockMvc.perform(get("/api/requests/my-requests"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void kimligiDogrulanmisKullaniciKendiTalepleriniIsteyebilmeli()
            throws Exception {

        when(requestService.getMyRequests()).thenReturn(List.of());

        mockMvc.perform(
                get("/api/requests/my-requests")
                        .with(jwt()
                                .jwt(token -> token
                                        .subject("boran.employee@company.com")
                                        .claim("employeeId", 4L)
                                        .claim(
                                                "authorities",
                                                List.of("ROLE_EMPLOYEE")
                                        )
                                )
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_EMPLOYEE")
                                )
                        )
        ).andExpect(status().isOk());

        verify(requestService).getMyRequests();
    }
}
