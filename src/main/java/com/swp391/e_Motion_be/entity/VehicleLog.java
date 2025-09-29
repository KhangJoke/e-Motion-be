package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.VehicleLogType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name ="vehicleLogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="log_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name="log_type", nullable=false)
    private VehicleLogType vehicleLogType;

    @Column(name="description", nullable=false)
    private String description;

    @Column(name="fee", nullable = false)
    private Double fee;

    @Column(name="create_at", nullable=false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    public VehicleLog(VehicleLogType vehicleLogType, String description, LocalDateTime createdAt, Vehicle vehicle, Staff staff) {
        this.vehicleLogType = vehicleLogType;
        this.description = description;
        this.createdAt = createdAt;
        this.vehicle = vehicle;
        this.staff = staff;
    }

}
