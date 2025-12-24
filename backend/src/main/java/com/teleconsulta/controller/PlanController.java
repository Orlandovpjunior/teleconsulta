package com.teleconsulta.controller;

import com.teleconsulta.dto.plan.CreatePlanRequest;
import com.teleconsulta.dto.plan.PlanDTO;
import com.teleconsulta.entity.User;
import com.teleconsulta.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Tag(name = "Planos", description = "Endpoints para gerenciamento de planos")
public class PlanController {

    private final PlanService planService;

    // Endpoints públicos
    @GetMapping("/public")
    @Operation(summary = "Listar planos ativos (público)")
    public ResponseEntity<List<PlanDTO>> getActivePlans() {
        return ResponseEntity.ok(planService.getActivePlans());
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "Obter plano por ID (público)")
    public ResponseEntity<PlanDTO> getPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    // Endpoints protegidos
    @GetMapping
    @Operation(summary = "Listar todos os planos")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<PlanDTO>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @PostMapping
    @Operation(summary = "Criar novo plano")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PlanDTO> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.createPlan(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar plano")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PlanDTO> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody CreatePlanRequest request
    ) {
        return ResponseEntity.ok(planService.updatePlan(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desativar plano")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deactivatePlan(@PathVariable Long id) {
        planService.deactivatePlan(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Ativar plano")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> activatePlan(@PathVariable Long id) {
        planService.activatePlan(id);
        return ResponseEntity.noContent().build();
    }

    // Assinatura de plano
    @PostMapping("/{planId}/subscribe")
    @Operation(summary = "Assinar plano")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> subscribeToPlan(
            @PathVariable Long planId,
            @AuthenticationPrincipal User currentUser
    ) {
        planService.subscribeToPlan(currentUser.getId(), planId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/subscription")
    @Operation(summary = "Cancelar assinatura")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> cancelSubscription(@AuthenticationPrincipal User currentUser) {
        planService.cancelSubscription(currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}

