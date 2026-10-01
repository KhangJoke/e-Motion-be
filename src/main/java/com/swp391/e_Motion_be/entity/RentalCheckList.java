package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.CheckType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rental_checklists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalCheckList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checklist_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_type")
    private CheckType type;

    private Double fee = 0.0;

    private String img;

    @Column(name = "current_battery")
    private Double currentBattery;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id")
    private Rental rental;
}
