package com.teleconsulta.service;

import com.teleconsulta.dto.plan.CreatePlanRequest;
import com.teleconsulta.dto.plan.PlanDTO;
import com.teleconsulta.entity.Plan;
import com.teleconsulta.entity.User;
import com.teleconsulta.exception.BadRequestException;
import com.teleconsulta.exception.ResourceNotFoundException;
import com.teleconsulta.repository.PlanRepository;
import com.teleconsulta.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    public Plan findById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano", "id", id));
    }

    public PlanDTO getPlanById(Long id) {
        Plan plan = findById(id);
        return PlanDTO.fromEntity(plan);
    }

    public List<PlanDTO> getAllPlans() {
        return planRepository.findAll().stream()
                .map(PlanDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PlanDTO> getActivePlans() {
        return planRepository.findByActiveTrueOrderByPriceAsc().stream()
                .map(PlanDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PlanDTO createPlan(CreatePlanRequest request) {
        if (planRepository.existsByName(request.getName())) {
            throw new BadRequestException("Já existe um plano com este nome");
        }

        Plan plan = Plan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationMonths(request.getDurationMonths())
                .maxAppointmentsMonth(request.getMaxAppointmentsMonth())
                .hasVideoCall(request.getHasVideoCall() != null ? request.getHasVideoCall() : true)
                .hasChat(request.getHasChat() != null ? request.getHasChat() : true)
                .hasPrescription(request.getHasPrescription() != null ? request.getHasPrescription() : true)
                .hasMedicalCertificate(request.getHasMedicalCertificate() != null ? request.getHasMedicalCertificate() : true)
                .features(request.getFeatures() != null ? request.getFeatures() : new ArrayList<>())
                .build();

        Plan savedPlan = planRepository.save(plan);
        return PlanDTO.fromEntity(savedPlan);
    }

    @Transactional
    public PlanDTO updatePlan(Long id, CreatePlanRequest request) {
        Plan plan = findById(id);

        // Verificar se o novo nome já existe (exceto para o plano atual)
        if (!plan.getName().equals(request.getName()) && planRepository.existsByName(request.getName())) {
            throw new BadRequestException("Já existe um plano com este nome");
        }

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setPrice(request.getPrice());
        plan.setDurationMonths(request.getDurationMonths());
        plan.setMaxAppointmentsMonth(request.getMaxAppointmentsMonth());
        
        if (request.getHasVideoCall() != null) {
            plan.setHasVideoCall(request.getHasVideoCall());
        }
        if (request.getHasChat() != null) {
            plan.setHasChat(request.getHasChat());
        }
        if (request.getHasPrescription() != null) {
            plan.setHasPrescription(request.getHasPrescription());
        }
        if (request.getHasMedicalCertificate() != null) {
            plan.setHasMedicalCertificate(request.getHasMedicalCertificate());
        }
        if (request.getFeatures() != null) {
            plan.setFeatures(request.getFeatures());
        }

        Plan savedPlan = planRepository.save(plan);
        return PlanDTO.fromEntity(savedPlan);
    }

    @Transactional
    public void deactivatePlan(Long id) {
        Plan plan = findById(id);
        plan.setActive(false);
        planRepository.save(plan);
    }

    @Transactional
    public void activatePlan(Long id) {
        Plan plan = findById(id);
        plan.setActive(true);
        planRepository.save(plan);
    }

    @Transactional
    public void subscribeToPlan(Long userId, Long planId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));
        
        Plan plan = findById(planId);
        
        if (!plan.getActive()) {
            throw new BadRequestException("Este plano não está disponível");
        }

        user.setPlan(plan);
        userRepository.save(user);
    }

    @Transactional
    public void cancelSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));
        
        user.setPlan(null);
        userRepository.save(user);
    }
}

