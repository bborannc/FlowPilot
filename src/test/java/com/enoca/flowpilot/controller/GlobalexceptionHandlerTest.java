package com.enoca.flowpilot.controller;

import com.enoca.flowpilot.core.exceptions.BusinessRuleException;
import com.enoca.flowpilot.core.exceptions.GlobalExceptionHandler;
import com.enoca.flowpilot.core.exceptions.ResourceNotFoundException;
import com.enoca.flowpilot.core.exceptions.UnauthorizedApprovalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test/exceptions")
    static class ExceptionTestController {

        @GetMapping("/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Kayıt bulunamadı: 404");
        }

        @GetMapping("/business-rule")
        public void throwBusinessRule() {
            throw new BusinessRuleException("İş kuralı ihlali: Geçersiz adım.");
        }

        @GetMapping("/unauthorized")
        public void throwUnauthorized() {
            throw new UnauthorizedApprovalException("Bu işlemi yapmaya yetkiniz yok.");
        }

        @GetMapping("/internal-error")
        public void throwInternalError() {
            throw new NullPointerException("Veritabanı bağlantı detayları ve gizli stacktrace");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ExceptionTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("ResourceNotFoundException fırlatıldığında 404 dönmeli")
    void shouldReturn404WhenResourceNotFoundExceptionThrown() throws Exception {
        mockMvc.perform(get("/test/exceptions/not-found")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Kayıt bulunamadı: 404")))
                .andExpect(jsonPath("$.path", is("/test/exceptions/not-found")));
    }

    @Test
    @DisplayName("BusinessRuleException fırlatıldığında 400 Bad Request dönmeli")
    void shouldReturn400WhenBusinessRuleExceptionThrown() throws Exception {
        mockMvc.perform(get("/test/exceptions/business-rule")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("İş kuralı ihlali: Geçersiz adım.")));
    }

    @Test
    @DisplayName("UnauthorizedApprovalException fırlatıldığında 403 Forbidden dönmeli")
    void shouldReturn403WhenUnauthorizedApprovalExceptionThrown() throws Exception {
        mockMvc.perform(get("/test/exceptions/unauthorized")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", is("Bu işlemi yapmaya yetkiniz yok.")));
    }

    @Test
    @DisplayName("Bilinmeyen bir hata fırlatıldığında 500 dönmeli ve iç detay client'a sızmamalı")
    void shouldReturn500AndMaskInternalErrorMessage() throws Exception {
        mockMvc.perform(get("/test/exceptions/internal-error")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("Sistemde beklenmeyen bir hata oluştu. Lütfen sistem yöneticisi ile iletişime geçin.")))
                .andExpect(jsonPath("$.validationErrors").doesNotExist());
    }
}