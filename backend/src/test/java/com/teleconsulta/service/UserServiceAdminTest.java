package com.teleconsulta.service;

import com.teleconsulta.entity.Role;
import com.teleconsulta.entity.User;
import com.teleconsulta.exception.ResourceNotFoundException;
import com.teleconsulta.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceAdminTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User activeUser;
    private User inactiveUser;
    private User doctor;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .name("Usuário Ativo")
                .email("ativo@test.com")
                .role(Role.PATIENT)
                .build();
        activeUser.setId(1L);
        activeUser.setActive(true);

        inactiveUser = User.builder()
                .name("Usuário Inativo")
                .email("inativo@test.com")
                .role(Role.PATIENT)
                .build();
        inactiveUser.setId(2L);
        inactiveUser.setActive(false);

        doctor = User.builder()
                .name("Dr. Teste")
                .email("doctor@test.com")
                .role(Role.DOCTOR)
                .crm("CRM/SP 123456")
                .specialty("Clínica Geral")
                .build();
        doctor.setId(3L);
        doctor.setActive(true);
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void shouldGetAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(activeUser, inactiveUser, doctor);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        var result = userService.getAllUsers();

        // Assert
        assertThat(result).hasSize(3);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar apenas médicos ativos")
    void shouldGetActiveDoctors() {
        // Arrange
        when(userRepository.findByRoleAndActiveTrue(Role.DOCTOR))
                .thenReturn(List.of(doctor));

        // Act
        var result = userService.getDoctors();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(Role.DOCTOR);
        assertThat(result.get(0).getActive()).isTrue();
    }

    @Test
    @DisplayName("Deve desativar um usuário")
    void shouldDeactivateUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        // Act
        userService.deactivateUser(1L);

        // Assert
        assertThat(activeUser.getActive()).isFalse();
        verify(userRepository).save(activeUser);
    }

    @Test
    @DisplayName("Deve ativar um usuário")
    void shouldActivateUser() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(inactiveUser));
        when(userRepository.save(any(User.class))).thenReturn(inactiveUser);

        // Act
        userService.activateUser(2L);

        // Assert
        assertThat(inactiveUser.getActive()).isTrue();
        verify(userRepository).save(inactiveUser);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar desativar usuário inexistente")
    void shouldThrowExceptionWhenDeactivatingNonExistentUser() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.deactivateUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuário");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve buscar médicos por especialidade")
    void shouldGetDoctorsBySpecialty() {
        // Arrange
        when(userRepository.findBySpecialtyContainingIgnoreCase("Clínica Geral"))
                .thenReturn(List.of(doctor));

        // Act
        var result = userService.getDoctorsBySpecialty("Clínica Geral");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpecialty()).containsIgnoringCase("Clínica Geral");
    }

    @Test
    @DisplayName("Deve verificar se email existe")
    void shouldCheckIfEmailExists() {
        // Arrange
        when(userRepository.existsByEmail("test@email.com")).thenReturn(true);

        // Act
        boolean exists = userService.existsByEmail("test@email.com");

        // Assert
        assertThat(exists).isTrue();
        verify(userRepository).existsByEmail("test@email.com");
    }

    @Test
    @DisplayName("Deve verificar se CPF existe")
    void shouldCheckIfCpfExists() {
        // Arrange
        when(userRepository.existsByCpf("12345678901")).thenReturn(true);

        // Act
        boolean exists = userService.existsByCpf("12345678901");

        // Assert
        assertThat(exists).isTrue();
        verify(userRepository).existsByCpf("12345678901");
    }
}

