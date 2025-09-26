package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.VehicleStatus;
import com.swp391.e_Motion_be.enums.VehicleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name ="vehicles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="vehicle_id")
    private Long id;

    @Column(name="vehicle_name",nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name="vehicle_type",nullable = false)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING) //save enum data thay vi number
    @Column(name="vehicle_status",nullable = false)
    private VehicleStatus vehicleStatus;

    @Column(name="consumption_rate",nullable = false)
    private double consumptionRate;

    @Column(name="current_battery_level",nullable = false)
    private double batteryLevel;

    @Column(name="battery_capacity",nullable = false)
    private double batteryCapacity;

    @Column(name="plate_number",nullable = false,unique = true)
    private String plateNumber;

    @Column(name="last_maintenance",nullable = false)
    private LocalDateTime lastMaintenance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VehicleLog> vehicleLogs;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    public Vehicle(String name, VehicleType vehicleType, VehicleStatus vehicleStatus, double consumptionRate, double batteryLevel, double batteryCapacity, String plateNumber, LocalDateTime lastMaintenance, Station station) {
        this.name = name;
        this.vehicleType = vehicleType;
        this.vehicleStatus = vehicleStatus;
        this.consumptionRate = consumptionRate;
        this.batteryLevel = batteryLevel;
        this.batteryCapacity = batteryCapacity;
        this.plateNumber = plateNumber;
        this.lastMaintenance = lastMaintenance;
        this.station = station;
    }
}
