package com.teleconsulta.dto.appointment;

import com.teleconsulta.entity.AppointmentStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentRequest {

    private LocalDateTime scheduledAt;

    private AppointmentStatus status;

    @Size(max = 2000, message = "Notas devem ter no máximo 2000 caracteres")
    private String notes;

    @Size(max = 2000, message = "Diagnóstico deve ter no máximo 2000 caracteres")
    private String diagnosis;

    @Size(max = 2000, message = "Prescrição deve ter no máximo 2000 caracteres")
    private String prescription;
}

