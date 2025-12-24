package com.teleconsulta.service;

import com.teleconsulta.dto.plan.CreatePlanRequest;
import com.teleconsulta.dto.plan.PlanDTO;
import com.teleconsulta.entity.Plan;
import com.teleconsulta.exception.BadRequestException;
import com.teleconsulta.exception.ResourceNotFoundException;
import com.teleconsulta.repository.PlanRepository;
import com.teleconsulta.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PlanService planService;

    private Plan plan;
    private CreatePlanRequest createPlanRequest;

    @BeforeEach
    void setUp() {
        plan = Plan.builder()
                .name("Plano Teste")
                .description("Descrição do plano teste")
                .price(new BigDecimal("99.90"))
                .durationMonths(1)
                .maxAppointmentsMonth(5)
                .hasVideoCall(true)
                .hasChat(true)
                .hasPrescription(true)
                .hasMedicalCertificate(true)
                .features(new ArrayList<>(List.of("Feature 1", "Feature 2")))
                .build();
        plan.setId(1L);
        plan.setActive(true);

        createPlanRequest = CreatePlanRequest.builder()
                .name("Novo Plano")
                .description("Descrição do novo plano")
                .price(new BigDecimal("149.90"))
                .durationMonths(1)
                .maxAppointmentsMonth(10)
                .hasVideoCall(true)
                .hasChat(true)
                .hasPrescription(true)
                .hasMedicalCertificate(true)
                .features(List.of("Feature A", "Feature B"))
                .build();
    }

    @Test
    @DisplayName("Deve retornar plano por ID")
    void shouldReturnPlanById() {
        // Arrange
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));

        // Act
        PlanDTO result = planService.getPlanById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Plano Teste");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("99.90"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando plano não encontrado")
    void shouldThrowExceptionWhenPlanNotFound() {
        // Arrange
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> planService.getPlanById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Plano");
    }

    @Test
    @DisplayName("Deve retornar lista de planos ativos")
    void shouldReturnActivePlans() {
        // Arrange
        when(planRepository.findByActiveTrueOrderByPriceAsc()).thenReturn(List.of(plan));

        // Act
        List<PlanDTO> result = planService.getActivePlans();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Plano Teste");
    }

    @Test
    @DisplayName("Deve criar um novo plano")
    void shouldCreatePlan() {
        // Arrange
        Plan newPlan = Plan.builder()
                .name(createPlanRequest.getName())
                .price(createPlanRequest.getPrice())
                .build();
        newPlan.setId(2L);
        newPlan.setActive(true);

        when(planRepository.existsByName(createPlanRequest.getName())).thenReturn(false);
        when(planRepository.save(any(Plan.class))).thenReturn(newPlan);

        // Act
        PlanDTO result = planService.createPlan(createPlanRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar plano com nome existente")
    void shouldThrowExceptionWhenPlanNameExists() {
        // Arrange
        when(planRepository.existsByName(createPlanRequest.getName())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> planService.createPlan(createPlanRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Já existe um plano com este nome");

        verify(planRepository, never()).save(any(Plan.class));
    }

    @Test
    @DisplayName("Deve desativar um plano")
    void shouldDeactivatePlan() {
        // Arrange
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenReturn(plan);

        // Act
        planService.deactivatePlan(1L);

        // Assert
        assertThat(plan.getActive()).isFalse();
        verify(planRepository).save(plan);
    }

    @Test
    @DisplayName("Deve ativar um plano")
    void shouldActivatePlan() {
        // Arrange
        plan.setActive(false);
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenReturn(plan);

        // Act
        planService.activatePlan(1L);

        // Assert
        assertThat(plan.getActive()).isTrue();
        verify(planRepository).save(plan);
    }
}

