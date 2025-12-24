package com.teleconsulta.repository;

import com.teleconsulta.entity.Appointment;
import com.teleconsulta.entity.AppointmentStatus;
import com.teleconsulta.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatient(User patient);

    List<Appointment> findByDoctor(User doctor);

    List<Appointment> findByPatientAndStatus(User patient, AppointmentStatus status);

    List<Appointment> findByDoctorAndStatus(User doctor, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND a.scheduledAt BETWEEN :start AND :end")
    List<Appointment> findByDoctorAndScheduledAtBetween(
            @Param("doctor") User doctor,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT a FROM Appointment a WHERE a.patient = :patient AND a.scheduledAt BETWEEN :start AND :end")
    List<Appointment> findByPatientAndScheduledAtBetween(
            @Param("patient") User patient,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT a FROM Appointment a WHERE a.scheduledAt BETWEEN :start AND :end ORDER BY a.scheduledAt ASC")
    List<Appointment> findByScheduledAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient = :patient AND a.status != 'CANCELLED' " +
            "AND YEAR(a.scheduledAt) = :year AND MONTH(a.scheduledAt) = :month")
    Long countPatientAppointmentsInMonth(
            @Param("patient") User patient,
            @Param("year") int year,
            @Param("month") int month
    );

    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND a.scheduledAt = :scheduledAt AND a.status != 'CANCELLED'")
    List<Appointment> findConflictingAppointments(
            @Param("doctor") User doctor,
            @Param("scheduledAt") LocalDateTime scheduledAt
    );
}

