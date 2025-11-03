package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.station.StationCity;
import com.swp391.e_Motion_be.enums.station.StationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "stations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "station_id")
    private Long id;

    @Column(name = "station_name")
    private String name;

    @Column(name = "station_address")
    private String address;

    @Column(name = "station_latitude")
    private Double latitude;

    @Column(name = "station_longitude")
    private Double longitude;

    @Column(name = "station_status")
    @Enumerated(EnumType.STRING)
    private StationStatus status;

    @Column(name = "station_city")
    @Enumerated(EnumType.STRING)
    private StationCity city;
    private boolean isDelete = false;
    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Staff> staffs;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vehicle> vehicles;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Rental> rentals;
}
