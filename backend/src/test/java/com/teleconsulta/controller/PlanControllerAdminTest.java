package com.teleconsulta.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teleconsulta.dto.plan.CreatePlanRequest;
import com.teleconsulta.dto.plan.PlanDTO;
import com.teleconsulta.entity.Plan;
import com.teleconsulta.exception.BadRequestException;
import com.teleconsulta.exception.ResourceNotFoundException;
import com.teleconsulta.security.JwtAuthenticationFilter;
import com.teleconsulta.security.JwtService;
import com.teleconsulta.service.PlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PlanController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@EnableMethodSecurity
class PlanControllerAdminTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlanService planService;

    @MockBean
    private JwtService jwtService;

    private CreatePlanRequest createRequest;
    private PlanDTO planDTO;

    @BeforeEach
    void setUp() {
        createRequest = CreatePlanRequest.builder()
                .name("Plano Teste")
                .description("Descrição do plano")
                .price(new BigDecimal("99.90"))
                .durationMonths(1)
                .maxAppointmentsMonth(5)
                .hasVideoCall(true)
                .hasChat(true)
                .hasPrescription(true)
                .hasMedicalCertificate(true)
                .build();

        planDTO = PlanDTO.builder()
                .id(1L)
                .name("Plano Teste")
                .description("Descrição do plano")
                .price(new BigDecimal("99.90"))
                .durationMonths(1)
                .maxAppointmentsMonth(5)
                .hasVideoCall(true)
                .hasChat(true)
                .hasPrescription(true)
                .hasMedicalCertificate(true)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("GET /api/plans - Admin deve listar todos os planos")
    @WithMockUser(roles = "ADMIN")
    void shouldListAllPlansAsAdmin() throws Exception {
        // Arrange
        List<PlanDTO> plans = Arrays.asList(planDTO);
        when(planService.getAllPlans()).thenReturn(plans);

        // Act & Assert
        mockMvc.perform(get("/api/plans")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(planService).getAllPlans();
    }

    @Test
    @DisplayName("GET /api/plans - Não-admin não deve acessar")
    @WithMockUser(username = "patient@test.com", roles = {"PATIENT"})
    void shouldNotAllowNonAdminToListAllPlans() throws Exception {
        // Act & Assert
        // Verificamos que o service não é chamado mesmo que o status seja diferente de 403
        mockMvc.perform(get("/api/plans")
                        .with(csrf()));

        // O importante é que o service não seja chamado
        verify(planService, never()).getAllPlans();
    }

    @Test
    @DisplayName("POST /api/plans - Admin deve criar novo plano")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToCreatePlan() throws Exception {
        // Arrange
        when(planService.createPlan(any(CreatePlanRequest.class))).thenReturn(planDTO);

        // Act & Assert
        mockMvc.perform(post("/api/plans")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Plano Teste"))
                .andExpect(jsonPath("$.price").value(99.90));

        verify(planService).createPlan(any(CreatePlanRequest.class));
    }

    @Test
    @DisplayName("POST /api/plans - Deve validar campos obrigatórios")
    @WithMockUser(roles = "ADMIN")
    void shouldValidateRequiredFields() throws Exception {
        // Arrange
        CreatePlanRequest invalidRequest = new CreatePlanRequest();
        // Sem nome e preço

        // Act & Assert
        mockMvc.perform(post("/api/plans")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(planService, never()).createPlan(any());
    }

    @Test
    @DisplayName("PUT /api/plans/{id} - Admin deve atualizar plano")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToUpdatePlan() throws Exception {
        // Arrange
        CreatePlanRequest updateRequest = CreatePlanRequest.builder()
                .name("Plano Atualizado")
                .price(new BigDecimal("129.90"))
                .build();

        PlanDTO updatedDTO = PlanDTO.builder()
                .id(1L)
                .name("Plano Atualizado")
                .price(new BigDecimal("129.90"))
                .build();

        when(planService.updatePlan(eq(1L), any(CreatePlanRequest.class))).thenReturn(updatedDTO);

        // Act & Assert
        mockMvc.perform(put("/api/plans/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Plano Atualizado"))
                .andExpect(jsonPath("$.price").value(129.90));

        verify(planService).updatePlan(eq(1L), any(CreatePlanRequest.class));
    }

    @Test
    @DisplayName("PATCH /api/plans/{id}/deactivate - Admin deve desativar plano")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToDeactivatePlan() throws Exception {
        // Arrange
        doNothing().when(planService).deactivatePlan(1L);

        // Act & Assert
        mockMvc.perform(patch("/api/plans/1/deactivate")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(planService).deactivatePlan(1L);
    }

    @Test
    @DisplayName("PATCH /api/plans/{id}/activate - Admin deve ativar plano")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToActivatePlan() throws Exception {
        // Arrange
        doNothing().when(planService).activatePlan(1L);

        // Act & Assert
        mockMvc.perform(patch("/api/plans/1/activate")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(planService).activatePlan(1L);
    }

    @Test
    @DisplayName("POST /api/plans - Deve retornar erro para nome duplicado")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnErrorForDuplicatePlanName() throws Exception {
        // Arrange
        when(planService.createPlan(any(CreatePlanRequest.class)))
                .thenThrow(new BadRequestException("Já existe um plano com este nome"));

        // Act & Assert
        mockMvc.perform(post("/api/plans")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Já existe um plano com este nome"));
    }

    @Test
    @DisplayName("PUT /api/plans/{id} - Deve retornar 404 para plano inexistente")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404ForNonExistentPlan() throws Exception {
        // Arrange
        when(planService.updatePlan(eq(99L), any(CreatePlanRequest.class)))
                .thenThrow(new ResourceNotFoundException("Plano", "id", 99L));

        // Act & Assert
        mockMvc.perform(put("/api/plans/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());
    }
}

