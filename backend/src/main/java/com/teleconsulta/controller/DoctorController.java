package com.teleconsulta.controller;

import com.teleconsulta.dto.user.UserDTO;
import com.teleconsulta.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Médicos", description = "Endpoints para consulta de médicos")
public class DoctorController {

    private final UserService userService;

    @GetMapping("/public")
    @Operation(summary = "Listar todos os médicos ativos (público)")
    public ResponseEntity<List<UserDTO>> getAllDoctors() {
        return ResponseEntity.ok(userService.getDoctors());
    }

    @GetMapping("/public/specialty/{specialty}")
    @Operation(summary = "Listar médicos por especialidade (público)")
    public ResponseEntity<List<UserDTO>> getDoctorsBySpecialty(@PathVariable String specialty) {
        return ResponseEntity.ok(userService.getDoctorsBySpecialty(specialty));
    }
}

