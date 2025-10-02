package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.VehicleLogType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name ="vehicle_logs")
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
    private VehicleLogType type;

    @Column(nullable=false)
    private String description;

    @Column(nullable = false)
    private Double fee;

    @Column(name="created_at", nullable=false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;
}
