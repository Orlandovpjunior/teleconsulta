package com.teleconsulta.integration;

import com.teleconsulta.entity.Role;
import com.teleconsulta.entity.User;
import com.teleconsulta.repository.PlanRepository;
import com.teleconsulta.repository.UserRepository;
import com.teleconsulta.service.PlanService;
import com.teleconsulta.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PlanService planService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;
    private User patient;
    private com.teleconsulta.entity.Plan plan;

    @BeforeEach
    void setUp() {
        // Criar admin
        admin = User.builder()
                .name("Admin Test")
                .email("admin.test@test.com")
                .password(passwordEncoder.encode("admin123"))
                .cpf("11111111111")
                .role(Role.ADMIN)
                .build();
        admin.setActive(true);
        admin = userRepository.save(admin);

        // Criar paciente
        patient = User.builder()
                .name("Patient Test")
                .email("patient.test@test.com")
                .password(passwordEncoder.encode("patient123"))
                .cpf("22222222222")
                .role(Role.PATIENT)
                .build();
        patient.setActive(true);
        patient = userRepository.save(patient);

        // Criar plano
        plan = com.teleconsulta.entity.Plan.builder()
                .name("Plano Teste")
                .price(new BigDecimal("99.90"))
                .durationMonths(1)
                .maxAppointmentsMonth(5)
                .hasVideoCall(true)
                .hasChat(true)
                .hasPrescription(true)
                .hasMedicalCertificate(true)
                .build();
        plan.setActive(true);
        plan = planRepository.save(plan);
    }

    @Test
    @DisplayName("Admin deve conseguir listar todos os usuários")
    void adminShouldListAllUsers() {
        // Act
        var users = userService.getAllUsers();

        // Assert
        assertThat(users).isNotEmpty();
        assertThat(users.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Admin deve conseguir desativar e reativar usuário")
    void adminShouldDeactivateAndReactivateUser() {
        // Arrange
        Long patientId = patient.getId();

        // Act - Desativar
        userService.deactivateUser(patientId);
        User deactivated = userRepository.findById(patientId).orElseThrow();
        assertThat(deactivated.getActive()).isFalse();

        // Act - Reativar
        userService.activateUser(patientId);
        User reactivated = userRepository.findById(patientId).orElseThrow();
        assertThat(reactivated.getActive()).isTrue();
    }

    @Test
    @DisplayName("Admin deve conseguir criar e gerenciar planos")
    void adminShouldCreateAndManagePlans() {
        // Arrange
        com.teleconsulta.dto.plan.CreatePlanRequest request = com.teleconsulta.dto.plan.CreatePlanRequest.builder()
                .name("Novo Plano Admin")
                .description("Plano criado por admin")
                .price(new BigDecimal("149.90"))
                .durationMonths(1)
                .maxAppointmentsMonth(10)
                .hasVideoCall(true)
                .hasChat(true)
                .hasPrescription(true)
                .hasMedicalCertificate(true)
                .build();

        // Act - Criar
        var createdPlan = planService.createPlan(request);
        assertThat(createdPlan).isNotNull();
        assertThat(createdPlan.getName()).isEqualTo("Novo Plano Admin");

        // Act - Desativar
        planService.deactivatePlan(createdPlan.getId());
        var deactivatedPlan = planRepository.findById(createdPlan.getId()).orElseThrow();
        assertThat(deactivatedPlan.getActive()).isFalse();

        // Act - Reativar
        planService.activatePlan(createdPlan.getId());
        var reactivatedPlan = planRepository.findById(createdPlan.getId()).orElseThrow();
        assertThat(reactivatedPlan.getActive()).isTrue();
    }

    @Test
    @DisplayName("Usuário desativado não deve aparecer em listagens públicas")
    void deactivatedUserShouldNotAppearInPublicLists() {
        // Arrange
        User doctor = User.builder()
                .name("Dr. Test")
                .email("doctor.test@test.com")
                .password(passwordEncoder.encode("doctor123"))
                .cpf("33333333333")
                .role(Role.DOCTOR)
                .crm("CRM/SP 123456")
                .specialty("Clínica Geral")
                .build();
        doctor.setActive(true);
        doctor = userRepository.save(doctor);

        // Act - Listar médicos (deve incluir o novo médico)
        Long doctorId = doctor.getId();
        var doctorsBefore = userService.getDoctors();
        assertThat(doctorsBefore).anyMatch(d -> d.getId().equals(doctorId));

        // Act - Desativar
        userService.deactivateUser(doctorId);

        // Act - Listar médicos novamente (não deve incluir o médico desativado)
        var doctorsAfter = userService.getDoctors();
        assertThat(doctorsAfter).noneMatch(d -> d.getId().equals(doctorId));
    }

    @Test
    @DisplayName("Plano desativado não deve aparecer em listagens públicas")
    void deactivatedPlanShouldNotAppearInPublicLists() {
        // Arrange
        Long planId = plan.getId();

        // Act - Listar planos ativos (deve incluir o plano)
        Long planIdFinal = planId;
        var activePlansBefore = planService.getActivePlans();
        assertThat(activePlansBefore).anyMatch(p -> p.getId().equals(planIdFinal));

        // Act - Desativar
        planService.deactivatePlan(planIdFinal);

        // Act - Listar planos ativos novamente (não deve incluir o plano desativado)
        var activePlansAfter = planService.getActivePlans();
        assertThat(activePlansAfter).noneMatch(p -> p.getId().equals(planIdFinal));
    }
}

