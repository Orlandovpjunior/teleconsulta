package com.teleconsulta.service;

import com.teleconsulta.dto.plan.CreatePlanRequest;
import com.teleconsulta.dto.plan.PlanDTO;
import com.teleconsulta.entity.Plan;
import com.teleconsulta.entity.User;
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
class PlanServiceAdminTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PlanService planService;

    private Plan activePlan;
    private Plan inactivePlan;
    private CreatePlanRequest createRequest;
    private User patient;

    @BeforeEach
    void setUp() {
        activePlan = Plan.builder()
                .name("Plano Ativo")
                .description("Plano de teste ativo")
                .price(new BigDecimal("99.90"))
                .durationMonths(1)
                .maxAppointmentsMonth(5)
                .hasVideoCall(true)
                .hasChat(true)
                .hasPrescription(true)
                .hasMedicalCertificate(true)
                .features(new ArrayList<>(List.of("Feature 1", "Feature 2")))
                .build();
        activePlan.setId(1L);
        activePlan.setActive(true);

        inactivePlan = Plan.builder()
                .name("Plano Inativo")
                .price(new BigDecimal("49.90"))
                .build();
        inactivePlan.setId(2L);
        inactivePlan.setActive(false);

        createRequest = CreatePlanRequest.builder()
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

        patient = User.builder()
                .name("Paciente Teste")
                .email("patient@test.com")
                .build();
        patient.setId(1L);
    }

    @Test
    @DisplayName("Deve listar todos os planos")
    void shouldGetAllPlans() {
        // Arrange
        List<Plan> plans = List.of(activePlan, inactivePlan);
        when(planRepository.findAll()).thenReturn(plans);

        // Act
        List<PlanDTO> result = planService.getAllPlans();

        // Assert
        assertThat(result).hasSize(2);
        verify(planRepository).findAll();
    }

    @Test
    @DisplayName("Deve criar um novo plano")
    void shouldCreatePlan() {
        // Arrange
        Plan newPlan = Plan.builder()
                .name(createRequest.getName())
                .price(createRequest.getPrice())
                .build();
        newPlan.setId(3L);
        newPlan.setActive(true);

        when(planRepository.existsByName(createRequest.getName())).thenReturn(false);
        when(planRepository.save(any(Plan.class))).thenReturn(newPlan);

        // Act
        PlanDTO result = planService.createPlan(createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Novo Plano");
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar plano com nome duplicado")
    void shouldThrowExceptionWhenCreatingPlanWithDuplicateName() {
        // Arrange
        when(planRepository.existsByName(createRequest.getName())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> planService.createPlan(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Já existe um plano com este nome");

        verify(planRepository, never()).save(any(Plan.class));
    }

    @Test
    @DisplayName("Deve atualizar um plano existente")
    void shouldUpdatePlan() {
        // Arrange
        CreatePlanRequest updateRequest = CreatePlanRequest.builder()
                .name("Plano Atualizado")
                .price(new BigDecimal("129.90"))
                .build();

        when(planRepository.findById(1L)).thenReturn(Optional.of(activePlan));
        when(planRepository.existsByName("Plano Atualizado")).thenReturn(false);
        when(planRepository.save(any(Plan.class))).thenReturn(activePlan);

        // Act
        PlanDTO result = planService.updatePlan(1L, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(planRepository).save(activePlan);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar plano inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentPlan() {
        // Arrange
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> planService.updatePlan(99L, createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Plano");

        verify(planRepository, never()).save(any(Plan.class));
    }

    @Test
    @DisplayName("Deve desativar um plano")
    void shouldDeactivatePlan() {
        // Arrange
        when(planRepository.findById(1L)).thenReturn(Optional.of(activePlan));
        when(planRepository.save(any(Plan.class))).thenReturn(activePlan);

        // Act
        planService.deactivatePlan(1L);

        // Assert
        assertThat(activePlan.getActive()).isFalse();
        verify(planRepository).save(activePlan);
    }

    @Test
    @DisplayName("Deve ativar um plano")
    void shouldActivatePlan() {
        // Arrange
        when(planRepository.findById(2L)).thenReturn(Optional.of(inactivePlan));
        when(planRepository.save(any(Plan.class))).thenReturn(inactivePlan);

        // Act
        planService.activatePlan(2L);

        // Assert
        assertThat(inactivePlan.getActive()).isTrue();
        verify(planRepository).save(inactivePlan);
    }

    @Test
    @DisplayName("Deve assinar um paciente a um plano")
    void shouldSubscribeUserToPlan() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(planRepository.findById(1L)).thenReturn(Optional.of(activePlan));
        when(userRepository.save(any(User.class))).thenReturn(patient);

        // Act
        planService.subscribeToPlan(1L, 1L);

        // Assert
        assertThat(patient.getPlan()).isEqualTo(activePlan);
        verify(userRepository).save(patient);
    }

    @Test
    @DisplayName("Deve lançar exceção ao assinar plano inativo")
    void shouldThrowExceptionWhenSubscribingToInactivePlan() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(planRepository.findById(2L)).thenReturn(Optional.of(inactivePlan));

        // Act & Assert
        assertThatThrownBy(() -> planService.subscribeToPlan(1L, 2L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Este plano não está disponível");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve cancelar assinatura de um paciente")
    void shouldCancelSubscription() {
        // Arrange
        patient.setPlan(activePlan);
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.save(any(User.class))).thenReturn(patient);

        // Act
        planService.cancelSubscription(1L);

        // Assert
        assertThat(patient.getPlan()).isNull();
        verify(userRepository).save(patient);
    }

    @Test
    @DisplayName("Deve retornar apenas planos ativos ordenados por preço")
    void shouldGetActivePlansOrderedByPrice() {
        // Arrange
        Plan cheapPlan = Plan.builder()
                .name("Plano Barato")
                .price(new BigDecimal("49.90"))
                .build();
        cheapPlan.setId(3L);
        cheapPlan.setActive(true);

        List<Plan> activePlans = List.of(cheapPlan, activePlan);
        when(planRepository.findByActiveTrueOrderByPriceAsc()).thenReturn(activePlans);

        // Act
        List<PlanDTO> result = planService.getActivePlans();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPrice()).isLessThan(result.get(1).getPrice());
    }
}

