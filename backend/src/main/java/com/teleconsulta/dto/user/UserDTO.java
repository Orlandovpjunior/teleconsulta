package com.teleconsulta.dto.user;

import com.teleconsulta.entity.Role;
import com.teleconsulta.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String cpf;
    private Role role;
    private String crm;
    private String specialty;
    private Long planId;
    private String planName;
    private Boolean active;

    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .cpf(user.getCpf())
                .role(user.getRole())
                .crm(user.getCrm())
                .specialty(user.getSpecialty())
                .planId(user.getPlan() != null ? user.getPlan().getId() : null)
                .planName(user.getPlan() != null ? user.getPlan().getName() : null)
                .active(user.getActive())
                .build();
    }
}

