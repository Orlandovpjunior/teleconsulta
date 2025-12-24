package com.teleconsulta.repository;

import com.teleconsulta.entity.Role;
import com.teleconsulta.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    List<User> findByRole(Role role);

    List<User> findByRoleAndActiveTrue(Role role);

    List<User> findBySpecialtyContainingIgnoreCase(String specialty);
}

