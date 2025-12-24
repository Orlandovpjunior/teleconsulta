package com.teleconsulta.service;

import com.teleconsulta.dto.appointment.AppointmentDTO;
import com.teleconsulta.dto.appointment.CreateAppointmentRequest;
import com.teleconsulta.dto.appointment.UpdateAppointmentRequest;
import com.teleconsulta.entity.Appointment;
import com.teleconsulta.entity.AppointmentStatus;
import com.teleconsulta.entity.Role;
import com.teleconsulta.entity.User;
import com.teleconsulta.exception.BadRequestException;
import com.teleconsulta.exception.ResourceNotFoundException;
import com.teleconsulta.exception.UnauthorizedException;
import com.teleconsulta.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserService userService;

    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta", "id", id));
    }

    public AppointmentDTO getAppointmentById(Long id, User currentUser) {
        Appointment appointment = findById(id);
        
        // Verificar se o usuário tem permissão para ver esta consulta
        if (!canAccessAppointment(appointment, currentUser)) {
            throw new UnauthorizedException("Você não tem permissão para acessar esta consulta");
        }

        return AppointmentDTO.fromEntity(appointment);
    }

    public List<AppointmentDTO> getMyAppointments(User currentUser) {
        List<Appointment> appointments;
        
        if (currentUser.getRole() == Role.DOCTOR) {
            appointments = appointmentRepository.findByDoctor(currentUser);
        } else {
            appointments = appointmentRepository.findByPatient(currentUser);
        }

        return appointments.stream()
                .map(AppointmentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByStatus(User currentUser, AppointmentStatus status) {
        List<Appointment> appointments;
        
        if (currentUser.getRole() == Role.DOCTOR) {
            appointments = appointmentRepository.findByDoctorAndStatus(currentUser, status);
        } else {
            appointments = appointmentRepository.findByPatientAndStatus(currentUser, status);
        }

        return appointments.stream()
                .map(AppointmentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByDateRange(
            User currentUser,
            LocalDateTime start,
            LocalDateTime end
    ) {
        List<Appointment> appointments;
        
        if (currentUser.getRole() == Role.DOCTOR) {
            appointments = appointmentRepository.findByDoctorAndScheduledAtBetween(currentUser, start, end);
        } else if (currentUser.getRole() == Role.ADMIN) {
            appointments = appointmentRepository.findByScheduledAtBetween(start, end);
        } else {
            appointments = appointmentRepository.findByPatientAndScheduledAtBetween(currentUser, start, end);
        }

        return appointments.stream()
                .map(AppointmentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentDTO createAppointment(CreateAppointmentRequest request, User patient) {
        // Verificar se o médico existe e é realmente um médico
        User doctor = userService.findById(request.getDoctorId());
        
        if (doctor.getRole() != Role.DOCTOR) {
            throw new BadRequestException("O profissional selecionado não é um médico");
        }

        if (!doctor.getActive()) {
            throw new BadRequestException("Este médico não está disponível");
        }

        // Verificar conflito de horário
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                doctor, request.getScheduledAt()
        );
        
        if (!conflicts.isEmpty()) {
            throw new BadRequestException("Este horário já está ocupado");
        }

        // Verificar limite de consultas do plano (se aplicável)
        if (patient.getPlan() != null && patient.getPlan().getMaxAppointmentsMonth() != null) {
            LocalDateTime now = LocalDateTime.now();
            Long appointmentsThisMonth = appointmentRepository.countPatientAppointmentsInMonth(
                    patient, now.getYear(), now.getMonthValue()
            );
            
            if (appointmentsThisMonth >= patient.getPlan().getMaxAppointmentsMonth()) {
                throw new BadRequestException("Você atingiu o limite de consultas do seu plano este mês");
            }
        }

        // Criar consulta
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .scheduledAt(request.getScheduledAt())
                .patientComplaint(request.getPatientComplaint())
                .status(AppointmentStatus.SCHEDULED)
                .videoRoomId(UUID.randomUUID().toString())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return AppointmentDTO.fromEntity(savedAppointment);
    }

    @Transactional
    public AppointmentDTO updateAppointment(Long id, UpdateAppointmentRequest request, User currentUser) {
        Appointment appointment = findById(id);
        
        if (!canAccessAppointment(appointment, currentUser)) {
            throw new UnauthorizedException("Você não tem permissão para modificar esta consulta");
        }

        // Pacientes só podem atualizar status para CANCELLED
        if (currentUser.getRole() == Role.PATIENT) {
            if (request.getStatus() != null && request.getStatus() != AppointmentStatus.CANCELLED) {
                throw new UnauthorizedException("Você só pode cancelar consultas");
            }
        }

        if (request.getScheduledAt() != null) {
            // Verificar conflito se estiver reagendando
            if (!request.getScheduledAt().equals(appointment.getScheduledAt())) {
                List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                        appointment.getDoctor(), request.getScheduledAt()
                );
                conflicts.removeIf(a -> a.getId().equals(id));
                
                if (!conflicts.isEmpty()) {
                    throw new BadRequestException("Este horário já está ocupado");
                }
            }
            appointment.setScheduledAt(request.getScheduledAt());
        }

        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
            
            // Registrar horários de início/fim
            if (request.getStatus() == AppointmentStatus.IN_PROGRESS) {
                appointment.setStartedAt(LocalDateTime.now());
            } else if (request.getStatus() == AppointmentStatus.COMPLETED) {
                appointment.setEndedAt(LocalDateTime.now());
                if (appointment.getStartedAt() != null) {
                    long minutes = ChronoUnit.MINUTES.between(
                            appointment.getStartedAt(), 
                            appointment.getEndedAt()
                    );
                    appointment.setDurationMinutes((int) minutes);
                }
            }
        }

        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        if (request.getDiagnosis() != null) {
            appointment.setDiagnosis(request.getDiagnosis());
        }

        if (request.getPrescription() != null) {
            appointment.setPrescription(request.getPrescription());
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return AppointmentDTO.fromEntity(savedAppointment);
    }

    @Transactional
    public AppointmentDTO cancelAppointment(Long id, User currentUser) {
        Appointment appointment = findById(id);
        
        if (!canAccessAppointment(appointment, currentUser)) {
            throw new UnauthorizedException("Você não tem permissão para cancelar esta consulta");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Não é possível cancelar uma consulta já concluída");
        }

        if (appointment.getStatus() == AppointmentStatus.IN_PROGRESS) {
            throw new BadRequestException("Não é possível cancelar uma consulta em andamento");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return AppointmentDTO.fromEntity(savedAppointment);
    }

    @Transactional
    public AppointmentDTO confirmAppointment(Long id, User doctor) {
        Appointment appointment = findById(id);
        
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new UnauthorizedException("Apenas o médico da consulta pode confirmá-la");
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BadRequestException("Apenas consultas agendadas podem ser confirmadas");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return AppointmentDTO.fromEntity(savedAppointment);
    }

    private boolean canAccessAppointment(Appointment appointment, User user) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        
        return appointment.getPatient().getId().equals(user.getId()) ||
               appointment.getDoctor().getId().equals(user.getId());
    }
}

