package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name ="reservations")
@Data
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="reservation_id")
    private Long id;
    @Column(name="reservation_code",nullable = false, unique = true)
    private String code;
    @Column(name="reservation_status",nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
    @Column(name="created_at",nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;
    @Column(name="reserved_start_time",nullable = false)
    private LocalDateTime startTime;
    @Column(name="reserved_end_time",nullable = false)
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    private Station station;

    public Reservation(String code, LocalDateTime endTime, User user, Vehicle vehicle, Station station, LocalDateTime startTime) {
        this.code = code;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = LocalDateTime.now();
        this.user = user;
        this.vehicle = vehicle;
        this.station = station;
        this.status = ReservationStatus.PENDING;
    }

    public Reservation(){
        this.createdAt = LocalDateTime.now();
        this.status = ReservationStatus.PENDING;
    }

}
