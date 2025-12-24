package com.teleconsulta.service;

import com.teleconsulta.dto.appointment.AppointmentDTO;
import com.teleconsulta.dto.appointment.CreateAppointmentRequest;
import com.teleconsulta.entity.*;
import com.teleconsulta.exception.BadRequestException;
import com.teleconsulta.exception.UnauthorizedException;
import com.teleconsulta.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AppointmentService appointmentService;

    private User patient;
    private User doctor;
    private Appointment appointment;
    private CreateAppointmentRequest createRequest;

    @BeforeEach
    void setUp() {
        patient = User.builder()
                .name("Paciente Teste")
                .email("paciente@test.com")
                .role(Role.PATIENT)
                .build();
        patient.setId(1L);
        patient.setActive(true);

        doctor = User.builder()
                .name("Dr. Teste")
                .email("doctor@test.com")
                .role(Role.DOCTOR)
                .crm("CRM/SP 123456")
                .specialty("Clínica Geral")
                .build();
        doctor.setId(2L);
        doctor.setActive(true);

        appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .status(AppointmentStatus.SCHEDULED)
                .videoRoomId("room-123")
                .build();
        appointment.setId(1L);

        createRequest = CreateAppointmentRequest.builder()
                .doctorId(2L)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .patientComplaint("Dor de cabeça")
                .build();
    }

    @Test
    @DisplayName("Deve retornar consulta por ID para paciente autorizado")
    void shouldReturnAppointmentByIdForAuthorizedPatient() {
        // Arrange
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // Act
        AppointmentDTO result = appointmentService.getAppointmentById(1L, patient);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPatientName()).isEqualTo("Paciente Teste");
        assertThat(result.getDoctorName()).isEqualTo("Dr. Teste");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não autorizado tenta acessar consulta")
    void shouldThrowExceptionWhenUnauthorizedUserAccessesAppointment() {
        // Arrange
        User otherPatient = User.builder()
                .name("Outro Paciente")
                .email("outro@test.com")
                .role(Role.PATIENT)
                .build();
        otherPatient.setId(99L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.getAppointmentById(1L, otherPatient))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Você não tem permissão para acessar esta consulta");
    }

    @Test
    @DisplayName("Deve criar consulta com sucesso")
    void shouldCreateAppointmentSuccessfully() {
        // Arrange
        when(userService.findById(2L)).thenReturn(doctor);
        when(appointmentRepository.findConflictingAppointments(any(), any()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // Act
        AppointmentDTO result = appointmentService.createAppointment(createRequest, patient);

        // Assert
        assertThat(result).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando há conflito de horário")
    void shouldThrowExceptionWhenTimeConflict() {
        // Arrange
        when(userService.findById(2L)).thenReturn(doctor);
        when(appointmentRepository.findConflictingAppointments(any(), any()))
                .thenReturn(List.of(appointment));

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest, patient))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Este horário já está ocupado");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando médico selecionado não é médico")
    void shouldThrowExceptionWhenSelectedUserIsNotDoctor() {
        // Arrange
        User notDoctor = User.builder()
                .name("Não Médico")
                .role(Role.PATIENT)
                .build();
        notDoctor.setId(2L);
        notDoctor.setActive(true);

        when(userService.findById(2L)).thenReturn(notDoctor);

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest, patient))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O profissional selecionado não é um médico");
    }

    @Test
    @DisplayName("Deve cancelar consulta com sucesso")
    void shouldCancelAppointmentSuccessfully() {
        // Arrange
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // Act
        AppointmentDTO result = appointmentService.cancelAppointment(1L, patient);

        // Assert
        assertThat(result).isNotNull();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("Não deve cancelar consulta já concluída")
    void shouldNotCancelCompletedAppointment() {
        // Arrange
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, patient))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Não é possível cancelar uma consulta já concluída");
    }

    @Test
    @DisplayName("Médico deve confirmar consulta com sucesso")
    void shouldConfirmAppointmentSuccessfully() {
        // Arrange
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // Act
        AppointmentDTO result = appointmentService.confirmAppointment(1L, doctor);

        // Assert
        assertThat(result).isNotNull();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Deve listar consultas do paciente")
    void shouldListPatientAppointments() {
        // Arrange
        when(appointmentRepository.findByPatient(patient)).thenReturn(List.of(appointment));

        // Act
        List<AppointmentDTO> result = appointmentService.getMyAppointments(patient);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientName()).isEqualTo("Paciente Teste");
    }

    @Test
    @DisplayName("Deve listar consultas do médico")
    void shouldListDoctorAppointments() {
        // Arrange
        when(appointmentRepository.findByDoctor(doctor)).thenReturn(List.of(appointment));

        // Act
        List<AppointmentDTO> result = appointmentService.getMyAppointments(doctor);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDoctorName()).isEqualTo("Dr. Teste");
    }
}

