package com.teleconsulta.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(name = "max_appointments_month")
    private Integer maxAppointmentsMonth;

    @Column(name = "has_video_call")
    @Builder.Default
    private Boolean hasVideoCall = true;

    @Column(name = "has_chat")
    @Builder.Default
    private Boolean hasChat = true;

    @Column(name = "has_prescription")
    @Builder.Default
    private Boolean hasPrescription = true;

    @Column(name = "has_medical_certificate")
    @Builder.Default
    private Boolean hasMedicalCertificate = true;

    @ElementCollection
    @CollectionTable(name = "plan_features", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "feature")
    @Builder.Default
    private List<String> features = new ArrayList<>();

    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY)
    @Builder.Default
    private List<User> subscribers = new ArrayList<>();
}

