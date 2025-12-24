package com.teleconsulta.service;

import com.teleconsulta.dto.auth.AuthResponse;
import com.teleconsulta.dto.auth.LoginRequest;
import com.teleconsulta.dto.auth.RegisterRequest;
import com.teleconsulta.entity.Role;
import com.teleconsulta.entity.User;
import com.teleconsulta.exception.BadRequestException;
import com.teleconsulta.repository.UserRepository;
import com.teleconsulta.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validações
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email já está em uso");
        }

        if (userRepository.existsByCpf(request.getCpf())) {
            throw new BadRequestException("CPF já está cadastrado");
        }

        // Validação específica para médicos
        if (request.getRole() == Role.DOCTOR) {
            if (request.getCrm() == null || request.getCrm().isBlank()) {
                throw new BadRequestException("CRM é obrigatório para médicos");
            }
            if (request.getSpecialty() == null || request.getSpecialty().isBlank()) {
                throw new BadRequestException("Especialidade é obrigatória para médicos");
            }
        }

        // Criar usuário
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .cpf(request.getCpf())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .crm(request.getCrm())
                .specialty(request.getSpecialty())
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Usuário não encontrado"));

        if (!user.getActive()) {
            throw new BadRequestException("Conta desativada. Entre em contato com o suporte.");
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}

