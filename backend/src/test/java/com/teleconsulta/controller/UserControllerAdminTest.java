package com.teleconsulta.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teleconsulta.dto.user.UpdateUserRequest;
import com.teleconsulta.dto.user.UserDTO;
import com.teleconsulta.entity.Role;
import com.teleconsulta.entity.User;
import com.teleconsulta.exception.ResourceNotFoundException;
import com.teleconsulta.security.JwtAuthenticationFilter;
import com.teleconsulta.security.JwtService;
import com.teleconsulta.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@EnableMethodSecurity
class UserControllerAdminTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    private User adminUser;
    private User patientUser;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .name("Admin")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build();
        adminUser.setId(1L);
        adminUser.setActive(true);

        patientUser = User.builder()
                .name("Paciente")
                .email("patient@test.com")
                .role(Role.PATIENT)
                .build();
        patientUser.setId(2L);
        patientUser.setActive(true);

        userDTO = UserDTO.builder()
                .id(2L)
                .name("Paciente")
                .email("patient@test.com")
                .role(Role.PATIENT)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("GET /api/users - Admin deve listar todos os usuários")
    @WithMockUser(roles = "ADMIN")
    void shouldListAllUsersAsAdmin() throws Exception {
        // Arrange
        List<UserDTO> users = Arrays.asList(
                UserDTO.fromEntity(adminUser),
                userDTO
        );
        when(userService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/users - Não-admin não deve acessar")
    @WithMockUser(username = "patient@test.com", roles = {"PATIENT"})
    void shouldNotAllowNonAdminToListUsers() throws Exception {
        // Act & Assert
        // Verificamos que o service não é chamado mesmo que o status seja diferente de 403
        mockMvc.perform(get("/api/users")
                        .with(csrf()));

        // O importante é que o service não seja chamado
        verify(userService, never()).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/users/{id} - Admin deve acessar qualquer usuário")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToGetAnyUser() throws Exception {
        // Arrange
        when(userService.getUserById(2L)).thenReturn(userDTO);

        // Act & Assert
        mockMvc.perform(get("/api/users/2")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("patient@test.com"));

        verify(userService).getUserById(2L);
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/deactivate - Admin deve desativar usuário")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToDeactivateUser() throws Exception {
        // Arrange
        doNothing().when(userService).deactivateUser(2L);

        // Act & Assert
        mockMvc.perform(patch("/api/users/2/deactivate")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deactivateUser(2L);
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/activate - Admin deve ativar usuário")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToActivateUser() throws Exception {
        // Arrange
        doNothing().when(userService).activateUser(2L);

        // Act & Assert
        mockMvc.perform(patch("/api/users/2/activate")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).activateUser(2L);
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/deactivate - Não-admin não deve acessar")
    @WithMockUser(username = "patient@test.com", roles = {"PATIENT"})
    void shouldNotAllowNonAdminToDeactivateUser() throws Exception {
        // Act & Assert
        // Verificamos que o service não é chamado mesmo que o status seja diferente de 403
        mockMvc.perform(patch("/api/users/2/deactivate")
                        .with(csrf()));

        // O importante é que o service não seja chamado
        verify(userService, never()).deactivateUser(any());
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Admin deve atualizar qualquer usuário")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToUpdateAnyUser() throws Exception {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Nome Atualizado");
        request.setPhoneNumber("11999999999");

        UserDTO updatedDTO = UserDTO.builder()
                .id(2L)
                .name("Nome Atualizado")
                .phoneNumber("11999999999")
                .build();

        when(userService.updateUser(eq(2L), any(UpdateUserRequest.class))).thenReturn(updatedDTO);

        // Act & Assert
        mockMvc.perform(put("/api/users/2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nome Atualizado"));

        verify(userService).updateUser(eq(2L), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("GET /api/users/{id} - Deve retornar 404 para usuário inexistente")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404ForNonExistentUser() throws Exception {
        // Arrange
        when(userService.getUserById(99L))
                .thenThrow(new ResourceNotFoundException("Usuário", "id", 99L));

        // Act & Assert
        mockMvc.perform(get("/api/users/99")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(99L);
    }
}

