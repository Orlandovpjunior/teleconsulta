package com.teleconsulta.dto.plan;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlanRequest {

    @NotBlank(message = "Nome do plano é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String name;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String description;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Preço deve ser maior que zero")
    private BigDecimal price;

    @Min(value = 1, message = "Duração mínima é 1 mês")
    private Integer durationMonths;

    @Min(value = 1, message = "Número mínimo de consultas é 1")
    private Integer maxAppointmentsMonth;

    @Builder.Default
    private Boolean hasVideoCall = true;
    @Builder.Default
    private Boolean hasChat = true;
    @Builder.Default
    private Boolean hasPrescription = true;
    @Builder.Default
    private Boolean hasMedicalCertificate = true;

    private List<String> features;
}

