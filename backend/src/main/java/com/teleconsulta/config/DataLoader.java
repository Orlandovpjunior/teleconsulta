package com.teleconsulta.config;

import com.teleconsulta.entity.Plan;
import com.teleconsulta.entity.Role;
import com.teleconsulta.entity.User;
import com.teleconsulta.repository.PlanRepository;
import com.teleconsulta.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        loadPlans();
        loadUsers();
        log.info("Dados de exemplo carregados com sucesso!");
    }

    private void loadPlans() {
        if (planRepository.count() == 0) {
            Plan basicPlan = Plan.builder()
                    .name("Plano Básico")
                    .description("Ideal para quem precisa de consultas ocasionais")
                    .price(new BigDecimal("49.90"))
                    .durationMonths(1)
                    .maxAppointmentsMonth(2)
                    .hasVideoCall(true)
                    .hasChat(false)
                    .hasPrescription(true)
                    .hasMedicalCertificate(false)
                    .features(List.of(
                            "2 consultas por mês",
                            "Videochamada",
                            "Prescrição digital",
                            "Suporte por email"
                    ))
                    .build();

            Plan standardPlan = Plan.builder()
                    .name("Plano Padrão")
                    .description("O plano mais popular para cuidados regulares")
                    .price(new BigDecimal("99.90"))
                    .durationMonths(1)
                    .maxAppointmentsMonth(5)
                    .hasVideoCall(true)
                    .hasChat(true)
                    .hasPrescription(true)
                    .hasMedicalCertificate(true)
                    .features(List.of(
                            "5 consultas por mês",
                            "Videochamada",
                            "Chat com médico",
                            "Prescrição digital",
                            "Atestado médico",
                            "Suporte prioritário"
                    ))
                    .build();

            Plan premiumPlan = Plan.builder()
                    .name("Plano Premium")
                    .description("Acesso ilimitado para cuidados completos")
                    .price(new BigDecimal("199.90"))
                    .durationMonths(1)
                    .maxAppointmentsMonth(null) // Ilimitado
                    .hasVideoCall(true)
                    .hasChat(true)
                    .hasPrescription(true)
                    .hasMedicalCertificate(true)
                    .features(List.of(
                            "Consultas ilimitadas",
                            "Videochamada",
                            "Chat 24/7 com médico",
                            "Prescrição digital",
                            "Atestado médico",
                            "Suporte VIP",
                            "Agendamento prioritário",
                            "Histórico médico completo"
                    ))
                    .build();

            planRepository.saveAll(List.of(basicPlan, standardPlan, premiumPlan));
            log.info("Planos criados: Básico, Padrão, Premium");
        }
    }

    private void loadUsers() {
        if (userRepository.count() == 0) {
            // Admin
            User admin = User.builder()
                    .name("Administrador")
                    .email("admin@teleconsulta.com")
                    .password(passwordEncoder.encode("admin123"))
                    .cpf("00000000000")
                    .phoneNumber("11999999999")
                    .role(Role.ADMIN)
                    .build();

            // Médicos
            User doctor1 = User.builder()
                    .name("Dr. João Silva")
                    .email("joao.silva@teleconsulta.com")
                    .password(passwordEncoder.encode("doctor123"))
                    .cpf("11111111111")
                    .phoneNumber("11988888888")
                    .role(Role.DOCTOR)
                    .crm("CRM/SP 123456")
                    .specialty("Clínica Geral")
                    .build();

            User doctor2 = User.builder()
                    .name("Dra. Maria Santos")
                    .email("maria.santos@teleconsulta.com")
                    .password(passwordEncoder.encode("doctor123"))
                    .cpf("22222222222")
                    .phoneNumber("11977777777")
                    .role(Role.DOCTOR)
                    .crm("CRM/SP 654321")
                    .specialty("Cardiologia")
                    .build();

            User doctor3 = User.builder()
                    .name("Dr. Pedro Oliveira")
                    .email("pedro.oliveira@teleconsulta.com")
                    .password(passwordEncoder.encode("doctor123"))
                    .cpf("33333333333")
                    .phoneNumber("11966666666")
                    .role(Role.DOCTOR)
                    .crm("CRM/SP 789012")
                    .specialty("Dermatologia")
                    .build();

            // Paciente
            User patient = User.builder()
                    .name("Carlos Paciente")
                    .email("carlos@email.com")
                    .password(passwordEncoder.encode("patient123"))
                    .cpf("44444444444")
                    .phoneNumber("11955555555")
                    .role(Role.PATIENT)
                    .build();

            userRepository.saveAll(List.of(admin, doctor1, doctor2, doctor3, patient));
            log.info("Usuários criados: 1 admin, 3 médicos, 1 paciente");
        }
    }
}

