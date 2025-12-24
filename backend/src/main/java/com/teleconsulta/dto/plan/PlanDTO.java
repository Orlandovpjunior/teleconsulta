package com.teleconsulta.dto.plan;

import com.teleconsulta.entity.Plan;
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
public class PlanDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMonths;
    private Integer maxAppointmentsMonth;
    private Boolean hasVideoCall;
    private Boolean hasChat;
    private Boolean hasPrescription;
    private Boolean hasMedicalCertificate;
    private List<String> features;
    private Boolean active;

    public static PlanDTO fromEntity(Plan plan) {
        return PlanDTO.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .durationMonths(plan.getDurationMonths())
                .maxAppointmentsMonth(plan.getMaxAppointmentsMonth())
                .hasVideoCall(plan.getHasVideoCall())
                .hasChat(plan.getHasChat())
                .hasPrescription(plan.getHasPrescription())
                .hasMedicalCertificate(plan.getHasMedicalCertificate())
                .features(plan.getFeatures())
                .active(plan.getActive())
                .build();
    }
}

