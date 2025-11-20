package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.ContractStatus;
import com.swp391.e_Motion_be.enums.RentalStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "rentals")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="rental_id")
    Long id;
    @Column(name="rental_status")
    @Enumerated(EnumType.STRING)
    RentalStatus status = RentalStatus.PENDING;
    @Column(name="start_time")
    LocalDateTime startTime;
    @Column(name="end_time")
    LocalDateTime endTime;
    @Column(name = "cancel_notified")
    Boolean cancelNotified = false;
    @Column(name = "overdue_notified")
     Boolean overdueNotified = false;
    @Column(name = "expiring_notified")
     Boolean expiringNotified = false;
    @Column(name="rent_fee")
     Double rentFee;
    @Column(name="discount_point")
    int discountPoint = 0;
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
     LocalDateTime createdAt;
    @Column(name = "pending_end_time")
     LocalDateTime pendingEndTime;
    @Column(name = "pending_rent_fee")
     Double pendingRentFee;
    @Enumerated(EnumType.STRING)
     RentalStatus preStatus;
     long submissionId;
     String contractUrl;
     String submissionUrl;
    @Enumerated(EnumType.STRING)
    ContractStatus contractStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    Reservation reservation;

    @OneToOne(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
    Deposit deposit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    Station station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    Staff staff;

    @OneToOne(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
    Rating rating;

    @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<Payment> payments;

    @OneToOne(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
    VehicleLog vehicleLog;

    @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<RentalCheckList> rentalCheckLists;

}
