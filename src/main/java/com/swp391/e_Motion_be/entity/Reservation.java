package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name ="reservations")
@Data
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="reservation_id")
    private long id;
    @Column(name="reservation_code", unique = true)
    private String code;
    @Column(name="reservation_status",nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(name = "cancel_notified")
    private Boolean cancelNotified = false;

    @Column(name = "overdue_notified")
    private Boolean overdueNotified = false;

    @Column(name = "expiring_notified")
    private Boolean expiringNotified = false;

    @Column(name="created_at",nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name="reserved_start_time",nullable = false)
    private LocalDateTime startTime;
    @Column(name="reserved_end_time",nullable = false)
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Deposit deposit;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Rental> rentals;

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
