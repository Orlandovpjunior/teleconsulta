package com.teleconsulta.dto.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {

    @NotNull(message = "ID do médico é obrigatório")
    private Long doctorId;

    @NotNull(message = "Data e hora do agendamento são obrigatórios")
    @Future(message = "A consulta deve ser agendada para uma data futura")
    private LocalDateTime scheduledAt;

    @Size(max = 1000, message = "Queixa deve ter no máximo 1000 caracteres")
    private String patientComplaint;
}

