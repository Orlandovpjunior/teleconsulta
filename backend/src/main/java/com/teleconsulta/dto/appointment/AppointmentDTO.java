package com.teleconsulta.dto.appointment;

import com.teleconsulta.entity.Appointment;
import com.teleconsulta.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialty;
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private AppointmentStatus status;
    private String notes;
    private String patientComplaint;
    private String diagnosis;
    private String prescription;
    private String videoRoomId;
    private Integer durationMinutes;
    private LocalDateTime createdAt;

    public static AppointmentDTO fromEntity(Appointment appointment) {
        return AppointmentDTO.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getName())
                .doctorSpecialty(appointment.getDoctor().getSpecialty())
                .scheduledAt(appointment.getScheduledAt())
                .startedAt(appointment.getStartedAt())
                .endedAt(appointment.getEndedAt())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .patientComplaint(appointment.getPatientComplaint())
                .diagnosis(appointment.getDiagnosis())
                .prescription(appointment.getPrescription())
                .videoRoomId(appointment.getVideoRoomId())
                .durationMinutes(appointment.getDurationMinutes())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}

