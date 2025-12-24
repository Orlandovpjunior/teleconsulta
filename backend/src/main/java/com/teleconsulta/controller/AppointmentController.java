package com.teleconsulta.controller;

import com.teleconsulta.dto.appointment.AppointmentDTO;
import com.teleconsulta.dto.appointment.CreateAppointmentRequest;
import com.teleconsulta.dto.appointment.UpdateAppointmentRequest;
import com.teleconsulta.entity.AppointmentStatus;
import com.teleconsulta.entity.User;
import com.teleconsulta.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Consultas", description = "Endpoints para gerenciamento de consultas")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    @Operation(summary = "Listar minhas consultas")
    public ResponseEntity<List<AppointmentDTO>> getMyAppointments(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(currentUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter consulta por ID")
    public ResponseEntity<AppointmentDTO> getAppointmentById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id, currentUser));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar consultas por status")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByStatus(
            @PathVariable AppointmentStatus status,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(currentUser, status));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Listar consultas por período")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDateRange(currentUser, start, end));
    }

    @PostMapping
    @Operation(summary = "Agendar nova consulta")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<AppointmentDTO> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(request, currentUser));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar consulta")
    public ResponseEntity<AppointmentDTO> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, request, currentUser));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar consulta")
    public ResponseEntity<AppointmentDTO> cancelAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, currentUser));
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirmar consulta (apenas médico)")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentDTO> confirmAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(appointmentService.confirmAppointment(id, currentUser));
    }
}

