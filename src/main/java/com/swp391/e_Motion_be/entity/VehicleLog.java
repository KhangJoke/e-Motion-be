package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.dto.convert.StringListConverter;
import com.swp391.e_Motion_be.dto.convert.VehicleLogItemListConverter;
import com.swp391.e_Motion_be.dto.vehicleLog.VehicleLogItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "repair_items",  columnDefinition = "TEXT")
    @Convert(converter = VehicleLogItemListConverter.class)
    private List<VehicleLogItem> repairItems;

    @Column(name ="total_cost")
    private Double cost;

    @Column(name = "images",  columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> imgs;

    @Column(name="created_at", nullable=false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="rental_id")
    private Rental rental;

}
